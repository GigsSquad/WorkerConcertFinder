package com.humandevice.wrk.backend.pojos;

import lombok.Getter;
import lombok.Setter;

public class ConcertPojo {

	@Getter
	@Setter
	private int id;

	@Getter
	@Setter
	private String artist;

	@Getter
	@Setter
	private String city;

	@Getter
	@Setter
	private String spot;

	@Getter
	@Setter
	private int day;

	@Getter
	@Setter
	private int month;

	@Getter
	@Setter
	private int year;

	@Getter
	@Setter
	private String agency;

	@Getter
	@Setter
	private String url;

}
