package it.uniroma2.festatosi.ama.model;

import it.uniroma2.festatosi.ama.utils.DataExtractor;
import it.uniroma2.festatosi.ama.utils.Rngs;

import java.io.File;
import java.io.IOException;

// Qui introduciamo le probabilità e costanti varie
public class Constants {


    public static final long SEED = 123456789L;

    public static final long REPLICATIONS = 96; //DEVE ESSERE <=K, in quanto la stampa delle replicazioni si basa su quella dei batch


    // ---- ARRIVAL RATES [req/sec]----

    public static final double LAMBDA = 0.0055; //è circa 60 mezzi in 3 ore, bisogna ragionarci su!


    // ---- SERVICE RATES  [sec] ----
    public static final double accettazione_SR = 10*60;
    public static final double scarico_SR = 10*60;

    public static final double[][] officina_SR = {{3600, 1800, 5400},{5400, 3600, 7200},{5400, 3600, 7200},
            {5400, 3600, 7200},{5400, 3600, 7200}};// {media, lowerBound, upperBound} per i servizi*/


    /*public static final double[][] officina_SR = {{1500, 1800, 3600},{1500, 1800, 3600},{1500, 1800, 3600},
            {1500, 1800, 3600},{1500, 1800, 3600}};// media lowerBound e upperBound per i servizi*/
    public static final double checkout_SR = 20 *60;//20*60;

    public static double STOP_FINITE = 86400; /*  terminazione lavoro giornaliero (24 ore in secondi) */
    public static double STOP_INFINITE= Double.MAX_VALUE;
    /*\*100 si toglie nel momento in cui orizzonte finito*/
    public static double START   = 0.0;
    public static int    SERVERS_SCARICO = 4;
    public static int    SERVERS_ACCETTAZIONE = 4;
    public static int    SERVERS_GOMMISTA = 3;
    public static int    SERVERS_CARROZZERIA = 3;
    public static int    SERVERS_ELETTRAUTO = 1;
    public static int    SERVERS_CARPENTERIA = 3;
    public static int    SERVERS_MECCANICA = 3;
    public static int    SERVERS_CHECKOUT = 1;
    public static int[] SERVERS_OFFICINA = {SERVERS_GOMMISTA, SERVERS_CARROZZERIA, SERVERS_ELETTRAUTO,
            SERVERS_CARPENTERIA, SERVERS_MECCANICA};

    /*indica il numero di code nel sistema, serve per gestire il fatto che i veicoli sono di numero finito*/
    public static int NODES_SISTEMA = 8; //servirà per la event list del sistema per gestire la precedenza degli eventi



  // Entrata diretta per lo scarico dei rifiuti

    public static final double P1 = 0.7;

    // probabilità guasto da identificare
    public static final double Q1 = 0.3; //1-P1

    /*
    Zona AUTOFFICINA
     */

    // probabilità difetto tipologia "gommista"
    public static final double P2 = 0.3;

    // probabilità difetto tipologia "carrozzeria"
    public static final double P3 = 0.1;

    // ---- SKIPP PROB ----
    // probabilità difetto tipologia "elettrauto"
    public static final double P4 = 0.1;

    // probabilità difetto tipologia "carpenteria meccanica"
    public static final double P5 = 0.2;

    // probabilità difetto tipologia "meccanica"
    public static final double P6 = 0.3;


    /*
       Zona CHECKOUT
     */

    // probabilità di uscire dal sistema senza checkout
    public static final double P7 = 0.9;



    // numero di veicoli per ogni tipo
    public static final int VEICOLI1 = 103; //veicoli piccoli #40

    public static final double  BOUNDV1 =  0.6;
    public static final double  BOUNDV2 =  0.6;
    public static final int MAX_VEICOLI1 = 70; //veicoli piccoli nel sistema al massimo
    public static final int VEICOLI2 = 79; //veicoli grandi #59
    public static final int MAX_VEICOLI2 = 32; //veicoli grandi nel sistema al massimo

    public static File datiSistema;
    public static File datiSistemaBatch;

    static {
        try {
            datiSistema = DataExtractor.initializeFile((new Rngs()).getSeed(), "Sistema");
            //datiSistemaBatch = DataExtractor.initializeFile((new Rngs()).getSeed(), "SistemaBatch");
        } catch (IOException e) {
            System.out.println("Problema nell'inizializzazione del file coi dati del Sistema.");
        }
    }


    /*
        Batch Means
     */
    public static final int B=1080; //numero di job nel singolo batch
    public static final int K=96; //numero di batch

    public static final double alpha = 0.05;  //confidenza, tipicamente 0.05

}
