package it.uniroma2.festatosi.ama.utils;

import static java.lang.Math.log;

public class RandomDistribution {
    //tempo di interarrivo al centro
    private double intTime;
    //tempo di servizio centro
    private double serviceTime;
    //tempo di abbandono centro
    private double leaveTime;

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

    public double getJobArrival() {
        rngs.selectStream(0);
        arrival += Exponential(1.0);
        return arrival;
    }

    public double getService() {
        rngs.selectStream(3);
        return Uniform(2.0, 10.0);
    }

}
