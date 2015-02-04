package com.humandevice.wrk.backend;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface ControlService {

	@WebMethod
	public void shutdown();
	
	/* triggers interuption on every thread (using Thread.notifyAll) */
	@WebMethod
	public void triggerHandling();
	
	@WebMethod
	public void triggerMassMail();
	
	@WebMethod
	public void triggerArchiveLogs();
	
	@WebMethod
	public void triggerMailSender();
}
