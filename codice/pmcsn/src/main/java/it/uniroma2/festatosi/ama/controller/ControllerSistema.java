package it.uniroma2.festatosi.ama.controller;

import it.uniroma2.festatosi.ama.model.EventListEntry;
import it.uniroma2.festatosi.ama.model.MsqSum;
import it.uniroma2.festatosi.ama.model.MsqT;
import it.uniroma2.festatosi.ama.utils.RandomDistribution;
import it.uniroma2.festatosi.ama.utils.Rngs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static it.uniroma2.festatosi.ama.model.Constants.*;
/*TODO: questa classe ha una event list che segna i tempi minimi di tutti gli eventi nelle varie code e al time-stamp successivo fa procedere la coda di interesse*/
/**
 * rappresenta la msq per il sistema
 */
public class ControllerSistema {
    long number =0;                 /*number in the node*/
    int e;                          /*next event index*/
    int s;                          /*server index*/
    private long jobServed=0;           /*contatore jobs processati*/
    private double area=0.0;        /*time integrated number in the node*/

    private final EventHandler eventHandler;  /*istanza dell'EventHandler per ottenere le info sugli eventi*/

    private final RandomDistribution rnd=RandomDistribution.getInstance();
    private final Rngs rngs=new Rngs();

    private final List<MsqSum> sum=new ArrayList<>(NODES_SISTEMA +1);
    private final MsqT time=new MsqT();
    private final List<EventListEntry> eventListSistema=new ArrayList<>(NODES_SISTEMA);

    private List<Object> controllerList;

    public ControllerSistema() throws Exception {

        /*ottengo l'istanza di EventHandler per la gestione degli eventi*/
        this.eventHandler=EventHandler.getInstance();

        /*istanza della classe per creare multi-stream di numeri random*/
        Rngs rngs = new Rngs();
        rngs.plantSeeds(123456789);

        ControllerScarico scarico=new ControllerScarico();
        ControllerAccettazione accettazione = new ControllerAccettazione();
        ControllerGommista gommista=new ControllerGommista();
        ControllerCarrozzeria carrozzeria= new ControllerCarrozzeria();
        ControllerElettrauto elettrauto=new ControllerElettrauto();
        ControllerCarpentiere carpenteria=new ControllerCarpentiere();
        ControllerMeccanica meccanica=new ControllerMeccanica();
        ControllerCheckout checkout= new ControllerCheckout();

        controllerList= Arrays.asList(scarico, accettazione, gommista, carrozzeria, elettrauto, carpenteria, meccanica, checkout);
        /*
        * entry list
        * 0: scarico
        * 1: accettazione
        * 2: gommista
        * 3: carrozzeria
        * 4: elettrauto
        * 5: carpenteria
        * 6: meccanica
        * 7: checkout
         */
        //0 scarico
        List<EventListEntry> listScarico=eventHandler.getEventsScarico();
        int nextEventScarico=EventListEntry.getNextEvent(listScarico, SERVERS_SCARICO+1);
        this.eventListSistema.add(0, new EventListEntry(listScarico.get(nextEventScarico).getT(),1));
        this.sum.add(0, new MsqSum());
        //1 accettazione
        List<EventListEntry> listAccettazione=eventHandler.getEventsAccettazione();
        int nextEventAccettazione=EventListEntry.getNextEvent(listAccettazione, SERVERS_ACCETTAZIONE);
        this.eventListSistema.add(1, new EventListEntry(listAccettazione.get(nextEventAccettazione).getT(),1));
        this.sum.add(1, new MsqSum());
        //2 gommista
        List<EventListEntry> listGommista=eventHandler.getEventsGommista();
        //int nextEventGommista=EventListEntry.getNextEvent(listGommista, SERVERS_GOMMISTA);
        this.eventListSistema.add(2, new EventListEntry(0,0));
        this.sum.add(2, new MsqSum());
        //3 carrozzeria
        List<EventListEntry> listCarrozzeria=eventHandler.getEventsCarrozzeria();
        //int nextEventCarrozzeria=EventListEntry.getNextEvent(listCarrozzeria, SERVERS_CARROZZERIA);
        this.eventListSistema.add(3, new EventListEntry(0,0));
        this.sum.add(3, new MsqSum());
        //4 elettrauto
        List<EventListEntry> listElettrauto=eventHandler.getEventsElettrauto();
        //int nextEventElettrauto=EventListEntry.getNextEvent(listElettrauto, SERVERS_ELETTRAUTO);
        this.eventListSistema.add(4, new EventListEntry(0,0));
        this.sum.add(4, new MsqSum());
        //5 carpenteria
        List<EventListEntry> listCarpenteria=eventHandler.getEventsCarpenteria();
        //int nextEventCarpenteria=EventListEntry.getNextEvent(listCarpenteria, SERVERS_CARPENTERIA);
        this.eventListSistema.add(5, new EventListEntry(0,0));
        this.sum.add(5, new MsqSum());  
        //6 meccanica
        List<EventListEntry> listMeccanica=eventHandler.getEventsMeccanica();
        //int nextEventMeccanica=EventListEntry.getNextEvent(listMeccanica, SERVERS_MECCANICA);
        this.eventListSistema.add(6, new EventListEntry(0,0));
        this.sum.add(6, new MsqSum());
        //7 checkout
        List<EventListEntry> listCheckout=eventHandler.getEventsCheckout();
        //int nextEventCheckout=EventListEntry.getNextEvent(listCheckout, SERVERS_CHECKOUT);
        this.eventListSistema.add(7, new EventListEntry(0,0));
        this.sum.add(7, new MsqSum());

        //viene settata la lista di eventi nell'handler

        for (EventListEntry ev:
             eventListSistema) {
            //System.out.println("sys "+ev.getX()+" "+ev.getT());
        }

        this.eventHandler.setEventsSistema(eventListSistema);
    }

