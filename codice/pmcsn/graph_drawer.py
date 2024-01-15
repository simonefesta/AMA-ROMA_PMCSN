import os
import csv
import matplotlib.pyplot as plt

def draw(valori_asse_x,valori_asse_y, nome_asse_x, nome_asse_y,seed,cartella,nome_centro):
    larghezza_immagine = 10
    altezza_immagine = 6  # Altezza dell'immagine (modifica a piacere)

    plt.figure(figsize=(larghezza_immagine, altezza_immagine))  # Imposta le dimensioni della figura
    plt.plot(valori_asse_x, valori_asse_y, marker='None', linestyle='-')  # Nessun marker, solo linee
    plt.xlabel(nome_asse_x)  # Utilizza il nome della colonna x come etichetta x
    plt.ylabel(nome_asse_y)  # Utilizza il nome della colonna y come etichetta y


    nome_centro = nome_centro.replace("Controller", "", 1)    # rimuovo la parola "Controller" dal nome del centro
    seed = seed.replace("seed ","",1)

    plt.suptitle("Evoluzione della popolazione per: " + nome_centro)
    plt.title("seed: "+seed)
    plt.grid(True)

    # Percorso completo per la sottodirectory "target"
    percorso_target = os.path.join(os.getcwd(), "target")

    percorso_grafico = os.path.join(cartella, "graph_"+nome_centro+"_"+seed+".png")
    plt.savefig(percorso_grafico)
    print("done")

def calcola_media_per_batch(reader):
    media_per_batch = {}
    headers_name = True  # Inizia con True per considerare la prima riga come header
    for riga in reader:
        if (headers_name != True):
            valore_batch = float(riga[1])
            #print(valore_batch)
            valore_popolazione = float(riga[2])  

            if valore_batch in media_per_batch:
                media_per_batch[valore_batch].append(valore_popolazione)
            else:
                media_per_batch[valore_batch] = [valore_popolazione]
        else:
            headers_name = False

    # Restituisci una lista di tuple (batch, media) per ogni batch
    risultati = [(batch, sum(media) / len(media)) for batch, media in media_per_batch.items()]
    return risultati


def elabora_files_csv(directory_corrente):
    # Definisci il percorso della cartella contenente i file CSV
    percorso_cartella = os.path.join(directory_corrente, "target/graphs")

    # Verifica se la cartella principale esiste
    if os.path.exists(percorso_cartella):
        # Scandisci ricorsivamente tutte le sottocartelle e i file CSV
        for cartella, sottocartelle, files in os.walk(percorso_cartella):
            percorso_img = ""
            for filename in files:
                percorso_file = os.path.join(cartella, filename)

                if filename.endswith(".csv") and os.path.isfile(percorso_file):
                    valori_asse_x = []
                    valori_asse_y = []
                    nome_asse_x = ""
                    nome_asse_y = ""
                    seed = ""
                    nome_centro = ""

                    headers_name = False
                    data = []

                    with open(percorso_file, 'r') as file_csv:
                        reader = csv.reader(file_csv, delimiter=';')
                        for riga in reader:
                            data.append(riga)  # Salva i dati in una lista
                            if not headers_name:
                                seed = riga[0]
                                nome_asse_x = riga[1]
                                nome_asse_y = riga[2]
                                nome_centro = riga[3]
                                headers_name = True
                            elif "Batch" not in filename:
                                valori_asse_x.append(float(riga[1]))
                                valori_asse_y.append(float(riga[2]))

                    seed = seed.replace("seed ", "", 1)
                    print(percorso_img + f"/{seed}")
                    percorso_img = percorso_cartella + "/" + seed + "/img/"
                    # Verifica se la cartella non esiste, quindi creala se necessario
                    if not os.path.exists(percorso_cartella + "/" + seed + "/img"):
                        os.makedirs(percorso_cartella + "/" + seed + "/img")
                    # Verifica se la parola "Batch" è presente nel nome del file
                    if "Batch" in filename:
                            risultati = calcola_media_per_batch(data)
                            #print("risultati per " + nome_centro)
                            for batch, media in risultati:
                                #print(f"Batch: {batch}, Media Popolazione: {media}")
                                valori_asse_x.append(batch)
                                valori_asse_y.append(media)


                    draw(valori_asse_x, valori_asse_y, nome_asse_x, nome_asse_y, seed, percorso_img, nome_centro)
    else:
        print(f"La cartella {percorso_cartella} non esiste.")

# Chiamata alla funzione nel main
if __name__ == "__main__":
    directory_corrente = os.getcwd()
    elabora_files_csv(directory_corrente)