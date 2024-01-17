package it.uniroma2.festatosi.ama.controller;

import it.uniroma2.festatosi.ama.model.EventListEntry;
import it.uniroma2.festatosi.ama.model.MsqSum;
import it.uniroma2.festatosi.ama.model.MsqT;

import it.uniroma2.festatosi.ama.utils.DataExtractor;
import it.uniroma2.festatosi.ama.utils.Rngs;
import it.uniroma2.festatosi.ama.utils.Rvms;
import it.uniroma2.festatosi.ama.utils.Statistics;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.uniroma2.festatosi.ama.controller.BatchSimulation.*;
import static it.uniroma2.festatosi.ama.model.Constants.*;
/*TODO: questa classe ha una event list che segna i tempi minimi di tutti gli eventi nelle varie code e al time-stamp successivo fa procedere la coda di interesse*/
/**
 * Rappresenta la msq per il sistema
 */
public class ControllerSistema {
    long number =0;                 /*number in the node*/
    int e;                          /*next event index*/
    int s;                          /*server index*/
    private long jobServed=0;           /*contatore jobs processati*/ //TODO incrementarlo ogni volta che un evento esce dal sistema
    private double area=0.0;        /*time integrated number in the node*/

    private final EventHandler eventHandler;  /*istanza dell'EventHandler per ottenere le info sugli eventi*/

    private final List<MsqSum> sum=new ArrayList<>(NODES_SISTEMA +1);
    private final MsqT time=new MsqT();
    private final List<EventListEntry> eventListSistema=new ArrayList<>(NODES_SISTEMA);

    private List<Object> controllerList=new ArrayList<>(NODES_SISTEMA);

    long seed;



    public void selectSeed(long seed){
        this.seed = seed;
    }

    public ControllerSistema(long seed) throws Exception {

        /*ottengo l'istanza di EventHandler per la gestione degli eventi*/
        this.eventHandler=EventHandler.getInstance();

        /*istanza della classe per creare multi-stream di numeri random*/
        Rngs rngs = new Rngs();

        rngs.plantSeeds(seed);

        ControllerAccettazione accettazione = new ControllerAccettazione(rngs.getSeed());
        ControllerCheckout checkout= new ControllerCheckout(rngs.getSeed());
        ControllerScarico scarico=new ControllerScarico(rngs.getSeed());

        controllerList.addAll(Arrays.asList(scarico, accettazione));
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
        //inizializzo la eventList del sistema, creo le entry per le varie officine
        for (int i=0;i<SERVERS_OFFICINA.length;i++) {
            controllerList.add(new ControllerOfficine(i,rngs.getSeed()));
            this.eventListSistema.add(i+2, new EventListEntry(0, 0));
            this.sum.add(i + 2, new MsqSum());
        }
        //7 checkout
        controllerList.add(checkout);
        this.eventListSistema.add(7, new EventListEntry(0,0));
        this.sum.add(7, new MsqSum());

        //viene settata la lista di eventi nell'handler
        this.eventHandler.setEventsSistema(eventListSistema);
    }

    public void simulation(int type) throws Exception {
        switch (type){
            case 0:
                baseSimulation();
                break;
            case 1:
                infiniteSimulation();
                break;
            default:
                throw new Exception("Type deve essere 0 o 1");
        }
    }