    public void baseSimulation() throws Exception {
        int e;
        //prende la lista di eventi per il sistema
        List<EventListEntry> eventList = this.eventHandler.getEventsSistema();
        /*
        *il ciclo continua finché non si verificano entrambe queste condizioni:
        * -eventList[0].x=0 (close door),
        * -number>0 ci sono ancora eventi nel sistema
        */

        for (EventListEntry ev:
             eventList) {
            //System.out.println(eventList.size()+" "+ev.getT()+" "+ev.getX());
        }
        while(getNextEvent(eventList)!=-1/*eventList.get(0).getX()!=0 || eventHandler.getNumber()!=0*/){
            //System.out.println("eventi numero "+eventHandler.getNumber());
            //prende l'indice del primo evento nella lista
            e=getNextEvent(eventList);
            //imposta il tempo del prossimo evento
            this.time.setNext(eventList.get(e).getT());
            //si calcola l'area dell'integrale
            this.area=this.area+(this.time.getNext()-this.time.getCurrent())*this.number;
            //imposta il tempo corrente a quello dell'evento corrente
            this.time.setCurrent(this.time.getNext());

            //System.out.println("ev "+e);

            if(e==0){
                ControllerScarico scarico=(ControllerScarico) controllerList.get(e);
                scarico.baseSimulation();
                ////System.out.println(e);
            }else if(e==1){
                ControllerAccettazione accettazione=(ControllerAccettazione) controllerList.get(e);
                accettazione.baseSimulation();
                ////System.out.println(e);
            }else if(e==2) {
                ControllerGommista gommista=(ControllerGommista) controllerList.get(e);
                gommista.baseSimulation();
                //System.out.println("gomme");
            }else if(e==3) {
                ControllerCarrozzeria carrozzeria=(ControllerCarrozzeria) controllerList.get(e);
                carrozzeria.baseSimulation();
                //System.out.println("carrozzeria");
            }else if(e==4) {
                ControllerElettrauto elettrauto=(ControllerElettrauto) controllerList.get(e);
                elettrauto.baseSimulation();
                //System.out.println("elettrauto");
            }else if(e==5) {
                ControllerCarpentiere carpentiere=(ControllerCarpentiere) controllerList.get(e);
                carpentiere.baseSimulation();
                //System.out.println("carpentiere");
            }else if(e==6) {
                ControllerMeccanica meccanica=(ControllerMeccanica) controllerList.get(e);
                meccanica.baseSimulation();
                //System.out.println("meccanica");
            }else{
                throw new Exception("Errore nessun evento tra i precedenti");
            }

            System.out.println("x "+eventList.get(0).getX());
            for (EventListEntry ev:
                    eventListSistema) {
                System.out.println("sys "+ev.getX()+" "+ev.getT());
            }

            eventList=eventHandler.getEventsSistema();

            //System.out.println(eventHandler.getNumber());
            //Thread.sleep(2000);
        }

        ((ControllerScarico) controllerList.get(0)).printStats();
        ((ControllerAccettazione) controllerList.get(1)).printStats();
//        ((ControllerGommista) controllerList.get(2)).printStats();
        System.out.println("a\n"+eventHandler.getInternalEventsCarrozzeria().size());
        System.out.println(eventHandler.getInternalEventsCarpenteria().size());
        System.out.println(eventHandler.getInternalEventsMeccanica().size());
        System.out.println(eventHandler.getInternalEventsScarico().size());
        System.out.println(eventHandler.getInternalEventsElettrauto().size());
        System.out.println(eventHandler.getInternalEventsCheckout().size());
        System.out.println(eventHandler.getInternalEventsGommista().size());
        System.out.println(eventHandler.getNumber());
    }

