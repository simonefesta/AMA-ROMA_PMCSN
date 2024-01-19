package it.uniroma2.festatosi.ama.utils;

import java.io.File;
import java.io.IOException;

import static it.uniroma2.festatosi.ama.model.Constants.SEED;



public  class ReplicationHelper {

    public static File replicationAccettazione;
    public static File replicationCarpenteria;
    public static File replicationCarrozzeria;
    public static File replicationCheckout;
    public static File replicationScarico;
    public static File replicationElettrauto;
    public static File replicationGommista;
    public static File replicationMeccanica;


    public static void initializeReplicationsFile() throws IOException {

       replicationAccettazione = DataExtractor.initializeFileReplication(SEED,"ControllerAccettazione");
       replicationCheckout  = DataExtractor.initializeFileReplication(SEED,"ControllerCheckout");
       replicationScarico  = DataExtractor.initializeFileReplication(SEED,"ControllerScarico");

       replicationCarpenteria =  DataExtractor.initializeFileReplication(SEED,"Carpenteria");
       replicationCarrozzeria  = DataExtractor.initializeFileReplication(SEED,"Carrozzeria");
       replicationElettrauto  = DataExtractor.initializeFileReplication(SEED,"Elettrauto");
       replicationGommista  = DataExtractor.initializeFileReplication(SEED,"Gommista");
       replicationMeccanica  =  DataExtractor.initializeFileReplication(SEED,"Meccanica");
    }


    public static File getReplicationFile(String nomeOfficina) {
            if ("Gommista".equals(nomeOfficina)) return replicationGommista;
            else if ("Carrozzeria".equals(nomeOfficina)) return replicationCarrozzeria;
            else if ("Elettrauto".equals(nomeOfficina)) return replicationElettrauto;
            else if ("Carpenteria".equals(nomeOfficina)) return replicationCarpenteria;
            else if ("Meccanica".equals(nomeOfficina)) return replicationMeccanica;

            else {
                System.out.println("Errore nel riconoscimento dell'officina");
                   return null;
            }

        }


}
