package it.uniroma2.festatosi.ama.controller;

import it.uniroma2.festatosi.ama.model.EventListEntry;
import it.uniroma2.festatosi.ama.model.MsqSum;
import it.uniroma2.festatosi.ama.model.MsqT;
import it.uniroma2.festatosi.ama.utils.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static it.uniroma2.festatosi.ama.model.Constants.*;

/**
 * rappresenta la msq per il checkout
 */
public class ControllerCheckout implements Controller{
    long number =0;                 /*number in the node*/
    long numberV1 =0;                 /*number in the node v1*/
    long numberV2 =0;                 /*number in the node v2*/
    int e;                          /*next event index*/
    int s;                          /*server index*/
    private long jobServed=0;           /*contatore jobs processati*/
    private double area=0.0;        /*time integrated number in the node*/
    private double area1=0.0;        /*time integrated number in the node*/
    private double area2=0.0;        /*time integrated number in the node*/

    private final EventHandler eventHandler;  /*istanza dell'EventHandler per ottenere le info sugli eventi*/

    private final RandomDistribution rnd=RandomDistribution.getInstance();

    private final List<MsqSum> sum=new ArrayList<>(SERVERS_CHECKOUT+1);
    private final MsqT time=new MsqT();
    private final List<EventListEntry> eventListCheckout=new ArrayList<>(SERVERS_CHECKOUT+1);

    private List<EventListEntry> queueCheckout=new LinkedList<>();

    File datiCheckout;
    File datiCheckoutBatch;
    private int jobInBatch=0;
    private Statistics statCheckout=new Statistics();
    private int batchNumber=1;
    private double batchDuration=0;

