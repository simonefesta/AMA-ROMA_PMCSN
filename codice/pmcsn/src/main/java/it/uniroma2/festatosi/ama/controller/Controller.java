package it.uniroma2.festatosi.ama.controller;

import it.uniroma2.festatosi.ama.model.EventListEntry;

import java.util.List;

public interface Controller {
    void baseSimulation() throws Exception;

    void infiniteSimulation() throws Exception;

    int getJobInBatch();

    void printFinalStats();
    void printStats() throws Exception;
}
