package it.uniroma2.festatosi.ama.controller;

import it.uniroma2.festatosi.ama.model.EventListEntry;
import it.uniroma2.festatosi.ama.model.MsqSum;
import it.uniroma2.festatosi.ama.model.MsqT;
import it.uniroma2.festatosi.ama.utils.RandomDistribution;
import it.uniroma2.festatosi.ama.utils.Rngs;

import java.util.ArrayList;
import java.util.List;

import static it.uniroma2.festatosi.ama.model.Constants.SERVERS_ACCETTAZIONE;
import static it.uniroma2.festatosi.ama.model.Constants.STOP;

/**
 * rappresenta la msq per l'accettazione
 */
public class ControllerAccettazione {
    long number =0;                 /*number in the node*/
    int e;                          /*next event index*/
    int s;                          /*server index*/
    private long index=0;           /*contatore jobs processati*/
    private double area=0.0;        /*time integrated number in the node*/

    private EventHandler eventHandler;  /*istanza dell'EventHandler per ottenere le info sugli eventi*/
    private Rngs rngs;      /*istanza della classe per creare multi-stream di numeri random*/

    private RandomDistribution rnd=RandomDistribution.getInstance();

    private List<MsqSum> sum=new ArrayList<>(SERVERS_ACCETTAZIONE+1);
    private MsqT time=new MsqT();
    private List<EventListEntry> eventListAccettazione=new ArrayList<>(SERVERS_ACCETTAZIONE+1);
    private int jobServed=0;

    public ControllerAccettazione(){

        /*ottengo l'istanza di EventHandler per la gestione degli eventi*/
        this.eventHandler=EventHandler.getInstance();

        this.rngs=new Rngs();
        rngs.plantSeeds(123456789);

        for(s=0; s<SERVERS_ACCETTAZIONE+1; s++){
            this.eventListAccettazione.add(s, new EventListEntry(0,0));
            this.sum.add(s, new MsqSum());
        }

        double firstArrival=this.rnd.getJobArrival();

        this.eventListAccettazione.set(0, new EventListEntry(firstArrival, 1));

        //viene settata la lista di eventi nell'handler
        this.eventHandler.setEventsAccettazione(eventListAccettazione);
    }

    public void baseSimulation(){
        int e;

        List<EventListEntry> eventList = this.eventHandler.getEventsAccettazione();
        while((eventList.get(0).getX() !=0) || (this.number>0)){
            e=EventListEntry.getNextEvent(eventList, SERVERS_ACCETTAZIONE);
            this.time.setNext(eventList.get(e).getT());
            this.area=this.area+(this.time.getNext()-this.time.getCurrent())*this.number;
            this.time.setCurrent(this.time.getNext());

            if(e==0){ // controllo se l'evento è un arrivo
                this.number++; //se è un arrivo incremento il numero di jobs nel sistema
                eventList.get(0).setT(this.rnd.getJobArrival());
                if(eventList.get(0).getT()>STOP){ //tempo maggiore della chiusura delle porte
                    eventList.get(0).setX(0);
                    this.eventHandler.setEventsAccettazione(eventListAccettazione);
                }
                if(this.number<=SERVERS_ACCETTAZIONE){
                    double service=this.rnd.getService();
                    this.s=findOneServerIdle(eventListAccettazione);
                    sum.get(s).incrementService(service);
                    sum.get(s).incrementServed();
                    eventListAccettazione.get(s).setT(this.time.getCurrent()+service);
                    eventListAccettazione.get(s).setX(1);
                    this.eventHandler.setEventsAccettazione(eventListAccettazione);
                }
            }
            else{
                this.number--;
                this.jobServed++;
                this.s=e;

                //TODO logica di routing

                if(this.number>=SERVERS_ACCETTAZIONE){
                    double service=this.rnd.getService();

                    sum.get(s).incrementService(service);
                    sum.get(s).incrementServed();

                    eventListAccettazione.get(s).setT(this.time.getCurrent()+service);
                    this.eventHandler.setEventsAccettazione(eventListAccettazione);
                }else{
                    eventListAccettazione.get(e).setX(0);
                    this.eventHandler.setEventsAccettazione(eventListAccettazione);
                }
                //TODO gestione inserimento dell'uscita da questo centro in quello successivo
            }
        }
    }

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
            System.out.println(i + "\t" + this.sum.get(i).getService() / this.time.getCurrent() + "\t" + this.sum.get(i).getService() / this.sum.get(i).getServed() + "\t" + this.sum.get(i).getServed() / this.jobServed);
            // System.out.println(i+"\t");
            // System.out.println("get service" + this.sumList[i].getService() + "\n");
            // System.out.println("getCurrent" + this.time.getCurrent() + "\n");
            // System.out.println("getserved"+this.sumList[i].getServed() + "\n");
            // System.out.println("jobServiti"+this.jobServiti + "\n");
            //System.out.println(i + "\t" + sumList[i].getService() / this.time.getCurrent() + "\t" + this.sumList[i].getService() / this.sumList[i].getServed() + "\t" + this.sumList[i].getServed() / this.jobServiti);
            System.out.println("\n");
            //System.out.println("jobServiti"+this.num_job_feedback + "\n");

        }
        System.out.println("\n");
    }

}