    public void baseSimulation() throws Exception {
        int e;
        //prende la lista di eventi per il sistema
        List<EventListEntry> eventList = this.eventHandler.getEventsSistema();
        /*
        * il ciclo continua finché non tutti i nodi sono idle e il tempo supera lo stop time
        */
        while(getNextEvent(eventList)!=-1) {
           // System.out.println("evl sys");
            for (EventListEntry ev:
                 eventList) {
                System.out.println(ev.getX()+" "+ev.getT());
            }
            //prende l'indice del primo evento nella lista
            e = getNextEvent(eventList);

           // System.out.println("servito "+e);
            //imposta il tempo del prossimo evento
            this.time.setNext(eventList.get(e).getT());
            //si calcola l'area dell'integrale
            this.area = this.area + (this.time.getNext() - this.time.getCurrent()) * this.number;
            //imposta il tempo corrente a quello dell'evento corrente
            this.time.setCurrent(this.time.getNext());

            //Se l'indice calcolato è maggiore di 7 ritorna errore, nel sistema ci sono 7 code
            if (e > 7) {
                throw new Exception("Errore nessun evento tra i precedenti");
            }

            if (e == 0) {
                ControllerScarico scarico = (ControllerScarico) controllerList.get(e);
                scarico.baseSimulation();
                ////System.out.println(e);
            } else if (e == 1) {
                ControllerAccettazione accettazione = (ControllerAccettazione) controllerList.get(e);
                accettazione.baseSimulation();
                ////System.out.println(e);
            }else if(e==7){
                ControllerCheckout checkout = (ControllerCheckout) controllerList.get(e);
                checkout.baseSimulation();
                ////System.out.println(e);
            }else{
                ControllerOfficine officina= (ControllerOfficine) controllerList.get(e);
                officina.baseSimulation();
            }


            eventList=eventHandler.getEventsSistema();
        }

        ((ControllerScarico) controllerList.get(0)).printStats(); //scarico
        ((ControllerAccettazione) controllerList.get(1)).printStats(); //accettazione
        for (int i = 0; i < SERVERS_OFFICINA.length; i++) {              //officine
            ((ControllerOfficine) controllerList.get(i+2)).printStats();
        }
        ((ControllerCheckout) controllerList.get(7)).printStats();         //checkout


        /*System.out.println("Popolazione: "+ eventHandler.getNumber());
        System.out.println("Gommista " + eventHandler.getInternalEventsGommista().size());
        System.out.println("Carrozzeria "+ eventHandler.getInternalEventsCarrozzeria().size());
        System.out.println("Elettrauti " + eventHandler.getInternalEventsElettrauto().size());
        System.out.println("Carpenteria "+ eventHandler.getInternalEventsCarpenteria().size());
        System.out.println("Meccanica "+ +eventHandler.getInternalEventsMeccanica().size());
        System.out.println("Scarico "+ eventHandler.getInternalEventsScarico().size());
        System.out.println("Checkout " + eventHandler.getInternalEventsCheckout().size());*/

        System.out.println("arrivi nelle 24 ore "+eventHandler.getArr());
    }


