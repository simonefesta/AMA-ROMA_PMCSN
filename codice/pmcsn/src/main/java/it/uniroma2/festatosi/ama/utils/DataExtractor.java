package it.uniroma2.festatosi.ama.utils;


import java.io.*;


public class DataExtractor{
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
            writer.write("seed " + seed + ";" + "Tempo" + ";" + "Popolazione" + ";" + "PopolazioneVeicoliPiccoli" +";" + "PopolazioneVeicoliGrandi"+ ";" + name + "\n");
            writer.write(";" + "0" + ";" + "0" + ";" + "0" + ";" + "0" + ";" + "\n");
        }
        return fileInTarget;
    }

    public static File initializeFileBatch(long seed, String name) throws IOException {
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
            writer.write("seed " + seed + ";" + "Batch" + ";" + "Popolazione" + ";" + "PopolazioneVeicoliPiccoli" +";" + "PopolazioneVeicoliGrandi"+ ";" + name + "\n");
            writer.write(";" + "0" + ";" + "0" + ";" + "0" + ";" + "0" + ";" + "\n");
        }
        return fileInTarget;
    }




        public static void writeSingleStat(File fileInTarget, double time, double value, double valueV1, double valueV2) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileInTarget, true))) {
                writer.write(";" + time + ";" + value + ";" +valueV1 + ";" + valueV2 + "\n");
            } catch (IOException e) {
                System.out.println("Si è verificato un errore durante la scrittura nel file CSV: " + e.getMessage());
            }
        }

    public static void writeBatchStat(File fileInTarget, int batch, double value, double valueV1, double valueV2) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileInTarget, true))) {
            writer.write(";" + batch + ";" + value + ";" +valueV1 + ";" + valueV2+ "\n");
        } catch (IOException e) {
            System.out.println("Si è verificato un errore durante la scrittura nel file CSV: " + e.getMessage());
        }
    }

    public static String convertCsvToTxt(/*String inputCsvPath, String outputTxtPath*/) throws IOException {
        String inputCsvPath = "target/graphs/123456789/ControllerAccettazioneBatch.csv";
        String outputTxtPath = "target/graphs/123456789/AutocorrelazioneAccettazioneBatch.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(inputCsvPath));
             PrintWriter writer = new PrintWriter(new FileWriter(outputTxtPath))) {

            // Salta l'intestazione
            br.readLine();

            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Salta la prima riga
                }
                String[] values = line.split(";");
                String lastColumnValue = values[values.length - 1];
                writer.println(lastColumnValue);
            }

            System.out.println("File TXT creato con successo: " + outputTxtPath);
            return outputTxtPath;

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Errore nella conversione da .csv a .txt");
            throw e; // Rilancia l'eccezione per indicare eventuali errori nella chiamata
        }


    }
}