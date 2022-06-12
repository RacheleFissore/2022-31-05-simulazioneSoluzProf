package it.polito.tdp.nyc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.nyc.db.NYCDao;

public class Model {
	
	private List<String> providers ;
	private List<City> cities;

	private Graph<City, DefaultWeightedEdge> grafo ;
	
	// risultati simulazione
	private int durata;
	private List<Integer> revisionati;
	
	public List<String> getProviders() {
		if(this.providers==null) {
			NYCDao dao = new NYCDao();
			this.providers = dao.getProviders();
		}
		return this.providers;
	}
	
	public String creaGrafo(String provider) {
		NYCDao dao = new NYCDao() ;
		this.cities = dao.getCities(provider);
		
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class) ;
		
		Graphs.addAllVertices(this.grafo, this.cities);
		
		for(City c1 : this.cities) {
			for(City c2 : this.cities) {
				if(!c1.equals(c2)) {
					double peso = LatLngTool.distance(c1.getPosizione(), c2.getPosizione(), LengthUnit.KILOMETER);
					Graphs.addEdge(this.grafo, c1, c2, peso);
				}
			}
		}
		
		return "Grafo creato con "+this.grafo.vertexSet().size()+
				" vertici e "+this.grafo.edgeSet().size()+" archi\n";	
		
	}
	
	public List<City> getCities() {
		return cities;
	}

	// La uso per popolare la table view
	public List<CityDistance> getCityDistances(City scelto) {
		List<CityDistance> result = new ArrayList<>();
		List<City> vicini = Graphs.neighborListOf(this.grafo, scelto);
		for(City v: vicini) {
			result.add(new CityDistance(v.getNome(), 
					this.grafo.getEdgeWeight(this.grafo.getEdge(scelto, v)))) ;
		}
		
		Collections.sort(result, new Comparator<CityDistance>() {
			@Override
			public int compare(CityDistance o1, CityDistance o2) {
				return o1.getDistanza().compareTo(o2.getDistanza());
			}			
		});
		
		return result ;
	}
	
	public void simula(City scelto, int N) {
		Simulator sim = new Simulator(this.grafo, this.cities);
		sim.init(scelto, N);
		sim.run();
		this.durata = sim.getDurata();
		this.revisionati = sim.getRevisionati();
	}

	public int getDurata() {
		return durata;
	}

	public List<Integer> getRevisionati() {
		return revisionati;
	}
	
}
