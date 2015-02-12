package com.humandevice.wrk.backend.workers;

//import com.humandevice.wrk.backend.others.Normalizer;
import com.mysql.jdbc.MysqlDataTruncation;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import java.io.IOException;
import java.sql.*;

public abstract class ParseWorker extends Worker {

	protected long lastRun;
	protected static int counter = 0;
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
			counter++;

//			conArtist = getBeautifulString(conArtist);
//			conCity = getBeautifulString(conCity);
//			conSpot = getBeautifulString(conSpot);
//			
			PreparedStatement pstm = null;
			

			System.out.printf("%-13s%-10d%-18s%.60s\n", agencyName, counter, conCity, conArtist);

			//System.out.println(agencyName + "\t wpsiuje do bazy koncert: '" + conCity + "'");
//			st.execute("INSERT IGNORE INTO Concerts(ARTIST,CITY,SPOT,DAY,MONTH,YEAR,AGENCY,URL) VALUES('" + conArtist + "','" + conCity + "','" + conSpot + "'," + conDay + "," + conMonth + ","
//					+ conYear + ",'" + agencyName + "','" + conUrl + "')");
			
			String insert = "INSERT IGNORE INTO Concerts(ARTIST,CITY,SPOT,DAY,MONTH,YEAR,AGENCY,URL) VALUES(?,?,?,?,?,?,?,?)";
			
		
				pstm = connection.prepareStatement(insert);
				pstm.setString(1,conArtist);
				pstm.setString(2, conCity);
				pstm.setString(3, conSpot);
				pstm.setInt(4, conDay);
				pstm.setInt(5,conMonth);
				pstm.setInt(6, conYear);
				pstm.setString(7, agencyName);
				pstm.setString(8, conUrl);
				pstm.executeUpdate();
	
//				
//				String insert = "INSERT INTO  Concerts(ARTIST,CITY,SPOT,DAY,MONTH,YEAR,AGENCY,URL)"
//						+ "SELECT * FROM (SELECT"
//						+ "? as ARTIST,"
//						+ "? as CITY,"
//						+"? as SPOT,"
//						+"? as DAY,"
//						+"? as MONTH,"
//							+"? as YEAR,?,"
//							+"? as URL) AS tmp"
//						+"WHERE NOT EXISTS ("
//							+"SELECT ARTIST FROM Concerts WHERE ARTIST = ? AND CITY = ? AND SPOT = ? AND DAY=? AND MONTH = ? AND YEAR = ?" 
//							+") LIMIT 1";
//
//		
//				pstm = connection.prepareStatement(insert);
//				pstm.setString(1,conArtist);
//				pstm.setString(2, conCity);
//				pstm.setString(3, conSpot);
//				pstm.setInt(4, conDay);
//				pstm.setInt(5,conMonth);
//				pstm.setInt(6, conYear);
//				pstm.setString(7, agencyName);
//				pstm.setString(8, conUrl);
//				pstm.setString(9, conArtist);
//				pstm.setString(10, conCity);
//				pstm.setString(11, conSpot);
//				pstm.setInt(12, conDay);
//				pstm.setInt(13,conMonth);
//				pstm.setInt(14, conYear);
//				
//				pstm.executeUpdate();
//				pstm.close();
				
			//st.execute("INSERT INTO  Concerts2(ARTIST,CITY,SPOT,DAY,MONTH,YEAR,AGENCY,URL)"+
		//	st.execute("INSERT IGNORE INTO Concerts(ARTIST,CITY,SPOT,DAY,MONTH,YEAR,AGENCY,URL))
			/*	"SELECT * FROM (SELECT"+
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
			
			
			
		} catch (MySQLIntegrityConstraintViolationException intgre) {
			sqlError(counter + ": zdublowane id");
			counter++;
		} catch (MysqlDataTruncation trnce) {
			sqlError(counter + ": za długa kolumna " + conArtist);
		} catch (SQLException e) {
			sqlError(counter + ": dunno");
			e.printStackTrace();
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
