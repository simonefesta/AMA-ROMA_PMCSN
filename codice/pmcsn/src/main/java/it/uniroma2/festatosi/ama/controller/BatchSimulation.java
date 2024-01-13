package it.uniroma2.festatosi.ama.controller;

public class BatchSimulation {

    private static double nBatch=1.0;
    private static int nJobInBatch=0; //numero totale di jobInBatch nel sistema


    public static void incrementNBatch(){
        nBatch++;
    }

    public static void incrementJobInBatch(){
        nJobInBatch++;
    }

    public static int getJobInBatch() {
        return nJobInBatch;
    }

    public static double getNBatch() {
        return nBatch;
    }


}
