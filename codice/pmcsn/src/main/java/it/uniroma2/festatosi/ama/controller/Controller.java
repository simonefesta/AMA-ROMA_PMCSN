package it.uniroma2.festatosi.ama.controller;


public interface Controller {
    void baseSimulation() throws Exception;

    void infiniteSimulation(int typeOfService) throws Exception;

    int getJobInBatch();

    void printFinalStats();
    void printStats(int replicationIndex) throws Exception;

    void betterBaseSimulation() throws Exception;

    void betterInfiniteSimulation(int typeOfService) throws Exception;
}
