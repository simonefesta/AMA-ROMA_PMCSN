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
    private double arrival=this.start;

    private RandomDistribution(){
        this.intTime=0;
        this.leaveTime=0;
        this.serviceTime=0;
        this.rngs.plantSeeds(123456789);
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
    public double getJobArrival(int queueType) {
        rngs.selectStream(2);
        switch (queueType) {
            case 0: //arrivo allo scarico
                arrival += Exponential(1/(LAMBDA*P1));
                break;
            case 1: //arrivo alla accettazione
                arrival += Exponential(1/(LAMBDA*Q1));
                break;
        }
        return arrival;
    }

    public int getExternalVehicleType() {
        rngs.selectStream(0);
        double rnd=rngs.random(); //si prende numero random
        /*se il numero random scelto è minore di 0.5 o i veicoli del secondo tipo sono tutti presenti nel sistema,
         * entra un veicolo del primo tipo
         */
        //System.out.println("v1 "+eventHandler.getNumberV1()+"v2 "+eventHandler.getNumberV2());
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

    public double getService(){
        rngs.selectStream(3);
        //bisogna inserire un controllo sul tipo di veicolo che esce, per semplicità ora è un solo tipo
        //TODO modificare valori rendere parametrico
        return rvms.idfTruncatedNormal(7200, 3600, 3600, 14400, rngs.random());
    }

    public void decrementVehicle(int vt) throws Exception {
        switch (vt) {
            case 1:
                eventHandler.decrementNumberV1();
                if(eventHandler.getNumberV1()<0){
                    throw new Exception("v1 negativo");
                }
                break;
            case 2:
                eventHandler.decrementNumberV2();
                if(eventHandler.getNumberV2()<0){
                    throw new Exception("v2 negativo");
                }
                break;
            default:
                throw new Exception("Tipo di veicolo non supportato dal sistema");
        }
    }

}