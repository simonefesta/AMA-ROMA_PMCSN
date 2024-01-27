package it.uniroma2.festatosi.ama;

import it.uniroma2.festatosi.ama.controller.ControllerSistema;
import it.uniroma2.festatosi.ama.controller.EventHandler;
import it.uniroma2.festatosi.ama.model.Constants;
import it.uniroma2.festatosi.ama.utils.ReplicationHelper;
import it.uniroma2.festatosi.ama.utils.Rngs;

import static it.uniroma2.festatosi.ama.model.Constants.*;
import static it.uniroma2.festatosi.ama.utils.ReplicationHelper.initializeReplicationsFile;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {



    public static void main(String[] args) throws Exception {
        Rngs rngs = new Rngs();
        rngs.plantSeeds(SEED);
        runSimulation(5);
    }

    /**
     *  @param simulationType   = 0; simulazione finita, servizi gaussiani troncati.
     *                          = 1; simulazione infinita, servizi esponenziali.
     *                          = 2; simulazione infinita con servizi gaussiani troncati.
     *                          = 3; simulazione infinita, servizi esponenziali, analisi bottleneck.
     *                          = 4; simulazione finita, modello migliorativo, servizi gaussiani troncati.
     *                          = 5; simulazione infinita, modello migliorativo, servizi esponenziali.
     *                          = 6; simulazione infinita, modello migliorativo, servizi gaussiani troncati.
     *                          = 7; simulazione infinita, servizi esponenziali, analisi bottleneck.
     */
    public static void runSimulation(int simulationType) throws Exception {
        if (simulationType == 0 || simulationType == 4) {

            initializeReplicationsFile(); //inizializza file per memorizzazione delle statistiche

            for (int i = 0; i < REPLICATIONS; i++) {
                ControllerSistema sistema = new ControllerSistema();
                sistema.simulation(simulationType, i);
            }
            ReplicationHelper.printFinalStatistics();
        } else if(simulationType == 3 || simulationType == 7){
            bottleneckAnalysis();
            ControllerSistema sistema = new ControllerSistema();
            sistema.simulation(simulationType, 0);
        }
        else{
            ControllerSistema sistema = new ControllerSistema();
            sistema.simulation(simulationType, 0);
        }
        System.out.println("Numero di volte in cui è stato superato il limite di veicoli nel sistema: "+ EventHandler.getInstance().getSuperatoMax());
        System.out.println("Per i veicoli di tipo 1: "+ EventHandler.getInstance().getSuperatoMaxVeicoli1() + " e per i veicoli di tipo 2:  " + EventHandler.getInstance().getSuperatoMaxVeicoli2());
    }

}
