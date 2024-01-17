import os
import csv
import matplotlib.pyplot as plt

def draw(valori_asse_x,valori_asse_y, nome_asse_x, nome_asse_y,seed,cartella,nome_centro):
    larghezza_immagine = 10
    altezza_immagine = 6 

    plt.figure(figsize=(larghezza_immagine, altezza_immagine))  # Dimensione del plot
    plt.plot(valori_asse_x, valori_asse_y, marker='None', linestyle='-')  # non evidenzio i punti (intersezione (x,y)) proveniente dai csv
    plt.xlabel(nome_asse_x)  # Associo alla label delle x la stringa "Nome_asse_x" (che può essere "Tempo" o "Numero Batch")
    plt.ylabel(nome_asse_y)  # Associo alla label delle y la stringa "Popolazione"


    nome_centro = nome_centro.replace("Controller", "", 1)    # rimuovo la parola "Controller" dal nome del centro (che userò anche per salvere il nome del grafico)
    titolo = nome_centro.replace("Batch", "", 1)         # rimuovo la parola "Batch", nel caso fosse presente, da mettere nel titolo nel grafico

    plt.suptitle("Evoluzione della popolazione per: " + titolo)
    plt.title("seed: "+seed)
    plt.grid(True)
   
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

                if filename.endswith(".csv") and os.path.isfile(percorso_file):

                    #inizializzo i valori di seguito, per ogni csv, nei primi due vettori metteremo i valori da plottare, gli ultimi 4 formano la legenda
                    valori_asse_x = []  
                    valori_asse_y = []

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
                                nome_centro = riga[3]
                                headers_name = True
                            else:
                                valori_asse_x.append(float(riga[1]))
                                valori_asse_y.append(float(riga[2]))

                    seed = seed.replace("seed ", "", 1) #tolgo la parola seed, lasciando solo il numero.

                    print("Produco un grafico per " + filename)

                    percorso_img = percorso_cartella + "/" + seed + "/img/"
                    # Se la cartella non esiste, la creo.
                    if not os.path.exists(percorso_cartella + "/" + seed + "/img"):
                        os.makedirs(percorso_cartella + "/" + seed + "/img")

                    draw(valori_asse_x, valori_asse_y, nome_asse_x, nome_asse_y, seed, percorso_img, nome_centro)
    else:
        print(f"La cartella {percorso_cartella} non esiste.")

# Chiamata alla funzione nel main
if __name__ == "__main__":
    directory_corrente = os.getcwd()
    elabora_files_csv(directory_corrente)