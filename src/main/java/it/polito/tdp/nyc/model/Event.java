package it.polito.tdp.nyc.model;

public class Event implements Comparable<Event> {
	
	// SChedulo tanti processi INIZIO_HS per ogni tecnoco finchè ho hotspot disponibili nel quartiere
	public enum EventType {
		INIZIO_HS, // Tecnico inizia a lavorare su un hotspot
		FINE_HS, // tecnico termina il lavoro su un hotspot
		NUOVO_QUARTIERE, // La squadra si sposta in un nuovo quartiere quando tutti i tecnici sono disoccupati
	}
	
	private int time ; // Il tempo è misurato in minuti
	private EventType type ; // Tipo di evento
	private int tecnico ; // Numero corrispondente al tecnico
	
	
	public Event(int time, EventType type, int tecnico) {
		super();
		this.time = time;
		this.type = type;
		this.tecnico = tecnico;
	}

	public int getTime() {
		return time;
	}

	public EventType getType() {
		return type;
	}

	public int getTecnico() {
		return tecnico;
	}

	@Override
	public int compareTo(Event o) {
		return this.time - o.time;
	}

}
