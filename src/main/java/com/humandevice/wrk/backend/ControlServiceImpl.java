package com.humandevice.wrk.backend;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.apache.log4j.Logger;

import com.humandevice.wrk.backend.workers.ArchiveLogs;
import com.humandevice.wrk.backend.workers.MailSender;
import com.humandevice.wrk.backend.workers.MassMail;
import com.humandevice.wrk.backend.workers.Worker;

/**
 * Implementation of ControlService. 
 *
 */
@WebService(endpointInterface = "com.humandevice.wrk.ControlService", serviceName = "ControlService")
public class ControlServiceImpl implements ControlService {
	
	Logger logger = Logger.getLogger(ControlServiceImpl.class);
	
	private List<Worker> workers;
	
	public ControlServiceImpl() {
	}
	
	public ControlServiceImpl(List<Worker> workers) {
		this.workers = workers;
	}

	public void shutdown() {

		for(Worker worker: workers) {
			
			worker.close();
			logger.info("Closing worker: " + worker.toString());

			try {
				synchronized (worker) {
					worker.notify();
				}
				logger.info("Notified worker [" + worker + "]");
			}
			catch (IllegalMonitorStateException e) {
				e.printStackTrace();
			}
		}
		
		logger.info("PortalWorker is going to exit...");
	}

	public void triggerHandling() {
		
		String workersToDisplay = "";
		
		for(Worker worker: workers) {
			try {
				synchronized (worker) {
					worker.notify();
				}
				workersToDisplay += (worker + ", ");
			}
			catch (IllegalMonitorStateException e) {
				e.printStackTrace();
			}
		}
		logger.info("Notified workers [" + workersToDisplay.substring(0, workersToDisplay.length()-2) + "]");
	}

	@WebMethod
	public void triggerMassMail() {
		for(Worker worker: workers) {
			if (worker instanceof MassMail) {
				try {
					synchronized (worker) {
						worker.notify();
					}
					logger.info("Notified mass mail adder (external)");
				}
				catch (IllegalMonitorStateException e) {
					e.printStackTrace();
				}
			}
		}
	}
	

	@WebMethod
	public void triggerArchiveLogs() {
		for(Worker worker: workers) {
			if (worker instanceof ArchiveLogs) {
				try {
					synchronized (worker) {
						worker.notify();
					}
					logger.info("Notified archive logs worker (external)");
				}
				catch (IllegalMonitorStateException e) {
					e.printStackTrace();
				}
			}
		}
	}
	

	@WebMethod
	public void triggerMailSender() {
		for(Worker worker: workers) {
			if (worker instanceof MailSender) {
				try {
					synchronized (worker) {
						worker.notify();
					}
					logger.info("Notified mail sender worker (external)");
				}
				catch (IllegalMonitorStateException e) {
					e.printStackTrace();
				}
			}
		}
	}
}