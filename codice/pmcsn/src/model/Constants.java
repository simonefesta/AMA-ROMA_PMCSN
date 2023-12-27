package model;

// Qui introduciamo le probabilità e costanti varie
public class Constants {

    public static double START   = 0.0;            /* inizio lavoro giornaliero       */
    public static double STOP    = 1440;        /*  terminazione lavoro giornaliero (24 ore in minuti) */
    public static int    SERVERS_SCARICO = 5;
    public static int    SERVERS_ACCETTAZIONE = 4;
    public static int    SERVERS_GOMMISTA = 2;
    public static int    SERVERS_CARROZZERIA = 3;
    public static int    SERVERS_ELETTRAUTI = 1;
    public static int    SERVERS_CARPENTERIA = 6;
    public static int    SERVERS_MECCANICA = 5;
    public static int    SERVERS_CHECKOUT = 1;



  // Entrata diretta per lo scarico dei rifiuti

    public static final double P1 = 0.64;

    // probabilità guasto da identificate
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

    /*
       Zona CHECKOUT
     */

    // probabilità di uscire dal sistema senza checkout
    public static final double P7 = 0.7;

    // probabilità di uscire dal sistema previo checkout
    public static final double Q7 = 0.3;


    // ---- ARRIVAL RATES [req/min]----

    public static final double LAMBDA = 0.11; // 135 mezzi in 1440 minuti. (?) DA VEDERE


    // ---- SERVICE RATES  [min] ----
    // smaltimento
    public static final double smaltimento_SR = 15;
    public static final double riparazione_SR = 2*60; //per tutte le officine (2 ore?)




}
