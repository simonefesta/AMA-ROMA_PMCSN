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
        Rngs rngs = new Rngs();
        rngs.plantSeeds(123456789);

        for(s=0; s<SERVERS_SCARICO+2; s++){
            this.eventListScarico.add(s, new EventListEntry(0,0));
            this.sum.add(s, new MsqSum());
        }

        double firstArrival=this.rnd.getJobArrival(0);

        this.eventListScarico.set(0, new EventListEntry(0, 1, 1));

//        EventListEntry event= eventHandler.getInternalEventsScarico().get(0);

        this.eventListScarico
                .set(eventListScarico.size()-1,new EventListEntry(Double.MAX_VALUE, 1));

        //viene settata la lista di eventi nell'handler
        this.eventHandler.setEventsScarico(eventListScarico);
    }

    public void baseSimulation() throws Exception {
        int e;
        //prende la lista di eventi per lo Scarico
        List<EventListEntry> eventList = this.eventHandler.getEventsScarico();
        List<EventListEntry> internalEventsScarico=eventHandler.getInternalEventsScarico();
        /*
        *il ciclo continua finchè non si verificano entrambe queste condizioni:
        * -eventList[0].x=0 (close door),
        * -number>0 ci sono ancora eventi nel sistema
        */

        System.out.println("interni scarico "+eventHandler.getInternalEventsScarico().size());

        for (EventListEntry event:
                eventList) {
            System.out.println(event.getX()+" "+event.getT());
        }

        if(eventList.get(0).getX()==0 && eventHandler.getInternalEventsScarico().size()==0 && this.number==0){
            eventHandler.getEventsSistema().get(0).setX(0);
            return;
        }

        //while((eventList.get(0).getX() !=0) || (this.number>0)){
            ////System.out.println("loop "+time.getCurrent());
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
                if(e==0) {
                    eventList.get(0).setT(this.time.getCurrent() + this.rnd.getJobArrival(0));
                    System.out.println("arrivo esterno");
                    vType = rnd.getExternalVehicleType(); //vedo quale tipo di veicolo sta arrivando
                    if (vType == Integer.MAX_VALUE) { // se il veicolo è pari a max_value vuol dire che non possono esserci arrivi
                        //System.out.println("pieno");
                        //continue;
                        return; //ho tolto il ciclo
                    }

                    event = new EventListEntry(eventList.get(0).getT(), 1, vType);
                    eventHandler.getEventsSistema().get(0).setT(event.getT());

                    //System.out.println("T "+eventList.get(0).getT());
                    if (eventList.get(0).getT() > STOP/*&& this.number == 0*/) { //tempo maggiore della chiusura delle porte
                        eventList.get(0).setX(0); //chiusura delle porte
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

                System.out.println("uscita scarico");

                this.s=e; //il server con index e è quello che si libera

                EventListEntry event=eventList.get(e);

                //System.out.println("prima "+eventHandler.getNumber());

                //TODO logica di routing (per ora esce dal sistema direttamente)
                //eventHandler.decrementVType(event.getVehicleType());
                //Thread.sleep(2000);
                //System.out.println("decrementato "+eventHandler.getNumber());

                double rndRouting= rngs.random();
                //TODO modificare la logica di routing
                if(rndRouting<=P7){
                    eventHandler.decrementVType(event.getVehicleType());
                    System.out.println("ins uscita");
                }
                else{
                    //aggiunta dell'evento alla coda del checkout
                    eventHandler.getInternalEventsCheckout()
                            .add(new EventListEntry(event.getT(), event.getX(), event.getVehicleType()));
                    eventHandler.getEventsCheckout().set(0,
                            new EventListEntry(event.getT(), 1, event.getVehicleType()));

                    eventHandler.getEventsSistema().get(7).setT(event.getT());
                    eventHandler.getEventsSistema().get(7).setX(1);
                    System.out.println("ins checkout "+eventHandler.getInternalEventsCheckout().size());
                }

                if(this.number>=SERVERS_SCARICO){ //controllo se ci sono altri eventi da gestire
                    //se ci sono ottengo un nuovo tempo di servizio
                    double service=this.rnd.getService();
                    //this.rnd.decrementVehicle(queueScarico.get(0).getVehicleType());
                    //incremento tempo di servizio totale e eventi totali gestiti
                    sum.get(s).incrementService(service);
                    sum.get(s).incrementServed();

                    //imposta il tempo alla fine del servizio
                    eventList.get(s).setT(this.time.getCurrent()+service);
                    eventList.get(s).setVehicleType(queueScarico.get(0).getVehicleType());
                    queueScarico.remove(0);
                    //aggiorna la lista degli eventi di Scarico
                    this.eventHandler.setEventsScarico(eventList);
                }else{
                    //se non ci sono altri eventi da gestire viene messo il server come idle (x=0)
                    eventList.get(e).setX(0);
                    //aggiorna la lista
                    this.eventHandler.setEventsScarico(eventList);
                }

                //TODO gestione inserimento dell'uscita da questo centro in quello successivo
            }

        //System.out.println("scarico lista");


        eventHandler.getEventsSistema().get(0)
                .setT(eventList.get(EventListEntry.getNextEvent(eventList, SERVERS_SCARICO)).getT());

        if(this.number==0 && this.time.getCurrent()>STOP){
            eventHandler.getEventsSistema().get(1).setX(0);
        }

        for (EventListEntry event:
                eventList) {
            //System.out.println(event.getX()+" "+event.getT());
        }

        //System.out.println("torno qunti ev "+eventHandler.getInternalEventsScarico().size()+" "+this.number);

        //Thread.sleep(2000);
        //}
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
