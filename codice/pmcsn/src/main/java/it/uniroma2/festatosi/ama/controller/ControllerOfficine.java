package it.uniroma2.festatosi.ama.controller;

import it.uniroma2.festatosi.ama.model.EventListEntry;
import it.uniroma2.festatosi.ama.model.MsqSum;
import it.uniroma2.festatosi.ama.model.MsqT;
import it.uniroma2.festatosi.ama.utils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static it.uniroma2.festatosi.ama.model.Constants.*;
import static it.uniroma2.festatosi.ama.utils.ReplicationHelper.getReplicationStatistics;

public class ControllerOfficine implements Controller{
    long number =0;                 /*number in the node*/
    public static long counter = 0;
    long numberV1 =0;                 /*number in the node v1*/
    long numberV2 =0;                 /*number in the node v2*/
    int e;                          /*next event index*/
    int s;                          /*server index*/
    private long jobServed=0;           /*contatore jobs processati*/
    private double area=0.0;        /*time integrated number in the node*/
    private double area1=0.0;        /*time integrated number in the node*/
    private double area2=0.0;        /*time integrated number in the node*/
    private String name;
    private final Rngs rngs=new Rngs();

    private final EventHandler eventHandler;  /*istanza dell'EventHandler per ottenere le info sugli eventi*/

    private final RandomDistribution rnd=RandomDistribution.getInstance();
    private final int id;

    private final List<MsqSum> sum;
    private final MsqT time=new MsqT();
    private final List<EventListEntry> eventListOfficina;


    File datiOfficina;
    File datiOfficinaBatch;

    // Creazione del vettore di file

    private final List<EventListEntry> queueOfficina=new LinkedList<>();
    private int jobInBatch=0;
    private final Statistics statOfficina=new Statistics();
    private double batchDuration=0;
    private int batchNumber=1;
    private int arrival=0;

