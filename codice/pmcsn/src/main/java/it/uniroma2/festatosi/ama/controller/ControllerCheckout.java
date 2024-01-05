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
 * rappresenta la msq per il checkout
 */
public class ControllerCheckout {
    long number =0;                 /*number in the node*/
    int e;                          /*next event index*/
    int s;                          /*server index*/
    private long jobServed=0;           /*contatore jobs processati*/
    private double area=0.0;        /*time integrated number in the node*/

    private final EventHandler eventHandler;  /*istanza dell'EventHandler per ottenere le info sugli eventi*/

    private final RandomDistribution rnd=RandomDistribution.getInstance();

    private final List<MsqSum> sum=new ArrayList<>(SERVERS_CHECKOUT+1);
    private final MsqT time=new MsqT();
    private final List<EventListEntry> eventListCheckout=new ArrayList<>(SERVERS_CHECKOUT+1);

    private List<EventListEntry> queueCheckout=new LinkedList<>();

    public ControllerCheckout(){

        /*ottengo l'istanza di EventHandler per la gestione degli eventi*/
        this.eventHandler=EventHandler.getInstance();

        /*istanza della classe per creare multi-stream di numeri random*/
        Rngs rngs = new Rngs();
        rngs.plantSeeds(123456789);

        for(s=0; s<SERVERS_CHECKOUT+1; s++){
            this.eventListCheckout.add(s, new EventListEntry(0,0));
            this.sum.add(s, new MsqSum());
        }

//        EventListEntry event= eventHandler.getInternalEventsCheckout().get(0);
        this.eventListCheckout.set(0,new EventListEntry(0,1));

        //viene settata la lista di eventi nell'handler
        this.eventHandler.setEventsCheckout(eventListCheckout);
    }

