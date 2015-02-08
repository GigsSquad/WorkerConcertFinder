package com.humandevice.wrk.backend;

import com.humandevice.wrk.backend.workers.AlterArt;
import com.humandevice.wrk.backend.workers.EBilet;
import com.humandevice.wrk.backend.workers.GoAhead;
import com.humandevice.wrk.backend.workers.LiveNation;
import com.humandevice.wrk.backend.workers.TicketPro;
import com.humandevice.wrk.backend.workers.Worker;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.xml.ws.Endpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class PortalWorker {

	private static List<Worker> workers;

	static Logger logger = Logger.getLogger(PortalWorker.class);

	/**
	 * Handle parameters using http://commons.apache.org/cli/usage.html, possible parameters: <br>
	 * -port=x - set port number for CXF (default 80)<br>
	 * -url=/control - set URL for CXF endpoint (default "/control")<br>
	 * -config=config.properties = set relative path to config file (default "configuration.properties")<br>
	 *
	 * @param args Command line arguments
	 * @throws org.apache.commons.cli.ParseException
	 */
	public static void main(String[] args) throws org.apache.commons.cli.ParseException, IOException {
		String config = "src/main/resources/configuration.properties";
		int port = 85;
		String url = "/control";
		

		// src/main/resources/configuration.properties mploy/dev/config/configuration.properties
		Options opt = new Options();
			
		opt.addOption("port", true, "Set port number for CXF");
		opt.addOption("url", true, "Set URL for CXF endpoint");
		opt.addOption("config", true, "Set relative path to config file");

		BasicParser parser = new BasicParser();
		CommandLine cl = parser.parse(opt, args);

		if (cl.hasOption("port")) {
			port = Integer.parseInt(cl.getOptionValue("port"));
			Logger.getLogger(PortalWorker.class).info("Specified port number for web service [parameter: port]: " + port);
		}
		if (cl.hasOption("url")) {
			url = cl.getOptionValue("url");
			Logger.getLogger(PortalWorker.class).info("Specified URL for veb service endpoint [parameter: url]: " + url);
		}
		if (cl.hasOption("config")) {
			config = cl.getOptionValue("config");
			Logger.getLogger(PortalWorker.class).info("Specified relative path to config file [parameter: config]: " + config);
		}

		try {

			File configFile = new File(config);

			if (null != configFile && configFile.canRead()) {

				new PortalWorker(configFile, url, port);
			} else {
				Logger.getLogger(PortalWorker.class).error("Cannot find or read file for given path: " + config
						+ ". Absolute path to file: " + configFile.getAbsolutePath());
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Logger.getLogger(PortalWorker.class).info("Worker stopped!");
	}

	/**
	 * @param configuration Pointer to a configuration file
	 * @param endpoint      URL for CXF SOAP endpoint for control interface
	 * @param port          Port number for web service, default 80
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public PortalWorker(File config, String endpoint, int port) throws IOException, ClassNotFoundException, SQLException {
		PortalWorker.workers = new ArrayList<Worker>();

		logger.info("Created PortalWorker with parameters [config=" + config.getPath() + ", url=" + endpoint + ", port=" + port + "]");

		int refreshInterval = 300;    // in seconds

		//create configuration service and populate with db data. Periodicaly refresh by ConfigurationRefresh
		
		InputStream configInputStream = new FileInputStream(config);
		Properties properties = new Properties();
		String driver, url, username, password;
		properties.load(configInputStream);
		configInputStream.close();

		driver = properties.getProperty("jdbc.driverClassName");
		if (driver != null) {
			Class.forName(driver);
		}

		url = properties.getProperty("jdbc.url");
		username = properties.getProperty("jdbc.username");
		password = properties.getProperty("jdbc.password");

		Connection connection = DriverManager.getConnection(url, username, password);

		connection.createStatement().execute("SET NAMES 'UTF8'");
		connection.createStatement().execute("DELETE FROM Concerts");////////
		Configuration configuration = new Configuration();
		Map<String, String> parameters = new HashMap<String, String>();

		//String selectConfigurationSQL = "SELECT * FROM config";
		//PreparedStatement statement = connection.prepareStatement(selectConfigurationSQL);
		//ResultSet resultSet = statement.executeQuery();

		//		while(resultSet.next()) {
		//			parameters.put(resultSet.getString("param"), resultSet.getString("value"));
		//
		//			logger.info("Added configuration parameter [" + resultSet.getString("param") + " = " + resultSet.getString("value") + "]");
		//		}

		configuration.setParameters(parameters);
		configuration.setConnection(connection);
		//		statement.close();
		// setting up timer to execute interrupt (Thread.notifyAll) every minute using QuartzScheduler

		JobDetail portalWrokerJob = JobBuilder.newJob(PortalWorkerJob.class)
				.withIdentity("PortalWorker")
				.build();

		Trigger trigger = TriggerBuilder.newTrigger()
				.withSchedule(
						SimpleScheduleBuilder.simpleSchedule()
								.withIntervalInSeconds(refreshInterval).repeatForever())     // repeat every  60 seconds
				.build();

		Scheduler scheduler = null;
		try {
			scheduler = new StdSchedulerFactory().getScheduler();

			scheduler.start();
			scheduler.scheduleJob(portalWrokerJob, trigger);
			logger.info("Scheduler set to wake up threads in every " + refreshInterval + " seconds");
		} catch (SchedulerException e1) {
			e1.printStackTrace();
		}

		// Creating the workers

		if ("1".equals(properties.getProperty("worker.AlterArt"))) {
			workers.add(new AlterArt());
		}
		if ("1".equals(properties.getProperty("worker.EBilet"))) {
			workers.add(new EBilet());
		}
		if ("1".equals(properties.getProperty("worker.GoAhead"))) {
			workers.add(new GoAhead());
		}
		if ("1".equals(properties.getProperty("worker.LiveNation"))) {
			workers.add(new LiveNation());
		}
		if ("1".equals(properties.getProperty("worker.TicketPro"))) {
			workers.add(new TicketPro());
		}

		List<Thread> workerThreads = new ArrayList<Thread>();

		for (Worker worker : workers) {
			worker.setConnection(connection);
			worker.setConfiguration(configuration);
			worker.init();

			Thread workerThread = new Thread(worker);
			workerThread.setName(worker.toString());
			workerThread.start();
			logger.info("Started worker - " + worker.toString());

			workerThreads.add(workerThread);
		}

		// creating shutdown hook (happend when killterm is being sent)
		createShutDownHook();

		if ("1".equals(properties.getProperty("endpoint.publish"))) {
			// publishing SOAP web service with interface pl.ardeo.youdash.ControlService (and implement it) under URL endpoint
			String wSAddress = getWebServiceAddress(endpoint, configuration.getParameter("web_service_host"), port);
			if (null == wSAddress) {
				connection.close();
				throw new NullPointerException("Web Service address cannot be null!");
			}
			ControlServiceImpl controlServiceImpl = new ControlServiceImpl(workers);
			Endpoint.publish(wSAddress, controlServiceImpl);
			logger.info("Web Service started successfuly on address: " + wSAddress);
		}

		logger.info("Portal worker started!");

		for (Thread wThread : workerThreads) {
			try {
				logger.info("Joined " + wThread.toString());
				wThread.join();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {
			if (null != scheduler) {
				scheduler.shutdown();
				logger.info("Scheduler shutdown...");
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}

		logger.info("Web Service shutdown...");
	}

	/**
	 * Prepares address for web service.<br><br>
	 * If host is null then it will be set to default value - <i>localhost</i>.<br>
	 *
	 * @param endpoint
	 * @param host
	 * @param port
	 * @return WebService address
	 */
	private String getWebServiceAddress(String endpoint, String host, int port) {

		try {
			if (null == endpoint) {
				throw new NullPointerException("Endpoint is null!");
			}
			if (port <= 0) {
				throw new IllegalArgumentException("Port value is incorrect! Port: " + port);
			} else {

				if (!endpoint.startsWith("/"))
					endpoint = "/" + endpoint;

				return "http://" + ((null != host && host.trim().length() > 0) ? host : "localhost") + ":" + port + endpoint;
			}
		} catch (NullPointerException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * @return the workers
	 */
	public static List<Worker> getWorkers() {
		return workers;
	}

	/**
	 * Shutdown hook. Invoke {@link ControlServiceImpl.shutdown()} in response to term signal
	 */
	private static void createShutDownHook() {

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			public void run() {
				for (Worker worker : workers) {
					if (!worker.isShuttingDown())
						worker.close();
				}
				logger.info("Worker container shutting down");
			}
		}));
	}
}