    public ControllerOfficine(int id) throws Exception {
        this.id=id;
        sum=new ArrayList<>(SERVERS_OFFICINA[id]+1);
        eventListOfficina=new ArrayList<>(SERVERS_OFFICINA[this.id]+1);

        initName(this.id);

        /*ottengo l'istanza di EventHandler per la gestione degli eventi*/
        this.eventHandler=EventHandler.getInstance();



        datiOfficina = DataExtractor.initializeFile(rngs.getSeed(),this.name); //fornisco il SEED al file delle statistiche, oltre che il nome del centro
        datiOfficinaBatch = DataExtractor.initializeFileBatch(rngs.getSeed(),this.name+"Batch");
        for(s=0; s<=SERVERS_OFFICINA[this.id]; s++){
            this.eventListOfficina.add(s, new EventListEntry(0,0));
            this.sum.add(s, new MsqSum());
        }

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
         *il ciclo continua finché non si verificano entrambe queste condizioni:
         * -eventList[0].x=0 (close door),
         * -number>0 ci sono ancora eventi nel sistema
         */
        if (!internalEventsOfficina.isEmpty()){
            eventList.get(0).setT(internalEventsOfficina.get(0).getT());
        }
        //prende l'indice del primo evento nella lista
        e=EventListEntry.getNextEvent(eventList, SERVERS_OFFICINA[this.id]);



        //imposta il tempo del prossimo evento
        this.time.setNext(eventList.get(e).getT());
        //si calcola l'area dell'integrale
        this.area = this.area + (this.time.getNext() - this.time.getCurrent()) * this.number;
        //imposta il tempo corrente a quello dell'evento corrente
        this.time.setCurrent(this.time.getNext());


        if (e == 0) { // controllo se l'evento è un arrivo
            EventListEntry event = internalEventsOfficina.get(e);

            internalEventsOfficina.remove(0);
            int vType = event.getVehicleType();
            eventList.set(0, new EventListEntry(event.getT(), event.getX(), vType));


            
            this.number++; //se è un arrivo incremento il numero di jobs nel sistema

            if(vType==1) {
                           this.numberV1++;
            }
            else {
                    this.numberV2++;
            }

            counter++;
            ControllerSistema.counter++;

            DataExtractor.writeSingleStat(datiOfficina, event.getT(), this.number,this.numberV1,this.numberV2);
            DataExtractor.writeSingleStat(datiSistema,this.time.getCurrent(),eventHandler.getNumber(),eventHandler.getNumberV1(),eventHandler.getNumberV2());



            if (this.number <= SERVERS_OFFICINA[this.id]) { //controllo se ci sono server liberi
                double service = this.rnd.getService(3+this.id); //ottengo tempo di servizio
                this.s = findOneServerIdle(eventList); //ottengo l'indice di un server libero
                //incrementa i tempi di servizio e il numero di job serviti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();


                double sum =event.getT() + service;
                //imposta nella lista degli eventi che il server s è busy

                eventList.get(s).setT(sum);
                eventList.get(s).setX(1);
                eventList.get(s).setVehicleType(vType);

                eventHandler.getEventsSistema().get(this.id + 2).setT(sum);

                //aggiorna la lista nell' handler
                this.eventHandler.setEventsOfficina(this.id, eventList);
            } else {
                queueOfficina.add(eventList.get(0)); //se server saturi, rimane in attesa
            }
            if (internalEventsOfficina.isEmpty()) {
                this.eventListOfficina.get(0).setX(0);
            }


        } else { //evento di fine servizio
                //decrementa il numero di eventi nel nodo considerato
                this.number--;
                //aumenta il numero di job serviti
                this.jobServed++;

                this.s = e; //il server con index e è quello che si libera

                EventListEntry event = eventList.get(s);

                if(event.getVehicleType()==1) this.numberV1--;
                else this.numberV2--;


                DataExtractor.writeSingleStat(datiOfficina, event.getT(), this.number,this.numberV1,this.numberV2);
                DataExtractor.writeSingleStat(datiSistema, event.getT(), eventHandler.getNumber(),eventHandler.getNumberV1(),eventHandler.getNumberV2());

                //aggiunta dell'evento alla coda dello scarico
                eventHandler.getInternalEventsScarico()
                        .add(new EventListEntry(event.getT(), event.getX(), event.getVehicleType()));
                if (eventHandler.getEventsScarico().get(eventHandler.getEventsScarico().size() - 1).getT() > event.getT() ||
                        eventHandler.getEventsScarico().get(eventHandler.getEventsScarico().size() - 1).getX() == 0) {
                    eventHandler.getEventsScarico().set(eventHandler.getEventsScarico().size() - 1,
                            new EventListEntry(event.getT(), 1, event.getVehicleType()));
                }
                if (eventHandler.getEventsSistema().get(0).getT() > event.getT() || eventHandler.getEventsSistema().get(0).getX() == 0) {
                    eventHandler.getEventsSistema().get(0).setT(event.getT());
                }
                eventHandler.getEventsSistema().get(0).setX(1);

                if (this.number >= SERVERS_OFFICINA[this.id]) { //controllo se ci sono altri eventi da gestire
                    //se ci sono ottengo un nuovo tempo di servizio
                    double service = this.rnd.getService(3+this.id);

                    //incremento tempo di servizio totale ed eventi totali gestiti
                    sum.get(s).incrementService(service);
                    sum.get(s).incrementServed();

                    //imposta il tempo alla fine del servizio
                    eventList.get(s).setT(event.getT()+ service);
                    eventList.get(s).setVehicleType(queueOfficina.get(0).getVehicleType());
                    queueOfficina.remove(0);
                    //aggiorna la lista degli eventi di officina
                    this.eventHandler.setEventsOfficina(this.id, eventList);
                } else {
                    //se non ci sono altri eventi da gestire viene messo il server come idle (x=0)
                    eventList.get(e).setX(0);

                    if (internalEventsOfficina.isEmpty() && this.number == 0) {
                        this.eventHandler.getEventsSistema().get(this.id + 2).setX(0);
                    }
                    //aggiorna la lista
                    this.eventHandler.setEventsOfficina(this.id, eventList);
                }
            }

        eventHandler.getEventsSistema().get(this.id+2).setT(eventHandler.getMinTime(eventList));
    }

