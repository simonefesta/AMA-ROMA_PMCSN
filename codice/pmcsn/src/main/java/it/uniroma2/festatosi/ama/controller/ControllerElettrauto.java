package it.uniroma2.festatosi.ama.controller;

import it.uniroma2.festatosi.ama.model.EventListEntry;
import it.uniroma2.festatosi.ama.model.MsqSum;
import it.uniroma2.festatosi.ama.model.MsqT;
import it.uniroma2.festatosi.ama.utils.RandomDistribution;
import it.uniroma2.festatosi.ama.utils.Rngs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static it.uniroma2.festatosi.ama.model.Constants.SERVERS_ELETTRAUTO;
import static it.uniroma2.festatosi.ama.model.Constants.STOP;

/**
 * rappresenta la msq per il gommista
 */
public class ControllerElettrauto {
    long number =0;                 /*number in the node*/
    int e;                          /*next event index*/
    int s;                          /*server index*/
    private long jobServed=0;           /*contatore jobs processati*/
    private double area=0.0;        /*time integrated number in the node*/

    private final EventHandler eventHandler;  /*istanza dell'EventHandler per ottenere le info sugli eventi*/

    private final RandomDistribution rnd=RandomDistribution.getInstance();
    private final Rngs rngs=new Rngs();

    private final List<MsqSum> sum=new ArrayList<>(SERVERS_ELETTRAUTO+1);
    private final MsqT time=new MsqT();
    private final List<EventListEntry> eventListElettrauto=new ArrayList<>(SERVERS_ELETTRAUTO+1);

    private List<EventListEntry> queueElettrauto=new LinkedList<>();

    public ControllerElettrauto(){

        /*ottengo l'istanza di EventHandler per la gestione degli eventi*/
        this.eventHandler=EventHandler.getInstance();

        /*istanza della classe per creare multi-stream di numeri random*/
        Rngs rngs = new Rngs();
        rngs.plantSeeds(123456789);

        for(s=0; s<SERVERS_ELETTRAUTO+1; s++){
            this.eventListElettrauto.add(s, new EventListEntry(0,0));
            this.sum.add(s, new MsqSum());
        }

//        EventListEntry event= eventHandler.getInternalEventsElettrauto().get(0);
        this.eventListElettrauto.set(0,new EventListEntry(0,1));

        //viene settata la lista di eventi nell'handler
        this.eventHandler.setEventsElettrauto(eventListElettrauto);
    }

    public void baseSimulation() throws Exception {
        int e;
        //prende la lista di eventi per il gommista
        List<EventListEntry> eventList = this.eventHandler.getEventsElettrauto();
        List<EventListEntry> internalEventsElettrauto=eventHandler.getInternalEventsElettrauto();

        /*
        *il ciclo continua finchè non si verificano entrambe queste condizioni:
        * -eventList[0].x=0 (close door),
        * -number>0 ci sono ancora eventi nel sistema
        */

        //while(eventHandler.getInternalEventsElettrauto().size()>0 || this.number>0){
            //prende l'indice del primo evento nella lista
            e=EventListEntry.getNextEvent(eventList, SERVERS_ELETTRAUTO);
            //imposta il tempo del prossimo evento
            this.time.setNext(eventList.get(e).getT());
            //si calcola l'area dell'integrale
            this.area=this.area+(this.time.getNext()-this.time.getCurrent())*this.number;
            //imposta il tempo corrente a quello dell'evento corrente
            this.time.setCurrent(this.time.getNext());

        //System.out.println("area "+area);
        //System.out.println("gom "+e);
        //System.out.println("size "+internalEventsElettrauto.size());

        if(internalEventsElettrauto.size()==0 && e==0) {
            eventHandler.getEventsSistema().get(4).setX(0);
            return;
        }

            if(e==0){ // controllo se l'evento è un arrivo
                EventListEntry event=internalEventsElettrauto.get(0);
                internalEventsElettrauto.remove(0);
                int vType=event.getVehicleType();
                eventList.set(0,new EventListEntry(event.getT(), event.getX(), vType));

                this.number++; //se è un arrivo incremento il numero di jobs nel sistema

                //se tempo maggiore della chiusura delle porte e numero di job nel sistema nullo, chiudo le porte
                if(eventList.get(0).getT()>STOP && this.number==0){
                    eventList.get(0).setX(0); //chiusura delle porte
                    this.eventHandler.setEventsElettrauto(eventList);
                }
                if(this.number<=SERVERS_ELETTRAUTO){ //controllo se ci sono server liberi
                    double service=this.rnd.getService(); //ottengo tempo di servizio
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
                    this.eventHandler.setEventsElettrauto(eventList);
                }else{
                    queueElettrauto.add(eventList.get(0));
                }
            }
            else{ //evento di fine servizio
                //decrementa il numero di eventi nel nodo considerato
                this.number--;
                //aumenta il numero di job serviti
                this.jobServed++;

                this.s=e; //il server con index e è quello che si libera

                EventListEntry event=eventList.get(s);

                //aggiunta dell'evento alla coda dello scarico
                eventHandler.getInternalEventsScarico()
                        .add(new EventListEntry(event.getT(), event.getX(), event.getVehicleType()));
                eventHandler.getEventsScarico().set(eventHandler.getEventsScarico().size()-1,
                        new EventListEntry(event.getT(), 1, event.getVehicleType()));

                eventHandler.getEventsSistema().get(0).setT(event.getT());
                eventHandler.getEventsSistema().get(0).setX(1);
                System.out.println("inviato scarico el");

                if(this.number>=SERVERS_ELETTRAUTO){ //controllo se ci sono altri eventi da gestire
                    //se ci sono ottengo un nuovo tempo di servizio
                    double service=this.rnd.getService();
                    //this.rnd.decrementVehicle(queueElettrauto.get(0).getVehicleType());

                    //incremento tempo di servizio totale e eventi totali gestiti
                    sum.get(s).incrementService(service);
                    sum.get(s).incrementServed();

                    //imposta il tempo alla fine del servizio
                    eventList.get(s).setT(this.time.getCurrent()+service);
                    eventList.get(s).setVehicleType(queueElettrauto.get(0).getVehicleType());
                    queueElettrauto.remove(0);
                    //aggiorna la lista degli eventi di gommista
                    this.eventHandler.setEventsElettrauto(eventList);
                }else{
                    //se non ci sono altri eventi da gestire viene messo il server come idle (x=0)
                    eventList.get(e).setX(0);
                    //aggiorna la lista
                    this.eventHandler.setEventsElettrauto(eventList);
                }

                //System.out.println("aggiunta centro scarico");

                //TODO gestione inserimento dell'uscita da questo centro in quello successivo
            }
        if(this.number==0) {
            eventHandler.getEventsSistema().get(4).setX(0);
        }
        //}
    }

