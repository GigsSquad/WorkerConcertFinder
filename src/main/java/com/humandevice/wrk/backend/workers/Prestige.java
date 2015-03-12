package com.humandevice.wrk.backend.workers;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Prestige extends ParseWorker {
	
	private String URL_PRESTIGE = "http://bilety.imprezyprestige.com/rezerwacja/termin.html";

	public Prestige()
	{
		super();
		agencyName = "PRESTIGE";
	}
	
	public void getData() throws IOException {
		Document doc = Jsoup.connect(URL_PRESTIGE).get();
		Elements concertData = doc.getElementsByClass("termin");
		String conUrl = "http://bilety.imprezyprestige.com/rezerwacja/termin.html";
		String conSpot = null;
		String conCity = null;
		for (Element el : concertData) {

			String conName = el.getElementsByClass("tytul").first().text();
			String conDate = el.getElementsByClass("data").first().text();
			// String conPlace = .first().text();
			String[] conPlace = el.getElementsByClass("obiekt").html()
					.split("<br>"); // place==city+spot
			//System.out.println(conPlace.length);
			
				try {
					conSpot = conPlace[0];
					conCity = conPlace[1];
				} catch (ArrayIndexOutOfBoundsException exc) {
					conCity = conPlace[0];
				}

		
			int conDay = Integer.valueOf(conDate.split(" ")[0].split("-")[0]);

			int conMonth = Integer.valueOf(conDate.split(" ")[0].split("-")[1]);

			int conYear = Integer.valueOf(conDate.split(" ")[0].split("-")[2]);

			// System.out.println(conDay+"\n"+ conMonth+"\n"+conYear+"\n");
			//System.out.println(conCity+"$$$"+conSpot);
			addConcert(conName, conCity, conSpot, conDay, conMonth, conYear, agencyName, conUrl);
			// agencyName, conUrl);
		}

	}

	public static void main(String[] args) {
		Prestige prest = new Prestige();
		try {
			prest.getData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
