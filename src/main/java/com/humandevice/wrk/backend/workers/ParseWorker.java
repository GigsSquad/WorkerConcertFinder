package com.humandevice.wrk.backend.workers;

import com.humandevice.wrk.backend.others.MapMgr;
import com.humandevice.wrk.backend.others.Normalizer;
import com.mysql.jdbc.MysqlDataTruncation;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import java.io.IOException;
import java.sql.*;

public abstract class ParseWorker extends Worker {

	protected long lastRun;
	protected static int counter = 0;
	String url = "jdbc:mysql://hd4.human-device.com:3306/gigs";
	protected Connection conn;
	protected Statement st;
	protected PreparedStatement preparedStatement = null;
	protected ResultSet resultSet = null;
	protected String agencyName;
	public static int errCount = 0;
	/*
	 * Abstract class to get data from agencies

	 */

	public abstract void getData() throws IOException;

	public ParseWorker() {
		try {
			// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, "gigs", "gigaFUN46534#");
			// Result set get the result of the SQL query
			st = conn.createStatement();

		} catch (SQLException sqlExc) {
			System.out.println("sqlException");
			sqlExc.printStackTrace();

		} catch (Exception exc) {
			System.out.println("excetion");
			exc.printStackTrace();
		}
	}

	public void addConcert(String conArtist, String conCity, String conSpot, int conDay, int conMonth, int conYear, String agencyName, String conUrl) {

		try {
			counter++;

			conArtist = conArtist.trim().replace("  "," ");
			conCity = Normalizer.normalizeCity(conCity);
			conSpot = conSpot.trim().replace("  "," ");

			System.out.printf("%-13s%-10d%-18s%.60s\n", agencyName, counter, conCity, conArtist);

            String[] lonlat = MapMgr.getCoordinates(conSpot,conCity,"");
            String lon = lonlat[0], lat = lonlat[1];

			//System.out.println(agencyName + "\t wpsiuje do bazy koncert: '" + conCity + "'");
			st.execute("INSERT INTO Concerts VALUES(" + counter + ",'" + conArtist + "','" + conCity + "','" + conSpot + "'," + conDay + "," + conMonth + ","
					+ conYear + ",'" + agencyName + "','" + conUrl + "','" + lat +"','" + lon + "')");
		} catch (MySQLIntegrityConstraintViolationException intgre) {
			sqlError(counter + ": zdublowane id");
			counter++;
		} catch (MysqlDataTruncation trnce) {
			sqlError(counter + ": za długa kolumna " + conArtist);
		} catch (SQLException e) {
			sqlError(counter + ": dunno");
			e.printStackTrace();
		}catch (IOException e){
            sqlError(counter + ": błąd z lon/lat");
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