    /**
     *
     * @param typeOfService = 0 se esponenziale (per verifica), 1 altrimenti.
     *
     */
    @Override
    public void infiniteSimulation(int typeOfService) throws Exception {
        int e;
        //prende la lista di eventi per l'officina
        List<EventListEntry> eventList = this.eventHandler.getEventsOfficina(this.id);
        List<EventListEntry> internalEventsOfficina=eventHandler.getInternalEventsOfficina(this.id);


        /*
         *il ciclo continua finché non si verificano entrambe queste condizioni:
         * -eventList[0].x=0 (close door),
         * -number>0 ci sono ancora eventi nel sistema
         */
        if (!internalEventsOfficina.isEmpty()){
            eventList.get(0).setT(internalEventsOfficina.get(0).getT());
        }
        //prende l'indice del primo evento nella lista
        e=EventListEntry.getNextEvent(eventList, SERVERS_OFFICINA[this.id]);


        //imposta il tempo del prossimo evento
        this.time.setNext(eventList.get(e).getT());
        //si calcola l'area dell'integrale
        this.area = this.area + (this.time.getNext() - this.time.getCurrent()) * this.number;
        this.area1=this.area1+(this.time.getNext()-this.time.getCurrent())*this.numberV1;
        this.area2=this.area2+(this.time.getNext()-this.time.getCurrent())*this.numberV2;
        //imposta il tempo corrente a quello dell'evento corrente
        this.time.setCurrent(this.time.getNext());


        if (e == 0) { // controllo se l'evento è un arrivo
            EventListEntry event = internalEventsOfficina.get(e);

            internalEventsOfficina.remove(0);
            int vType = event.getVehicleType();
            eventList.set(0, new EventListEntry(event.getT(), event.getX(), vType));

            this.jobInBatch++;
            this.number++; //se è un arrivo incremento il numero di jobs nel sistema

            if (typeOfService==0) {
                arrival++;
            }
            if(vType==1) this.numberV1++;
            else this.numberV2++;

            counter++;
            ControllerSistema.counter++;

            DataExtractor.writeSingleStat(datiOfficina, event.getT(), this.number,this.numberV1,this.numberV2);
            DataExtractor.writeSingleStat(datiSistema,this.time.getCurrent(),eventHandler.getNumber(),eventHandler.getNumberV1(),eventHandler.getNumberV2());

            if(this.jobInBatch%B==0 && this.jobInBatch<=B*K){
                this.batchDuration= this.time.getCurrent()-this.time.getBatch();


                getStatistics();
                this.batchNumber++;
                this.time.setBatch(this.time.getCurrent());
            }
            
            if (this.number <= SERVERS_OFFICINA[this.id]) { //controllo se ci sono server liberi
                double service;
                if  (typeOfService == 0) service =  this.rnd.getServiceBatch(3+this.id); //ottengo tempo di servizio
                else service = this.rnd.getService(3+this.id);
                this.s = findOneServerIdle(eventList); //ottengo l'indice di un server libero
                //incrementa i tempi di servizio e il numero di job serviti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();


                double sum =event.getT() + service;


                eventList.get(s).setT(sum);
                eventList.get(s).setX(1);
                eventList.get(s).setVehicleType(vType);

                eventHandler.getEventsSistema().get(this.id + 2).setT(sum);

                //aggiorna la lista nell'handler
                this.eventHandler.setEventsOfficina(this.id, eventList);
            } else {
                queueOfficina.add(eventList.get(0)); //se server saturi, rimane in attesa
            }
            if (internalEventsOfficina.isEmpty()) {
                this.eventListOfficina.get(0).setX(0);
            }


        } else { //evento di fine servizio
                //decrementa il numero di eventi nel nodo considerato
                this.number--;
                //aumenta il numero di job serviti
                this.jobServed++;

                this.s = e; //il server con index e è quello che si libera

                EventListEntry event = eventList.get(s);

                if(event.getVehicleType()==1) this.numberV1--;
                else this.numberV2--;

                DataExtractor.writeSingleStat(datiOfficina, event.getT(), this.number,this.numberV1,this.numberV2);
                DataExtractor.writeSingleStat(datiSistema,this.time.getCurrent(),eventHandler.getNumber(),eventHandler.getNumberV1(),eventHandler.getNumberV2());



               //aggiunta dell'evento alla coda dello scarico
                eventHandler.getInternalEventsScarico()
                        .add(new EventListEntry(event.getT(), event.getX(), event.getVehicleType()));
                if (eventHandler.getEventsScarico().get(eventHandler.getEventsScarico().size() - 1).getT() > event.getT() ||
                        eventHandler.getEventsScarico().get(eventHandler.getEventsScarico().size() - 1).getX() == 0) {
                    eventHandler.getEventsScarico().set(eventHandler.getEventsScarico().size() - 1,
                            new EventListEntry(event.getT(), 1, event.getVehicleType()));
                }
                if (eventHandler.getEventsSistema().get(0).getT() > event.getT() || eventHandler.getEventsSistema().get(0).getX() == 0) {
                    eventHandler.getEventsSistema().get(0).setT(event.getT());
                }
                eventHandler.getEventsSistema().get(0).setX(1);

                if (this.number >= SERVERS_OFFICINA[this.id]) { //controllo se ci sono altri eventi da gestire
                    //se ci sono ottengo un nuovo tempo di servizio
                    double service;
                    if  (typeOfService == 0) service =  this.rnd.getServiceBatch(3+this.id); //ottengo tempo di servizio
                    else service = this.rnd.getService(3+this.id);

                    //incremento tempo di servizio totale ed eventi totali gestiti
                    sum.get(s).incrementService(service);
                    sum.get(s).incrementServed();

                    //imposta il tempo alla fine del servizio
                    eventList.get(s).setT(event.getT()+ service);
                    eventList.get(s).setVehicleType(queueOfficina.get(0).getVehicleType());
                    queueOfficina.remove(0);
                    //aggiorna la lista degli eventi di officina
                    this.eventHandler.setEventsOfficina(this.id, eventList);
                } else {
                    //se non ci sono altri eventi da gestire viene messo il server come idle (x=0)
                    eventList.get(e).setX(0);

                    if (internalEventsOfficina.isEmpty() && this.number == 0) {
                        this.eventHandler.getEventsSistema().get(this.id + 2).setX(0);
                    }
                    //aggiorna la lista
                    this.eventHandler.setEventsOfficina(this.id, eventList);
                }
            }

        eventHandler.getEventsSistema().get(this.id+2).setT(eventHandler.getMinTime(eventList));
    }

