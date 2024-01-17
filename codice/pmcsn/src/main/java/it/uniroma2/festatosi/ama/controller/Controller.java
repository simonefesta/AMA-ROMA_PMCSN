package it.uniroma2.festatosi.ama.controller;


public interface Controller {
    void baseSimulation() throws Exception;

    void infiniteSimulation(int typeOfService) throws Exception;

    int getJobInBatch();

    void printFinalStats();
    void printStats() throws Exception;
}
