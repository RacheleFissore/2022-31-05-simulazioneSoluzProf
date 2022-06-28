package it.polito.tdp.nyc.model;

public class CityDistance {
	
	private String nome ;
	private Double distanza ;
	
	public CityDistance(String nome, Double distanza) {
		super();
		this.nome = nome;
		this.distanza = distanza;
	}
	
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public Double getDistanza() {
		return distanza;
	}
	public void setDistanza(Double distanza) {
		this.distanza = distanza;
	}
	
}