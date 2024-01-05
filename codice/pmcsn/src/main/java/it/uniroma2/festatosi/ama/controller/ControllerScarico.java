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
 * rappresenta la msq per lo scarico
 */
public class ControllerScarico {
    long number =0;                 /*number in the node*/
    int e;                          /*next event index*/
    int s;                          /*server index*/
    private long jobServed=0;           /*contatore jobs processati*/
    private double area=0.0;        /*time integrated number in the node*/

    private final EventHandler eventHandler;  /*istanza dell'EventHandler per ottenere le info sugli eventi*/

    private final RandomDistribution rnd=RandomDistribution.getInstance();
    private final Rngs rngs=new Rngs();

    private final List<MsqSum> sum=new ArrayList<>(SERVERS_SCARICO+2);
    private final MsqT time=new MsqT();
    private final List<EventListEntry> eventListScarico=new ArrayList<>(SERVERS_SCARICO+2);

    private List<EventListEntry> queueScarico=new LinkedList<>();

    public ControllerScarico() throws Exception {

        /*ottengo l'istanza di EventHandler per la gestione degli eventi*/
        this.eventHandler=EventHandler.getInstance();

        /*istanza della classe per creare multi-stream di numeri random*/
        rngs.plantSeeds(rngs.getSeed());
        System.out.println(rngs.getSeed());

        /*inizializza la lista degli eventi dello scarico*/
        for(s=0; s<SERVERS_SCARICO+2; s++){
            this.eventListScarico.add(s, new EventListEntry(0,0));
            this.sum.add(s, new MsqSum());
        }
        /*imposta a 1 l'evento di arrivo da fuori, si aprono le porte*/
        this.eventListScarico.set(0, new EventListEntry(0, 1, 1));

        //viene settata la lista di eventi nell'handler
        this.eventHandler.setEventsScarico(eventListScarico);
    }

    public void baseSimulation() throws Exception {
        int e;
        //prende la lista di eventi per lo Scarico
        List<EventListEntry> eventList = this.eventHandler.getEventsScarico();
        //lista degli eventi dello scarico che arrivano dalle officine
        List<EventListEntry> internalEventsScarico=eventHandler.getInternalEventsScarico();

        /*
        * se le porte sono chiuse, la lista degli eventi arrivati dall'interno è vuota e sono stati processati
        * tutti gli eventi nel sistema si imposta a 0 la x nella event list in modo da non essere invocato nuovamente
        * */
        if(eventList.get(0).getX()==0 && eventHandler.getInternalEventsScarico().size()==0 && this.number==0){
            eventHandler.getEventsSistema().get(0).setX(0);
            return;
        }

        //prende l'indice del primo evento nella lista
        e=EventListEntry.getNextEvent(eventList, SERVERS_SCARICO);
        //imposta il tempo del prossimo evento
        this.time.setNext(eventList.get(e).getT());
        //si calcola l'area dell'integrale
        this.area=this.area+(this.time.getNext()-this.time.getCurrent())*this.number;
        //imposta il tempo corrente a quello dell'evento corrente
        this.time.setCurrent(this.time.getNext());

        if(e==0 || e==eventList.size()-1){ // controllo se l'evento è un arrivo
            //System.out.println("e scarico: "+e);
            int vType;
            EventListEntry event;
            if(e==0) { //arrivo dall'esterno
                vType = rnd.getExternalVehicleType(); //vedo quale tipo di veicolo sta arrivando
                if (vType == Integer.MAX_VALUE) { //se i veicoli sono già tutti nel sistema il VType viene impostato a MAX
                    return; //se i veicoli sono già tutti presenti nel sistema non possono esserci altri arrivi
                }

                //viene creato l'evento in base alle informazioni ricavate
                event = new EventListEntry(eventList.get(0).getT(), 1, vType);
                //si imposta il tempo del prossimo arrivo
                eventList.get(0).setT(this.time.getCurrent() + this.rnd.getJobArrival(0));
                //si imposta la event list di tutto il sistema con il tempo dell'evento corrente
                eventHandler.getEventsSistema().get(0).setT(event.getT());


                if (eventList.get(0).getT() > STOP) { //tempo maggiore della chiusura delle porte
                    eventList.get(0).setX(0); //chiusura delle porte dall'esterno
                    this.eventHandler.setEventsScarico(eventList);
                }
            }else{ //arrivo dall'interno del sistema
                event=internalEventsScarico.get(0);
                internalEventsScarico.remove(0);
                System.out.println("interno");
                vType=event.getVehicleType();

            }

            this.number++; //se è un arrivo incremento il numero di jobs nel sistema

            if(this.number<=SERVERS_SCARICO){ //controllo se ci sono server liberi
                double service=this.rnd.getService(2); //ottengo tempo di servizio
                //this.rnd.decrementVehicle(vType);
                this.s=findOneServerIdle(eventList); //ottengo l'indice di un server libero
                //incrementa i tempi di servizio e il numero di job serviti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();
                //imposta nella lista degli eventi che il server s è busy
                eventList.get(s).setT(this.time.getCurrent()+service);
                eventList.get(s).setX(1);
                eventList.get(s).setVehicleType(vType);

                //aggiorna la lista nell'handler
                this.eventHandler.setEventsScarico(eventList);
            }else{
                queueScarico.add(event);
            }
        }
        else{ //evento di fine servizio
            //decrementa il numero di eventi nel nodo considerato
            this.number--;
            //aumenta il numero di job serviti
            this.jobServed++;

            this.s=e; //il server con index e è quello che si libera

            EventListEntry event=eventList.get(e);

            //logica di routing
            double rndRouting= rngs.random();
            if(rndRouting<=P7){ //uscita dal sistema
                //se il veicolo esce viene decrementato il numero di veicoli dello stesso tipo presenti nel sistema
                eventHandler.decrementVType(event.getVehicleType());
            }
            else{
                //aggiunta dell'evento alla coda del checkout
                eventHandler.getInternalEventsCheckout()
                        .add(new EventListEntry(event.getT(), event.getX(), event.getVehicleType()));
                //impostata a 1 la x degli arrivi del checkout per dire che c'è un arrivo da gestire
                eventHandler.getEventsCheckout().set(0,
                        new EventListEntry(event.getT(), 1, event.getVehicleType()));

                /*impostata la eventList del sistema in modo che quando arriva il suo turno il checkout
                 * può prendere il controllo*/
                eventHandler.getEventsSistema().get(7).setT(event.getT());
                eventHandler.getEventsSistema().get(7).setX(1);
            }

            if(this.number>=SERVERS_SCARICO){ //controllo se ci sono altri eventi da gestire
                //se ci sono ottengo un nuovo tempo di servizio
                double service=this.rnd.getService(2);

                //incremento tempo di servizio totale ed eventi totali gestiti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();

                //imposta il tempo alla fine del servizio
                eventList.get(s).setT(this.time.getCurrent()+service);
                eventList.get(s).setVehicleType(queueScarico.get(0).getVehicleType());

                //rimuovo dalla coda di scarico l'evento preso in gestione dal servente s
                queueScarico.remove(0);

                //aggiorna la lista degli eventi di Scarico
                this.eventHandler.setEventsScarico(eventList);
            }else{
                //se non ci sono altri eventi da gestire viene messo il server come idle (x=0)
                eventList.get(e).setX(0);
                //aggiorna la lista
                this.eventHandler.setEventsScarico(eventList);
            }
        }

        /*viene impostato nella event list del sistema il tempo in cui lo scarico dovrà riprendere servizio come il
          prossimo evento disponibile per lo scarico*/
        eventHandler.getEventsSistema().get(0)
                .setT(eventList.get(EventListEntry.getNextEvent(eventList, SERVERS_SCARICO)).getT());

        /*se sono stati processati tutti gli eventi arrivati e il tempo corrente supera il tempo di stop vengono chiuse
         * le porte, i prossimi arrivi possono arrivare solo da dentro il sistema*/
        if(this.number==0 && this.time.getCurrent()>STOP){
            this.eventHandler.getEventsScarico().get(0).setX(0);
        }
    }

