package it.polito.tdp.nyc.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.javadocmd.simplelatlng.LatLng;

import it.polito.tdp.nyc.model.City;
import it.polito.tdp.nyc.model.Hotspot;

public class NYCDao {
	
	public List<String> getProviders() {
		String sql = "SELECT DISTINCT Provider "
				+ "FROM nyc_wifi_hotspot_locations "
				+ "ORDER BY Provider";
		Connection conn = DBConnect.getConnection();
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			List<String> result = new ArrayList<String>();
			while(res.next()) {
				result.add(res.getString("Provider"));
			}
			conn.close();
			return result ;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// La COUNT(*) conta il numero di righe che rappresentano il numero di hotspot presenti in una determinata city
	public List<City> getCities(String provider) {
		String sql = "SELECT DISTINCT City, AVG(Latitude) AS Lat, AVG(Longitude) AS Lng, COUNT(*) AS NUM "
				+ "FROM nyc_wifi_hotspot_locations "
				+ "WHERE Provider= ? "
				+ "GROUP BY City "
				+ "ORDER BY City";
		
		Connection conn = DBConnect.getConnection();
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, provider);
			ResultSet res = st.executeQuery() ;
			List<City> result = new ArrayList<City>() ;
			while(res.next()) {
				result.add(new City( res.getString("City"),
						new LatLng(res.getDouble("Lat"), res.getDouble("Lng")),
						res.getInt("NUM")
						));
			}
			conn.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Hotspot> getAllHotspot(){
		String sql = "SELECT * FROM nyc_wifi_hotspot_locations";
		List<Hotspot> result = new ArrayList<>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(new Hotspot(res.getInt("OBJECTID"), res.getString("Borough"),
						res.getString("Type"), res.getString("Provider"), res.getString("Name"),
						res.getString("Location"),res.getDouble("Latitude"),res.getDouble("Longitude"),
						res.getString("Location_T"),res.getString("City"),res.getString("SSID"),
						res.getString("SourceID"),res.getInt("BoroCode"),res.getString("BoroName"),
						res.getString("NTACode"), res.getString("NTAName"), res.getInt("Postcode")));
			}
			
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Error");
		}

		return result;
	}
	
}
