package it.uniroma2.festatosi.ama;

import it.uniroma2.festatosi.ama.controller.ControllerSistema;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws Exception {

        long seed = 123456789;

        ControllerSistema sistema=new ControllerSistema(seed);
        sistema.selectSeed(seed);
        /*  Utilizzo di sistema.simulation(int type)
         *  @ type = 0; simulazione finita, servizi gaussiani.
         *         = 1; simulazione infinita, servizi esponenziali
         *         = 2; simulazione infinita con servizi gaussiani troncati
         */
        sistema.simulation(2);


    }
}