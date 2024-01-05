package it.uniroma2.festatosi.ama.controller;

import it.uniroma2.festatosi.ama.model.EventListEntry;
import it.uniroma2.festatosi.ama.model.MsqSum;
import it.uniroma2.festatosi.ama.model.MsqT;
import it.uniroma2.festatosi.ama.utils.RandomDistribution;
import it.uniroma2.festatosi.ama.utils.Rngs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static it.uniroma2.festatosi.ama.model.Constants.SERVERS_OFFICINA;

public class ControllerOfficine {
    long number =0;                 /*number in the node*/
    int e;                          /*next event index*/
    int s;                          /*server index*/
    private long jobServed=0;           /*contatore jobs processati*/
    private double area=0.0;        /*time integrated number in the node*/
    private String name;
    private final EventHandler eventHandler;  /*istanza dell'EventHandler per ottenere le info sugli eventi*/

    private final RandomDistribution rnd=RandomDistribution.getInstance();
    private final int id;

    private final List<MsqSum> sum;
    private final MsqT time=new MsqT();
    private final List<EventListEntry> eventListOfficina;

    private List<EventListEntry> queueOfficina=new LinkedList<>();

    public ControllerOfficine(int id) throws Exception {
        this.id=id;
        sum=new ArrayList<>(SERVERS_OFFICINA[id]+1);
        eventListOfficina=new ArrayList<>(SERVERS_OFFICINA[this.id]+1);

        initName(this.id);

        /*ottengo l'istanza di EventHandler per la gestione degli eventi*/
        this.eventHandler=EventHandler.getInstance();

        /*istanza della classe per creare multi-stream di numeri random*/
        Rngs rngs = new Rngs();
        rngs.plantSeeds(123456789);

        for(s=0; s<=SERVERS_OFFICINA[this.id]; s++){
            this.eventListOfficina.add(s, new EventListEntry(0,0));
            this.sum.add(s, new MsqSum());
        }

        this.eventListOfficina.set(0,new EventListEntry(0, 1));

        //viene settata la lista di eventi nell'handler
        this.eventHandler.setEventsOfficina(this.id, eventListOfficina);
    }

    private void initName(int id) throws Exception {
        switch (id){
            case 0:
                this.name="Gommista";
                break;
            case 1:
                this.name="Carrozzeria";
                break;
            case 2:
                this.name="Elettrauto";
                break;
            case 3:
                this.name="Carpenteria";
                break;
            case 4:
                this.name="Meccanica";
                break;
            default:
                throw new Exception("Id non riconosciuto");
        }
    }