    private int getNextEvent(List<EventListEntry> eventList) {
        double min=Double.MAX_VALUE;
        int e=-1;
        int i=0;
        for (EventListEntry event: eventList) {
            if(event.getT()<min && event.getX()==1){
                min=event.getT();
                e=i;
            }
            i++;
        }
        return e;
    }



    /*
    *//**
     * ritorna l'indice del server libero da più tempo
     *
     * @param eventListSistema lista degli eventi di sistema
     * @return index del server libero da più tempo
     *//*
    private int findOneServerIdle(List<EventListEntry> eventListSistema) {
        int s;
        int i = 1;

        while (eventListSistema.get(i).getX() == 1)       *//* find the index of the first available *//*
            i++;                        *//* (idle) server                         *//*
        s = i;
        while (i < NODES_SISTEMA) {         *//* now, check the others to find which   *//*
            i++;                        *//* has been idle longest                 *//*
            if ((eventListSistema.get(i).getX() == 0) && (eventListSistema.get(i).getT() < eventListSistema.get(s).getT()))
                s = i;
        }
        return (s);
    }

    public void printStats() {
        //System.out.println("Sistema\n\n");
        //System.out.println("for " + this.jobServed + " jobs the service node statistics are:\n\n");
        //System.out.println("  avg interarrivals .. = " + this.eventHandler.getEventsSistema().get(0).getT() / this.jobServed);
        //System.out.println("  avg wait ........... = " + this.area / this.jobServed);
        //System.out.println("  avg # in node ...... = " + this.area / this.time.getCurrent());

        for(int i = 1; i <= NODES_SISTEMA; i++) {
            this.area -= this.sum.get(i).getService();
        }
        //System.out.println("  avg delay .......... = " + this.area / this.jobServed);
        //System.out.println("  avg # in queue ..... = " + this.area / this.time.getCurrent());
        //System.out.println("\nthe server statistics are:\n\n");
        //System.out.println("    server     utilization     avg service        share\n");
        for(int i = 1; i <= NODES_SISTEMA; i++) {
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
    }*/

}
