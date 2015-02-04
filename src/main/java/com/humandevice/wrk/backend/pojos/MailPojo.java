package com.humandevice.wrk.backend.pojos;

import lombok.Getter;
import lombok.Setter;

public class MailPojo {

	@Getter @Setter
	private long id;
	
	@Getter @Setter
	private String email;
	
	@Getter @Setter
	private String subject;
	
	@Getter @Setter
	private String contentPlain;
	
	@Getter @Setter
	private String contentHTML;
	
	@Getter @Setter
	private int mail_id;

		
}
