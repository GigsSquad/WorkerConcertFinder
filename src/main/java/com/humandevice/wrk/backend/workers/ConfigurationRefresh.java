package com.humandevice.wrk.backend.workers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.humandevice.wrk.backend.Configuration;


/**
 * Simply refreshes configuration. Every 5 minutes loads actual value from <i>config</i> DB table
 *  and set them in map of parameters in {@link Configuration} service.
 *
 */
public class ConfigurationRefresh extends Worker {
	long lastRun = 0;
	/**
	 * klasa zgrywa nowe dane do configuration, odswieza wyniki
	 */
	@Override
	public void process() {
		/*w klasie worker, wpisuje czas ostatniego uruchomienia do bazy*/
		workerActivityLogEntry("config");
		
		lastRun = System.currentTimeMillis();
		String sql = "SELECT * FROM config";
		
		Map<String, String> parameters = new HashMap<String, String>();

		try {
			Statement stm = connection.createStatement();
			/*w rs jest zapisana cala tablica config*/
			ResultSet rs = stm.executeQuery(sql);
			
			/*rs jest przepisaywany do parameters*/
			while(rs.next()){
				parameters.put(rs.getString("param"), rs.getString("value"));
			}
			/*wpisanie wynikow z parameters do config*/
			configuration.setParameters(parameters);

		} catch (SQLException e) {
			e.getStackTrace();
		}
		/*komunikat po skonczeniu*/
		Logger.getLogger(ConfigurationRefresh.class).info("Configuration refreshed with actual data from config [DB table]");
	}

	
	/**
	*sprawdza czy od ostatniego uzycia minelo 15 min
	*/
	@Override
	public boolean checkConditions() {
			long currentTimeInMinutes = System.currentTimeMillis();
			return (currentTimeInMinutes - lastRun) > 15*60*1000;
	}

	@Override
	public String toString() {
		return ConfigurationRefresh.class.getSimpleName();
	}
}
