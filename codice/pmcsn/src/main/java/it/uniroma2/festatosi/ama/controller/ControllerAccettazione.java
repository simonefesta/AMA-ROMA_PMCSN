package it.uniroma2.festatosi.ama.controller;

import it.uniroma2.festatosi.ama.model.EventListEntry;
import it.uniroma2.festatosi.ama.model.MsqSum;
import it.uniroma2.festatosi.ama.model.MsqT;
import it.uniroma2.festatosi.ama.utils.RandomDistribution;
import it.uniroma2.festatosi.ama.utils.Rngs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static it.uniroma2.festatosi.ama.model.Constants.*;

/**
 * rappresenta la msq per l'accettazione
 */
public class ControllerAccettazione {
    long number =0;                 /*number in the node*/
    int e;                          /*next event index*/
    int s;                          /*server index*/
    private long jobServed=0;           /*contatore jobs processati*/
    private double area=0.0;        /*time integrated number in the node*/

    private final EventHandler eventHandler;  /*istanza dell'EventHandler per ottenere le info sugli eventi*/

    private final RandomDistribution rnd=RandomDistribution.getInstance();
    private final Rngs rngs=new Rngs();

    private final List<MsqSum> sum=new ArrayList<>(SERVERS_ACCETTAZIONE+1);
    private final MsqT time=new MsqT();
    private final List<EventListEntry> eventListAccettazione=new ArrayList<>(SERVERS_ACCETTAZIONE+1);

    private List<EventListEntry> queueAccettazione=new LinkedList<>();

    public ControllerAccettazione() throws Exception {

        /*ottengo l'istanza di EventHandler per la gestione degli eventi*/
        this.eventHandler=EventHandler.getInstance();

        /*istanza della classe per creare multi-stream di numeri random*/
        Rngs rngs = new Rngs();
        rngs.plantSeeds(123456789);

        for(s=0; s<SERVERS_ACCETTAZIONE+1; s++){
            this.eventListAccettazione.add(s, new EventListEntry(0,0));
            this.sum.add(s, new MsqSum());
        }

        double firstArrival=this.rnd.getJobArrival(1);

        this.eventListAccettazione.set(0, new EventListEntry(0, 1, 1));

        //viene settata la lista di eventi nell'handler
        this.eventHandler.setEventsAccettazione(eventListAccettazione);
    }

