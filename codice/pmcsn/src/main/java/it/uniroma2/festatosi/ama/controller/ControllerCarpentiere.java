package it.uniroma2.festatosi.ama.controller;

import it.uniroma2.festatosi.ama.model.EventListEntry;
import it.uniroma2.festatosi.ama.model.MsqSum;
import it.uniroma2.festatosi.ama.model.MsqT;
import it.uniroma2.festatosi.ama.utils.RandomDistribution;
import it.uniroma2.festatosi.ama.utils.Rngs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static it.uniroma2.festatosi.ama.model.Constants.SERVERS_CARPENTERIA;
import static it.uniroma2.festatosi.ama.model.Constants.STOP;

/**
 * rappresenta la msq per il carpentiere
 */
public class ControllerCarpentiere {
    long number =0;                 /*number in the node*/
    int e;                          /*next event index*/
    int s;                          /*server index*/
    private long jobServed=0;           /*contatore jobs processati*/
    private double area=0.0;        /*time integrated number in the node*/

    private final EventHandler eventHandler;  /*istanza dell'EventHandler per ottenere le info sugli eventi*/

    private final RandomDistribution rnd=RandomDistribution.getInstance();
    private final Rngs rngs=new Rngs();

    private final List<MsqSum> sum=new ArrayList<>(SERVERS_CARPENTERIA+1);
    private final MsqT time=new MsqT();
    private final List<EventListEntry> eventListCarpenteria=new ArrayList<>(SERVERS_CARPENTERIA+1);

    private List<EventListEntry> queueCarpenteria=new LinkedList<>();

    public ControllerCarpentiere(){
        /*ottengo l'istanza di EventHandler per la gestione degli eventi*/
        this.eventHandler=EventHandler.getInstance();

        /*istanza della classe per creare multi-stream di numeri random*/
        Rngs rngs = new Rngs();
        rngs.plantSeeds(123456789);

        for(s=0; s<SERVERS_CARPENTERIA+1; s++){
            this.eventListCarpenteria.add(s, new EventListEntry(0,0));
            this.sum.add(s, new MsqSum());
        }

//        EventListEntry event= eventHandler.getInternalEventsCarpenteria().get(0);
        this.eventListCarpenteria.set(0,new EventListEntry(0,1));

        //viene settata la lista di eventi nell'handler
        this.eventHandler.setEventsCarpenteria(eventListCarpenteria);
    }

