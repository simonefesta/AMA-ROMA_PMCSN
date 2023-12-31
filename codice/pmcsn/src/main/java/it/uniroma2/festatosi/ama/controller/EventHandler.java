package it.uniroma2.festatosi.ama.controller;


import it.uniroma2.festatosi.ama.model.EventListEntry;

import java.util.ArrayList;
import java.util.List;

import static it.uniroma2.festatosi.ama.model.Constants.*;

/**
 * Questa classe singleton si occupa della gestione degli eventi nel sistema
 */
public class EventHandler {

    //instanza unica di EventHandler
    private static EventHandler instance=null;

    private List<EventListEntry> eventsAccettazione=new ArrayList<>(SERVERS_ACCETTAZIONE); /*event list della msq di accettazione*/
    private List<EventListEntry> eventsScarico=new ArrayList<>(SERVERS_SCARICO); /*event list della msq di scarico*/
    private List<EventListEntry> eventsGommista=new ArrayList<>(SERVERS_GOMMISTA); /*event list della msq di gommista*/
    private List<EventListEntry> eventsCarrozzeria=new ArrayList<>(SERVERS_CARROZZERIA); /*event list della msq di carrozzeria*/
    private List<EventListEntry> eventsElettrauto=new ArrayList<>(SERVERS_ELETTRAUTI); /*event list della msq di elettrauto*/
    private List<EventListEntry> eventsCarpenteria=new ArrayList<>(SERVERS_CARPENTERIA); /*event list della msq di carpenteria*/
    private List<EventListEntry> eventsMeccanica=new ArrayList<>(SERVERS_MECCANICA); /*event list della msq di meccanica*/
    private List<EventListEntry> eventsCheckout=new ArrayList<>(SERVERS_CHECKOUT); /*event list della msq di checkout*/

    private EventHandler() {

        /*event list per i vari msq*/
        this.eventsAccettazione = new ArrayList<>(SERVERS_ACCETTAZIONE+1); /* event list della msq di accettazione*/
        this.eventsScarico = new ArrayList<>(SERVERS_SCARICO+1); /* event list della msq di scarico*/
        this.eventsGommista = new ArrayList<>(SERVERS_GOMMISTA+1); /* event list della msq di gommista*/
        this.eventsCarrozzeria = new ArrayList<>(SERVERS_CARROZZERIA+1); /* event list della msq di carrozzeria*/
        this.eventsElettrauto = new ArrayList<>(SERVERS_ELETTRAUTI+1); /* event list della msq di elettrauto*/
        this.eventsCarpenteria = new ArrayList<>(SERVERS_CARPENTERIA+1); /* event list della msq di carpenteria*/
        this.eventsMeccanica = new ArrayList<>(SERVERS_MECCANICA+1); /* event list della msq di meccanica*/
        this.eventsCheckout = new ArrayList<>(SERVERS_CHECKOUT+1); /* event list della msq di checkout*/
    }

    //crea la instanza di singleton per l'event handler se questa non esiste già
    public static EventHandler getInstance() {
        if(instance==null){
            instance = new EventHandler();
        }
        return instance;
    }

    public List<EventListEntry> getEventsAccettazione() {
        return eventsAccettazione;
    }

    public List<EventListEntry> getEventsScarico() {
        return eventsScarico;
    }

    public List<EventListEntry> getEventsGommista() {
        return eventsGommista;
    }

    public List<EventListEntry> getEventsCarrozzeria() {
        return eventsCarrozzeria;
    }

    public List<EventListEntry> getEventsElettrauto() {
        return eventsElettrauto;
    }

    public List<EventListEntry> getEventsCarpenteria() {
        return eventsCarpenteria;
    }

    public List<EventListEntry> getEventsMeccanica() {
        return eventsMeccanica;
    }

    public List<EventListEntry> getEventsCheckout() {
        return eventsCheckout;
    }

    public void setEventsAccettazione(List<EventListEntry> eventListAccettazione) {
        this.eventsAccettazione=eventListAccettazione;
    }
}