    public void baseSimulation() throws Exception {
        int e;
        //prende la lista di eventi per l'accettazione
        List<EventListEntry> eventList = this.eventHandler.getEventsAccettazione();
        /*
        *il ciclo continua finchè non si verificano entrambe queste condizioni:
        * -eventList[0].x=0 (close door),
        * -number>0 ci sono ancora eventi nel sistema
        */

        if(eventList.get(0).getX()==0){
            eventHandler.getEventsSistema().get(0).setT(Double.MAX_VALUE);
            return;
        }
    //while((eventList.get(0).getX() !=0) || (this.number>0)){: per decommentare dare un tab a tutto il ciclo
        //prende l'indice del primo evento nella lista
        e=EventListEntry.getNextEvent(eventList, SERVERS_ACCETTAZIONE);
        //imposta il tempo del prossimo evento
        this.time.setNext(eventList.get(e).getT());
        //si calcola l'area dell'integrale
        this.area=this.area+(this.time.getNext()-this.time.getCurrent())*this.number;
        //imposta il tempo corrente a quello dell'evento corrente
        this.time.setCurrent(this.time.getNext());


        if(e==0){ // controllo se l'evento è un arrivo
            eventList.get(0).setT(this.time.getCurrent()+this.rnd.getJobArrival(1));
            int vType=rnd.getExternalVehicleType(); //vedo quale tipo di veicolo sta arrivando
            System.out.println("arrivo accettazione ");
            if(vType==Integer.MAX_VALUE) { // se il veicolo è pari a max_value vuol dire che non possono esserci arrivi
                ////System.out.println("pieno");
                //continue;
                eventHandler.getEventsSistema().get(1).setT(eventList.get(0).getT());
                return; //non c'è più il ciclo la funzione viene chiamata dall'esterno, se non può essere arrivato nessun veicolo aggiorno arrivo e ritorno
            }
            this.number++; //se è un arrivo incremento il numero di jobs nel sistema

            EventListEntry event=new EventListEntry(eventList.get(0).getT(), 1, vType);

            if(eventList.get(0).getT()>STOP && eventList.get(0).getT()!=Double.MAX_VALUE){ //tempo maggiore della chiusura delle porte
                //eventHandler.getEventsSistema().get(0).setX(0);
                eventHandler.getEventsSistema().get(1).setX(0);
                eventList.get(0).setX(0); //chiusura delle porte
                this.eventHandler.setEventsAccettazione(eventList);
                return;
            }
            if(this.number<=SERVERS_ACCETTAZIONE){ //controllo se ci sono server liberi
                double service=this.rnd.getService(); //ottengo tempo di servizio
                this.s=findOneServerIdle(eventList); //ottengo l'indice di un server libero
                //incrementa i tempi di servizio e il numero di job serviti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();
                //imposta nella lista degli eventi che il server s è busy
                eventList.get(s).setT(this.time.getCurrent()+service);
                eventList.get(s).setX(1);
                eventList.get(s).setVehicleType(vType);

                //aggiorna la lista nell'handler
                this.eventHandler.setEventsAccettazione(eventList);
            }else{
                queueAccettazione.add(event);
            }
        }
        else{ //evento di fine servizio
            //decrementa il numero di eventi nel nodo considerato
            this.number--;
            //aumenta il numero di job serviti
            this.jobServed++;

            this.s=e; //il server con index e è quello che si libera

            EventListEntry event=eventList.get(e);

            //TODO logica di routing

            double rndRouting= rngs.random();
            //TODO volendo vedere se si può fare somma tra i primi x elementi di un array
            if(rndRouting<=P2){
                eventHandler.getInternalEventsGommista().add(new EventListEntry(event.getT(), event.getX(), event.getVehicleType()));
                //List<EventListEntry> it=eventHandler.getInternalEventsGommista();
                System.out.println("ins gom "+eventHandler.getInternalEventsGommista().size());
                eventHandler.getEventsSistema().get(2).setT(event.getT());
                eventHandler.getEventsSistema().get(2).setX(1);
            }
            else if(rndRouting<=(P2+P3)){
                eventHandler.getInternalEventsCarrozzeria().add(new EventListEntry(event.getT(), event.getX(), event.getVehicleType()));
                System.out.println("ins car");
                eventHandler.getEventsSistema().get(3).setT(event.getT());
                eventHandler.getEventsSistema().get(3).setX(1);
            }
            else if(rndRouting<=(P2+P3+P4)){
                eventHandler.getInternalEventsElettrauto().add(new EventListEntry(event.getT(), event.getX(), event.getVehicleType()));
                System.out.println("ins el");
                eventHandler.getEventsSistema().get(4).setT(event.getT());
                eventHandler.getEventsSistema().get(4).setX(1);
            }
            else if(rndRouting<=(P2+P3+P4+P5)){
                eventHandler.getInternalEventsCarpenteria().add(new EventListEntry(event.getT(), event.getX(), event.getVehicleType()));
                System.out.println("ins carp");
                eventHandler.getEventsSistema().get(5).setT(event.getT());
                eventHandler.getEventsSistema().get(5).setX(1);
            }
            else if(rndRouting<=(P2+P3+P4+P5+P6)){
                eventHandler.getInternalEventsMeccanica().add(new EventListEntry(event.getT(), event.getX(), event.getVehicleType()));
                System.out.println("ins mecc");
                eventHandler.getEventsSistema().get(6).setT(event.getT());
                eventHandler.getEventsSistema().get(6).setX(1);
            }
            else{
                eventHandler.decrementVType(event.getVehicleType());
                System.out.println("abbandono");

                //TODO abbandono, diminuire il numero di veicoli disponibili di quel tipo e incrementare abbandono
            }
            //System.out.println("num "+this.number);
            if(this.number>=SERVERS_ACCETTAZIONE){ //controllo se ci sono altri eventi da gestire
                //se ci sono ottengo un nuovo tempo di servizio
                double service=this.rnd.getService();
                //incremento tempo di servizio totale e eventi totali gestiti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();

                //imposta il tempo alla fine del servizio
                eventList.get(s).setT(this.time.getCurrent()+service);
                eventList.get(s).setVehicleType(queueAccettazione.get(0).getVehicleType());
                queueAccettazione.remove(0);
                //aggiorna la lista degli eventi di accettazione
                this.eventHandler.setEventsAccettazione(eventList);
            }else{
                //se non ci sono altri eventi da gestire viene messo il server come idle (x=0)
                eventList.get(e).setX(0);
                //aggiorna la lista
                this.eventHandler.setEventsAccettazione(eventList);
            }


            //TODO gestione inserimento dell'uscita da questo centro in quello successivo
        //}
        }
        //System.out.println("coda size accettazione "+queueAccettazione.size()+" numero "+this.number);
        eventHandler.getEventsSistema().get(1)
                .setT(eventList.get(EventListEntry.getNextEvent(eventList, SERVERS_ACCETTAZIONE)).getT());
    }

