package it.uniroma2.festatosi.ama.model;

import java.util.List;

/**classe che implementa la EventList, si hanno n entry con n=c+1 (c sono i channels ossia i server paralleli nella
 * multi-server queue, il posto 0 è occupato dalla entry che rappresenta gli arrivi).
 * - x: stato di attività corrente dell'evento (per l'evento[0] rappresenta se arrivi attivi, per gli altri se i server
 * sono busy o meno)
 * - t: tempo di arrivo dell'evento
 *
 * Implementazione basata sul libro di testo di Lemmis p. 204.
*/
public class EventListEntry {
    private double t;
    private int x;
    private int vehicleType;

    public EventListEntry(double t, int x){
        this.t=t;
        this.x=x;
        this.vehicleType=0;
    }

    public EventListEntry(double t, int x, int vehicleType){
        this.t=t;
        this.x=x;
        this.vehicleType=vehicleType;
    }

    public double getT() {
        return t;
    }

    public void setT(double t) {
        this.t = t;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(int vehicleType) {
        this.vehicleType = vehicleType;
    }

    /**
     * Viene invocata per farsi restituire il tipo del prossimo evento gestito
     *
     * @param event lista degli eventi dal quale bisogna estrarre il prossimo
     * @param channels numero di server per la msq considerata
     * @return index del prossimo event type
     *
     * implementazione presa dalla classe 'Msq.java' fornita dal libro di testo
     */
    public static int getNextEvent(List<EventListEntry> event, int channels){
        int e;
        int i = 0;

        while (event.get(i).x == 0)       /* find the index of the first 'active' */
            i++;                        /* element in the event list            */
        e = i;
        while (i < channels) {         /* now, check the others to find which  */
            i++;                        /* event type is most imminent          */
            if ((event.get(i).x == 1) && (event.get(i).t < event.get(e).t))
                e = i;
        }
        return e;
    }
}
