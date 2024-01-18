package it.uniroma2.festatosi.ama.controller;

import it.uniroma2.festatosi.ama.model.EventListEntry;
import it.uniroma2.festatosi.ama.model.MsqSum;
import it.uniroma2.festatosi.ama.model.MsqT;
import it.uniroma2.festatosi.ama.utils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static it.uniroma2.festatosi.ama.model.Constants.*;


/**
 * Rappresenta la msq per l'accettazione
 */
public class ControllerAccettazione implements Controller {
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

    private final List<EventListEntry> queueAccettazione=new LinkedList<>();

    File datiAccettazione;
    File datiAccettazioneBatch;
    private int jobInBatch=0;
    private double batchDuration=0;
    private int batchNumber=1;
    private final Statistics statAccettazione = new Statistics();;

    public ControllerAccettazione() throws Exception {

        /*ottengo l'istanza di EventHandler per la gestione degli eventi*/
        this.eventHandler=EventHandler.getInstance();

        /*istanza della classe per creare multi-stream di numeri random*/
        Rngs rngs = new Rngs();
        rngs.plantSeeds(SEED);


        datiAccettazione = DataExtractor.initializeFile(rngs.getSeed(),this.getClass().getSimpleName()); //fornisco il SEED al file delle statistiche, oltre che il nome del centro
        datiAccettazioneBatch = DataExtractor.initializeFileBatch(rngs.getSeed(),this.getClass().getSimpleName()+ "Batch");

        List<EventListEntry> eventListAccettazione = new ArrayList<>(SERVERS_ACCETTAZIONE + 1);
        for(s=0; s<SERVERS_ACCETTAZIONE+1; s++){
            eventListAccettazione.add(s, new EventListEntry(0,0));
            this.sum.add(s, new MsqSum());
        }

        double firstArrival=this.rnd.getJobArrival(1);


        //viene settata la lista di eventi nell'handler
        this.eventHandler.setEventsAccettazione(eventListAccettazione);

        //inizializzo il primo evento con tempo pari a un primo arrivo.
        eventListAccettazione.set(0, new EventListEntry(firstArrival, 1, 1));

        //viene settata la lista di eventi nell'handler
        this.eventHandler.setEventsAccettazione(eventListAccettazione);
    }

