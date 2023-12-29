package it.uniroma2.festatosi.ama.controller;
/* -------------------------------------------------------------------------
 * This program is a next-event simulation of a multi-server, single-queue
 * service node.  The service node is assumed to be initially idle, no
 * arrivals are permitted after the terminal time STOP and the node is then
 * purged by processing any remaining jobs.
 *
 * Name              : Msq.java (Multi-Server Queue)
 * Authors           : Steve Park & Dave Geyer
 * Translated by     : Jun Wang
 * Language          : Java
 * Latest Revision   : 6-16-06
 * -------------------------------------------------------------------------
 */


import it.uniroma2.festatosi.ama.utils.Rngs;

import java.text.DecimalFormat;

import static it.uniroma2.festatosi.ama.model.Constants.*;
import static it.uniroma2.festatosi.ama.model.Events.*;

/*
MsqT simula il flusso temporale, composto da:
- current: tempo corrente
- next: clock del prossimo evento
 */
class MsqT {
    double current;                   /* current time                       */
    double next;                      /* next (most imminent) event time    */
}
/*
MsqSum mantiene, per ogni nodo:
- tempo di processamento totale
- numero di job processati
 */
class MsqSum {                      /* accumulated sums of                */
    double service;                   /*   service times                    */
    long   served;                    /*   number served                    */
}

/*
MsqEvent mantiene il tempo corrente 't', del prossimo evento,
x è un flag che ci dice se tale evento sia attivo(=1, lo processo) o meno (=0).
 */
class MsqEvent{                     /* the next-event list    */
    double t;                         /*   next event time      */
    int    x;                         /*   event status, 0 or 1 */
}


class Msq {


    static double sarrival = START;

