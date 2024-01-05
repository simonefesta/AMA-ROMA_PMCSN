package it.uniroma2.festatosi.ama.utils;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class DataExtractor{
    static File fileInTarget;
    public static void initializeFile() throws IOException {
        String currentDirectory = System.getProperty("user.dir"); //prende la cartella "pmcsn"
        String targetDirectoryName = "target";                    // andiamo nella sottodirectory target
        String fileName = "statistiche.csv";                     // nome del file prodotto
        File targetDirectory = new File(currentDirectory, targetDirectoryName);  //compongo il percorso in cui metterò le stats


        fileInTarget = new File(targetDirectory, fileName);                     //creo il file "fileName" nella cartella target

        if (!targetDirectory.exists()) {
            boolean directoryCreated = targetDirectory.mkdir();
            if (directoryCreated) {
                System.out.println("La cartella 'target' è stata creata.");
            } else {
                System.out.println("Impossibile creare la cartella 'target'.");
            }
        }


    }



    public static void writeHeaders(long seed, String name) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileInTarget))) {
            // Scrivi le intestazioni nel file CSV
            writer.write("seed " +seed +";" + "Tempo" + ";" + "Popolazione" +";" + name+"\n");
        }
    }

    public static void writeSingleStat(double time, double value) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileInTarget, true))) {
            // Scrivi il tempo e il valore nel file CSV
            writer.write(";" + time + ";" + value + "\n"); //il primo ; è per lasciare spazio al ssed

            //System.out.println("Dati scritti nel file CSV con successo.");
        } catch (IOException e) {
            System.out.println("Si è verificato un errore durante la scrittura nel file CSV: " + e.getMessage());
        }
    }
}