    @Override
    public void betterBaseSimulation() throws Exception {
        int e;
        //prende la lista di eventi per l'officina
        List<EventListEntry> eventList = this.eventHandler.getEventsOfficina(this.id);
        List<EventListEntry> internalEventsOfficina=eventHandler.getInternalEventsOfficina(this.id);

        /*
         *il ciclo continua finché non si verificano entrambe queste condizioni:
         * -eventList[0].x=0 (close door),
         * -number>0 ci sono ancora eventi nel sistema
         */
        if (!internalEventsOfficina.isEmpty()){
            eventList.get(0).setT(internalEventsOfficina.get(0).getT());
        }
        //prende l'indice del primo evento nella lista
        e=EventListEntry.getNextEvent(eventList, SERVERS_OFFICINA[this.id]);



        //imposta il tempo del prossimo evento
        this.time.setNext(eventList.get(e).getT());
        //si calcola l'area dell'integrale
        this.area = this.area + (this.time.getNext() - this.time.getCurrent()) * this.number;
        //imposta il tempo corrente a quello dell'evento corrente
        this.time.setCurrent(this.time.getNext());


        if (e == 0) { // controllo se l'evento è un arrivo
            EventListEntry event = internalEventsOfficina.get(e);

            internalEventsOfficina.remove(0);
            int vType = event.getVehicleType();
            eventList.set(0, new EventListEntry(event.getT(), event.getX(), vType));



            this.number++; //se è un arrivo incremento il numero di jobs nel sistema

            if(vType==1) {
                this.numberV1++;
            }
            else {
                this.numberV2++;
            }

            DataExtractor.writeSingleStat(datiOfficina, event.getT(), this.number,this.numberV1,this.numberV2);
            DataExtractor.writeSingleStat(datiSistema,this.time.getCurrent(),eventHandler.getNumber(),eventHandler.getNumberV1(),eventHandler.getNumberV2());



            if (this.number <= SERVERS_OFFICINA[this.id]) { //controllo se ci sono server liberi
                double service = this.rnd.getService(3+this.id); //ottengo tempo di servizio
                this.s = findOneServerIdle(eventList); //ottengo l'indice di un server libero
                //incrementa i tempi di servizio e il numero di job serviti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();


                double sum =event.getT() + service;
                //imposta nella lista degli eventi che il server s è busy

                eventList.get(s).setT(sum);
                eventList.get(s).setX(1);
                eventList.get(s).setVehicleType(vType);

                eventHandler.getEventsSistema().get(this.id + 2).setT(sum);

                //aggiorna la lista nell' handler
                this.eventHandler.setEventsOfficina(this.id, eventList);
            } else {
                queueOfficina.add(eventList.get(0)); //se server saturi, rimane in attesa
            }
            if (internalEventsOfficina.isEmpty()) {
                this.eventListOfficina.get(0).setX(0);
            }


        } else { //evento di fine servizio
            //decrementa il numero di eventi nel nodo considerato
            this.number--;
            //aumenta il numero di job serviti
            this.jobServed++;

            this.s = e; //il server con index e è quello che si libera

            EventListEntry event = eventList.get(s);

            if(event.getVehicleType()==1) this.numberV1--;
            else this.numberV2--;


            DataExtractor.writeSingleStat(datiOfficina, event.getT(), this.number,this.numberV1,this.numberV2);
            DataExtractor.writeSingleStat(datiSistema, event.getT(), eventHandler.getNumber(),eventHandler.getNumberV1(),eventHandler.getNumberV2());

            //aggiunta dell'evento alla coda dello scarico
            eventHandler.getInternalEventsScarico()
                    .add(new EventListEntry(event.getT(), event.getX(), event.getVehicleType()));
            if (eventHandler.getEventsScarico().get(eventHandler.getEventsScarico().size() - 1).getT() > event.getT() ||
                    eventHandler.getEventsScarico().get(eventHandler.getEventsScarico().size() - 1).getX() == 0) {
                eventHandler.getEventsScarico().set(eventHandler.getEventsScarico().size() - 1,
                        new EventListEntry(event.getT(), 1, event.getVehicleType()));
            }
            if (eventHandler.getEventsSistema().get(0).getT() > event.getT() || eventHandler.getEventsSistema().get(0).getX() == 0) {
                eventHandler.getEventsSistema().get(0).setT(event.getT());
            }
            eventHandler.getEventsSistema().get(0).setX(1);

            if (this.number >= SERVERS_OFFICINA[this.id]) { //controllo se ci sono altri eventi da gestire
                int eventIndex= eventHandler.getNextEventFromQueue(queueOfficina);
                //se ci sono ottengo un nuovo tempo di servizio
                double service = this.rnd.getService(3+this.id);

                //incremento tempo di servizio totale ed eventi totali gestiti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();

                //imposta il tempo alla fine del servizio
                eventList.get(s).setT(event.getT()+ service);
                eventList.get(s).setVehicleType(queueOfficina.get(eventIndex).getVehicleType());
                queueOfficina.remove(eventIndex);
                //aggiorna la lista degli eventi di officina
                this.eventHandler.setEventsOfficina(this.id, eventList);
            } else {
                //se non ci sono altri eventi da gestire viene messo il server come idle (x=0)
                eventList.get(e).setX(0);

                if (internalEventsOfficina.isEmpty() && this.number == 0) {
                    this.eventHandler.getEventsSistema().get(this.id + 2).setX(0);
                }
                //aggiorna la lista
                this.eventHandler.setEventsOfficina(this.id, eventList);
            }
        }

        eventHandler.getEventsSistema().get(this.id+2).setT(eventHandler.getMinTime(eventList));
    }

