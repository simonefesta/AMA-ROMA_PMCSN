package it.uniroma2.festatosi.ama.model;

/**classe che accumula le somme*/
public class MsqSum {
    private double service;         /*tempo di servizio*/
    private long served;            /*numero di servizi*/

    public MsqSum(){
        this.service=0;
        this.served=0;
    }

    public double getService() {
        return service;
    }

    public void setService(double service) {
        this.service = service;
    }

    public long getServed() {
        return served;
    }

    public void setServed(long served) {
        this.served = served;
    }

    public void incrementService(double service) {
        this.service+=service;
    }

    public void incrementServed() {
        this.served++;
    }
}
