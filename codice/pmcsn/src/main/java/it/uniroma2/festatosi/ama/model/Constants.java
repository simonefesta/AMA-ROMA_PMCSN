package it.uniroma2.festatosi.ama.model;

// Qui introduciamo le probabilità e costanti varie
public class Constants {

    public static double START   = 0.0;            /* inizio lavoro giornaliero       */
    public static double STOP    = 86400.0;        /*  terminazione lavoro giornaliero (24 ore in secondi) */
    public static int    SERVERS_SCARICO = 5;
    public static int    SERVERS_ACCETTAZIONE = 4;
    public static int    SERVERS_GOMMISTA = 2;
    public static int    SERVERS_CARROZZERIA = 3;
    public static int    SERVERS_ELETTRAUTO = 1;
    public static int    SERVERS_CARPENTERIA = 6;
    public static int    SERVERS_MECCANICA = 5;
    public static int    SERVERS_CHECKOUT = 1;
    public static int    SERVERS_OFFICINA[] = {SERVERS_GOMMISTA, SERVERS_CARROZZERIA, SERVERS_ELETTRAUTO,
            SERVERS_CARPENTERIA, SERVERS_MECCANICA};

    /*indica il numero di code nel sistema, serve per gestire il fatto che i veicoli sono di numero finito*/
    public static int NODES_SISTEMA = 8; //servirà per la event list del sistema per gestire la precedenza degli eventi



  // Entrata diretta per lo scarico dei rifiuti

    public static final double P1 = 0.64;

    // probabilità guasto da identificare
    public static final double Q1 = 0.36; //1-P1

    /*
    Zona AUTOFFICINA
     */

    // probabilità mezzo non riparabile
    public static final double Q2 = 0.02;


    // probabilità difetto tipologia "gommista"
    public static final double P2 = 0.3;

    // probabilità difetto tipologia "carrozzeria"
    public static final double P3 = 0.08;

    // ---- SKIPP PROB ----
    // probabilità difetto tipologia "elettrauti"
    public static final double P4 = 0.1;

    // probabilità difetto tipologia "carpenteria meccanica"
    public static final double P5 = 0.2;

    // probabilità difetto tipologia "meccanica"
    public static final double P6 = 0.3;

    public static final double [] Percentuali_OFFICINA = {Q2,  P2,  P3,  P4,  P5,  P6};


    /*
       Zona CHECKOUT
     */

    // probabilità di uscire dal sistema senza checkout
    public static final double P7 = 0.7;

    // probabilità di uscire dal sistema previo checkout
    public static final double Q7 = 0.3;


    // ---- ARRIVAL RATES [req/sec]----

    public static final double LAMBDA = 0.005; //è circa 60 mezzi in 3 ore, bisogna ragionarci su!


    // ---- SERVICE RATES  [sec] ----
    public static final double accettazione_SR = 10*60;
    public static final double scarico_SR = 15*60;
    public static final double officina_SR = 2*3600; //per tutte le officine (2 ore?)
    public static final double checkout_SR = 20*60;

    // numero di veicoli per ogni tipo
    public static final int VEICOLI1 =40; //veicoli piccoli
    public static final int VEICOLI2 =59; //veicoli grandi

}
