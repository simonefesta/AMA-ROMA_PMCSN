package it.uniroma2.festatosi.ama.utils;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class DataExtractor{
    //static File fileInTarget;
    public static File initializeFile(long seed, String name) throws IOException {
        String currentDirectory = System.getProperty("user.dir");
        String targetDirectoryName = "target";
        String fileName = name + ".csv";
        String targetDirectoryPath = currentDirectory + File.separator + targetDirectoryName + File.separator + "graphs" + File.separator + seed;

        File targetDirectory = new File(targetDirectoryPath);

        if (!targetDirectory.exists()) {
            boolean directoryCreated = targetDirectory.mkdirs();
            if (directoryCreated) {
                System.out.println("La cartella 'seed_" + seed + "' è stata creata in 'target'.");
            } else {
                System.out.println("Impossibile creare la cartella 'seed_" + seed + "' in 'target'.");
            }
        }

        File fileInTarget = new File(targetDirectory, fileName);
        //System.out.println("CREO :" + fileName + " ");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileInTarget))) {
            writer.write("seed " + seed + ";" + "Tempo" + ";" + "Popolazione" + ";" + name + "\n");
            writer.write(";" + "0" + ";" + "0" + ";\n");
        }
        return fileInTarget;
    }




        public static void writeSingleStat(File fileInTarget, double time, double value) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileInTarget, true))) {
                writer.write(";" + time + ";" + value + "\n");
            } catch (IOException e) {
                System.out.println("Si è verificato un errore durante la scrittura nel file CSV: " + e.getMessage());
            }
        }
}