    public static void main(String[] args) {
        int streamIndex = 1;
        long number = 0;   /* number nel sistema   */

        long numberAccettazione = 0;  /* number in 'Accettazione'   */
        long numberGommista     = 0;
        long numberCarrozzeria  = 0;
        long numberElettrauti   = 0;
        long numberCarpenteria  = 0;
        long numberMeccanica    = 0;
        long numberScarico      = 0;
        long numberCheckout     = 0;
        int e;                       /* next event index                    */
        int s;                       /* server index                        */

        long indexAccettazione = 0;             /* used to count processed jobs        */
        long indexGommista     = 0;             /* used to count processed jobs        */
        long indexCarrozzeria  = 0;             /* used to count processed jobs        */
        long indexElettrauti   = 0;             /* used to count processed jobs        */
        long indexCarpenteria  = 0;             /* used to count processed jobs        */
        long indexMeccanica    = 0;             /* used to count processed jobs        */
        long indexScarico      = 0;             /* used to count processed jobs        */
        long indexCheckout     = 0;             /* used to count processed jobs        */


        /*
        aree per il calcolo dell'integrale
         */
        double area             = 0.0;
        double areaAccettazione = 0.0;           /* time integrated number in the node */
        double areaGommista     = 0;
        double areaCarrozzeria  = 0;
        double areaElettrauti   = 0;
        double areaCarpenteria  = 0;
        double areaMeccanica    = 0;
        double areaScarico      = 0.0;           /* time integrated number in the node */
        double areaCheckout     = 0.0;

        double areaAccettazioneQueue = 0.0;           /* time integrated number in the node */
        double areaGommistaQueue     = 0;
        double areaCarrozzeriaQueue  = 0;
        double areaElettrautiQueue   = 0;
        double areaCarpenteriaQueue  = 0;
        double areaMeccanicaQueue    = 0;
        double areaScaricoQueue      = 0.0;           /* time integrated number in the node */
        double areaCheckoutQueue     = 0.0;

        double service;

        Msq m = new Msq();
        Rngs r = new Rngs();
        r.plantSeeds(123456789);


        MsqEvent[] event = new MsqEvent[ALL_EVENTS_ACCETTAZIONE + ALL_EVENTS_GOMMISTA + ALL_EVENTS_CARROZZERIA + ALL_EVENTS_ELETTRAUTI + ALL_EVENTS_CARPENTERIA + ALL_EVENTS_MECCANICA + ALL_EVENTS_SCARICO + ALL_EVENTS_CHECKOUT];
        MsqSum[] sum = new MsqSum[ALL_EVENTS_ACCETTAZIONE + ALL_EVENTS_GOMMISTA + ALL_EVENTS_CARROZZERIA + ALL_EVENTS_ELETTRAUTI + ALL_EVENTS_CARPENTERIA + ALL_EVENTS_MECCANICA + ALL_EVENTS_SCARICO + ALL_EVENTS_CHECKOUT];
        for (s = 0; s < ALL_EVENTS_ACCETTAZIONE + ALL_EVENTS_GOMMISTA + ALL_EVENTS_CARROZZERIA + ALL_EVENTS_ELETTRAUTI + ALL_EVENTS_CARPENTERIA + ALL_EVENTS_MECCANICA + ALL_EVENTS_SCARICO + ALL_EVENTS_CHECKOUT; s++) {
            event[s] = new MsqEvent();
            sum[s] = new MsqSum();
        }


        MsqT t = new MsqT();
        t.current = START;


        event[0].t = m.getArrival(r);
        event[0].x = 1;


        for (s = 1; s < ALL_EVENTS_ACCETTAZIONE + ALL_EVENTS_GOMMISTA + ALL_EVENTS_CARROZZERIA + ALL_EVENTS_ELETTRAUTI + ALL_EVENTS_CARPENTERIA + ALL_EVENTS_MECCANICA + ALL_EVENTS_SCARICO + ALL_EVENTS_CHECKOUT; s++) {
            event[s].t = START;          /* this value is arbitrary because */
            event[s].x = 0;              /* all servers are initially idle  */
            sum[s].service = 0.0;
            sum[s].served = 0;
            System.out.print("ciclo s");

        }
        while ((event[0].x != 0) || (number + numberAccettazione + numberGommista + numberCarrozzeria + numberElettrauti + numberCarpenteria + numberMeccanica + numberScarico + numberCheckout != 0)) {
            e = m.nextEvent(event);                /* next event index */
            t.next = event[e].t;                        /* next event time  */
            areaAccettazione += (t.next - t.current) * numberAccettazione;     /* update integral  */
            areaGommista += (t.next - t.current) * numberGommista;
            areaCarrozzeria += (t.next - t.current) * numberCarrozzeria;
            areaElettrauti += (t.next - t.current) * numberElettrauti;
            areaCarpenteria += (t.next - t.current) * numberCarpenteria;
            areaMeccanica += (t.next - t.current) * numberMeccanica;
            areaScarico += (t.next - t.current) * numberScarico;
            areaCheckout += (t.next - t.current) * numberCheckout;     /* update integral  */
            System.out.print("CICLO while esterno!\n");

            t.current = t.next;                            /* advance the clock*/

            if (e == EVENT_ARRIVE_ACCETTAZIONE - 1) {  /* process an arrival in accettazione*/
                System.out.print("IF accettazione!\n");

                numberAccettazione++;
                // genero un evento di tipo "arrivo all'accettazione"
                event[EVENT_ARRIVE_ACCETTAZIONE-1].t = m.getArrival(r);
                if (event[EVENT_ARRIVE_ACCETTAZIONE-1].t > STOP)
                    event[EVENT_ARRIVE_ACCETTAZIONE-1].x = 0;

                if (numberAccettazione <= SERVERS_ACCETTAZIONE) {  //se ho serventi liberi, li sfrutto

                    service = m.getService(r,accettazione_SR);
                    s = m.findOneOfficina(event,SERVERS_ACCETTAZIONE); // cerco un servente idle
                    sum[s].service += service;
                    sum[s].served++;
                    event[s].t = t.current + service;
                    event[s].x = 1; //eleggibile per next event
                }
            } else if (e == ALL_EVENTS_ACCETTAZIONE) {      /* uscita dall'accettazione == entrata in una delle officine) */
                numberAccettazione--; // un job lascia l'accettazione
                indexAccettazione++; // un job è stato quindi processato nell'accettazione

                event[ALL_EVENTS_ACCETTAZIONE].x = 0;

                int indexTypeOfficina = -1; //messo valore per inizializzare il tutto
                boolean prob = false;
                while (!prob) { //qui, tramite probabilità, stabilisco l'officina in cui andrà il mezzo uscente dall'accettazione. Finchè non trova un'officina, continua la ricerca.
                    for (int i = 0; i < Percentuali_OFFICINA.length; i++) {
                        prob = generateProbability(r, streamIndex, Percentuali_OFFICINA[i]);
                        if (prob) {
                            indexTypeOfficina = i;
                            break;
                        }
                    }

                }
                if (indexTypeOfficina == 1) { //caso gommista

                    /* genero (e attivo) l'evento "in coda gommista" */
                    event[ALL_EVENTS_ACCETTAZIONE + EVENT_ARRIVE_GOMMISTA].t = t.current;
                    event[ALL_EVENTS_ACCETTAZIONE + EVENT_ARRIVE_GOMMISTA].x = 1;
                    numberGommista++;
                    if (numberGommista <= SERVERS_GOMMISTA) {   // if false, there's queue
                        service = m.getService(r, riparazione_SR);
                        s = m.findOneOfficina(event, SERVERS_GOMMISTA);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].x = 1;
                    }
                } else if (indexTypeOfficina == 2) { //caso carrozzeria

                    /* genero (e attivo) l'evento "in coda carrozzeria" */
                    event[ALL_EVENTS_ACCETTAZIONE + EVENT_ARRIVE_CARROZZERIA].t = t.current;
                    event[ALL_EVENTS_ACCETTAZIONE + EVENT_ARRIVE_CARROZZERIA].x = 1;

                    numberCarrozzeria++;
                    if (numberCarrozzeria <= SERVERS_GOMMISTA) {   // if false, there's queue
                        service = m.getService(r, riparazione_SR);
                        s = m.findOneOfficina(event, SERVERS_CARROZZERIA);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].x = 1;
                    }
                }
                else if (indexTypeOfficina == 3) { //caso elettrauti

                    /* genero (e attivo) l'evento "in coda elettrauti" */
                    event[ALL_EVENTS_ACCETTAZIONE + EVENT_ARRIVE_ELETTRAUTI].t = t.current;
                    event[ALL_EVENTS_ACCETTAZIONE + EVENT_ARRIVE_ELETTRAUTI].x = 1;

                    numberElettrauti++;
                    if (numberElettrauti <= SERVERS_GOMMISTA) {   // if false, there's queue
                        service = m.getService(r, riparazione_SR);
                        s = m.findOneOfficina(event, SERVERS_ELETTRAUTI);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].x = 1;
                    }
                }

