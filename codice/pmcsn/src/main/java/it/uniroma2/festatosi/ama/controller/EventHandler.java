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

    //event Lists (come presentate da libro una entry per server più una per gli ingressi)
    private List<EventListEntry> eventsAccettazione; /*event list della msq di accettazione*/
    private List<EventListEntry> eventsScarico; /*event list della msq di scarico*/
    private List<EventListEntry> eventsGommista; /*event list della msq di gommista*/
    private List<EventListEntry> eventsCarrozzeria; /*event list della msq di carrozzeria*/
    private List<EventListEntry> eventsElettrauto; /*event list della msq di elettrauto*/
    private List<EventListEntry> eventsCarpenteria; /*event list della msq di carpenteria*/
    private List<EventListEntry> eventsMeccanica; /*event list della msq di meccanica*/
    private List<EventListEntry> eventsCheckout; /*event list della msq di checkout*/
    private List<EventListEntry> eventsSistema; /*event list della msq di checkout*/

    //arrivi interni dai serventi precedenti nella rete
    private List<EventListEntry> internalEventsGommista; /*eventi interni alla coda di gommista*/
    private List<EventListEntry> internalEventsCarrozzeria; /*eventi interni alla coda di carrozzeria*/
    private List<EventListEntry> internalEventsElettrauto; /*eventi interni alla coda di elettrauto*/
    private List<EventListEntry> internalEventsCarpenteria; /*eventi interni alla coda di carpenteria*/
    private List<EventListEntry> internalEventsMeccanica; /*eventi interni alla coda di meccanica*/
    private List<EventListEntry> internalEventsCheckout; /*eventi interni alla coda di checkout*/
    private List<EventListEntry> internalEventsScarico; /*eventi interni alla coda di checkout*/

    private int numberV1=0; /*conta i veicoli del primo tipo nel sistema*/
    private int numberV2=0; /*conta i veicoli del secondo tipo nel sistema*/

    private EventHandler() {
        /*event list per i vari msq*/
        this.eventsAccettazione = new ArrayList<>(SERVERS_ACCETTAZIONE+1); /* event list della msq di accettazione*/
        this.eventsScarico = new ArrayList<>(SERVERS_SCARICO+2); /* event list della msq di scarico,
        servono due entry in più perché una è quella per gli arrivi dall'esterno l'altra per quelli interni*/
        this.eventsGommista = new ArrayList<>(SERVERS_GOMMISTA+1); /* event list della msq di gommista*/
        this.eventsCarrozzeria = new ArrayList<>(SERVERS_CARROZZERIA+1); /* event list della msq di carrozzeria*/
        this.eventsElettrauto = new ArrayList<>(SERVERS_ELETTRAUTO +1); /* event list della msq di elettrauto*/
        this.eventsCarpenteria = new ArrayList<>(SERVERS_CARPENTERIA+1); /* event list della msq di carpenteria*/
        this.eventsMeccanica = new ArrayList<>(SERVERS_MECCANICA+1); /* event list della msq di meccanica*/
        this.eventsCheckout = new ArrayList<>(SERVERS_CHECKOUT+1); /* event list della msq di checkout*/
        this.eventsSistema = new ArrayList<>(NODES_SISTEMA); /* event list della msq di checkout*/

        /*internal arrivals*/
        this.internalEventsGommista=new ArrayList<>();
        this.internalEventsCarrozzeria=new ArrayList<>();
        this.internalEventsElettrauto=new ArrayList<>();
        this.internalEventsCarpenteria=new ArrayList<>();
        this.internalEventsMeccanica=new ArrayList<>();
        this.internalEventsCheckout=new ArrayList<>();
        this.internalEventsScarico=new ArrayList<>();
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

    public void setEventsGommista(List<EventListEntry> eventListGommista) {
        this.eventsGommista=eventListGommista;
    }

    public void setEventsScarico(List<EventListEntry> eventListScarico) {
        this.eventsScarico=eventListScarico;
    }

    public int getNumberV1() {
        return numberV1;
    }

    public void incrementNumberV1(){
        this.numberV1++;
    }

    public void incrementNumberV2(){
        this.numberV2++;
    }

    public void decrementNumberV1(){
        this.numberV1--;
    }

    public void decrementNumberV2(){
        this.numberV2--;
    }

    public int getNumberV2() {
        return numberV2;
    }

    public int getNumber() {
        return numberV1+numberV2;
    }

    public List<EventListEntry> getInternalEventsGommista() {
        return internalEventsGommista;
    }

    public void setInternalEventsGommista(List<EventListEntry> internalEventsGommista) {
        this.internalEventsGommista = internalEventsGommista;
    }

    public List<EventListEntry> getInternalEventsCarrozzeria() {
        return internalEventsCarrozzeria;
    }

    public void setInternalEventsCarrozzeria(List<EventListEntry> internalEventsCarrozzeria) {
        this.internalEventsCarrozzeria = internalEventsCarrozzeria;
    }

    public List<EventListEntry> getInternalEventsElettrauto() {
        return internalEventsElettrauto;
    }

    public void setInternalEventsElettrauto(List<EventListEntry> internalEventsElettrauto) {
        this.internalEventsElettrauto = internalEventsElettrauto;
    }

    public List<EventListEntry> getInternalEventsCarpenteria() {
        return internalEventsCarpenteria;
    }

    public void setInternalEventsCarpenteria(List<EventListEntry> internalEventsCarpenteria) {
        this.internalEventsCarpenteria = internalEventsCarpenteria;
    }

    public List<EventListEntry> getInternalEventsMeccanica() {
        return internalEventsMeccanica;
    }

    public void setInternalEventsMeccanica(List<EventListEntry> internalEventsMeccanica) {
        this.internalEventsMeccanica = internalEventsMeccanica;
    }

    public List<EventListEntry> getInternalEventsCheckout() {
        return internalEventsCheckout;
    }

    public void setInternalEventsCheckout(List<EventListEntry> internalEventsCheckout) {
        this.internalEventsCheckout = internalEventsCheckout;
    }

    public List<EventListEntry> getInternalEventsScarico() {
        return internalEventsScarico;
    }

    public void setInternalEventsScarico(List<EventListEntry> internalEventsScarico) {
        this.internalEventsScarico = internalEventsScarico;
    }



    public void decrementVType(int vType) throws Exception {
        switch (vType) {
            case 1:
                decrementNumberV1();
                break;
            case 2:
                decrementNumberV2();
                break;
            default:
                throw new Exception("Tipo di veicolo non supportato dal sistema");
        }
    }

    public List<EventListEntry> getEventsSistema() {
        return eventsSistema;
    }

    public void setEventsSistema(List<EventListEntry> eventsSistema) {
        this.eventsSistema = eventsSistema;
    }

    public void setEventsMeccanica(List<EventListEntry> eventsMeccanica) {
        this.eventsMeccanica=eventsMeccanica;
    }

    public void setEventsCarpenteria(List<EventListEntry> eventsCarpenteria) {
        this.eventsCarpenteria=eventsCarpenteria;
    }

    public void setEventsCarrozzeria(List<EventListEntry> eventsCarrozzeria) {
        this.eventsCarrozzeria=eventsCarrozzeria;
    }

    public void setEventsElettrauto(List<EventListEntry> eventsElettrauto) {
        this.eventsElettrauto=eventsElettrauto;
    }

    public void setEventsCheckout(List<EventListEntry> eventsCheckout) {
        this.eventsCheckout=eventsCheckout;
    }
}