    public ControllerCheckout() throws IOException {

        /*ottengo l'istanza di EventHandler per la gestione degli eventi*/
        this.eventHandler=EventHandler.getInstance();

        /*istanza della classe per creare multi-stream di numeri random*/
        Rngs rngs = new Rngs();

        rngs.plantSeeds(SEED);

        datiCheckout = DataExtractor.initializeFile(rngs.getSeed(),this.getClass().getSimpleName()); //fornisco il SEED al file delle statistiche, oltre che il nome del centro
        //datiCheckoutBatch = DataExtractor.initializeFileBatch(rngs.getSeed(),this.getClass().getSimpleName()+"Batch");

        for(s=0; s<SERVERS_CHECKOUT+1; s++){
            this.eventListCheckout.add(s, new EventListEntry(0,0));
            this.sum.add(s, new MsqSum());
        }

        this.eventListCheckout.set(0,new EventListEntry(this.time.getCurrent(),1));

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



        //prende l'indice del primo evento nella lista
        e=EventListEntry.getNextEvent(eventList, SERVERS_CHECKOUT);
        //imposta il tempo del prossimo evento
        this.time.setNext(eventList.get(e).getT());
        //si calcola l'area dell'integrale
        this.area=this.area+(this.time.getNext()-this.time.getCurrent())*this.number;
        //imposta il tempo corrente a quello dell'evento corrente
        this.time.setCurrent(this.time.getNext());



        if(internalEventsCheckout.isEmpty() && e==0) {
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

            if(vType==1) {
                            this.numberV1++;
                         }
            else {
                          this.numberV2++;
            }

            DataExtractor.writeSingleStat(datiCheckout,this.time.getCurrent(),this.number,this.numberV1,this.numberV2);
            DataExtractor.writeSingleStat(datiSistema,this.time.getCurrent(),eventHandler.getNumber(),eventHandler.getNumberV1(),eventHandler.getNumberV2());


            if(this.number<=SERVERS_CHECKOUT){ //controllo se ci sono server liberi
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
                this.eventHandler.setEventsCheckout(eventList);
            }else{
                queueCheckout.add(eventList.get(0));
                //System.out.println("messo in coda "+queueCheckout.size());
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

            if(event.getVehicleType()==1) {
                                           this.numberV1--;

            }
            else {
                    this.numberV2--;
            }

            eventHandler.decrementVType(event.getVehicleType());


            DataExtractor.writeSingleStat(datiCheckout,this.time.getCurrent(),this.number,this.numberV1,this.numberV2);
            DataExtractor.writeSingleStat(datiSistema,this.time.getCurrent(),eventHandler.getNumber(),eventHandler.getNumberV1(),eventHandler.getNumberV2());

            if(event.getT()< STOP_FINITE && eventHandler.getNumber()==(VEICOLI1+VEICOLI2-1)){
                //attivo di nuovo arrivi per scarico
                eventHandler.getEventsScarico().get(0).setX(1);
                eventHandler.getEventsScarico().get(0).setT(this.time.getCurrent()+this.rnd.getJobArrival(0));
                eventHandler.getEventsSistema().get(0).setT(eventHandler.getMinTime(eventHandler.getEventsScarico()));
                //attivo di nuovo arrivi per accettazione
                eventHandler.getEventsAccettazione().get(0).setX(1);
                eventHandler.getEventsAccettazione().get(0).setT(this.time.getCurrent()+this.rnd.getJobArrival(1));
                eventHandler.getEventsSistema().get(1).setT(eventHandler.getMinTime(eventHandler.getEventsAccettazione()));
            }

            if(this.number>=SERVERS_CHECKOUT){ //controllo se ci sono altri eventi da gestire
                //se ci sono ottengo un nuovo tempo di servizio
                double service=this.rnd.getService(2);
                //this.rnd.decrementVehicle(queueCheckout.get(0).getVehicleType());

                //incremento tempo di servizio totale ed eventi totali gestiti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();

                //imposta il tempo alla fine del servizio
                eventList.get(s).setT(this.time.getCurrent()+service);
                eventList.get(s).setVehicleType(queueCheckout.get(0).getVehicleType());
                queueCheckout.remove(0);
                //aggiorna la lista degli eventi di checkout
                this.eventHandler.setEventsCheckout(eventList);
                //System.out.println("preso coda");
            }else{
                //se non ci sono altri eventi da gestire viene messo il server come idle (x=0)
                eventList.get(e).setX(0);
                if(internalEventsCheckout.size()==0 && this.number==0){
                    this.eventHandler.getEventsSistema().get(7).setX(0);
                }
                //aggiorna la lista
                this.eventHandler.setEventsCheckout(eventList);
            }


            //TODO gestione inserimento dell'uscita da questo centro in quello successivo
        }


        eventHandler.getEventsSistema().get(7).setT(eventHandler.getMinTime(eventList));

    }

    /**
     *
      @param typeOfService = 0 servizi esponenziali, 1 servizi normali

     **/
    public void infiniteSimulation(int typeOfService) throws Exception {
        int e;
        //prende la lista di eventi per il checkout
        List<EventListEntry> eventList = this.eventHandler.getEventsCheckout();
        List<EventListEntry> internalEventsCheckout=eventHandler.getInternalEventsCheckout();

        datiCheckoutBatch=DataExtractor.initializeFileBatch(SEED, this.getClass().getSimpleName());

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
        this.area1=this.area1+(this.time.getNext()-this.time.getCurrent())*this.numberV1;
        this.area2=this.area2+(this.time.getNext()-this.time.getCurrent())*this.numberV2;
        //imposta il tempo corrente a quello dell'evento corrente
        this.time.setCurrent(this.time.getNext());

        if(internalEventsCheckout.isEmpty() && e==0) {
            eventHandler.getEventsSistema().get(7).setX(0);
            System.out.println("ck served "+this.jobServed);
            return;
        }

        if(e==0){ // controllo se l'evento è un arrivo
            EventListEntry event=internalEventsCheckout.get(0);
            internalEventsCheckout.remove(0);
            int vType=event.getVehicleType();
            eventList.set(0,new EventListEntry(event.getT(), event.getX(), vType));
            //System.out.println("[Checkout] TIME: "+ this.time.getCurrent() + " popolazione decrementa " + this.number +"\n");
            
            this.jobInBatch++;
            
            this.number++; //se è un arrivo incremento il numero di jobs nel sistema

            if(vType==1) this.numberV1++;
            else this.numberV2++;

            DataExtractor.writeSingleStat(datiCheckout,this.time.getCurrent(),this.number,this.numberV1,this.numberV2);
            DataExtractor.writeSingleStat(datiSistema,this.time.getCurrent(),eventHandler.getNumber(),eventHandler.getNumberV1(),eventHandler.getNumberV2());

            if(this.jobInBatch%B==0 && this.jobInBatch<=B*K){
                this.batchDuration= this.time.getCurrent()-this.time.getBatch();


                getStatistics();
                this.batchNumber++;
                this.time.setBatch(this.time.getCurrent());
            }
            

            if(this.number<=SERVERS_CHECKOUT){ //controllo se ci sono server liberi

                double service;
                if (typeOfService == 0) service = this.rnd.getServiceBatch(2); //ottengo tempo di servizio
                else service = this.rnd.getService(2);


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
                //System.out.println("messo in coda "+queueCheckout.size());
            }
            if(internalEventsCheckout.isEmpty()){
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

            if(event.getVehicleType()==1) this.numberV1--;
            else this.numberV2--;

            eventHandler.decrementVType(event.getVehicleType());

            DataExtractor.writeSingleStat(datiCheckout,this.time.getCurrent(),this.number,this.numberV1,this.numberV2);
            DataExtractor.writeSingleStat(datiSistema,this.time.getCurrent(),eventHandler.getNumber(),eventHandler.getNumberV1(),eventHandler.getNumberV2());



            if(event.getT()< STOP_INFINITE && eventHandler.getNumber()==(VEICOLI1+VEICOLI2-1)){
                //attivo di nuovo arrivi per scarico
                eventHandler.getEventsScarico().get(0).setX(1);
                eventHandler.getEventsScarico().get(0).setT(this.time.getCurrent()+this.rnd.getJobArrival(0));
                eventHandler.getEventsSistema().get(0).setT(eventHandler.getMinTime(eventHandler.getEventsScarico()));
                //attivo di nuovo arrivi per accettazione
                eventHandler.getEventsAccettazione().get(0).setX(1);
                eventHandler.getEventsAccettazione().get(0).setT(this.time.getCurrent()+this.rnd.getJobArrival(1));
                eventHandler.getEventsSistema().get(1).setT(eventHandler.getMinTime(eventHandler.getEventsAccettazione()));
            }

            if(this.number>=SERVERS_CHECKOUT){ //controllo se ci sono altri eventi da gestire
                //se ci sono ottengo un nuovo tempo di servizio
                double service;
                if (typeOfService == 0) service = this.rnd.getServiceBatch(2); //ottengo tempo di servizio
                else service = this.rnd.getService(2);
                //this.rnd.decrementVehicle(queueCheckout.get(0).getVehicleType());

                //incremento tempo di servizio totale ed eventi totali gestiti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();

                //imposta il tempo alla fine del servizio
                eventList.get(s).setT(this.time.getCurrent()+service);
                eventList.get(s).setVehicleType(queueCheckout.get(0).getVehicleType());
                queueCheckout.remove(0);
                //aggiorna la lista degli eventi di checkout
                this.eventHandler.setEventsCheckout(eventList);
                //System.out.println("preso coda");
            }else{
                //se non ci sono altri eventi da gestire viene messo il server come idle (x=0)
                eventList.get(e).setX(0);
                if(internalEventsCheckout.isEmpty() && this.number==0){
                    this.eventHandler.getEventsSistema().get(7).setX(0);
                }
                //aggiorna la lista
                this.eventHandler.setEventsCheckout(eventList);
            }


            //TODO gestione inserimento dell'uscita da questo centro in quello successivo
        }


        eventHandler.getEventsSistema().get(7).setT(eventHandler.getMinTime(eventList));

    }

    /**
     * Ritorna l'indice del server libero da più tempo
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

    private void getStatistics(){

        System.out.println("\n\nCheckout, batch: " + batchNumber);
        double meanUtilization;

        double Ens = this.area/(this.batchDuration);
        double Ens1 = this.area1/(this.batchDuration);
        double Ens2 = this.area2/(this.batchDuration);
        double Ets = (this.area)/this.jobServed;
        System.out.println("E[Ns]: " + Ens + " Ets " + Ets); //  Da msq è definita come area/t_Current
        statCheckout.setBatchPopolazioneSistema(Ens, batchNumber); //metto dentro il vettore Ens del batch
        statCheckout.setBatchTempoSistema(Ets, batchNumber); //Metto dentro il vettore Ets del batch


        double sumService = 0; //qui metto la somma dei service time

        // Salviamo i tempi di servizio in una variabile di appoggio
        for(int i = 1; i <= SERVERS_CHECKOUT; i++) {
            sumService += this.sum.get(i).getService();
            this.sum.get(i).setService(0); //azzero il servizio i-esimo, altrimenti per ogni batch conterà anche i precedenti batch
            this.sum.get(i).setServed(0);
        }

        double Etq = (this.area-sumService)/this.jobServed;             /// E[Tq] = area/nCompletamenti (cosi definito)
        double Enq = (this.area-sumService)/(this.batchDuration);

        statCheckout.setBatchPopolazioneCodaArray(Enq ,  batchNumber); // E[Nq] = area/DeltaT (cosi definito)
        statCheckout.setBatchTempoCoda(Etq, batchNumber);

        System.out.println("Delay E[Tq]: " + Etq + " ; E[Nq] " + Enq);


        meanUtilization = sumService/(this.batchDuration*SERVERS_CHECKOUT);
        statCheckout.setBatchUtilizzazione(meanUtilization, batchNumber);

        System.out.println("MeanUtilization "+ meanUtilization);

        DataExtractor.writeBatchStat(datiCheckoutBatch,batchNumber,Ens, Ens1, Ens2);



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
        System.out.println(this.getClass().getSimpleName());
        System.out.print("Statistiche per E[Tq] ");
        statCheckout.setDevStd(statCheckout.getBatchTempoCoda(), 0);     // calcolo la devstd per Etq
        System.out.println("Critical endpoints " + statCheckout.getMeanDelay() + " +/- " + criticalValue * statCheckout.getDevStd(0)/(Math.sqrt(K-1)));
        System.out.print("statistiche per E[Nq] ");
        statCheckout.setDevStd(statCheckout.getBatchPopolazioneCodaArray(),1);     // calcolo la devstd per Enq
        System.out.println("Critical endpoints " + statCheckout.getPopMediaCoda() + " +/- " + criticalValue * statCheckout.getDevStd(1)/(Math.sqrt(K-1)));
        System.out.print("statistiche per rho ");
        statCheckout.setDevStd(statCheckout.getBatchUtilizzazione(),2);     // calcolo la devstd per Enq
        System.out.println("Critical endpoints " + statCheckout.getMeanUtilization() + " +/- " + criticalValue * statCheckout.getDevStd(2)/(Math.sqrt(K-1)));
        System.out.print("statistiche per E[Ts] ");
        statCheckout.setDevStd(statCheckout.getBatchTempoSistema(),3);     // calcolo la devstd per Ens
        System.out.println("Critical endpoints " + statCheckout.getMeanWait() + " +/- " + criticalValue * statCheckout.getDevStd(3)/(Math.sqrt(K-1)));
        System.out.print("statistiche per E[Ns] ");
        statCheckout.setDevStd(statCheckout.getBatchPopolazioneSistema(),4);     // calcolo la devstd per Ets
        System.out.println("Critical endpoints " + statCheckout.getPopMediaSistema() + " +/- " + criticalValue * statCheckout.getDevStd(4)/(Math.sqrt(K-1)));
        System.out.println();
    }
    
}