                else if (indexTypeOfficina == 4) { //caso carpenteria

                    /* genero (e attivo) l'evento "in coda carpenteria" */
                    event[ALL_EVENTS_ACCETTAZIONE + EVENT_ARRIVE_CARPENTERIA].t = t.current;
                    event[ALL_EVENTS_ACCETTAZIONE + EVENT_ARRIVE_CARPENTERIA].x = 1;

                    numberCarpenteria++;
                    if (numberCarpenteria <= SERVERS_CARPENTERIA) {   // if false, there's queue
                        service = m.getService(r, riparazione_SR);
                        s = m.findOneOfficina(event, SERVERS_CARPENTERIA);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].x = 1;
                    }
                }
                else if (indexTypeOfficina == 5) { //caso meccanica

                    /* genero (e attivo) l'evento "in coda meccanica" */
                    event[ALL_EVENTS_ACCETTAZIONE + EVENT_ARRIVE_MECCANICA].t = t.current;
                    event[ALL_EVENTS_ACCETTAZIONE + EVENT_ARRIVE_MECCANICA].x = 1;

                    numberMeccanica++;
                    if (numberMeccanica <= SERVERS_MECCANICA) {   // if false, there's queue
                        service = m.getService(r, riparazione_SR);
                        s = m.findOneOfficina(event, SERVERS_MECCANICA);
                        sum[s].service += service;
                        sum[s].served++;
                        event[s].t = t.current + service;
                        event[s].x = 1;
                    }
                }
            }
            else if ((e >= ALL_EVENTS_ACCETTAZIONE + ALL_EVENTS_GOMMISTA) && (e <= ALL_EVENTS_ACCETTAZIONE + ALL_EVENTS_MECCANICA)){
                /* con questo check identifichiamo l'uscita dalle varie officine. Per differenziarle, ogni officina ha un proprio
                valore "ALL_EVENTS_TipoOfficina", che va da 2 nel caso Gommista ( 1 + 1, arrivo + partenza, dove questi '1'
                sono delle etichette, non hanno nulla a che fare con QUANTE uscite ci sono!) e 6 nel caso Meccanica (1+5,
                ossia 1 etichetta per dire che si è entrati in coda, e 5 etichetta per dire che si è stati serviti).
                Ad entrambe sommiamo l'etichetta "ALL_EVENTS_ACCETTAZIONE", perchè per arrivare all'officina di competenza
                dobbiamo prima entrare in coda all'accettazione, e poi uscirne!
                 */

                if (e == ALL_EVENTS_ACCETTAZIONE + ALL_EVENTS_GOMMISTA){
                    numberGommista--;           // esco dal gommista
                    indexGommista++;            //ho processato job dal gommista
                }
                else if (e == ALL_EVENTS_ACCETTAZIONE + ALL_EVENTS_CARROZZERIA){
                    numberCarrozzeria--;        // esco da carrozzeria
                    indexCarrozzeria++;         //ho processato job da carrozzeria
                }
                else if (e == ALL_EVENTS_ACCETTAZIONE + ALL_EVENTS_ELETTRAUTI ){
                    numberElettrauti--;         // esco da elettrauti
                    indexElettrauti++;          //ho processato job da elettrauti
                }
                else if (e == ALL_EVENTS_ACCETTAZIONE + ALL_EVENTS_CARPENTERIA) {
                    numberCarpenteria--;        // esco da carpenteria
                    indexCarpenteria++;         //ho processato job da carpenteria
                }
                else if (e == ALL_EVENTS_ACCETTAZIONE + ALL_EVENTS_MECCANICA){
                    numberMeccanica--;          // esco da meccanica
                    indexMeccanica++;           //ho processato job da meccanica
                }

                //creiamo nuovo evento per indicare che stiamo entrando nella zona scarico
                event[ALL_EVENTS_ACCETTAZIONE + ALL_EVENTS_OFFICINA].t = t.current;
                event[ALL_EVENTS_ACCETTAZIONE + ALL_EVENTS_OFFICINA].x = 1;

                numberScarico++;
                if (numberScarico <= SERVERS_SCARICO) {   // if false, there's queue
                    service = m.getService(r, scarico_SR);
                    s = m.findOneOfficina(event, SERVERS_SCARICO);
                    sum[s].service += service;
                    sum[s].served++;
                    event[s].t = t.current + service;
                    event[s].x = 1;
                }
            }

            System.out.print("\nfine if!");


            DecimalFormat f = new DecimalFormat("###0.00");
            DecimalFormat g = new DecimalFormat("###0.000");

            /* ACCETTAZIONE */

            System.out.println("\nfor " + indexAccettazione + " jobs the service node statistics are:\n");
            System.out.println("  avg interarrivals .. =   " + f.format(event[EVENT_ARRIVE_ACCETTAZIONE - 1].t / indexAccettazione));
            System.out.println("  avg wait (resp time)......... =   " + f.format(areaAccettazione / indexAccettazione));
            System.out.println("  avg # in node ...... =   " + f.format(areaAccettazione / t.current));

            for (s = 1; s <= SERVERS_ACCETTAZIONE; s++)          /* adjust area to calculate */
                area -= sum[s].service;              /* averages for the queue   */

            System.out.println("  avg delay .......... =   " + f.format(areaAccettazione / indexAccettazione));
            System.out.println("  avg # in queue ..... =   " + f.format(areaAccettazione / t.current));
            System.out.println("\nthe server statistics are:\n");
            System.out.println("    server     utilization     avg service      share");
            for (s = 1; s <= SERVERS_ACCETTAZIONE; s++) {
                System.out.print("       " + s + "          " + g.format(sum[s].service / t.current) + "            ");
                System.out.println(f.format(sum[s].service / sum[s].served) + "         " + g.format(sum[s].served / (double) indexAccettazione));
            }

            System.out.println("");



        }
    }

    double exponential(double m, Rngs r) {
        /* ---------------------------------------------------
         * generate an Exponential random variate, use m > 0.0
         * ---------------------------------------------------
         */
        return (-m * Math.log(1.0 - r.random()));
    }

    double uniform(double a, double b, Rngs r) {
        /* --------------------------------------------
         * generate a Uniform random variate, use a < b
         * --------------------------------------------
         */
        return (a + (b - a) * r.random());
    }

    double getArrival(Rngs r) {
        /* --------------------------------------------------------------
         * generate the next arrival time, with rate 1/2
         * --------------------------------------------------------------
         */
        r.selectStream(0);
        sarrival += exponential(2.0, r);
        return (sarrival);
    }


    double getService(Rngs r, double serviceTime) {
        /* ------------------------------
         * generate the next service time, with rate 1/6
         * ------------------------------
         */
        r.selectStream(1);
        return (exponential(serviceTime, r));
    }

    private int nextEvent(MsqEvent [] event) {
        /* ---------------------------------------
         * return the index of the next event type
         * ---------------------------------------
         */
        int e;
        int i = 0;

        while (event[i].x == 0)       /* find the index of the first 'active' */
            i++;                        /* element in the event list            */
        e = i;
        while (i < ALL_EVENTS_ACCETTAZIONE+ALL_EVENTS_GOMMISTA + ALL_EVENTS_CARROZZERIA + ALL_EVENTS_ELETTRAUTI + ALL_EVENTS_CARPENTERIA + ALL_EVENTS_MECCANICA + ALL_EVENTS_SCARICO + ALL_EVENTS_CHECKOUT -1) {         /* now, check the others to find which  */
            i++;                        /* event type is most imminent          */
            if ((event[i].x == 1) && (event[i].t < event[e].t))
                e = i;
        }
        return (e);
    }

    int findOneOfficina(MsqEvent [] event, double nServer) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;
        int i = 1;

        while (event[i].x == 1)       /* find the index of the first available */
            i++;                        /* (idle) server                         */
        s = i;
        while (i < nServer+1) {         /* now, check the others to find which   */
            i++;                        /* has been idle longest                 */
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }


    static public boolean generateProbability(Rngs rngs, int streamIndex, double percentage) {
        rngs.selectStream(1 + streamIndex);
        return rngs.random() <= percentage;
    }




}

