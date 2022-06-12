package it.polito.tdp.nyc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.nyc.model.Event.EventType;

public class Simulator {
	
	// Dati in ingresso: sono costanti e non cambieranno mai durante la simulazione
	private Graph<City, DefaultWeightedEdge> grafo;
	private List<City> cities;
	private City partenza; // Quertiere di partenza
	private int N ; // Numero di tecnici
	
	// Dati in uscita
	private int durata ; // Durata in minuti della simulazione
	private List<Integer> revisionati ; // revisionati.get(i) = numero di hotspot revisionati dal tecnico 'i' (i tra 0 e N-1)
	
	// Modello del mondo
	private List<City> daVisitare; // Quartieri ancora da visitare (escluso currentCity)
	private City currentCity ; // Quartiere in lavorazione
	private int hotSpotRimanenti ; // Hotspot ancora da revisionare nel quartiere
	private int tecniciOccupati; // Quanti tecnici sono impegnati => quando arriva a 0, cambio quartiere e genero l'evento NUOVO_QUARTIERE 
								 // (se ho ancora quartieri da elaborare, altrimenti finisce la simulazione)
	
	// Coda degli eventi
	private PriorityQueue<Event> queue ;
	
	public Simulator(Graph<City, DefaultWeightedEdge> grafo, List<City> cities) {
		this.grafo = grafo ;
		this.cities = cities ;
	}
	
	public void init (City partenza, int N)  {
		this.partenza = partenza ;
		this.N = N ;
		
		// Inizializzo gli output
		this.durata = 0 ;
		this.revisionati = new ArrayList<Integer>();
		for(int i=0; i<N; i++)
			revisionati.add(0); // All'inizio nessun hotspot è ancora stato revisionato dai tecnici
		
		// Inizializzo il mondo
		this.currentCity = this.partenza; // All'inizio la città che sto visitando ora è quella di partenza
		this.daVisitare = new ArrayList<>(this.cities); // Le città da visitare sono tutte le città a cui tolgo quella di partenza
		this.daVisitare.remove(this.currentCity); // Tolgo la città di partenza dalla lista delle città da visitare
		this.hotSpotRimanenti = this.currentCity.getnHotSpot(); // Sono gli hotspot della città corrente
		this.tecniciOccupati = 0; // All'inizio non ho ancora nessun tecnico occupato
		
		// Crea la coda
		this.queue = new PriorityQueue<>();
		
		// Caricamento iniziale della coda, devo schedulare l'inizio del lavoro per tutti i tecnici finchè ho tecnici da assegnare e 
		// hotspot da revisionare
		int i = 0; // Numero del tecnico
		while(this.tecniciOccupati<this.N && this.hotSpotRimanenti>0) {
			// Posso assegnare un tecnico ad un hotspot, quindi genero un evento di inizio lavoro all'istante 0 per il tecnico i
			queue.add(new Event( 0, EventType.INIZIO_HS, i )) ;
			this.tecniciOccupati++ ; // Aggiungendo un tecnico alla coda aumento il numero di tecnici occupati
			this.hotSpotRimanenti--; 
			i++;
		}
		
	}
	
	public void run() {
		while(!this.queue.isEmpty()) {
			Event e = this.queue.poll();
			this.durata = e.getTime(); // In questo modo nel campo durata alla fine della simulazione avrò l'evento di durata massima
			processEvent(e);
		}
	}

	private void processEvent(Event e) {
		int time = e.getTime();
		EventType type = e.getType();
		int tecnico = e.getTecnico();
		
		switch(type) {
		case INIZIO_HS:
			// Il tecnico prende in carico un lavoro e quindi aggiorno il numero di hotspot revisionati da quel tecnico
			this.revisionati.set(tecnico, this.revisionati.get(tecnico)+1); // Aumento di +1 il numero di hotspot revisionati da quel tecnico
			
			if(Math.random()<0.1) { 
				// Nel 10% dei casi la durata è di 25 minuti e quindi dopo 25 minuti dall'istante attuale termino il lavoro
				queue.add(new Event(time+25, EventType.FINE_HS, tecnico)) ;
			} else {
				queue.add(new Event(time+10, EventType.FINE_HS, tecnico)) ;
			}
			break;
			
		case FINE_HS:
			
			this.tecniciOccupati-- ;
			
			if(this.hotSpotRimanenti>0) {
				// Finisco di lavorare su un hotspot e ho un altro hotspot in cui devo lavorare
				int spostamento = (int)(Math.random()*11)+10 ; // Comprendo 10 e 20 come tempi aggiuntivi per iniziare un nuovo hotspot
				this.tecniciOccupati++;
				this.hotSpotRimanenti--;
				queue.add(new Event(time+spostamento, EventType.INIZIO_HS, tecnico));
			} else if(this.tecniciOccupati>0) {
				// Non fai nulla se oltre a me c'è ancora qualcuno che sta lavorando nello stesso quartiere e quindi devo aspettare che finisca di 
				// lavorare prima di cambiare quartiere
			} else if(this.daVisitare.size()>0){
				// Sono l'ultimo tecnico che sta lavorando in un quartiere e quindi tutti cambiamo quartiere
				
				// Scelgo il quartiere più vicino come prossimo quartiere da visitare
				City destinazione = piuVicino(this.currentCity, this.daVisitare);
				
				int spostamento = (int)(this.grafo.getEdgeWeight(this.grafo.getEdge(this.currentCity, destinazione)) / 50.0 *60.0); // Ottengo il tempo in minuti
				this.currentCity = destinazione ;
				this.daVisitare.remove(destinazione); // Elimino la nuova destinazione dai quartieri ancora da visitare
				this.hotSpotRimanenti = this.currentCity.getnHotSpot();
				
				this.queue.add(new Event(time+spostamento, EventType.NUOVO_QUARTIERE, -1)); // Non assegno nessun tecnico quando mi sposto in nuovo quartiere
			} else {
				// fine simulazione :)
			}
			
			break;
			
		case NUOVO_QUARTIERE:
			int i = 0;
			while(this.tecniciOccupati<this.N && this.hotSpotRimanenti>0) {
				// posso assegnare un tecnico ad un hotspot
				queue.add(new Event( time, EventType.INIZIO_HS, i )) ;
				this.tecniciOccupati++ ;
				this.hotSpotRimanenti--;
				i++;
			}

			break;
		}
		
	}

	private City piuVicino(City current, List<City> vicine) {
		double min = 100000.0 ;
		City destinazione = null ;
		for(City v: vicine) {
			double peso = this.grafo.getEdgeWeight(this.grafo.getEdge(current, v)); 
			if(peso<min) {
				min = peso;
				destinazione = v ;
			}
		}
		return destinazione ;
	}

	public int getDurata() {
		return durata;
	}

	public List<Integer> getRevisionati() {
		return revisionati;
	}
}