    public void baseSimulation() throws Exception {
        int e;
        //prende la lista di eventi per il carpentiere
        List<EventListEntry> eventList = this.eventHandler.getEventsCarpenteria();
        List<EventListEntry> internalEventsCarpenteria=eventHandler.getInternalEventsCarpenteria();

        /*
        *il ciclo continua finchè non si verificano entrambe queste condizioni:
        * -eventList[0].x=0 (close door),
        * -number>0 ci sono ancora eventi nel sistema
        */

        for (EventListEntry event:
                eventList) {
            System.out.println("carp "+event.getX()+" "+event.getT());
        }

        //while(eventHandler.getInternalEventsCarpenteria().size()>0 || this.number>0){
            //prende l'indice del primo evento nella lista
            e=EventListEntry.getNextEvent(eventList, SERVERS_CARPENTERIA);
            //imposta il tempo del prossimo evento
            this.time.setNext(eventList.get(e).getT());
            //si calcola l'area dell'integrale
            this.area=this.area+(this.time.getNext()-this.time.getCurrent())*this.number;
            //imposta il tempo corrente a quello dell'evento corrente
            this.time.setCurrent(this.time.getNext());

        //System.out.println("area "+area);
        //System.out.println("gom "+e);
        //System.out.println("size "+internalEventsCarpenteria.size());



        if(internalEventsCarpenteria.size()==0 && e==0) {
            System.out.println("carp num "+this.number);
            eventHandler.getEventsSistema().get(5).setX(0);
            return;
        }

            if(e==0){ // controllo se l'evento è un arrivo
                EventListEntry event=internalEventsCarpenteria.get(0);
                internalEventsCarpenteria.remove(0);
                int vType=event.getVehicleType();
                eventList.set(0,new EventListEntry(event.getT(), event.getX(), vType));

                this.number++; //se è un arrivo incremento il numero di jobs nel sistema

                //se tempo maggiore della chiusura delle porte e numero di job nel sistema nullo, chiudo le porte
                /*if(eventList.get(0).getT()>STOP && this.number==0){
                    eventList.get(0).setX(0); //chiusura delle porte
                    this.eventHandler.setEventsCarpenteria(eventList);
                }*/
                if(this.number<=SERVERS_CARPENTERIA){ //controllo se ci sono server liberi
                    double service=this.rnd.getService(); //ottengo tempo di servizio
                    //this.rnd.decrementVehicle(vType);

                    System.out.println("in carp");

                    System.out.println("in servizio carp "+this.number+" "+service);

                    this.s=findOneServerIdle(eventList); //ottengo l'indice di un server libero
                    //incrementa i tempi di servizio e il numero di job serviti
                    sum.get(s).incrementService(service);
                    sum.get(s).incrementServed();
                    //imposta nella lista degli eventi che il server s è busy
                    eventList.get(s).setT(this.time.getCurrent()+service);
                    eventList.get(s).setX(1);
                    eventList.get(s).setVehicleType(vType);

                    //aggiorna la lista nell'handler
                    this.eventHandler.setEventsCarpenteria(eventList);
                }else{
                    queueCarpenteria.add(eventList.get(0));
                }
                //se non ci sono altri servizi nella coda metto 'off' gli arrivi
                if(queueCarpenteria.size()==0 && internalEventsCarpenteria.size()==0){
                    eventList.get(0).setX(0);
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
                System.out.println("inviato scarico carp");

                if(this.number>=SERVERS_CARPENTERIA){ //controllo se ci sono altri eventi da gestire
                    //se ci sono ottengo un nuovo tempo di servizio
                    double service=this.rnd.getService();
                    //this.rnd.decrementVehicle(queueCarpenteria.get(0).getVehicleType());

                    //incremento tempo di servizio totale e eventi totali gestiti
                    sum.get(s).incrementService(service);
                    sum.get(s).incrementServed();

                    //imposta il tempo alla fine del servizio
                    eventList.get(s).setT(this.time.getCurrent()+service);
                    eventList.get(s).setVehicleType(queueCarpenteria.get(0).getVehicleType());
                    queueCarpenteria.remove(0);
                    //aggiorna la lista degli eventi di carpentiere
                    this.eventHandler.setEventsCarpenteria(eventList);
                }else{
                    //se non ci sono altri eventi da gestire viene messo il server come idle (x=0)
                    eventList.get(e).setX(0);
                    //aggiorna la lista
                    this.eventHandler.setEventsCarpenteria(eventList);
                }

                //System.out.println("aggiunta centro scarico");

                //TODO gestione inserimento dell'uscita da questo centro in quello successivo
            }
        if(this.number==0) {
            eventHandler.getEventsSistema().get(5).setX(0);
        }
        //}
    }

    /**
     * ritorna l'indice del server libero da più tempo
     *
     * @param eventListCarpenteria lista degli eventi di carpentiere
     * @return index del server libero da più tempo
     */
    private int findOneServerIdle(List<EventListEntry> eventListCarpenteria) {
        int s;
        int i = 1;

        while (eventListCarpenteria.get(i).getX() == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < SERVERS_CARPENTERIA) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((eventListCarpenteria.get(i).getX() == 0) && (eventListCarpenteria.get(i).getT() < eventListCarpenteria.get(s).getT()))
                s = i;
        }
        return (s);
    }

    public void printStats() {
        //System.out.println("Carpenteria\n\n");
        //System.out.println("for " + this.jobServed + " jobs the service node statistics are:\n\n");
        //System.out.println("  avg interarrivals .. = " + this.eventHandler.getEventsCarpenteria().get(0).getT() / this.jobServed);
        //System.out.println("  avg wait ........... = " + this.area / this.jobServed);
        //System.out.println("  avg # in node ...... = " + this.area / this.time.getCurrent());

        for(int i = 1; i <= SERVERS_CARPENTERIA; i++) {
            this.area -= this.sum.get(i).getService();
        }
        //System.out.println("  avg delay .......... = " + this.area / this.jobServed);
        //System.out.println("  avg # in queue ..... = " + this.area / this.time.getCurrent());
        //System.out.println("\nthe server statistics are:\n\n");
        //System.out.println("    server     utilization     avg service        share\n");
        for(int i = 1; i <= SERVERS_CARPENTERIA; i++) {
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
