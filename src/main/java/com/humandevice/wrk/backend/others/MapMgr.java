package com.humandevice.wrk.backend.others;

import com.humandevice.wrk.backend.pojos.LatLngPojo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by Kuba on 13/02/2015.
 * Map Manager
 */
public class MapMgr {

	private static String url_front = "http://nominatim.openstreetmap.org/search?q=";
	private static String format = "json";
	private static String email = null; // do kontaktu z nominatim jakby coś się zjebało, coooo?

	/**
	 * @param params parametry
	 * @return zwraca obiekt LatLngPojo ze współrzędnymi
	 */
	private static LatLngPojo getCoordinates(String params) {
		params = params.replace(" ", "+") + (email != null ? email : "");
		JSONObject jso;
		try {
			jso = getJSON(params).getJSONObject(0);
		} catch (Exception e) {
			return new LatLngPojo("0", "0");
		}
		return new LatLngPojo(jso.getString("lat"), jso.getString("lon"));
	}

	private static JSONArray getJSON(String params) throws IOException {
		Document doc = Jsoup.connect(url_front + params).ignoreContentType(true).get();
		String docContent = doc.toString().split("<body>")[1].split("</body>")[0];
		return docContent.equals("[]") ? null : new JSONArray(docContent);
	}

	/**
	 * @param city miasto
	 * @param spot klub
	 * @return zwraca obiekt LatLngPojo ze współrzędnymi
	 */
	public static LatLngPojo getCoordinates(String city, String spot) {
		String params = spot + ", " + Normalizer.grbgDel(city) + "&format=" + format;
		LatLngPojo coordinates = getCoordinates(params);

		if (coordinates.isEmpty())
			return getCityCoordinates(city);
		else
			return coordinates;
	}

	/**
	 * @param city miasto
	 * @return zwraca obiekt LatLngPojo ze współrzędnymi
	 */
	public static LatLngPojo getCityCoordinates(String city) {
		String params = Normalizer.grbgDel(city) + "&format=" + format;
		return getCoordinates(params);
	}
}
