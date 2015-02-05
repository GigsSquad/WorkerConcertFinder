package com.humandevice.wrk.backend;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.apache.log4j.Logger;

import com.humandevice.wrk.backend.workers.TicketPro;
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

	
	@WebMethod
	public void triggerTicketPro() {
		for(Worker worker: workers) {
			if (worker instanceof TicketPro) {
				try {
					synchronized (worker) {
						worker.notify();
					}
					logger.info("Notified TicketPro pozdrawiam");
				}
				catch (IllegalMonitorStateException e) {
					e.printStackTrace();
				}
			}
		}
	}
	

	
	

	
}