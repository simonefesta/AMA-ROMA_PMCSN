package it.uniroma2.festatosi.ama.controller;

import it.uniroma2.festatosi.ama.model.EventListEntry;
import it.uniroma2.festatosi.ama.model.MsqSum;
import it.uniroma2.festatosi.ama.model.MsqT;

import it.uniroma2.festatosi.ama.utils.ReplicationHelper;
import it.uniroma2.festatosi.ama.utils.Rngs;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private List<Controller> controllerList=new ArrayList<>(NODES_SISTEMA);


    

    public ControllerSistema() throws Exception {

        /*ottengo l'istanza di EventHandler per la gestione degli eventi*/
        this.eventHandler=EventHandler.getInstance();

        /*istanza della classe per creare multi-stream di numeri random*/
        Rngs rngs = new Rngs();

        rngs.plantSeeds(SEED);


        ControllerAccettazione accettazione = new ControllerAccettazione();
        ControllerCheckout checkout= new ControllerCheckout();
        ControllerScarico scarico=new ControllerScarico();

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
            controllerList.add(new ControllerOfficine(i));
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

    public void simulation(int type, int replicationIndex) throws Exception {
        switch (type){
            case 0:
                System.out.println("\nAvvio simulazione transiente, servizi gaussiani.");
                baseSimulation(replicationIndex);
                break;
            case 1:
                System.out.println("\nAvvio simulazione orizzonte infinito, servizi esponenziali.");
                infiniteSimulation(0);  //batch con servizi esponenziali
                break;
            case 2:
                System.out.println("\nAvvio simulazione orizzonte infinito, servizi gaussiani.");
                infiniteSimulation(1);  //batch con servizi normali
                break;
            case 3:
                System.out.println("\nAvvio simulazione MIGLIORATIVA transiente, servizi gaussiani.");
                betterBaseSimulation(replicationIndex);
                break;
            case 4:
                System.out.println("\nAvvio simulazione MIGLIORATIVA infinita, servizi esponenziali.");
                betterInfiniteSimulation(0);
                break;
            case 5:
                System.out.println("\nAvvio simulazione MIGLIORATIVA infinita, servizi gaussiani.");
                betterInfiniteSimulation(1);
                break;
            default:
                throw new Exception("Type deve essere 0, 1 o 2");
        }
    }

    public void baseSimulation(int replicationIndex) throws Exception {
        int e;
        //prende la lista di eventi per il sistema
        List<EventListEntry> eventList = this.eventHandler.getEventsSistema();
        /*
        * il ciclo continua finché non tutti i nodi sono idle e il tempo supera lo stop time
        */
        while(getNextEvent(eventList)!=-1) {

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
            if (e < 0 || e > 7) {
                throw new Exception("Errore nessun evento tra i precedenti");
            }

            controllerList.get(e).baseSimulation();

            eventList=eventHandler.getEventsSistema();
        }

        for(Controller controller: controllerList){
            controller.printStats(replicationIndex);
        }

        System.out.println("arrivi nelle 24 ore "+eventHandler.getArr());
        eventHandler.setArr(0);
    }


    public void infiniteSimulation(int typeOfService) throws Exception {
        int e;
        MsqT time=new MsqT();
        time.setCurrent(START);
        time.setNext(START);


        //prende la lista di eventi per il sistema
        List<EventListEntry> eventList = this.eventHandler.getEventsSistema();
        /*
        * il ciclo continua finché non tutti i nodi sono idle e il tempo supera lo stop time
        */

        while (checkWhile()) {
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
            if (e < 0 || e > 7) {
                throw new Exception("Errore nessun evento tra i precedenti");
            }

            controllerList.get(e).infiniteSimulation(typeOfService);
            this.time.setCurrent(this.time.getNext());
        }

        System.out.println("\n\n*** STATISTICHE FINALI con confidenza " + (1- alpha)*100 +  "%");

        printFinalStats();
        System.out.println("\nArrivi batch per "+ B*K +" = B*K job, si hanno "+ eventHandler.getArr());
    }

    private void printFinalStats() {
        for(Controller controller:controllerList){
            controller.printFinalStats();
        }
    }

    private boolean checkWhile() {
        for(Controller controller:controllerList){
            if(controller.getJobInBatch()<=B*K){
                return true;
            }
        }
        return false;
    }

    public void betterBaseSimulation(int replicationIndex) throws Exception {
        int e;
        //prende la lista di eventi per il sistema
        List<EventListEntry> eventList = this.eventHandler.getEventsSistema();
        /*
         * il ciclo continua finché non tutti i nodi sono idle e il tempo supera lo stop time
         */
        while(getNextEvent(eventList)!=-1) {


            if(eventHandler.getNumberV1()>=(MAX_VEICOLI1*0.5)){
                eventHandler.setPriorityClassV1();
                System.out.println("priorità veicoli 1");
            }
            if(eventHandler.getNumberV2()>=(MAX_VEICOLI2*0.5)){
                eventHandler.setPriorityClassV2();
                System.out.println("priorità veicoli 2");
            }

            if(eventHandler.getNumberV1()>=MAX_VEICOLI1){
                //todo incrementare un contatore per segnare il numero di volte in cui viene superato il limite
                System.out.println("superato il limite per i veicoli 1");
            }

            if(eventHandler.getNumberV2()>=MAX_VEICOLI2){
                //todo incrementare un contatore per segnare il numero di volte in cui viene superato il limite
                System.out.println("superato il limite per i veicoli 2");
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
            if (e < 0 || e > 7) {
                throw new Exception("Errore nessun evento tra i precedenti");
            }

            controllerList.get(e).betterBaseSimulation();

            eventList=eventHandler.getEventsSistema();
        }

        for(Controller controller: controllerList){
            controller.printStats(replicationIndex);
        }

        System.out.println("arrivi nelle 24 ore "+eventHandler.getArr());
    }


    public void betterInfiniteSimulation(int typeOfService) throws Exception {
        int e;
        MsqT time=new MsqT();
        time.setCurrent(START);
        time.setNext(START);


        //prende la lista di eventi per il sistema
        List<EventListEntry> eventList = this.eventHandler.getEventsSistema();
        /*
         * il ciclo continua finché non tutti i nodi sono idle e il tempo supera lo stop time
         */

        while (checkWhile()) {
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
            if (e < 0 || e > 7) {
                throw new Exception("Errore nessun evento tra i precedenti");
            }

            controllerList.get(e).betterInfiniteSimulation(typeOfService);
            this.time.setCurrent(this.time.getNext());
        }

        System.out.println("\n\n*** STATISTICHE FINALI con confidenza " + (1- alpha)*100 +  "%");

        printFinalStats();
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
