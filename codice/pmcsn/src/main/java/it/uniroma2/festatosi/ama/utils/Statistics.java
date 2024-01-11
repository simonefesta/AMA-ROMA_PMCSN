package it.uniroma2.festatosi.ama.utils;

public class Statistics {

    private double meanDelay;
    private double meanUtilization;

    public Statistics(){
        meanDelay = 0;
        meanUtilization = 0;
    }


    public double getMeanDelay() {
        return meanDelay;
    }

    public void setMeanDelay(double meanDelay) {
        this.meanDelay = meanDelay;
    }

    public double getMeanUtilization() {
        return meanUtilization;
    }

    public void setMeanUtilization(double meanUtilization) {
        this.meanUtilization = meanUtilization;
    }


}
