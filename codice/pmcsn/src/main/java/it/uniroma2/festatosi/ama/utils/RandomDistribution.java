package it.uniroma2.festatosi.ama.utils;

import it.uniroma2.festatosi.ama.controller.EventHandler;

import static it.uniroma2.festatosi.ama.model.Constants.*;
import static java.lang.Math.log;

public class RandomDistribution {
    //tempo di interarrivo al centro
    private double intTime;
    //tempo di servizio centro
    private double serviceTime;
    //tempo di abbandono centro
    private double leaveTime;

    private EventHandler eventHandler=EventHandler.getInstance();

    private static RandomDistribution instance=null;

    private Rngs rngs=new Rngs();
    private Rvms rvms=new Rvms();

    private final double start=0;
    private double arrivalScarico=this.start;
    private double arrivalAccettazione=this.start;

    private RandomDistribution(){
        this.intTime=0;
        this.leaveTime=0;
        this.serviceTime=0;
        this.rngs.plantSeeds(SEED);
    }

    public static RandomDistribution getInstance(){
        if(instance==null){
            instance=new RandomDistribution();
        }
        return instance;
    }

    private RandomDistribution(double intTime, double leaveTime, double serviceTime) {
        this.intTime = intTime;
        this.serviceTime = serviceTime;
        this.leaveTime = leaveTime;
    }

    public double Exponential(double mu) {
        return (-mu * log(1.0 - rngs.random()));
    }
    public double Uniform(double a, double b) {
        return (a + (b - a) * rngs.random());
    }

    /**
     * Funzione che torna il tempo di arrivo di un evento nel sistema
     * Torna il double massimo nel caso in cui non possono arrivare nuovi eventi dall'esterno in modo che venga
     * selezionato l'evento di completamento di un servente
     * @param queueType indica la coda a cui va l'arrivo (0 scarico, 1 accettazione)
     * @return ritorna il tempo di arrivo dell'evento
     */
    public double getJobArrival(int queueType) throws Exception {
        rngs.selectStream(2);
        eventHandler.incrementArr();
        switch (queueType) {
            case 0: //arrivo allo scarico
                arrivalScarico = Exponential(1.0/(LAMBDA*P1));
                return arrivalScarico;
            case 1: //arrivo alla accettazione
                arrivalAccettazione = Exponential(1.0/(LAMBDA*Q1));
                return arrivalAccettazione;
            default:
                throw new Exception("Arrivo non definito");
        }
    }

    public int getExternalVehicleType() {
        rngs.selectStream(0);
        double rnd=rngs.random(); //si prende numero random
        /*se il numero random scelto è minore di 0.5 o i veicoli del secondo tipo sono tutti presenti nel sistema,
         * entra un veicolo del primo tipo
         */
        if((rnd<=(double)VEICOLI1/(VEICOLI1+VEICOLI2) && eventHandler.getNumberV1()< VEICOLI1)
                || (eventHandler.getNumberV2()==VEICOLI2 && eventHandler.getNumberV1()< VEICOLI1)){
            eventHandler.incrementNumberV1();
            return 1;
        }
        /*se il numero random scelto è maggiore di 0.5 o i veicoli del primo tipo sono tutti presenti nel sistema,
         * entra un veicolo del secondo tipo
         */
        else if((rnd>(double)VEICOLI1/(VEICOLI1+VEICOLI2) && eventHandler.getNumberV2()< VEICOLI2)
                || (eventHandler.getNumberV1()==VEICOLI1 && eventHandler.getNumberV2()< VEICOLI2)){
            eventHandler.incrementNumberV2();
            return 2;
        }else{
            //se sono finiti i veicoli all'esterno del sistema non si possono avere nuovi arrivi
            return Integer.MAX_VALUE;
        }
    }

    /**
     @param typeOfService indica se il servizio è di tipo:
     0 - accettazione,
     1 - officina (da gommista a meccanica),
     2 - scarico
     3 - check-out.

     **/
    public double getService(int typeOfService) throws Exception {
        rngs.selectStream(3);

        switch (typeOfService) {
            case 0:  //accettazione
                return rvms.idfTruncatedNormal(accettazione_SR, 100, 300, 900, rngs.random());

            case 1: //scarico
                return rvms.idfTruncatedNormal(scarico_SR, 450, 480, 900, rngs.random());
            case 2: //checkout
                return rvms.idfTruncatedNormal(checkout_SR, 450, 600, 1200, rngs.random());
            case 3:  //gommista
            case 4:  //carrozzeria
            case 5:  //elettrauto
            case 6:  //carpenteria
            case 7:  //meccanica
                return rvms.idfTruncatedNormal(officina_SR[typeOfService-3][0],officina_SR[typeOfService-3][1],officina_SR[typeOfService-3][1],officina_SR[typeOfService-3][2], rngs.random());
            default:
                throw new Exception("Tipo di servizio non supportato dal sistema");

        }
    }

    public double getServiceBatch(int typeOfService) throws Exception {
        rngs.selectStream(3);

        switch (typeOfService) {
            case 0:  //accettazione
                return Exponential(accettazione_SR);
            case 1: //scarico
                return Exponential(scarico_SR);
            case 2: //checkout
                return Exponential(checkout_SR);
            case 3:  //gommista
            case 4:  //carrozzeria
            case 5:  //elettrauto
            case 6:  //carpenteria
            case 7:  //meccanica
                return Exponential(officina_SR[typeOfService-3][0]);
            default:
                throw new Exception("Tipo di servizio non supportato dal sistema");
        }
    }



}

