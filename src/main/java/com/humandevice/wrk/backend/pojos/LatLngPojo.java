package com.humandevice.wrk.backend.pojos;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Evelan on 27-02-2015 - 16:27
 */
public class LatLngPojo {

	@Setter @Getter String latitude;
	@Setter @Getter String longitude;

	public LatLngPojo(String latitude, String longitude) {
		setCoordinates(latitude, longitude);
	}

	public void setCoordinates(String latitude, String longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public boolean isEmpty()
	{
		return (longitude.equals("0") || latitude.equals("0"));
	}

}