    /**
     * ritorna l'indice del server libero da più tempo
     *
     * @param eventListAccettazione lista degli eventi di accettazione
     * @return index del server libero da più tempo
     */
    private int findOneServerIdle(List<EventListEntry> eventListAccettazione) {
        int s;
        int i = 1;

        while (eventListAccettazione.get(i).getX() == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < SERVERS_ACCETTAZIONE) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((eventListAccettazione.get(i).getX() == 0) && (eventListAccettazione.get(i).getT() < eventListAccettazione.get(s).getT()))
                s = i;
        }
        return (s);
    }

    public void printStats() {
        System.out.println("Accettazione\n\n");
        System.out.println("for " + this.jobServed + " jobs the service node statistics are:\n\n");
        System.out.println("  avg interarrivals .. = " + this.eventHandler.getEventsAccettazione().get(0).getT() / this.jobServed);
        System.out.println("  avg wait ........... = " + this.area / this.jobServed);
        System.out.println("  avg # in node ...... = " + this.area / this.time.getCurrent());

        for(int i = 1; i <= SERVERS_ACCETTAZIONE; i++) {
            this.area -= this.sum.get(i).getService();
        }
        System.out.println("  avg delay .......... = " + this.area / this.jobServed);
        System.out.println("  avg # in queue ..... = " + this.area / this.time.getCurrent());
        System.out.println("\nthe server statistics are:\n\n");
        System.out.println("    server     utilization     avg service        share\n");
        for(int i = 1; i <= SERVERS_ACCETTAZIONE; i++) {
            System.out.println(i + "\t" + this.sum.get(i).getService() / this.time.getCurrent() + "\t" + this.sum.get(i).getService() / this.sum.get(i).getServed() + "\t" + ((double)this.sum.get(i).getServed() / this.jobServed));
            //System.out.println(i+"\t");
            //System.out.println("get service" + this.sumList[i].getService() + "\n");
            //System.out.println("getCurrent" + this.time.getCurrent() + "\n");
            //System.out.println("getserved"+this.sumList[i].getServed() + "\n");
            //System.out.println("jobServiti"+this.jobServiti + "\n");
            //System.out.println(i + "\t" + sumList[i].getService() / this.time.getCurrent() + "\t" + this.sumList[i].getService() / this.sumList[i].getServed() + "\t" + this.sumList[i].getServed() / this.jobServiti);
            System.out.println("\n");
            //System.out.println("jobServiti"+this.num_job_feedback + "\n");

        }
        System.out.println("\n");
    }

}
