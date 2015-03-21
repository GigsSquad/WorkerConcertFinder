package com.humandevice.wrk.backend.workers;

import com.humandevice.wrk.backend.others.MapMgr;
import com.humandevice.wrk.backend.others.Normalizer;
import com.humandevice.wrk.backend.pojos.LatLngPojo;
import com.mysql.jdbc.MysqlDataTruncation;
import com.mysql.jdbc.Statement;


import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ParseWorker extends Worker {

	protected long lastRun;
	protected PreparedStatement pstm = null;
	protected ResultSet resultSet = null;
	protected String agencyName;
	private static final String table = "gigs";

	/*
	 * Abstract class to get data from agencies
	 */

	public abstract void getData() throws IOException;

	public void addConcert(String conArtist, String conCity, String conSpot, int conDay, int conMonth, int conYear, String agencyName, String conUrl) {

		try {
			conSpot = Normalizer.normalizeSpot(conSpot);
			conCity = Normalizer.normalizeCity(conCity);

			LatLngPojo latlng = MapMgr.getCoordinates(conSpot, conCity);

			//pobieram
			String get = String.format("select * from %s" +
                    " join spots on gigs.id_spot = spots.id_spot" +
                    " join artists on gigs.id_artist = artists.id_artist "+
                    "WHERE artist = ? AND city = ? AND date=?", table);

			pstm = connection.prepareStatement(get);
			pstm.setString(1, conArtist);
			pstm.setString(2, conCity);
			pstm.setString(3, String.format("%d-%d-%d", conYear, conMonth, conDay));

          //  System.out.println()
			resultSet = pstm.executeQuery();
			String log;

			if (resultSet.next()) {
				log = String.format("ISTNIEJE %-4.4s %-12.12s %-20.20s %-18.18s %-18.18s %-40.40s", agencyName, conCity, conSpot, latlng.getLatitude(),
						latlng.getLongitude(), conArtist);
				System.out.println(log);

			} else {
				log = String.format("DODAJE %-4.4s %-12.12s %-20.20s %-18.18s %-18.18s %-40.40s", agencyName, conCity, conSpot, latlng.getLatitude(),
						latlng.getLongitude(), conArtist);
				System.out.println(log);
            ////////////////////nowa wersja

                //patrzymy czy jest artysta
                String getArt = "select id_artist from artists where artist=?";
                pstm = connection.prepareStatement(getArt);
                pstm.setString(1, conArtist);
                resultSet = pstm.executeQuery();

                int id_artist=0;
                if (!resultSet.next())// jezeli nie bylo artysty to go dodajemy
                {
                    String insertToArtTable = "INSERT INTO artists(artist) VALUES(?)";
                    pstm = connection.prepareStatement(insertToArtTable, Statement.RETURN_GENERATED_KEYS);
                    pstm.setString(1, conArtist);
                    pstm.executeUpdate();

                    ResultSet idSet = pstm.getGeneratedKeys(); //get last inserted id
                    idSet.next();
                    id_artist = idSet.getInt(1);
                    System.out.println("id_artist"+id_artist);
                } else {
                    id_artist = resultSet.getInt("id_artist");
                }




                //patrzymy czy jest lokalizacja
                String getSpot = "select id_spot from spots where city = ? and spot=? ";
                pstm = connection.prepareStatement(getSpot);
                pstm.setString(1,conCity);
                pstm.setString(2,conSpot);
                resultSet = pstm.executeQuery();
                int id_spot=0;

                if(!resultSet.next()) { // jezeli nie ma spot to dodajemy
                    String insertToArtTable = "INSERT IGNORE INTO spots(city,spot,lat,lon,country) VALUES(?,?,?,?,?)";
                    pstm = connection.prepareStatement(insertToArtTable,Statement.RETURN_GENERATED_KEYS);
                    pstm.setString(1, conCity);
                    pstm.setString(2, conSpot);
                    pstm.setString(3, latlng.getLatitude());
                    pstm.setString(4, latlng.getLongitude());
                    pstm.setString(5, "PL");
                   pstm.executeUpdate();

                        ResultSet idSet = pstm.getGeneratedKeys();//get last inserted id
                    idSet.next();
                    id_spot = idSet.getInt(1);
                            System.out.println("id_spot"+id_spot);
                }
                else {
                    id_spot = resultSet.getInt("id_spot");
                }
                // okej teraz wsadzamy wszystko razem

                String insertToGigs = String
                        .format("INSERT IGNORE INTO %s(id_artist, id_spot, date, agency, url) VALUES(?,?,DATE ?,?,?)", table);

                pstm = connection.prepareStatement(insertToGigs);
                pstm.setInt(1, id_artist);
                pstm.setInt(2, id_spot);
                pstm.setString(3, String.format("%d-%d-%d", conYear, conMonth, conDay));
                pstm.setString(4,agencyName);
                pstm.setString(5, conUrl);

                pstm.executeUpdate();

		}
		} catch (MysqlDataTruncation trnce) {
			sqlError("za długa kolumna " + conArtist);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
        catch (NullPointerException npe)
        {
            System.err.println("NULL POINTER"+conArtist); // to bedzie do wyjebania
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
			updateLatLng(false);
			getData();
		} catch (IOException | SQLException ioe) {
			ioe.printStackTrace();
		}

	}

	/**
	 * Bierze wszystkie koncerty pokolei które mają lat = 0 lub lon = 0 i próbuje do nich znaleźć poprawne współrzędne
	 *
	 * @param forceUpdate - aktualizuje wszystkie współrzędne z bazy, a nie tylko brakujące
	 * @throws SQLException
	 */
	public void updateLatLng(boolean forceUpdate) throws SQLException {
		String select = "SELECT * FROM spots";

		if (!forceUpdate) {
			select = String.format("%s WHERE lat = 0 OR lon = 0", select);
		}

		pstm = connection.prepareStatement(select);
		resultSet = pstm.executeQuery();

		while (resultSet.next()) {
			String update = "UPDATE spots SET lat = ?, lon = ? WHERE ord = ?";
			pstm = connection.prepareStatement(update);

			LatLngPojo latlon = MapMgr.getCoordinates(resultSet.getString(3), resultSet.getString(4));
			if (!latlon.isEmpty()) {
				pstm.setString(1, latlon.getLatitude());
				pstm.setString(2, latlon.getLongitude());
				pstm.setInt(3, resultSet.getInt(1));
				pstm.executeUpdate();
			}
			System.out.println(
					"UPDATE LatLng: " + resultSet.getString(3) + " " + resultSet.getString(4) + " - " + latlon.getLatitude() + " / " + latlon.getLongitude());
		}

	}

	public boolean checkConditions() {
		long currentTime = System.currentTimeMillis();

		return (currentTime - lastRun) > 900000; //15 minut
	}

	protected void parseError(String err) {
		System.err.println(agencyName + ": błąd parsowania (" + err + ")");
	}

	private void sqlError(String err) {
		System.err.println(err);
	}

}