    @Override
    public void betterInfiniteSimulation(int typeOfService) throws Exception {
        int e;
        //prende la lista di eventi per l'officina
        List<EventListEntry> eventList = this.eventHandler.getEventsOfficina(this.id);
        List<EventListEntry> internalEventsOfficina=eventHandler.getInternalEventsOfficina(this.id);


        /*
         *il ciclo continua finché non si verificano entrambe queste condizioni:
         * -eventList[0].x=0 (close door),
         * -number>0 ci sono ancora eventi nel sistema
         */
        if (!internalEventsOfficina.isEmpty()){
            eventList.get(0).setT(internalEventsOfficina.get(0).getT());
        }
        //prende l'indice del primo evento nella lista
        e=EventListEntry.getNextEvent(eventList, SERVERS_OFFICINA[this.id]);


        //imposta il tempo del prossimo evento
        this.time.setNext(eventList.get(e).getT());
        //si calcola l'area dell'integrale
        this.area = this.area + (this.time.getNext() - this.time.getCurrent()) * this.number;
        this.area1=this.area1+(this.time.getNext()-this.time.getCurrent())*this.numberV1;
        this.area2=this.area2+(this.time.getNext()-this.time.getCurrent())*this.numberV2;
        //imposta il tempo corrente a quello dell'evento corrente
        this.time.setCurrent(this.time.getNext());


        if (e == 0) { // controllo se l'evento è un arrivo
            EventListEntry event = internalEventsOfficina.get(e);

            internalEventsOfficina.remove(0);
            int vType = event.getVehicleType();
            eventList.set(0, new EventListEntry(event.getT(), event.getX(), vType));

            this.jobInBatch++;
            this.number++; //se è un arrivo incremento il numero di jobs nel sistema

            if (typeOfService==0){
                arrival++;
            }
            if(vType==1) this.numberV1++;
            else this.numberV2++;

            DataExtractor.writeSingleStat(datiOfficina, event.getT(), this.number,this.numberV1,this.numberV2);
            DataExtractor.writeSingleStat(datiSistema,this.time.getCurrent(),eventHandler.getNumber(),eventHandler.getNumberV1(),eventHandler.getNumberV2());

            if(this.jobInBatch%B==0 && this.jobInBatch<=B*K){
                this.batchDuration= this.time.getCurrent()-this.time.getBatch();


                getStatistics();
                this.batchNumber++;
                this.time.setBatch(this.time.getCurrent());
            }

            if (this.number <= SERVERS_OFFICINA[this.id]) { //controllo se ci sono server liberi
                double service;
                if  (typeOfService == 0) service =  this.rnd.getServiceBatch(3+this.id); //ottengo tempo di servizio
                else service = this.rnd.getService(3+this.id);
                this.s = findOneServerIdle(eventList); //ottengo l'indice di un server libero
                //incrementa i tempi di servizio e il numero di job serviti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();


                double sum =event.getT() + service;


                eventList.get(s).setT(sum);
                eventList.get(s).setX(1);
                eventList.get(s).setVehicleType(vType);

                eventHandler.getEventsSistema().get(this.id + 2).setT(sum);

                //aggiorna la lista nell'handler
                this.eventHandler.setEventsOfficina(this.id, eventList);
            } else {
                queueOfficina.add(eventList.get(0)); //se server saturi, rimane in attesa
            }
            if (internalEventsOfficina.isEmpty()) {
                this.eventListOfficina.get(0).setX(0);
            }


        } else { //evento di fine servizio
            //decrementa il numero di eventi nel nodo considerato
            this.number--;
            //aumenta il numero di job serviti
            this.jobServed++;

            this.s = e; //il server con index e è quello che si libera

            EventListEntry event = eventList.get(s);

            if(event.getVehicleType()==1) this.numberV1--;
            else this.numberV2--;

            DataExtractor.writeSingleStat(datiOfficina, event.getT(), this.number,this.numberV1,this.numberV2);
            DataExtractor.writeSingleStat(datiSistema,this.time.getCurrent(),eventHandler.getNumber(),eventHandler.getNumberV1(),eventHandler.getNumberV2());



            //aggiunta dell'evento alla coda dello scarico
            eventHandler.getInternalEventsScarico()
                    .add(new EventListEntry(event.getT(), event.getX(), event.getVehicleType()));
            if (eventHandler.getEventsScarico().get(eventHandler.getEventsScarico().size() - 1).getT() > event.getT() ||
                    eventHandler.getEventsScarico().get(eventHandler.getEventsScarico().size() - 1).getX() == 0) {
                eventHandler.getEventsScarico().set(eventHandler.getEventsScarico().size() - 1,
                        new EventListEntry(event.getT(), 1, event.getVehicleType()));
            }
            if (eventHandler.getEventsSistema().get(0).getT() > event.getT() || eventHandler.getEventsSistema().get(0).getX() == 0) {
                eventHandler.getEventsSistema().get(0).setT(event.getT());
            }
            eventHandler.getEventsSistema().get(0).setX(1);

            if (this.number >= SERVERS_OFFICINA[this.id]) { //controllo se ci sono altri eventi da gestire
                int eventIndex= eventHandler.getNextEventFromQueue(queueOfficina);//se ci sono ottengo un nuovo tempo di servizio
                double service;
                if  (typeOfService == 0) service =  this.rnd.getServiceBatch(3+this.id); //ottengo tempo di servizio
                else service = this.rnd.getService(3+this.id);

                //incremento tempo di servizio totale ed eventi totali gestiti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();

                //imposta il tempo alla fine del servizio
                eventList.get(s).setT(event.getT()+ service);
                eventList.get(s).setVehicleType(queueOfficina.get(eventIndex).getVehicleType());
                queueOfficina.remove(eventIndex);
                //aggiorna la lista degli eventi di officina
                this.eventHandler.setEventsOfficina(this.id, eventList);
            } else {
                //se non ci sono altri eventi da gestire viene messo il server come idle (x=0)
                eventList.get(e).setX(0);

                if (internalEventsOfficina.isEmpty() && this.number == 0) {
                    this.eventHandler.getEventsSistema().get(this.id + 2).setX(0);
                }
                //aggiorna la lista
                this.eventHandler.setEventsOfficina(this.id, eventList);
            }
        }

        eventHandler.getEventsSistema().get(this.id+2).setT(eventHandler.getMinTime(eventList));
    }


