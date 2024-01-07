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


# Ottieni la directory corrente
directory_corrente = os.getcwd()

# Definisci il percorso della cartella contenente i file CSV
percorso_cartella = os.path.join(directory_corrente, "target\graphs")

# Verifica se la cartella principale esiste
if os.path.exists(percorso_cartella):
    # Scandisci ricorsivamente tutte le sottocartelle e i file CSV
    for cartella, sottocartelle, files in os.walk(percorso_cartella):
        percorso_img=percorso_cartella+"\\img"
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

                with open(percorso_file, 'r') as file_csv:
                    reader = csv.reader(file_csv, delimiter=';')
                    for riga in reader:
                        if not headers_name:
                            seed = riga[0]
                            nome_asse_x = riga[1]
                            nome_asse_y = riga[2]
                            nome_centro = riga[3]
                            headers_name = True
                        else:
                            valori_asse_x.append(float(riga[1]))
                            valori_asse_y.append(float(riga[2]))

                # Verifica se la cartella non esiste, quindi creala se necessario
                if not os.path.exists(percorso_cartella+"\\img"):
                    os.makedirs(percorso_cartella+"\\img")

                draw(valori_asse_x, valori_asse_y, nome_asse_x, nome_asse_y, seed, percorso_img, nome_centro)
else:
    print(f"La cartella {percorso_cartella_principale} non esiste.")