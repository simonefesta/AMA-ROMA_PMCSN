package it.uniroma2.festatosi.ama;

import it.uniroma2.festatosi.ama.controller.ControllerAccettazione;
import it.uniroma2.festatosi.ama.controller.ControllerGommista;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("START");

        ControllerAccettazione accettazione = new ControllerAccettazione();
        accettazione.baseSimulation();
        accettazione.printStats();
        ControllerGommista gommista=new ControllerGommista();
        gommista.baseSimulation();
        gommista.printStats();

    }
}