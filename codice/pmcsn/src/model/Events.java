package model;

//Qui definiamo gli eventi caratterizzanti il sistema
public class Events {

    /* -- Sistema completo */
    public static final int EVENT_ARRIVE_SISTEMA = 1;
    public static final int EVENT_DEPARTURE_SISTEMA = 1;

    public static final int ALL_EVENTS_SYSTEM = EVENT_ARRIVE_SISTEMA + EVENT_DEPARTURE_SISTEMA;

    /* -- Accettazione */
    public static final int EVENT_ARRIVE_ACCETTAZIONE = 1;
    public static final int EVENT_DEPARTURE_ACCETTAZIONE = 1;
    public static final int EVENT_BROKEN_ACCETTAZIONE = 1; //mezzo non riparabile.

    public static final int ALL_EVENTS_ACCETTAZIONE = EVENT_ARRIVE_ACCETTAZIONE + EVENT_DEPARTURE_ACCETTAZIONE + EVENT_BROKEN_ACCETTAZIONE;


    /* -- Autofficina */

    public static final int EVENT_ARRIVE_GOMMISTA = 1;
    public static final int EVENT_DEPARTURE_GOMMISTA = 1;

    public static final int ALL_EVENTS_GOMMISTA = EVENT_ARRIVE_GOMMISTA + EVENT_DEPARTURE_GOMMISTA;

    public static final int EVENT_ARRIVE_CARROZZERIA = 1;
    public static final int EVENT_DEPARTURE_CARROZZERIA = 1;

    public static final int ALL_EVENTS_CARROZZERIA = EVENT_ARRIVE_CARROZZERIA + EVENT_DEPARTURE_CARROZZERIA;


    public static final int EVENT_ARRIVE_ELETTRAUTI = 1;
    public static final int EVENT_DEPARTURE_ELETTRAUTI = 1;

    public static final int ALL_EVENTS_ELETTRAUTI = EVENT_ARRIVE_ELETTRAUTI + EVENT_DEPARTURE_ELETTRAUTI;


    public static final int EVENT_ARRIVE_CARPENTERIA = 1;
    public static final int EVENT_DEPARTURE_CARPENTERIA = 1;

    public static final int ALL_EVENTS_CARPENTERIA = EVENT_ARRIVE_CARPENTERIA + EVENT_DEPARTURE_CARPENTERIA;


    public static final int EVENT_ARRIVE_MECCANICA = 1;
    public static final int EVENT_DEPARTURE_MECCANICA = 1;

    public static final int ALL_EVENTS_MECCANICA = EVENT_ARRIVE_MECCANICA + EVENT_DEPARTURE_MECCANICA;



    /* -- Scarico rifiuti */
    public static final int EVENT_ARRIVE_SCARICO = 1;
    public static final int EVENT_DEPARTURE_SCARICO = 1;

    public static final int ALL_EVENTS_SCARICO = EVENT_ARRIVE_SCARICO + EVENT_DEPARTURE_SCARICO;


    /* -- CheckOut */
    public static final int EVENT_ARRIVE_CHECKOUT = 1;
    public static final int EVENT_DEPARTURE_CHECKOUT = 1;

    public static final int ALL_EVENTS_CHECKOUT = EVENT_ARRIVE_CHECKOUT + EVENT_DEPARTURE_CHECKOUT;













}

