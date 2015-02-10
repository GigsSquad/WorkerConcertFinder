package com.humandevice.wrk.backend.workers;

import com.humandevice.wrk.backend.Configuration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public abstract class Worker implements Runnable {
	
	/**
	 * Inicjalizacja zmiennych czyWylaczonny, poleczenia i konfiguracji
	 * czasu ostatniego uruchomienia
	 */
	protected boolean shutDown = false;
	protected Connection connection;
	protected Connection connection2;
	protected Configuration configuration;
	
	public void setConnection(Connection connection) { //utawiania polaczenie
		this.connection = connection;
	}
	
	public void setConfiguration(Configuration configuration) { //ustawia konfiguracje
		this.configuration = configuration;
	}

	public void close() { //konczy dzialania workera
		shutDown = true;
	}
	
	public void init() { //jak nizej nie robi nic - potrzebne do zainicjowania!
		//do nothing
	}
	
	/**
	 * Metoda odpowiedzialna za wykonywania działań konkretnego workera
	 */
	public abstract void process();
	/**
	 * Metoda odpowiedzialna za sprawdzenie warunków czy worker ma zadziałać
	 */
	public abstract boolean checkConditions();
	
	/**
	 * aktulizuje w bazie danych, date ostatniego uruchomienia w tablicy 'worker'
	 * @param worker
	 */
	public void workerActivityLogEntry(String worker) {
		try {
			String logs = "UPDATE worker_logs SET timestamp = ? WHERE worker = ?";
			PreparedStatement log = connection
					.prepareStatement(logs);
			Date date = new java.util.Date();
			log.setTimestamp(1, new Timestamp(date.getTime()));
			log.setString(2, worker);
			log.executeUpdate();
			log.close();
		} catch (SQLException e) {
			//Logger.getLogger(ConfigurationRefresh.class)
			//		.error("Warning - possible problem with " + worker + " worker detected");
			
		}
	}
	
	public boolean isShuttingDown() { //czy zostal wylaczony
		return shutDown;
	}
	
	/**
	 * Odpowiada za cykliczne uruchamianie workerow
	 */
	public final void run() {
				
		while (!isShuttingDown()) {
			while(!isShuttingDown() && !checkConditions()) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {}
			}
			process();
		}
	}
	
	@Override
	public String toString() {
		
		return Worker.class.getSimpleName();
	}
}
