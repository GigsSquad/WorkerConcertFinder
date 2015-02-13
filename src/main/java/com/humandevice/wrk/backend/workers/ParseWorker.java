package com.humandevice.wrk.backend.workers;

import com.humandevice.wrk.backend.others.MapMgr;
import com.humandevice.wrk.backend.others.Normalizer;
import com.mysql.jdbc.MysqlDataTruncation;
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

			String[] lonlat = MapMgr.getCoordinates(conSpot, conCity, "");
			String lon = lonlat[0], lat = lonlat[1];

			//logi
			conCity = Normalizer.normalizeCity(conCity);
			String log = String.format("%-4.4s %-12.12s %-20.20s %-18.18s %-18.18s %-40.40s", agencyName, conCity, conSpot, lon, lat, conArtist);
			System.out.println(log);
//			logger.info(log);

			String insert = "INSERT IGNORE INTO Concerts(ARTIST,CITY,SPOT,DAY,MONTH,YEAR,AGENCY,URL, LAT, LON) VALUES(?,?,?,?,?,?,?,?,?,?)";

			pstm = connection.prepareStatement(insert);
			pstm.setString(1, conArtist);
			pstm.setString(2, conCity);
			pstm.setString(3, conSpot);
			pstm.setInt(4, conDay);
			pstm.setInt(5, conMonth);
			pstm.setInt(6, conYear);
			pstm.setString(7, agencyName);
			pstm.setString(8, conUrl);
			pstm.setString(9, lon);
			pstm.setString(10, lat);
			pstm.executeUpdate();

			//ta ściana zakomentowanych liniej kodu jest na gitcie dd1a221bed23b8d162659e3e5b661605ed9b8f13

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
