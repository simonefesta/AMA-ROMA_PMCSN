import os
import csv
import matplotlib.pyplot as plt

def draw(valori_asse_x,valori_asse_y, nome_asse_x, nome_asse_y,seed,cartella,nome_centro,valori_asse_y_veicoli_piccoli,valori_asse_y_veicoli_grandi):

    drawDifference = True # se falso, NON disegna i veicoli grandi e piccoli sul grafico, ma solo la loro somma.
    
    larghezza_immagine = 16
    altezza_immagine = 6 

    plt.figure(figsize=(larghezza_immagine, altezza_immagine))  # Dimensione del plot
    plt.xlabel(nome_asse_x)  # Associo alla label delle x la stringa "Nome_asse_x" (che può essere "Tempo" o "Numero Batch")
    plt.ylabel(nome_asse_y)  # Associo alla label delle y la stringa "Popolazione"
    
   # Plot per i punti (valori_asse_x, valori_asse_y)
    plt.plot(valori_asse_x, valori_asse_y, label="Veicoli totali", marker='None', linestyle='-',linewidth=2)

    if (drawDifference is True):
        plt.plot(valori_asse_x, valori_asse_y_veicoli_piccoli, label="Veicoli Piccoli", marker='None', linestyle='-', color='orange',linewidth=1)

        plt.plot(valori_asse_x, valori_asse_y_veicoli_grandi, label="Veicoli Grandi", marker='None', linestyle='-', color='green',linewidth=1)


    nome_centro = nome_centro.replace("Controller", "", 1)    # rimuovo la parola "Controller" dal nome del centro (che userò anche per salvere il nome del grafico)
    titolo = nome_centro.replace("Batch", "", 1)         # rimuovo la parola "Batch", nel caso fosse presente, da mettere nel titolo nel grafico

    plt.suptitle("Evoluzione della popolazione per: " + titolo)
    plt.title("seed: "+seed)
    plt.grid(True)
    plt.legend()
   
    # Specifico come andrò a salvare il tutto
    percorso_grafico = os.path.join(cartella, "graph_"+nome_centro+"_"+seed+".png")
    plt.savefig(percorso_grafico)
    print("done")


def elabora_files_csv(directory_corrente):
    # Dove trovo i csv
    percorso_cartella = os.path.join(directory_corrente, "target/graphs")

    # Check su esistenza della cartella
    if os.path.exists(percorso_cartella):
        # Scandisco tutti i csv
        for cartella, sottocartelle, files in os.walk(percorso_cartella):
            percorso_img = ""
            for filename in files:

                percorso_file = os.path.join(cartella, filename)

                if filename.endswith(".csv") and os.path.isfile(percorso_file) and ("Replication" not in percorso_file):

                    #inizializzo i valori di seguito, per ogni csv, nei primi due vettori metteremo i valori da plottare, gli ultimi 4 formano la legenda
                    valori_asse_x = []  
                    valori_asse_y_veicoli_totali = []
                    valori_asse_y_veicoli_piccoli = []
                    valori_asse_y_veicoli_grandi = []

                    # legenda, per migliorare leggibilità grafico
                    nome_asse_x = ""
                    nome_asse_y = ""
                    seed = ""
                    nome_centro = ""

                    headers_name = False    # booleano per discriminare la prima riga (in cui specifichiamo il nome delle colonne) dalle altre, in cui ho i valori.
          

                    with open(percorso_file, 'r') as file_csv:
                        reader = csv.reader(file_csv, delimiter=';')
                        for riga in reader:
                            if not headers_name: #se ancora non ho incontrato l'header (nomi colonne), allora sto alla prima riga, prelevo nomi colonne
                                seed = riga[0]
                                nome_asse_x = riga[1]
                                nome_asse_y = riga[2]
                                nome_centro = riga[5]
                                headers_name = True
                            else:
                                valori_asse_x.append(float(riga[1]))
                                valori_asse_y_veicoli_totali.append(float(riga[2]))
                                valori_asse_y_veicoli_piccoli.append(float(riga[3]))
                                valori_asse_y_veicoli_grandi.append(float(riga[4]))


                    seed = seed.replace("seed ", "", 1) #tolgo la parola seed, lasciando solo il numero.

                    print("Produco un grafico per " + filename)

                    percorso_img = percorso_cartella + "/" + seed + "/img/"
                    # Se la cartella non esiste, la creo.
                    if not os.path.exists(percorso_cartella + "/" + seed + "/img"):
                        os.makedirs(percorso_cartella + "/" + seed + "/img")

                    draw(valori_asse_x, valori_asse_y_veicoli_totali, nome_asse_x, nome_asse_y, seed, percorso_img, nome_centro,valori_asse_y_veicoli_piccoli,valori_asse_y_veicoli_grandi)
    else:
        print(f"La cartella {percorso_cartella} non esiste.")

# Chiamata alla funzione nel main
if __name__ == "__main__":
    directory_corrente = os.getcwd()
    elabora_files_csv(directory_corrente)