    /**
     * ritorna l'indice del server libero da più tempo
     *
     * @param eventListScarico lista degli eventi di Scarico
     * @return index del server libero da più tempo
     */
    private int findOneServerIdle(List<EventListEntry> eventListScarico) {
        int s;
        int i = 1;

        while (eventListScarico.get(i).getX() == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < SERVERS_SCARICO) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((eventListScarico.get(i).getX() == 0) && (eventListScarico.get(i).getT() < eventListScarico.get(s).getT()))
                s = i;
        }
        return (s);
    }

    public void printStats() {
        System.out.println("Scarico\n\n");
        System.out.println("for " + this.jobServed + " jobs the service node statistics are:\n\n");
        System.out.println("  avg interarrivals .. = " + this.eventHandler.getEventsScarico().get(0).getT() / this.jobServed);
        System.out.println("  avg wait ........... = " + this.area / this.jobServed);
        System.out.println("  avg # in node ...... = " + this.area / this.time.getCurrent());

        for(int i = 1; i <= SERVERS_SCARICO; i++) {
            this.area -= this.sum.get(i).getService();
        }
        System.out.println("  avg delay .......... = " + this.area / this.jobServed);
        System.out.println("  avg # in queue ..... = " + this.area / this.time.getCurrent());
        System.out.println("\nthe server statistics are:\n\n");
        System.out.println("server\tutilization\t\t\tavg service\t\t\tshare");
        for(int i = 1; i <= SERVERS_SCARICO; i++) {
            System.out.println("\t"+i + "\t" + this.sum.get(i).getService() / this.time.getCurrent() + "\t" + this.sum.get(i).getService() / this.sum.get(i).getServed() + "\t" + ((double)this.sum.get(i).getServed() / this.jobServed));
            //System.out.println(i+"\t");
            //System.out.println("get service" + this.sumList[i].getService() + "\n");
            //System.out.println("getCurrent" + this.time.getCurrent() + "\n");
            //System.out.println("getserved"+this.sumList[i].getServed() + "\n");
            //System.out.println("jobServiti"+this.jobServiti + "\n");
            //System.out.println(i + "\t" + sumList[i].getService() / this.time.getCurrent() + "\t" + this.sumList[i].getService() / this.sumList[i].getServed() + "\t" + this.sumList[i].getServed() / this.jobServiti);
            //System.out.println("\n");
            //System.out.println("jobServiti"+this.num_job_feedback + "\n");

        }
        System.out.println("\n");
    }

}
