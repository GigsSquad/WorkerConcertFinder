package com.humandevice.wrk.backend.workers;

import com.humandevice.wrk.backend.others.MapMgr;
import com.humandevice.wrk.backend.others.Normalizer;
import com.itextpdf.text.log.SysoLogger;
import com.mysql.jdbc.MysqlDataTruncation;
import com.mysql.jdbc.exceptions.jdbc4.MySQLDataException;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//import com.humandevice.wrk.backend.others.Normalizer;

public abstract class ParseWorker extends Worker {

	static Logger logger = Logger.getLogger(ParseWorker.class);
	protected long lastRun;
	protected PreparedStatement preparedStatement = null;
	protected ResultSet resultSet = null;
	protected String agencyName;
	public static int errCount = 0;
	/*
	 * Abstract class to get data from agencies
	 */

	public abstract void getData() throws IOException;

	public void addConcert(String conArtist, String conCity, String conSpot, int conDay, int conMonth, int conYear, String agencyName, String conUrl) {

		try {
			PreparedStatement pstm = null;
            ResultSet rs = null;
            conCity = Normalizer.normalizeCity(conCity);
            //conSpot = Normalizer.normalizeSpot(conSpot) <- to jest na którymś branchu

            //sprawdzamy czy miejsce jest już w bazie
            String getPlace = "SELECT * FROM new_places WHERE city = ? AND spot = ?";
            pstm = connection.prepareStatement(getPlace);
            pstm.setString(1,conCity);
            pstm.setString(2,conSpot);

            rs = pstm.executeQuery();

            if(rs.next()){ // miejsce w bazie
                System.out.println("Już w bazie: "+conCity+" "+conSpot);
            }
            else { // musimy dodać miejsce do bazy
                Double[] lonlat = MapMgr.getCoordinates(conSpot, conCity, "");
                Double lon = lonlat[0], lat = lonlat[1];
                String insertPlace = "INSERT INTO new_places (`city`,`spot`,`lat`,`lon`) VALUES (?,?,?,?)";
                pstm = connection.prepareStatement(insertPlace);
                pstm.setString(1,conCity);
                pstm.setString(2,conSpot);
                pstm.setDouble(3,lat);
                pstm.setDouble(4,lon);
                pstm.executeUpdate();
            }
            // miejsce na bank jest już w bazie, można dodać miasto i stworzyć relację
            // do relacji przyda nam się id_place, więc zanim dodamy koncert musimy to wyciągnąć
            String getPlaceId = "SELECT place_id FROM new_places WHERE city = '"+conCity+"' AND spot = '"+conSpot+"'";
            rs = pstm.executeQuery(getPlaceId);
            rs.next();
            Integer placeID = rs.getInt(1);

            System.out.println("Agencja: "+agencyName);
            System.out.println("PL_ID ("+conCity+","+conSpot+"): "+ (placeID==null? "chuj" : placeID)+"\n ------------");

            String insertGig = "INSERT IGNORE INTO new_concerts (`artist`,`place_id`,`day`,`month`,`year`,`agency`,`url`)" +
                    "VALUES (?,?,?,?,?,?,?)";

			pstm = connection.prepareStatement(insertGig);
            pstm.setString(1,conArtist);
            pstm.setInt(2,placeID);
            pstm.setInt(3, conDay);
            pstm.setInt(4,conMonth);
            pstm.setInt(5,conYear);
            pstm.setString(6,agencyName);
            pstm.setString(7,conUrl);

            pstm.executeUpdate();

			//String log = String.format("%-4.4s %-12.12s %-20.20s %-18.18s %-18.18s %-40.40s", agencyName, conCity, conSpot, lon, lat, conArtist);
			//System.out.println(log);
			
			
			//pobieram
			
//			String get = "SELECT* FROM concerts_test WHERE artist =? AND CITY = ? AND DAY=? AND MONTH=? AND YEAR =?";
//
//			pstm = connection.prepareStatement(get);
//			pstm.setString(1, conArtist);
//			pstm.setString(2, conCity);
//			pstm.setInt(3, conDay);
//			pstm.setInt(4, conMonth);
//			pstm.setInt(5, conYear);
//
//			ResultSet rs = pstm.executeQuery();
//			String log;
//			if(rs.next())
//			{
//			log = String.format("ISTNIEJE %-4.4s %-12.12s %-20.20s %-18.18s %-18.18s %-40.40s", agencyName, conCity, conSpot, lon, lat, conArtist);
//			System.out.println(log);
//			}
//			else
//			{
//			log = String.format("DODAJE %-4.4s %-12.12s %-20.20s %-18.18s %-18.18s %-40.40s", agencyName, conCity, conSpot, lon, lat, conArtist);
//			System.out.println(log);
//
//
//			String insert = "INSERT IGNORE INTO concerts_test(ARTIST,CITY,SPOT,DAY,MONTH,YEAR,AGENCY,URL, LAT, LON) VALUES(?,?,?,?,?,?,?,?,?,?)";
//
//			pstm = connection.prepareStatement(insert);
//			pstm.setString(1, conArtist);
//			pstm.setString(2, conCity);
//			pstm.setString(3, conSpot);
//			pstm.setInt(4, conDay);
//			pstm.setInt(5, conMonth);
//			pstm.setInt(6, conYear);
//			pstm.setString(7, agencyName);
//			pstm.setString(8, conUrl);
//			pstm.setString(9, lon);
//			pstm.setString(10, lat);
//			pstm.executeUpdate();
//
//
//			}
			//ta ściana zakomentowanych liniej kodu jest na gitcie dd1a221bed23b8d162659e3e5b661605ed9b8f13

			//
			// String insert = "INSERT INTO Concerts(ARTIST,CITY,SPOT,DAY,MONTH,YEAR,AGENCY,URL)"
			// + "SELECT * FROM (SELECT"
			// + "? as ARTIST,"
			// + "? as CITY,"
			// +"? as SPOT,"
			// +"? as DAY,"
			// +"? as MONTH,"
			// +"? as YEAR,?,"
			// +"? as URL) AS tmp"
			// +"WHERE NOT EXISTS ("
			// +"SELECT ARTIST FROM Concerts WHERE ARTIST = ? AND CITY = ? AND SPOT = ? AND DAY=? AND MONTH = ? AND YEAR = ?"
			// +") LIMIT 1";
			//
			//
			// pstm = connection.prepareStatement(insert);
			// pstm.setString(1,conArtist);
			// pstm.setString(2, conCity);
			// pstm.setString(3, conSpot);
			// pstm.setInt(4, conDay);
			// pstm.setInt(5,conMonth);
			// pstm.setInt(6, conYear);
			// pstm.setString(7, agencyName);
			// pstm.setString(8, conUrl);
			// pstm.setString(9, conArtist);
			// pstm.setString(10, conCity);
			// pstm.setString(11, conSpot);
			// pstm.setInt(12, conDay);
			// pstm.setInt(13,conMonth);
			// pstm.setInt(14, conYear);
			//
			// pstm.executeUpdate();
			// pstm.close();
			//st.execute("INSERT INTO Concerts2(ARTIST,CITY,SPOT,DAY,MONTH,YEAR,AGENCY,URL)"+
			// st.execute("INSERT IGNORE INTO Concerts(ARTIST,CITY,SPOT,DAY,MONTH,YEAR,AGENCY,URL))
			/* "SELECT * FROM (SELECT"+
			"'"+conArtist+"'"+" as ARTIST,"+
			"'"+conCity+"'"+" as CITY,"+
			"'"+conSpot+"'"+"as SPOT,"+
			5 +"as DAY,"+
			5 +"as MONTH,"+
			2015+ "as YEAR,"+
			"'"+agencyName+"',"+
			"'"+conUrl+"'+" as URL) AS tmp"+
			"WHERE NOT EXISTS("+
			"SELECT ARTIST FROM Concerts2 WHERE ARTIST = '"+conArt+"' AND CITY = '"+conCity+"' AND SPOT = '"+conSpot+"' AND DAY="+conDay+"AND MONTH = "+conMonth+" AND YEAR = "+conYear+
			") LIMIT 1;");
			*/
		} catch (MysqlDataTruncation trnce) {
			sqlError("za długa kolumna " + conArtist);
		} catch (SQLException e) {
			sqlError("dunno");
			e.printStackTrace();
		} catch (IOException e) {
			sqlError("błąd z lon/lat");
		}

	}
	
	/**
	 * Metoda odpowiedzialna za wykonywania działań konkretnego workera
	 */
	public void process() {
		//metoda odpowiedzialna za wykonywanie zadan konkretnego workera
		// TODO Auto-generated method stub

		lastRun = System.currentTimeMillis();
		try {
			getData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean checkConditions() {
		long currentTime = System.currentTimeMillis();

		return (currentTime - lastRun) > 50 * 1000;
	}

	protected void parseError(String err) {
		System.err.println(agencyName + ": błąd parsowania (" + err + ")");
		errCount++;
	}

	private void sqlError(String err) {
		System.err.println(err);
		errCount++;
	}

	public int getErrCounter() {
		return errCount;
	}
	
}
