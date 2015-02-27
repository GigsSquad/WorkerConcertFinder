package com.humandevice.wrk.backend.workers;

import com.humandevice.wrk.backend.others.MapMgr;
import com.humandevice.wrk.backend.others.Normalizer;
import com.humandevice.wrk.backend.pojos.LatLngPojo;
import com.mysql.jdbc.MysqlDataTruncation;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ParseWorker extends Worker {

	protected long lastRun;
	protected PreparedStatement pstm = null;
	protected ResultSet resultSet = null;
	protected String agencyName;
	private static final String table = "concerts_test";

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
			String get = String.format("SELECT * FROM %s WHERE artist = ? AND city = ? AND day = ? AND month = ? AND year = ?", table);
			pstm = connection.prepareStatement(get);
			pstm.setString(1, conArtist);
			pstm.setString(2, conCity);
			pstm.setInt(3, conDay);
			pstm.setInt(4, conMonth);
			pstm.setInt(5, conYear);
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

				String insert = String
						.format("INSERT IGNORE INTO %s(artist, city, spot, day, month, year, agency, url, lat, lon) VALUES(?,?,?,?,?,?,?,?,?,?)", table);

				pstm = connection.prepareStatement(insert);
				pstm.setString(1, conArtist);
				pstm.setString(2, conCity);
				pstm.setString(3, conSpot);
				pstm.setInt(4, conDay);
				pstm.setInt(5, conMonth);
				pstm.setInt(6, conYear);
				pstm.setString(7, agencyName);
				pstm.setString(8, conUrl);
				pstm.setString(9, latlng.getLatitude());
				pstm.setString(10, latlng.getLongitude());
				pstm.executeUpdate();

			}
		} catch (MysqlDataTruncation trnce) {
			sqlError("za długa kolumna " + conArtist);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
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
		String select = String.format("SELECT * FROM %s", table);

		if (!forceUpdate) {
			select = String.format("%s WHERE lat = 0 OR lon = 0", select);
		}

		pstm = connection.prepareStatement(select);
		resultSet = pstm.executeQuery();

		while (resultSet.next()) {
			String update = String.format("UPDATE %s SET lat = ?, lon = ? WHERE ord = ?", table);
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
