package com.humandevice.wrk.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


public class Configuration {

	private Map<String, String> parameters;
	private Connection connection;
	Logger logger = Logger.getLogger(Configuration.class);
	
	/**
	 * Return string value of given parameter from store.<br>
	 * Method is synchronized on whole object.
	 * 
	 * @param name
	 * @return
	 */
	
	public String getParameter(String name) {
		
		try {
			synchronized (this) {
				
				if(null != name && name.length() > 0) {
					return parameters.get(name);
				}
			}
		}
		catch (NullPointerException e ) {
			e.printStackTrace();
		}
		return null;
	}
	
	/** Sets given value for given parameter in {@link Configuration#parameters}
	 * and also in in DB config table - update if already exist or insert if not exist.<br>
	 * Method is synchronized on whole object.
	 * 
	 * @param name
	 * @param value
	 */
	public void setParameter(String name, String value) {
		
		if(parameters.containsKey(name)) {
			
			synchronized (this) {
				parameters.put(name, value);
			}
			String sqlUpdate = "UPDATE config SET value = ? WHERE param = ?";
			try {
				PreparedStatement pstm = connection.prepareStatement(sqlUpdate);
				pstm.setString(1, value);
				pstm.setString(2, name);
				pstm.executeUpdate();
				pstm.close();
				
				logger.info("Updated in DB->config: param=" + name + ", value=" + value);

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else {
			
			synchronized (this) {
				parameters.put(name, value);
			}
			
			String sqlInsert = "INSERT INTO config (value, param) VALUES (?, ?)";
			try {
				PreparedStatement pstm = connection.prepareStatement(sqlInsert);
				pstm.setString(1, value);
				pstm.setString(2, name);
				pstm.executeUpdate();
				pstm.close();
				
				logger.info("Inserted to DB->config: param=" + name + ", value=" + value);

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/** Return integer value of given parameter. 
	 * If exception will occurs, -1 will be returned.<br>
	 * Method is synchronized on whole object.
	 * 
	 * @param name
	 * @return
	 */
	public int getIntParameter(String name) {

		int value = -1;
		try {
			synchronized (this) {
				value = Integer.parseInt(getParameter(name));
			}
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	/**
	 * Sets all parameter in {@link Configuration#parameters} <br>
	 * Method is synchronized on whole object.
	 * 
	 * @param parameters
	 */
	public void setParameters(Map<String, String> parameters) {

		synchronized (this) {
			this.parameters = new HashMap<String, String>();
			this.parameters.putAll(parameters);
		}
	}
	
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
}
