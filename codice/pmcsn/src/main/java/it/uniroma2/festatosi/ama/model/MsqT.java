package it.uniroma2.festatosi.ama.model;

public class MsqT {
    private double current;     /*tempo corrente*/
    private double next;        /*tempo dell'evento più imminente*/
    private double batch;

    public MsqT(){
        this.current=0;
        this.next=0;
        this.batch=0;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

    public double getNext() {
        return next;
    }

    public void setNext(double next) {
        this.next = next;
    }

    public double getBatch() {
        return batch;
    }

    public void setBatch(double batch) {
        this.batch = batch;
    }
}
