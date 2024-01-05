package it.uniroma2.festatosi.ama;

<<<<<<< Updated upstream
import it.uniroma2.festatosi.ama.controller.ControllerAccettazione;
import it.uniroma2.festatosi.ama.controller.ControllerGommista;
=======
import it.uniroma2.festatosi.ama.controller.ControllerSistema;
import it.uniroma2.festatosi.ama.utils.DataExtractor;
import it.uniroma2.festatosi.ama.utils.Rngs;
>>>>>>> Stashed changes

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
<<<<<<< Updated upstream
=======
        ControllerScarico scarico=new ControllerScarico();
        scarico.baseSimulation();
        scarico.printStats();*/


        Rngs rngs = new Rngs();
        rngs.plantSeeds(123456789);
        DataExtractor.initializeFile();
        ControllerSistema sistema=new ControllerSistema();
        //sistema.setSeed(rngs.getSeed());
        //sistema.setSeed(rngs.getSeed());
        sistema.baseSimulation();

>>>>>>> Stashed changes

    }
}