    /**
     * ritorna l'indice del server libero da più tempo
     *
     * @param eventListElettrauto lista degli eventi di gommista
     * @return index del server libero da più tempo
     */
    private int findOneServerIdle(List<EventListEntry> eventListElettrauto) {
        int s;
        int i = 1;

        while (eventListElettrauto.get(i).getX() == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < SERVERS_ELETTRAUTO) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((eventListElettrauto.get(i).getX() == 0) && (eventListElettrauto.get(i).getT() < eventListElettrauto.get(s).getT()))
                s = i;
        }
        return (s);
    }

    public void printStats() {
        //System.out.println("Elettrauto\n\n");
        //System.out.println("for " + this.jobServed + " jobs the service node statistics are:\n\n");
        //System.out.println("  avg interarrivals .. = " + this.eventHandler.getEventsElettrauto().get(0).getT() / this.jobServed);
        //System.out.println("  avg wait ........... = " + this.area / this.jobServed);
        //System.out.println("  avg # in node ...... = " + this.area / this.time.getCurrent());

        for(int i = 1; i <= SERVERS_ELETTRAUTO; i++) {
            this.area -= this.sum.get(i).getService();
        }
        //System.out.println("  avg delay .......... = " + this.area / this.jobServed);
        //System.out.println("  avg # in queue ..... = " + this.area / this.time.getCurrent());
        //System.out.println("\nthe server statistics are:\n\n");
        //System.out.println("    server     utilization     avg service        share\n");
        for(int i = 1; i <= SERVERS_ELETTRAUTO; i++) {
            //System.out.println(i + "\t" + this.sum.get(i).getService() / this.time.getCurrent() + "\t" + this.sum.get(i).getService() / this.sum.get(i).getServed() + "\t" + ((double)this.sum.get(i).getServed() / this.jobServed));
            // //System.out.println(i+"\t");
            // //System.out.println("get service" + this.sumList[i].getService() + "\n");
            // //System.out.println("getCurrent" + this.time.getCurrent() + "\n");
            // //System.out.println("getserved"+this.sumList[i].getServed() + "\n");
            // //System.out.println("jobServiti"+this.jobServiti + "\n");
            ////System.out.println(i + "\t" + sumList[i].getService() / this.time.getCurrent() + "\t" + this.sumList[i].getService() / this.sumList[i].getServed() + "\t" + this.sumList[i].getServed() / this.jobServiti);
            //System.out.println("\n");
            ////System.out.println("jobServiti"+this.num_job_feedback + "\n");

        }
        //System.out.println("\n");
    }

}
