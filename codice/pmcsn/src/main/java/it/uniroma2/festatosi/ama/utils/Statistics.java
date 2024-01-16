package it.uniroma2.festatosi.ama.utils;

import static it.uniroma2.festatosi.ama.model.Constants.K;

public class Statistics {

    private double meanDelay; // attesa media in coda, ovvero E[Tq]
    private double meanUtilization;  // utilizzo rho = lambda/mu = lambda * E[S]

    private double popMediaCoda;

    private double meanWait;  // attesa media nel centro, ovvero E[Ts] = E[Tq] + E[S_i]

    private double devEtq; //dev std per Etq
    private double devEnq; //dev std per Enq
    private double devRho;
    private double[] batchMedia;

    private double[] batchPopolazioneCoda;
    private double[] batchUtilizzazione;

    private static Statistics instance;
    // Costruttore privato per evitare inizializzazione diretta
    private Statistics() {
        meanDelay = 0;
        meanUtilization = 0;
        batchMedia = new double[K + 1];
        batchPopolazioneCoda = new double[K+1];
        batchUtilizzazione = new double[K+1];
    }


    // Metodo per ottenere l'istanza del singleton
    public static Statistics getInstance() {
        if (instance == null) {
            instance = new Statistics();
        }
        return instance;
    }

    /*
      Set e Get di E[Tq] del singolo batch
     */
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

    public double getDevStd(int type) {
        if (type == 0)  return devEtq;
        else if ( type == 1) return devEnq;
        else if (type == 2) return devRho;
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
        media /= K/*batchMedia.length*/;
        if (type == 0) setMeanDelay(media);     // type 0: media E[Tq]
        else if (type == 1) setPopMediaCoda(media); //type 1: media E[Nq]
        else if (type == 2) setMeanUtilization(media);

        // Calcola la somma dei quadrati delle differenze dalla media
        double sommaQuadratiDifferenze = 0.0;
        for (double elemento : batchMedia) {
            double differenza = elemento - media;
            sommaQuadratiDifferenze += differenza * differenza;
        }

        // Calcola la varianza

        double devStd = Math.sqrt(sommaQuadratiDifferenze / K)/*batchMedia.length*/;

        if (type == 0) devEtq = devStd;
        else if (type == 1) devEnq = devStd;
        else if (type == 2) devRho = devStd;

    }


    /*
            Set e Get dei singoli E[Tq] nel vettore di batch
     */
    public void setBatchMeanDelayArray( double media, int index){
        batchMedia[index] = media;
    }

    public double[] getBatchMeanDelayArray(){
        return batchMedia;
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

    public void setMeanUtilizationBatch(double meanUtilization) {
        this.meanUtilization = meanUtilization;
    }

    public double[] getBatchUtilizzazione() {
        return batchUtilizzazione;
    }

    public void setBatchUtilizzazione(double utilizzazione, int index) {
        this.batchUtilizzazione[index] = utilizzazione;
    }
}
