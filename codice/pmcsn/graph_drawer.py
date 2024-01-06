import os
import csv
import matplotlib.pyplot as plt

def draw(valori_asse_x,valori_asse_y, nome_asse_x, nome_asse_y,seed,nome_centro):
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

    percorso_grafico = os.path.join(percorso_target, "graph_"+nome_centro+"_"+seed+".png")
    plt.savefig(percorso_grafico)
    plt.show()
    print("done")


# Ottieni la directory corrente
directory_corrente = os.getcwd()

# Definisci il percorso del file statistiche.csv
percorso_file = os.path.join(directory_corrente,"target", "statistiche.csv")

# Verifica se il file esiste prima di tentare di leggerlo
if os.path.exists(percorso_file):
    # Apre il file in modalità lettura e stampa il suo contenuto
     # Liste per memorizzare i valori di asse x e asse y
    valori_asse_x = []
    valori_asse_y = []

    nome_asse_x = ""
    nome_asse_y = ""
    seed = ""
    nome_centro =""

    headers_name = False  # False se non ho ancora letto il nome delle colonne

    with open(percorso_file, 'r') as file_csv:
        reader = csv.reader(file_csv, delimiter=';')
        for riga in reader:
              if (headers_name == False): #sto leggendo la prima riga, ovvero nome colonne
                
                seed = riga[0]
                nome_asse_x = riga[1]
                nome_asse_y = riga[2]
                nome_centro = riga[3]
                
                
                headers_name = True  # Imposta a False dopo aver salvato i nomi delle colonne
              else:   
                valori_asse_x.append(float(riga[1]))
                valori_asse_y.append(float(riga[2]))
 

    draw(valori_asse_x,valori_asse_y,nome_asse_x,nome_asse_y,seed,nome_centro)

else:
    print(f"Il file {percorso_file} non esiste.")