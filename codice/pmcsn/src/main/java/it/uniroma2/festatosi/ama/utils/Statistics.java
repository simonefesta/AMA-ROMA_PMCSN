package it.uniroma2.festatosi.ama.utils;

import static it.uniroma2.festatosi.ama.model.Constants.K;

public class Statistics {

    private double meanDelay; // attesa media in coda, ovvero E[Tq]
    private double meanWait;  // attesa media nel centro, ovvero E[Ts] = E[Tq] + E[S_i]

    private double meanUtilization;  // utilizzo rho = lambda/mu = lambda * E[S]

    private double popMediaCoda;  //Enq
    private double popMediaSistema; //Ens



    private double devEtq; //dev std per Etq
    private double devEts; //dev std per Ets

    private double devEnq; //dev std per Enq
    private double devEns; //dev std per Enq
    private double devRho; //dev std per Rho

    private final double[] batchPopolazioneCoda;
    private final double[] batchTempoCoda;
    private final double[] batchPopolazioneSistema;
    private final double[] batchTempoSistema;
    private final double[] batchUtilizzazione;


    // Costruttore privato per evitare inizializzazione diretta
    public Statistics() {
        meanDelay = 0;
        meanUtilization = 0;
        batchUtilizzazione = new double[K+1];
        batchPopolazioneCoda = new double[K+1];
        batchTempoCoda = new double[K+1];
        batchPopolazioneSistema = new double[K+1];
        batchTempoSistema = new double[K+1];
    }


    public double getMeanDelay() { //ritorna E[Tq] del singolo batch
        return meanDelay;
    }

    public void setMeanDelay(double meanDelay) { //tempo in coda E[Tq] del singolo batch
        this.meanDelay = meanDelay;
    }

    /*
        Set e Get della singola utilizzazione nel batch
     */
    public double getMeanUtilization() {
        return meanUtilization;
    }

    public void setMeanUtilization(double meanUtilization) {
        this.meanUtilization = meanUtilization;
    }

    /*
        Set di E[Ts] del singolo batch
     */
    public void setMeanWait(double meanWait) {
        this.meanWait = meanWait;
    }

    public double getMeanWait() {
        return this.meanWait;
    }

    public double getDevStd(int type) {
        if (type == 0)  return devEtq;
        else if ( type == 1) return devEnq;
        else if (type == 2) return devRho;
        else if (type == 3) return devEts;
        else if (type == 4) return devEns;
        else return -1;
    }

    /*
        Varianza calcolata prendendo in ingresso tutte le medie campionarie nel vettore
     */
    public void setDevStd(double[] batchMedia, int type) {  // type 0: Etq, type 1 : Enq, type 2 = rho

        if (batchMedia.length == 0) {
            System.out.println("Il vettore è vuoto, impossibile calcolare la varianza.");
            return;
        }

        double media = 0.0;
        for (double elemento : batchMedia) {
            media += elemento;
        }
        media /= K;
        if (type == 0) setMeanDelay(media);     // type 0: media E[Tq]
        else if (type == 1) setPopMediaCoda(media); //type 1: media E[Nq]
        else if (type == 2) setMeanUtilization(media);
        else if (type == 3) setMeanWait(media);   //Ets
        else if (type == 4) setPopMediaSistema(media);

        // Calcola la somma dei quadrati delle differenze dalla media
        double sommaQuadratiDifferenze = 0.0;
        for (double elemento : batchMedia) {
            double differenza = elemento - media;
            sommaQuadratiDifferenze += differenza * differenza;
        }

        // Calcola la varianza

        double devStd = Math.sqrt(sommaQuadratiDifferenze / K);

        if (type == 0) devEtq = devStd;
        else if (type == 1) devEnq = devStd;
        else if (type == 2) devRho = devStd;
        else if (type == 3) devEts = devStd;
        else if (type == 4) devEns = devStd;

    }

    private void setPopMediaSistema(double media) {
        this.popMediaSistema = media;

    }

    public double getPopMediaSistema() {
       return this.popMediaSistema;
    }


    /*
           Set e Get dei singoli E[Nq] nel vettore di batch
    */
    public double[] getBatchPopolazioneCodaArray() {
        return batchPopolazioneCoda;
    }

    public void setBatchPopolazioneCodaArray(double batchPopolazioneCoda, int index) {
        this.batchPopolazioneCoda[index] = batchPopolazioneCoda;
    }

    public double getPopMediaCoda() {
        return popMediaCoda;
    }

    public void setPopMediaCoda(double popMediaCoda) {
        this.popMediaCoda = popMediaCoda;
    }

 
    public double[] getBatchUtilizzazione() {
        return batchUtilizzazione;
    }

    public void setBatchUtilizzazione(double utilizzazione, int index) {
        this.batchUtilizzazione[index] = utilizzazione;
    }

    public double[] getBatchPopolazioneSistema() {
        return this.batchPopolazioneSistema;
    }

    public void setBatchPopolazioneSistema(double batchPopSistema, int index) {
        this.batchPopolazioneSistema[index] = batchPopSistema;
    }

    public double[] getBatchTempoCoda() {
        return batchTempoCoda;
    }

    public void setBatchTempoCoda( double tempoCoda, int index) {
        this.batchTempoCoda[index] = tempoCoda;
    }

    public double[] getBatchTempoSistema() {
        return batchTempoSistema;
    }

    public void setBatchTempoSistema(double tempoSistema, int index) {
        this.batchTempoSistema[index] = tempoSistema;
    }


}
