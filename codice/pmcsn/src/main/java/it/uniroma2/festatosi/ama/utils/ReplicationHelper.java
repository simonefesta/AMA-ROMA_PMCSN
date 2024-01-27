package it.uniroma2.festatosi.ama.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.uniroma2.festatosi.ama.model.Constants.*;
import static it.uniroma2.festatosi.ama.model.Constants.K;


public  class ReplicationHelper {

    public static File replicationAccettazione;
    public static File replicationCarpenteria;
    public static File replicationCarrozzeria;
    public static File replicationCheckout;
    public static File replicationScarico;
    public static File replicationElettrauto;
    public static File replicationGommista;
    public static File replicationMeccanica;
    public static Statistics replicationStatisticsAccettazione=new Statistics();
    public static Statistics replicationStatisticsCarpenteria=new Statistics();
    public static Statistics replicationStatisticsCarrozzeria=new Statistics();
    public static Statistics replicationStatisticsCheckout=new Statistics();
    public static Statistics replicationStatisticsScarico=new Statistics();
    public static Statistics replicationStatisticsElettrauto=new Statistics();
    public static Statistics replicationStatisticsGommista=new Statistics();
    public static Statistics replicationStatisticsMeccanica=new Statistics();
    public static List<Statistics> statistics;
    public static List<String> className;
    
    
    public static void initializeReplicationsFile() throws IOException {

       replicationAccettazione = DataExtractor.initializeFileReplication(SEED,"ControllerAccettazione");
       replicationCheckout  = DataExtractor.initializeFileReplication(SEED,"ControllerCheckout");
       replicationScarico  = DataExtractor.initializeFileReplication(SEED,"ControllerScarico");

       replicationCarpenteria =  DataExtractor.initializeFileReplication(SEED,"Carpenteria");
       replicationCarrozzeria  = DataExtractor.initializeFileReplication(SEED,"Carrozzeria");
       replicationElettrauto  = DataExtractor.initializeFileReplication(SEED,"Elettrauto");
       replicationGommista  = DataExtractor.initializeFileReplication(SEED,"Gommista");
       replicationMeccanica  =  DataExtractor.initializeFileReplication(SEED,"Meccanica");
       
       statistics= Arrays.asList(new Statistics[]{replicationStatisticsAccettazione,
               replicationStatisticsCarpenteria, replicationStatisticsCarrozzeria, replicationStatisticsCheckout,
               replicationStatisticsScarico, replicationStatisticsElettrauto, replicationStatisticsGommista,
               replicationStatisticsMeccanica});
       className= Arrays.asList(new String[]{"Accettazione", "Carpenteria", "Carrozzeria", "Checkout",
               "Scarico", "Elettrauto", "Gommista", "Meccanica"});
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
    
    public static Statistics getReplicationStatistics(String nomeOfficina) {
            if ("Gommista".equals(nomeOfficina)) return replicationStatisticsGommista;
            else if ("Carrozzeria".equals(nomeOfficina)) return replicationStatisticsCarrozzeria;
            else if ("Elettrauto".equals(nomeOfficina)) return replicationStatisticsElettrauto;
            else if ("Carpenteria".equals(nomeOfficina)) return replicationStatisticsCarpenteria;
            else if ("Meccanica".equals(nomeOfficina)) return replicationStatisticsMeccanica;

            else {
                System.out.println("Errore nel riconoscimento dell'officina");
                   return null;
            }

    }
    
    public static void printFinalStatistics(){
        for(int i=0; i<statistics.size();i++) {
            Statistics stats= statistics.get(i);
            Rvms rvms = new Rvms();
            double criticalValue = rvms.idfStudent(K - 1, 1 - alpha / 2);
            System.out.println(className.get(i));
            System.out.print("Statistiche per E[Tq] ");
            stats.setDevStd(stats.getBatchTempoCoda(), 0);     // calcolo la devstd per Etq
            System.out.println("Critical endpoints " + stats.getMeanDelay() + " +/- " + criticalValue * stats.getDevStd(0) / (Math.sqrt(K - 1)));
            System.out.print("statistiche per E[Nq] ");
            stats.setDevStd(stats.getBatchPopolazioneCodaArray(), 1);     // calcolo la devstd per Enq
            System.out.println("Critical endpoints " + stats.getPopMediaCoda() + " +/- " + criticalValue * stats.getDevStd(1) / (Math.sqrt(K - 1)));
            System.out.print("statistiche per rho ");
            stats.setDevStd(stats.getBatchUtilizzazione(), 2);     // calcolo la devstd per Enq
            System.out.println("Critical endpoints " + stats.getMeanUtilization() + " +/- " + criticalValue * stats.getDevStd(2) / (Math.sqrt(K - 1)));
            System.out.print("statistiche per E[Ts] ");
            stats.setDevStd(stats.getBatchTempoSistema(), 3);     // calcolo la devstd per Ens
            System.out.println("Critical endpoints " + stats.getMeanWait() + " +/- " + criticalValue * stats.getDevStd(3) / (Math.sqrt(K - 1)));
            System.out.print("statistiche per E[Ns] ");
            stats.setDevStd(stats.getBatchPopolazioneSistema(), 4);     // calcolo la devstd per Ets
            System.out.println("Critical endpoints " + stats.getPopMediaSistema() + " +/- " + criticalValue * stats.getDevStd(4) / (Math.sqrt(K - 1)));
            System.out.println();
        }
    }


}