    public void baseSimulation() throws Exception {

        int e;
        //prende la lista di eventi per l'accettazione
        List<EventListEntry> eventList = this.eventHandler.getEventsAccettazione();

        /*
        *il ciclo continua finché non si verificano entrambe queste condizioni:
        * -eventList[0].x=0 (close door),
        * -number>0 ci sono ancora eventi nel sistema
        */

        if(eventList.get(0).getX()==0 && this.number==0){
            eventHandler.getEventsSistema().get(1).setX(0);
            return;
        }
        //prende l'indice del primo evento nella lista
        e=EventListEntry.getNextEvent(eventList, SERVERS_ACCETTAZIONE);

        //imposta il tempo del prossimo evento
        this.time.setNext(eventList.get(e).getT());
        //si calcola l'area dell'integrale
        this.area=this.area+(this.time.getNext()-this.time.getCurrent())*this.number;
        //imposta il tempo corrente a quello dell'evento corrente
        this.time.setCurrent(this.time.getNext());


        if(e==0){ // controllo se l'evento è un arrivo

            eventList.get(0).setT(this.time.getCurrent()+this.rnd.getJobArrival(1)); // genero il tempo del prossimo arrivo come tempo attuale + interrarivo random
            //System.out.println("time is " + time + " = "+ this.time.getCurrent() + " + " + random);

            //System.out.println("Tempo nuovo arrivo accettazione "+eventList.get(0).getT());

            int vType=rnd.getExternalVehicleType(); //vedo quale tipo di veicolo sta arrivando
            if(vType==Integer.MAX_VALUE) { // se il veicolo è pari a max_value vuol dire che non possono esserci arrivi
                eventList.get(0).setX(0);
                eventHandler.setEventsAccettazione(eventList);
                return; //non c'è più il ciclo la funzione viene chiamata dall'esterno, se non può essere arrivato nessun veicolo aggiorno arrivo e ritorno
            }
            this.number++; //poiché sto processando un arrivo, la popolazione aumenta
            EventListEntry event=new EventListEntry(eventList.get(0).getT(), 1, vType);


            //System.out.println("[Accettazione entrata] TIME: "+ this.time.getCurrent() + " popolazione incrementa " + this.number +"\n");
            //System.out.println("Arrivo accettazione at time: " + event.getT()+  " popolazione " + this.number +);
            DataExtractor.writeSingleStat(datiAccettazione,this.time.getCurrent(),this.number);
            DataExtractor.writeSingleStat(datiSistema,this.time.getCurrent(),eventHandler.getNumber());

            if(eventList.get(0).getT()> STOP_FINITE){ // Se il tempo del prossimo arrivo (generato prima) eccede il tempo di chiusura delle porte, non lo servirò.
                //eventHandler.getEventsSistema().get(0).setX(0);
                eventList.get(0).setX(0); //chiusura delle porte
                this.eventHandler.setEventsAccettazione(eventList);
                //return;
            }

            if(this.number<=SERVERS_ACCETTAZIONE){ //controllo se ci sono server liberi per servire il job che sto analizzando
                double service=this.rnd.getService(0); //ottengo tempo di servizio
                this.s=findOneServerIdle(eventList); //ottengo l'indice di un server libero
                //incrementa i tempi di servizio e il numero di job serviti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();
                //imposta nella lista degli eventi che il server s è busy
                //System.out.println("SERVIZIO on server : " + s + " actual time " + this.time.getCurrent() +  " service " + service + " total is " + sum);
                eventList.get(s).setT(this.time.getCurrent() +service);
                eventList.get(s).setX(1);
                eventList.get(s).setVehicleType(vType);

                //aggiorna la lista nell' handler
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
            //System.out.println("job served " +this.jobServed);

            // System.out.println("[accettazione] jobServer" +this.jobServed);
            //System.out.println("[Accettazione uscita] TIME: "+ this.time.getCurrent() + " popolazione decrementa " + this.number +"\n");
            DataExtractor.writeSingleStat(datiAccettazione,this.time.getCurrent(),this.number);
            DataExtractor.writeSingleStat(datiSistema,this.time.getCurrent(),eventHandler.getNumber());

            this.s=e; //il server con index e è quello che si libera

            EventListEntry event=eventList.get(e);



            //Logica di routing

            double rndRouting= rngs.random();
            int off;
            if(rndRouting<=(P2+P3+P4+P5+P6)) {
                if(rndRouting<=P2){
                    off=0;
                    //System.out.println("Goto gommista");
                }
                else if(rndRouting<=(P2+P3)){
                    off=1;
                    //System.out.println("Goto carrozziere");
                }
                else if(rndRouting<=(P2+P3+P4)){
                    off=2;
                    //System.out.println("goto elettrauto");
                }
                else if(rndRouting<=(P2+P3+P4+P5)){
                    off=3;
                    //System.out.println("goto carpentiere");
                }
                else{
                    off=4;
                    //System.out.println("goto meccanico");
                }

                //Qui indirizziamo sulle varie officine, con i tempi di uscita (cioè quelli di entrata per le officine)
                eventHandler.getInternalEventsOfficina(off).add(new EventListEntry(event.getT(), event.getX(), event.getVehicleType()));
                if(eventHandler.getEventsSistema().get(off+2).getT()>eventList.get(e).getT() || eventHandler.getEventsSistema().get(off+2).getX()==0){
                    eventHandler.getEventsSistema().get(off+2).setT(eventList.get(e).getT());
                }
                eventHandler.getEventsSistema().get(off+2).setX(1);
                eventHandler.getEventsOfficina(off).get(0).setX(1);
            } else{
                eventHandler.decrementVType(event.getVehicleType());
                //System.out.println("abbandono");
                //TODO abbandono, diminuire il numero di veicoli disponibili di quel tipo e incrementare abbandono
            }


            if(this.number>=SERVERS_ACCETTAZIONE){ //controllo se ci sono altri eventi da gestire
                //se ci sono altri job nel sistema, dovrò generare altri tempi di servizio
                double service=this.rnd.getService(0);
                //incremento tempo di servizio totale ed eventi totali gestiti
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

        }

        eventHandler.getEventsSistema().get(1).setT(eventHandler.getMinTime(eventList));

        if(this.number==0 && this.time.getCurrent()> STOP_FINITE){
            this.eventHandler.getEventsAccettazione().get(0).setX(0);
            //return;
        }

    }

    /**
     *
     * @param typeOfService = 0 servizi esponenziali (per verifica), 1 servizi normali
     */

    public void infiniteSimulation(int typeOfService) throws Exception {

        int e;

        //prende la lista di eventi per l'accettazione
        List<EventListEntry> eventList = this.eventHandler.getEventsAccettazione();

        /*
         *il ciclo continua finché non si verificano entrambe queste condizioni:
         * -eventList[0].x=0 (close door),
         * -number>0 ci sono ancora eventi nel sistema
         */

        if(eventList.get(0).getX()==0 && this.number==0){
            eventHandler.getEventsSistema().get(1).setX(0);
            return;
        }
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
            if(vType==Integer.MAX_VALUE) { // se il veicolo è pari a max_value vuol dire che non possono esserci arrivi
                eventList.get(0).setX(0);
                eventHandler.setEventsAccettazione(eventList);
                return; //non c'è più il ciclo la funzione viene chiamata dall'esterno, se non può essere arrivato nessun veicolo aggiorno arrivo e ritorno
            }

            BatchSimulation.incrementJobInBatch(); //arriva un job, incremento il numero di job nel batch corrente
            this.jobInBatch++;
            this.number++; //se è un arrivo incremento il numero di jobs nel sistema
            //System.out.println("[acc] popolazione " + this.number + " at time " + this.time.getCurrent() +" numero job in batch " + BatchSimulation.getJobInBatch() + " numero batch " + BatchSimulation.getNBatch() );
            EventListEntry event=new EventListEntry(eventList.get(0).getT(), 1, vType);


            //System.out.println("[Accettazione entrata] TIME: "+ this.time.getCurrent() + " popolazione attuale " + this.number +"\n");
            DataExtractor.writeSingleStat(datiAccettazione,this.time.getCurrent(),this.number);
            DataExtractor.writeSingleStat(datiSistema,this.time.getCurrent(),eventHandler.getNumber());

            if(this.jobInBatch%B==0 && this.jobInBatch<=B*K){
                this.batchDuration= this.time.getCurrent()-this.time.getBatch();


                getStatistics();
                this.batchNumber++;
                this.time.setBatch(this.time.getCurrent());
            }

            if(this.number<=SERVERS_ACCETTAZIONE){ //controllo se ci sono server liberi

                double service;

                if (typeOfService == 0)  service = this.rnd.getServiceBatch(0); //ottengo tempo di servizio
                else service = this.rnd.getService(0);

                this.s=findOneServerIdle(eventList); //ottengo l'indice di un server libero
                //incrementa i tempi di servizio e il numero di job serviti
                sum.get(s).incrementService(service);
                sum.get(s).incrementServed();
                //imposta nella lista degli eventi che il server s è busy
                eventList.get(s).setT(this.time.getCurrent() +service);
                eventList.get(s).setX(1);
                eventList.get(s).setVehicleType(vType);

                //aggiorna la lista nell' handler
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
            //System.out.println("job served " +this.jobServed + " at time " + this.time.getCurrent());
            //System.out.println("[Accettazione uscita] TIME: "+ this.time.getCurrent() + " popolazione decrementa " + this.number +"\n");
            DataExtractor.writeSingleStat(datiAccettazione,this.time.getCurrent(),this.number);
            DataExtractor.writeSingleStat(datiSistema,this.time.getCurrent(),eventHandler.getNumber());

            this.s=e; //il server con index e è quello che si libera

            EventListEntry event=eventList.get(e);

            double rndRouting= rngs.random();
            int off;
            if(rndRouting<=(P2+P3+P4+P5+P6)) {
                if(rndRouting<=P2){
                    off=0;
                    //System.out.println("Goto gommista");
                }
                else if(rndRouting<=(P2+P3)){
                    off=1;
                    //System.out.println("Goto carrozziere");
                }
                else if(rndRouting<=(P2+P3+P4)){
                    off=2;
                    //System.out.println("goto elettrauto");
                }
                else if(rndRouting<=(P2+P3+P4+P5)){
                    off=3;
                    //System.out.println("goto carpentiere");
                }
                else{
                    off=4;
                    //System.out.println("goto meccanico");
                }

                //Qui indirizziamo sulle varie officine, con i tempi di uscita (cioè quelli di entrata per le officine)
                eventHandler.getInternalEventsOfficina(off).add(new EventListEntry(event.getT(), event.getX(), event.getVehicleType()));
                if(eventHandler.getEventsSistema().get(off+2).getT()>eventList.get(e).getT() || eventHandler.getEventsSistema().get(off+2).getX()==0){
                    eventHandler.getEventsSistema().get(off+2).setT(eventList.get(e).getT());
                }
                eventHandler.getEventsSistema().get(off+2).setX(1);
                eventHandler.getEventsOfficina(off).get(0).setX(1);
            } else{
                eventHandler.decrementVType(event.getVehicleType());
                //System.out.println("abbandono");
                //TODO abbandono, diminuire il numero di veicoli disponibili di quel tipo e incrementare abbandono
            }


            if(this.number>=SERVERS_ACCETTAZIONE){ //controllo se ci sono altri eventi da gestire
                //se ci sono ottengo un nuovo tempo di servizio
                double service;
                if (typeOfService == 0) service=this.rnd.getServiceBatch(0);
                else service = this.rnd.getService(0);
                //incremento tempo di servizio totale ed eventi totali gestiti
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

        }

        eventHandler.getEventsSistema().get(1).setT(eventHandler.getMinTime(eventList));

    }

    /**
     * Ritorna l'indice del server libero da più tempo
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

    private void getStatistics(){

        System.out.println(" \n\nAccettazione, batch: " + batchNumber);
        double meanUtilization;
        //System.out.println("Area ovvero Popolazione TOT: " + this.area + " ; job serviti: " + this.jobServed + " ; batch time " + batchTime);
        double Ens = this.area/(this.batchDuration);
        double Ets = (this.area)/this.jobServed;
        System.out.println("E[Ns]: " + Ens + " Ets " + Ets); //  Da msq è definita come area/t_Current
        statAccettazione.setBatchPopolazioneSistema(Ens, batchNumber); //metto dentro il vettore Ens del batch
        statAccettazione.setBatchTempoSistema(Ets, batchNumber); //Metto dentro il vettore Ets del batch


        double sumService = 0; //qui metto la somma dei service time

        // Salviamo i tempi di servizio in una variabile di appoggio
        for(int i = 1; i <= SERVERS_ACCETTAZIONE; i++) {
            sumService += this.sum.get(i).getService();
            this.sum.get(i).setService(0); //azzero il servizio i-esimo, altrimenti per ogni batch conterà anche i precedenti batch
            this.sum.get(i).setServed(0);
        }

        double Etq = (this.area-sumService)/this.jobServed;             /// E[Tq] = area/nCompletamenti (cosi definito)
        double Enq = (this.area-sumService)/(this.batchDuration);

        statAccettazione.setBatchPopolazioneCodaArray(Enq ,  batchNumber); // E[Nq] = area/DeltaT (cosi definito)
        statAccettazione.setBatchTempoCoda(Etq, batchNumber);

        System.out.println("Delay E[Tq]: " + Etq + " ; E[Nq] " + Enq);


        meanUtilization = sumService/(this.batchDuration*SERVERS_ACCETTAZIONE);
        statAccettazione.setBatchUtilizzazione(meanUtilization, batchNumber);

        System.out.println("MeanUtilization "+ meanUtilization);
        DataExtractor.writeBatchStat(datiAccettazioneBatch,batchNumber,Ens);







        this.area = 0;
         this.jobServed = 0;

    }


    public int getJobInBatch() {
        return this.jobInBatch;
    }

    public void printFinalStats() {
        Rvms rvms = new Rvms();
        double criticalValue = rvms.idfStudent(K-1,1- alpha/2);
        System.out.println("Accettazione");
        System.out.print("Statistiche per E[Tq] ");
        statAccettazione.setDevStd(statAccettazione.getBatchTempoCoda(), 0);     // calcolo la devstd per Etq
        System.out.println("Critical endpoints " + statAccettazione.getMeanDelay() + " +/- " + criticalValue * statAccettazione.getDevStd(0)/(Math.sqrt(K-1)));
        System.out.print("statistiche per E[Nq] ");
        statAccettazione.setDevStd(statAccettazione.getBatchPopolazioneCodaArray(),1);     // calcolo la devstd per Enq
        System.out.println("Critical endpoints " + statAccettazione.getPopMediaCoda() + " +/- " + criticalValue * statAccettazione.getDevStd(1)/(Math.sqrt(K-1)));
        System.out.print("statistiche per rho ");
        statAccettazione.setDevStd(statAccettazione.getBatchUtilizzazione(),2);     // calcolo la devstd per Enq
        System.out.println("Critical endpoints " + statAccettazione.getMeanUtilization() + " +/- " + criticalValue * statAccettazione.getDevStd(2)/(Math.sqrt(K-1)));
        System.out.print("statistiche per E[Ts] ");
        statAccettazione.setDevStd(statAccettazione.getBatchTempoSistema(),3);     // calcolo la devstd per Ens
        System.out.println("Critical endpoints " + statAccettazione.getMeanWait() + " +/- " + criticalValue * statAccettazione.getDevStd(3)/(Math.sqrt(K-1)));
        System.out.print("statistiche per E[Ns] ");
        statAccettazione.setDevStd(statAccettazione.getBatchPopolazioneSistema(),4);     // calcolo la devstd per Ets
        System.out.println("Critical endpoints " + statAccettazione.getPopMediaSistema() + " +/- " + criticalValue * statAccettazione.getDevStd(4)/(Math.sqrt(K-1)));
        System.out.println();
    }
}