    /**
     * Ritorna l'indice del server libero da più tempo
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

    public void printStats(int replicationIndex) throws Exception {
        double utilizzazione=0;
        System.out.println(this.name+"\n\n");
        System.out.println("for " + this.jobServed + " jobs the service node statistics are:\n\n");
        System.out.println("  avg interarrivals .. = " + this.eventHandler.getEventsOfficina(this.id).get(0).getT() / this.jobServed);
        double Ets = this.area / this.jobServed;
        double Ens = this.area / this.time.getCurrent();
        System.out.println("  avg wait ........... = " + Ets);
        System.out.println("  avg # in node ...... = " + Ens);

        for(int i = 1; i <= SERVERS_OFFICINA[this.id]; i++) {
            this.area -= this.sum.get(i).getService();
        }
        double Etq = this.area / this.jobServed;
        double Enq = this.area / this.time.getCurrent();

        System.out.println("  avg delay .......... = " + Etq);
        System.out.println("  avg # in queue ..... = " + Enq);
        System.out.println("\nthe server statistics are:\n\n");
        System.out.println("    server     utilization     avg service        share\n");
        for(int i = 1; i <= SERVERS_OFFICINA[this.id]; i++) {
            System.out.println(i + "\t" + this.sum.get(i).getService() / this.time.getCurrent() + "\t" + this.sum.get(i).getService() / this.sum.get(i).getServed() + "\t" + ((double)this.sum.get(i).getServed() / this.jobServed));
            utilizzazione+=this.sum.get(i).getService() / (SERVERS_ACCETTAZIONE*this.time.getCurrent());
            System.out.println("\n");
        }
        Objects.requireNonNull(getReplicationStatistics(this.name)).setBatchTempoCoda(Etq, replicationIndex);
        Objects.requireNonNull(getReplicationStatistics(this.name)).setBatchPopolazioneSistema(Ens, replicationIndex);
        Objects.requireNonNull(getReplicationStatistics(this.name)).setBatchTempoSistema(Ets, replicationIndex);
        Objects.requireNonNull(getReplicationStatistics(this.name)).setBatchPopolazioneCodaArray(Enq, replicationIndex);
        Objects.requireNonNull(getReplicationStatistics(this.name)).setBatchUtilizzazione(utilizzazione, replicationIndex);
        DataExtractor.writeReplicationStat(ReplicationHelper.getReplicationFile(this.name),Ets, Ens, Etq, Enq);

        System.out.println("\n");
    }

    private void getStatistics(){

        System.out.println("\n\n" + this.name + ", batch: " + batchNumber);
        double meanUtilization;
        double Ens = this.area/(this.batchDuration);
        double Ens1 = this.area1/(this.batchDuration);
        double Ens2 = this.area2/(this.batchDuration);
        double Ets = (this.area)/this.jobServed;
        System.out.println("E[Ns]: " + Ens + " Ets " + Ets); //  Da msq è definita come area/t_Current
        statOfficina.setBatchPopolazioneSistema(Ens, batchNumber); //metto dentro il vettore Ens del batch
        statOfficina.setBatchTempoSistema(Ets, batchNumber); //Metto dentro il vettore Ets del batch


        double sumService = 0; //qui metto la somma dei service time

        // Salviamo i tempi di servizio in una variabile di appoggio
        for(int i = 1; i <= SERVERS_OFFICINA[this.id]; i++) {
            sumService += this.sum.get(i).getService();
            this.sum.get(i).setService(0); //azzero il servizio i-esimo, altrimenti per ogni batch conterà anche i precedenti batch
            this.sum.get(i).setServed(0);
        }

        double Etq = (this.area-sumService)/this.jobServed;             /// E[Tq] = area/nCompletamenti (cosi definito)
        double Enq = (this.area-sumService)/(this.batchDuration);

        statOfficina.setBatchPopolazioneCodaArray(Enq ,  batchNumber); // E[Nq] = area/DeltaT (cosi definito)
        statOfficina.setBatchTempoCoda(Etq, batchNumber);

        System.out.println("Delay E[Tq]: " + Etq + " ; E[Nq] " + Enq);


        meanUtilization = sumService/(this.batchDuration*SERVERS_OFFICINA[this.id]);
        statOfficina.setBatchUtilizzazione(meanUtilization, batchNumber);

        System.out.println("MeanUtilization "+ meanUtilization);

        DataExtractor.writeBatchStat(datiOfficinaBatch, batchNumber, Ens, Ens1, Ens2);


        this.area1 = 0;
        this.area2 = 0;
        this.area = 0;
        this.jobServed = 0;

    }
    
    public int getJobInBatch() {
        return this.jobInBatch;
    }

    public void printFinalStats() {
        Rvms rvms = new Rvms();
        double criticalValue = rvms.idfStudent(K-1,1- alpha/2);
        System.out.println(this.name);
        System.out.print("Statistiche per E[Tq] ");
        statOfficina.setDevStd(statOfficina.getBatchTempoCoda(), 0);     // calcolo la devstd per Etq
        System.out.println("Critical endpoints " + statOfficina.getMeanDelay() + " +/- " + criticalValue * statOfficina.getDevStd(0)/(Math.sqrt(K-1)));
        System.out.print("statistiche per E[Nq] ");
        statOfficina.setDevStd(statOfficina.getBatchPopolazioneCodaArray(),1);     // calcolo la devstd per Enq
        System.out.println("Critical endpoints " + statOfficina.getPopMediaCoda() + " +/- " + criticalValue * statOfficina.getDevStd(1)/(Math.sqrt(K-1)));
        System.out.print("statistiche per rho ");
        statOfficina.setDevStd(statOfficina.getBatchUtilizzazione(),2);     // calcolo la devstd per Enq
        System.out.println("Critical endpoints " + statOfficina.getMeanUtilization() + " +/- " + criticalValue * statOfficina.getDevStd(2)/(Math.sqrt(K-1)));
        System.out.print("statistiche per E[Ts] ");
        statOfficina.setDevStd(statOfficina.getBatchTempoSistema(),3);     // calcolo la devstd per Ens
        System.out.println("Critical endpoints " + statOfficina.getMeanWait() + " +/- " + criticalValue * statOfficina.getDevStd(3)/(Math.sqrt(K-1)));
        System.out.print("statistiche per E[Ns] ");
        statOfficina.setDevStd(statOfficina.getBatchPopolazioneSistema(),4);     // calcolo la devstd per Ets
        System.out.println("Critical endpoints " + statOfficina.getPopMediaSistema() + " +/- " + criticalValue * statOfficina.getDevStd(4)/(Math.sqrt(K-1)));
        System.out.println("Visite: "+arrival/(double)eventHandler.getTotArrival());
        System.out.println("Domanda: "+arrival/(double)eventHandler.getTotArrival()*officina_SR[this.id][0]);
        System.out.println();
        System.out.println("Counter ingressi: "+this.counter);
    }

}
