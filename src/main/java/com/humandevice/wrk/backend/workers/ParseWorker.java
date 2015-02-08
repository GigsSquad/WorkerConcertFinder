package com.humandevice.wrk.backend.workers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class ParseWorker extends Worker  {

	protected long lastRun;
	protected static int counter = 0;
	String url = "jdbc:mysql://hd4.human-device.com:3306/gigs";
	protected Connection conn;
	protected Statement st;
	protected PreparedStatement preparedStatement = null;
	protected ResultSet resultSet = null;
	protected String agencyName;
	/*
	 * Abstract class to get data from agencies

	 */
	
	public abstract void getData() throws IOException ;
	
	public ParseWorker()
	{
		try {
			 // This will load the MySQL driver, each DB has its own driver
		      Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url,"gigs","gigaFUN46534#");
			// Result set get the result of the SQL query
			st  = conn.createStatement();	
			
			
					
		}
		catch (SQLException sqlExc) {
			System.out.println("sqlException");
			sqlExc.printStackTrace();
			
		}catch (Exception exc){
			System.out.println("excetion");
			exc.printStackTrace();
		}
	}
	
	
	public void addConcert(String conArtist, String conCity, String conSpot, int conDay, int conMonth, int conYear,String agencyName,String conUrl)
	{
		  
		
			try {
				counter++;
				
					System.out.println(agencyName+"wpsiuje do bazy koncert:"+ conArtist);
					//st.execute("INSERT INTO Concerts VALUES(1,'art','city','spot',12,1,2014,'ticketPro','url')");
					st.execute("INSERT INTO Concerts VALUES("+counter+",'"+conArtist+"','"+conCity+"','"+conSpot+"',"+conDay+","+conMonth+","+conYear+",'"+agencyName+"','"+conUrl+"')");
			
				///	System.out.println("wpisalem do bazy");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
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
}
