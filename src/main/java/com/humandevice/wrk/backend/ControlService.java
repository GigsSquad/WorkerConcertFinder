package com.humandevice.wrk.backend;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface ControlService {

	@WebMethod
	public void shutdown();
	
	@WebMethod
	public void triggerTicketPro();
	
	
	
}