    public void baseSimulation() throws Exception {
        int e;
        //prende la lista di eventi per l'officina
        List<EventListEntry> eventList = this.eventHandler.getEventsOfficina(this.id);
        List<EventListEntry> internalEventsOfficina=eventHandler.getInternalEventsOfficina(this.id);

        /*
         *il ciclo continua finchè non si verificano entrambe queste condizioni:
         * -eventList[0].x=0 (close door),
         * -number>0 ci sono ancora eventi nel sistema
         */

        //prende l'indice del primo evento nella lista
        e=EventListEntry.getNextEvent(eventList, SERVERS_OFFICINA[this.id]);
        //imposta il tempo del prossimo evento
        this.time.setNext(eventList.get(e).getT());
        //si calcola l'area dell'integrale
        this.area=this.area+(this.time.getNext()-this.time.getCurrent())*this.number;
        //imposta il tempo corrente a quello dell'evento corrente
        this.time.setCurrent(this.time.getNext());

        /*if(internalEventsOfficina.size()==0 && e==0) {
            eventHandler.getEventsSistema().get(this.id+2).setX(0);
            return;
        }*/

        if(e==0){ // controllo se l'evento è un arrivo
            EventListEntry event=internalEventsOfficina.get(0);
            internalEventsOfficina.remove(0);
            int vType=event.getVehicleType();
            eventList.set(0,new EventListEntry(event.getT(), event.getX(), vType));

            this.number++; //se è un arrivo incremento il numero di jobs nel sistema

            if(this.number<=SERVERS_OFFICINA[this.id]){ //controllo se ci sono server liberi
                double service=this.rnd.getService(); //ottengo tempo di servizio
                System.out.println(this.name+" in servizio "+this.number+" "+service);
                this.s=findOneServerIdle(eventList); //ottengo l'indice di un server libero
                //incrementa i tempi di servizio e il numero di job serviti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();
                //imposta nella lista degli eventi che il server s è busy
                eventList.get(s).setT(this.time.getCurrent()+service);
                eventList.get(s).setX(1);
                eventList.get(s).setVehicleType(vType);

                eventHandler.getEventsSistema().get(this.id+2).setT(this.time.getCurrent()+service);

                //aggiorna la lista nell'handler
                this.eventHandler.setEventsOfficina(this.id, eventList);
            }else{
                queueOfficina.add(eventList.get(0));
            }
            if(internalEventsOfficina.size()==0){
                this.eventListOfficina.get(0).setX(0);
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
            if (eventHandler.getEventsScarico().get(eventHandler.getEventsScarico().size() - 1).getT() > event.getT() ||
                    eventHandler.getEventsScarico().get(eventHandler.getEventsScarico().size() - 1).getX()==0) {
                eventHandler.getEventsScarico().set(eventHandler.getEventsScarico().size() - 1,
                        new EventListEntry(event.getT(), 1, event.getVehicleType()));
            }
            if(eventHandler.getEventsSistema().get(0).getT()>event.getT() || eventHandler.getEventsSistema().get(0).getX()==0) {
                eventHandler.getEventsSistema().get(0).setT(event.getT());
            }
            eventHandler.getEventsSistema().get(0).setX(1);
            System.out.println("inviato scarico "+this.name);

            if(this.number>=SERVERS_OFFICINA[this.id]){ //controllo se ci sono altri eventi da gestire
                //se ci sono ottengo un nuovo tempo di servizio
                double service=this.rnd.getService();

                //incremento tempo di servizio totale ed eventi totali gestiti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();

                //imposta il tempo alla fine del servizio
                eventList.get(s).setT(this.time.getCurrent()+service);
                eventList.get(s).setVehicleType(queueOfficina.get(0).getVehicleType());
                queueOfficina.remove(0);
                //aggiorna la lista degli eventi di officina
                this.eventHandler.setEventsOfficina(this.id, eventList);
            }else{
                //se non ci sono altri eventi da gestire viene messo il server come idle (x=0)
                eventList.get(e).setX(0);

                if(internalEventsOfficina.size()==0 && this.number==0){
                    this.eventHandler.getEventsSistema().get(this.id+2).setX(0);
                }
                //aggiorna la lista
                this.eventHandler.setEventsOfficina(this.id,eventList);
            }
        }

        for (EventListEntry ev:
             eventList) {
            System.out.println(this.name +" "+ev.getT()+" "+ev.getX());
            System.out.println(this.name +" num "+this.number);
        }
    }



    /**
     * ritorna l'indice del server libero da più tempo
     *
     * @param eventListOfficina lista degli eventi di officina
     * @return index del server libero da più tempo
     */
    private int findOneServerIdle(List<EventListEntry> eventListOfficina) {
        int s;
        int i = 1;

        while (eventListOfficina.get(i).getX() == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < SERVERS_OFFICINA[this.id]) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((eventListOfficina.get(i).getX() == 0) && (eventListOfficina.get(i).getT() < eventListOfficina.get(s).getT()))
                s = i;
        }
        return (s);
    }

    public void printStats() throws Exception {
        System.out.println(this.name+"\n\n");
        System.out.println("for " + this.jobServed + " jobs the service node statistics are:\n\n");
        System.out.println("  avg interarrivals .. = " + this.eventHandler.getEventsOfficina(this.id).get(0).getT() / this.jobServed);
        System.out.println("  avg wait ........... = " + this.area / this.jobServed);
        System.out.println("  avg # in node ...... = " + this.area / this.time.getCurrent());

        for(int i = 1; i <= SERVERS_OFFICINA[this.id]; i++) {
            this.area -= this.sum.get(i).getService();
        }
        System.out.println("  avg delay .......... = " + this.area / this.jobServed);
        System.out.println("  avg # in queue ..... = " + this.area / this.time.getCurrent());
        System.out.println("\nthe server statistics are:\n\n");
        System.out.println("    server     utilization     avg service        share\n");
        for(int i = 1; i <= SERVERS_OFFICINA[this.id]; i++) {
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