    public void baseSimulation() throws Exception {
        int e;
        //prende la lista di eventi per il checkout
        List<EventListEntry> eventList = this.eventHandler.getEventsCheckout();
        List<EventListEntry> internalEventsCheckout=eventHandler.getInternalEventsCheckout();

        /*
         *il ciclo continua finchè non si verificano entrambe queste condizioni:
         * -eventList[0].x=0 (close door),
         * -number>0 ci sono ancora eventi nel sistema
         */


        //while(eventHandler.getInternalEventsCheckout().size()>0 || this.number>0){
        //prende l'indice del primo evento nella lista
        e=EventListEntry.getNextEvent(eventList, SERVERS_CHECKOUT);
        //imposta il tempo del prossimo evento
        this.time.setNext(eventList.get(e).getT());
        //si calcola l'area dell'integrale
        this.area=this.area+(this.time.getNext()-this.time.getCurrent())*this.number;
        //imposta il tempo corrente a quello dell'evento corrente
        this.time.setCurrent(this.time.getNext());

        //System.out.println("area "+area);
        //System.out.println("gom "+e);
        //System.out.println("size "+internalEventsCheckout.size());

        if(internalEventsCheckout.size()==0 && e==0) {
            eventHandler.getEventsSistema().get(7).setX(0);
            System.out.println("ck served "+this.jobServed);
            return;
        }

        if(e==0){ // controllo se l'evento è un arrivo
            EventListEntry event=internalEventsCheckout.get(0);
            internalEventsCheckout.remove(0);
            int vType=event.getVehicleType();
            eventList.set(0,new EventListEntry(event.getT(), event.getX(), vType));

            this.number++; //se è un arrivo incremento il numero di jobs nel sistema

            System.out.println("ins check "+event);
            //se tempo maggiore della chiusura delle porte e numero di job nel sistema nullo, chiudo le porte
                /*if(eventList.get(0).getT()>STOP && this.number==0){
                    eventList.get(0).setX(0); //chiusura delle porte
                    this.eventHandler.setEventsCheckout(eventList);
                }*/
            if(this.number<=SERVERS_CHECKOUT){ //controllo se ci sono server liberi
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
                this.eventHandler.setEventsCheckout(eventList);
            }else{
                queueCheckout.add(eventList.get(0));
                System.out.println("messo in coda "+queueCheckout.size());
            }
            if(internalEventsCheckout.size()==0){
                this.eventHandler.getEventsCheckout().get(0).setX(0);
            }
        }
        else{ //evento di fine servizio
            //decrementa il numero di eventi nel nodo considerato
            this.number--;
            //aumenta il numero di job serviti
            this.jobServed++;

            this.s=e; //il server con index e è quello che si libera

            EventListEntry event=eventList.get(s);

            System.out.println("uscito dal sistema "+this.number+" "+event);

            eventHandler.decrementVType(event.getVehicleType());

            if(this.number>=SERVERS_CHECKOUT){ //controllo se ci sono altri eventi da gestire
                //se ci sono ottengo un nuovo tempo di servizio
                double service=this.rnd.getService();
                //this.rnd.decrementVehicle(queueCheckout.get(0).getVehicleType());

                //incremento tempo di servizio totale e eventi totali gestiti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();

                //imposta il tempo alla fine del servizio
                eventList.get(s).setT(this.time.getCurrent()+service);
                eventList.get(s).setVehicleType(queueCheckout.get(0).getVehicleType());
                queueCheckout.remove(0);
                //aggiorna la lista degli eventi di checkout
                this.eventHandler.setEventsCheckout(eventList);
                System.out.println("preso coda");
            }else{
                //se non ci sono altri eventi da gestire viene messo il server come idle (x=0)
                eventList.get(e).setX(0);
                if(internalEventsCheckout.size()==0 && this.number==0){
                    this.eventHandler.getEventsSistema().get(7).setX(0);
                }
                //aggiorna la lista
                this.eventHandler.setEventsCheckout(eventList);
            }

            //System.out.println("aggiunta centro scarico");

            System.out.println("size chck "+queueCheckout.size());
            //TODO gestione inserimento dell'uscita da questo centro in quello successivo
        }

        for (EventListEntry ev:
             eventList) {
            System.out.println("check "+ ev.getX()+" "+ev.getT());
        }
        System.out.println("chk "+this.number);
        System.out.println("chk 2 "+this.jobServed);

        //}
        for (EventListEntry ev:
                eventList) {
            System.out.println("ev ck "+ev.getX()+" "+ev.getT());
        }

    }

    /**
     * ritorna l'indice del server libero da più tempo
     *
     * @param eventListCheckout lista degli eventi di checkout
     * @return index del server libero da più tempo
     */
    private int findOneServerIdle(List<EventListEntry> eventListCheckout) {
        int s;
        int i = 1;

        while (eventListCheckout.get(i).getX() == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < SERVERS_CHECKOUT) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((eventListCheckout.get(i).getX() == 0) && (eventListCheckout.get(i).getT() < eventListCheckout.get(s).getT()))
                s = i;
        }
        return (s);
    }

    public void printStats() {
        System.out.println("Checkout\n\n");
        System.out.println("for " + this.jobServed + " jobs the service node statistics are:\n\n");
        System.out.println("  avg interarrivals .. = " + this.eventHandler.getEventsCheckout().get(0).getT() / this.jobServed);
        System.out.println("  avg wait ........... = " + this.area / this.jobServed);
        System.out.println("  avg # in node ...... = " + this.area / this.time.getCurrent());

        for(int i = 1; i <= SERVERS_CHECKOUT; i++) {
            this.area -= this.sum.get(i).getService();
        }
        System.out.println("  avg delay .......... = " + this.area / this.jobServed);
        System.out.println("  avg # in queue ..... = " + this.area / this.time.getCurrent());
        System.out.println("\nthe server statistics are:\n\n");
        System.out.println("    server     utilization     avg service        share\n");
        for(int i = 1; i <= SERVERS_CHECKOUT; i++) {
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
