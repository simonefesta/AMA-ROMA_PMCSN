### Descrizione del sistema

L'azienda di rifiuti dispone di un tot di automezzi, suddivisi per capienza. Ipotizziamo, per ora, un lavoro diviso a fasce orarie, con dei lower bound in merito alla quantità di rifiuti da incenerire.

- Ogni mezzo entrante, viene inizialmente pesato all'entrata dello stabilmento. La taratura dura qualche minuto e notoriamente non comporta rallentamenti. 

- Ogni mezzo ha una probabilità $p_1$ di terminare la raccolta con il massimo dell'efficienza. Il passo successivo sarà lo smaltimento dei rifiuti.
  La restante probabilità $q_1$ indica un problema tecnico che non ha permesso al mezzo di completare la raccolta.

- Nel caso in cui il mezzo abbia avuto un problema tecnico, esso deve essere passa per l'*accettazione*, dove viene diagnosticato l'errore e reindirizzato il mezzo presso l'autofficina di competenza.
  
  - Le probabilità da $p_2$ a $p_6$ sono associate alle diverse autofficine, ciascuna avente un numero diverso di serventi e di tempi di servizio.
    Tutti i mezzi passanti per queste officine, dispongono ancora della raccolta effettuata e da smaltire.
  
  - Con probabilità $q_2$, l'accettazione stabilisce che il mezzo è non riparabile, o che il costo della riparazione non copra l'effettivo valore del mezzo. Tipicamente questa probabilità è ridotta, ad esempio come in casi di combustione del mezzo.

- Nella fase di smaltimento, disponiamo di una coda unica con $5$ serventi, ovvero aree in cui scaricare i rifiuti. (Non c'è priorità, può essere aggiunta dopo?)

- Terminato lo smaltimento, si verifica se il mezzo è pronto per un nuovo giro:
  
  - con probabilità $p_8$ il mezzo ha il rifornimento necessario, è sanificato e pronto per essere di nuovo disponibile.
  
  - con probabilità $q_3$ il mezzo richiede di essere sanificato.
  
  - con probabilità $q_4$ il mezzo non ha il rifornimento necessario per essere di nuovo disponibile, e quindi si mette in coda per fare rifornimento.

## Nomenclatura:

- Gommista: cambia le gomme

- Carrozzeria: estetica del mezzo

- Stazione di servizio: rifornimento.

- Elettrauto: parte circuiteria del mezzo.

- Meccanica: parte meccanica dl mezzo.

- Carpenteria meccanica: parte posteriore (dove c'è il carico).

## Obiettivi

- Minimizzare la quantità di tempo nel sistema...

- Il numero di mezzi nel sistema non può superare una certa soglia.

- Smaltire una certa quantità di rifiuti, leggasi "deve esserci un certo numero di ingressi di camion nel sistema" (throughput giornaliero).
