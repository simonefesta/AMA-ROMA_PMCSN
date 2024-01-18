package it.uniroma2.festatosi.ama;

import it.uniroma2.festatosi.ama.controller.ControllerSistema;
import it.uniroma2.festatosi.ama.utils.RandomDistribution;
import it.uniroma2.festatosi.ama.utils.Rngs;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws Exception {



        ControllerSistema sistema=new ControllerSistema();
        //sistema.selectSeed();
        /*  Utilizzo di sistema.simulation(int type)
         *  @ type = 0; simulazione finita, servizi gaussiani.
         *         = 1; simulazione infinita, servizi esponenziali
         *         = 2; simulazione infinita con servizi gaussiani troncati
         */
        sistema.simulation(2);


    }
}