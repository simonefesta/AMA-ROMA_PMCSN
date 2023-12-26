package model;

// Qui introduciamo le probabilità e costanti varie
public class Constants {

    public static final double P1 = 0.64;

    // probabilità guasto da identificate
    public static final double Q1 = 0.36; //1-P1

    // probabilità mezzo non riparabile
    public static final double Q2 = 0.02;

    // probabilità difetto tipologia "gommista"
    public static final double P2 = 0.02;

    // probabilità difetto tipologia "carrozzeria"
    public static final double P3 = 0.02;

    // ---- SKIPP PROB ----
    // probabilità difetto tipologia "elettrauti"
    public static final double P4 = 0.3;

    // probabilità difetto tipologia "carpenteria meccanica"
    public static final double P5 = 0.3;

    // probabilità difetto tipologia "meccanica"
    public static final double P6 = 0.3;

    // probabilità di uscire dal sistema senza checkout
    public static final double P7 = 0.3;

    // probabilità di uscire dal sistema previo checkout
    public static final double Q7 = 0.3;


    // ---- ARRIVAL RATES [req/min]----

    // first time window rate
    public static final double LAMBDA = 1; //TODO


    // ---- SERVICE RATES  [min] ----
    // smaltimento
    public static final double smaltimento_SR = 15;
    public static final double riparazione_SR = 2*60; //per tutte le officine (2 ore?)




}