    public void infiniteSimulation() throws Exception {
        int e;
        MsqT time=new MsqT();
        time.setCurrent(START);
        time.setNext(START);
        double batchDuration;
        int numVeicoliSys;
        DataExtractor.initializeFile(seed, "Infinite_simulation");

        //prende la lista di eventi per il sistema
        List<EventListEntry> eventList = this.eventHandler.getEventsSistema();
        /*
        * il ciclo continua finché non tutti i nodi sono idle e il tempo supera lo stop time
        */

        while (((ControllerAccettazione)controllerList.get(1)).getJobInBatch() < B * K
            || ((ControllerScarico)controllerList.get(0)).getJobInBatch() < B * K) {
            numVeicoliSys=eventHandler.getNumber();
            eventList = this.eventHandler.getEventsSistema();

            //prende l'indice del primo evento nella lista
            e = getNextEvent(eventList);

           //imposta il tempo del prossimo evento
            this.time.setNext(eventList.get(e).getT());
            //System.out.println(" time event is " + eventList.get(e).getT());
            //si calcola l'area dell'integrale
            this.area = this.area + (this.time.getNext() - this.time.getCurrent()) * this.number;
            //imposta il tempo corrente a quello dell'evento corrente
            //this.time.setCurrent(this.time.getNext());

            //Se l'indice calcolato è maggiore di 7 ritorna errore, nel sistema ci sono 7 code
            if (e > 7) {
                throw new Exception("Errore nessun evento tra i precedenti");
            }

            if (e == 0) {
                ControllerScarico scarico = (ControllerScarico) controllerList.get(e);
                scarico.infiniteSimulation();
//                scarico.baseSimulation();
                ////System.out.println(e);
            } else if (e == 1) {
                ControllerAccettazione accettazione = (ControllerAccettazione) controllerList.get(e);
                accettazione.infiniteSimulation();
                //accettazione.baseSimulation();
                ////System.out.println(e);
            } else if (e == 7) {
                ControllerCheckout checkout = (ControllerCheckout) controllerList.get(e);
                checkout.baseSimulation();
                ////System.out.println(e);
            } else {
                ControllerOfficine officina = (ControllerOfficine) controllerList.get(e);
                officina.baseSimulation();
            }

            /*if(getJobInBatch()%B==0 && numVeicoliSys<eventHandler.getNumber()){

                batchDuration= this.time.getCurrent()-this.time.getBatch();
                System.out.println("\nbatch duration "+ batchDuration + " con " + getJobInBatch() + " job");
                ((ControllerAccettazione) controllerList.get(1)).getStatistics(batchDuration,getNBatch());
                System.out.println("batch "+getNBatch());

                System.out.println("job in batch "+getJobInBatch() +"\n");
                //todo media e varianza

                incrementNBatch();





                this.time.setBatch(this.time.getCurrent());
            }*/
            //System.out.println("");

           // eventList = eventHandler.getEventsSistema();
            this.time.setCurrent(this.time.getNext());
        }

        //stampo statistiche finali scarico
        ((ControllerScarico)controllerList.get(0)).printFinalStats();
        ((ControllerAccettazione)controllerList.get(1)).printFinalStats();

        /*Rvms rvms = new Rvms();
        double criticalValue = rvms.idfStudent(K-1,1- alpha/2);*/

        //Statistics stat = Statistics.getInstance();       //finiti i batch
        /*System.out.println("*** STATISTICHE FINALI con confidenza " + (1- alpha)*100 +  "%");
        System.out.print("Statistiche per E[Tq] ");
        stat.setDevStd(stat.getBatchTempoCoda(), 0);     // calcolo la devstd per Etq
        System.out.println("Critical endpoints " + stat.getMeanDelay() + " +/- " + criticalValue * stat.getDevStd(0)/(Math.sqrt(K-1)));
        System.out.print("Statistiche per E[Nq] ");
        stat.setDevStd(stat.getBatchPopolazioneCodaArray(),1);     // calcolo la devstd per Enq
        System.out.println("Critical endpoints " + stat.getPopMediaCoda() + " +/- " + criticalValue * stat.getDevStd(1)/(Math.sqrt(K-1)));
        System.out.print("Statistiche per rho ");
        stat.setDevStd(stat.getBatchUtilizzazione(),2);     // calcolo la devstd per Enq
        System.out.println("Critical endpoints " + stat.getMeanUtilization() + " +/- " + criticalValue * stat.getDevStd(2)/(Math.sqrt(K-1)));
        System.out.print("Statistiche per E[Ts] ");
        stat.setDevStd(stat.getBatchTempoSistema(),3);     // calcolo la devstd per Ens
        System.out.println("Critical endpoints " + stat.getMeanWait() + " +/- " + criticalValue * stat.getDevStd(3)/(Math.sqrt(K-1)));
        System.out.print("Statistiche per E[Ns] ");
        stat.setDevStd(stat.getBatchPopolazioneSistema(),4);     // calcolo la devstd per Ets
        System.out.println("Critical endpoints " + stat.getPopMediaSistema() + " +/- " + criticalValue * stat.getDevStd(4)/(Math.sqrt(K-1)));*/


       /* System.out.println("MeanDelay Etq");
        for(double mean : stat.getBatchMeanDelayArray()){
            System.out.print(mean+" ");
        }*/



        // STAMPA STATISTICHE, PER ORA DISABILITATO
        //((ControllerScarico) controllerList.get(0)).printStats(); //scarico
        //((ControllerAccettazione) controllerList.get(1)).printStats(); //accettazione
       /* for (int i = 0; i < SERVERS_OFFICINA.length; i++) {         //officine
            ((ControllerOfficine) controllerList.get(i+2)).printStats(); //checkout
        }
        ((ControllerCheckout) controllerList.get(7)).printStats();*/


        /*System.out.println("Popolazione: "+ eventHandler.getNumber());
        System.out.println("Gommista " + eventHandler.getInternalEventsGommista().size());
        System.out.println("Carrozzeria "+ eventHandler.getInternalEventsCarrozzeria().size());
        System.out.println("Elettrauti " + eventHandler.getInternalEventsElettrauto().size());
        System.out.println("Carpenteria "+ eventHandler.getInternalEventsCarpenteria().size());
        System.out.println("Meccanica "+ +eventHandler.getInternalEventsMeccanica().size());
        System.out.println("Scarico "+ eventHandler.getInternalEventsScarico().size());
        System.out.println("Checkout " + eventHandler.getInternalEventsCheckout().size());*/

        System.out.println("\nArrivi batch per "+ B*K +" = B*K job, si hanno "+ eventHandler.getArr());


    }

    /**
     * Seleziona tra gli eventi di sistema quello con tempo più basso per farlo gestire dal controller di interesse
     * @param eventList lista degli eventi di tutto il sistema
     * @return indice dell'evento da gestire all'interno della lista
     */
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

    public void printStats() {
        System.out.println("Sistema\n\n");
        System.out.println("for " + this.jobServed + " jobs the service node statistics are:\n\n");
        System.out.println("  avg interarrivals .. = " + this.eventHandler.getEventsSistema().get(0).getT() / this.jobServed);
        System.out.println("  avg wait ........... = " + this.area / this.jobServed);
        System.out.println("  avg # in node ...... = " + this.area / this.time.getCurrent());

        for(int i = 1; i <= NODES_SISTEMA; i++) {
            this.area -= this.sum.get(i).getService();
        }
        System.out.println("  avg delay .......... = " + this.area / this.jobServed);
        System.out.println("  avg # in queue ..... = " + this.area / this.time.getCurrent());
        System.out.println("\nthe server statistics are:\n\n");
        System.out.println("    server     utilization     avg service        share\n");
        for(int i = 1; i <= NODES_SISTEMA; i++) {
            System.out.println(i + "\t" + this.sum.get(i).getService() / this.time.getCurrent() + "\t" + this.sum.get(i).getService() / this.sum.get(i).getServed() + "\t" + ((double)this.sum.get(i).getServed() / this.jobServed));
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
