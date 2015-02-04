package com.humandevice.wrk.backend;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.humandevice.wrk.backend.workers.Worker;


/**
 * Executes interrupt every minute. 
 * @author Michal
 *
 */
public class PortalWorkerJob implements Job {
	
	Logger logger = Logger.getLogger(PortalWorkerJob.class);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		List<Worker> workers = PortalWorker.getWorkers();
		StringBuilder workersToDisplay = new StringBuilder();

		for(Worker worker: workers) {
			try{
				synchronized (worker) {
					worker.notify();
				}
				if (workersToDisplay.length()>0) {
					workersToDisplay.append("\", \"");
				}
				workersToDisplay.append(worker);
			}
			catch (IllegalMonitorStateException e) {
				logger.error("Error during execution of notifyAll!");
				e.printStackTrace();
			}
		}
		logger.info("Time: " + sdf.format(new Date()));
		logger.info("Notified workers [\"" + ((workersToDisplay.length() > 0) ? workersToDisplay.toString() : "no workers") + "\"]");
		
	}
}
