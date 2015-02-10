package com.humandevice.wrk.backend.workers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class LiveNation extends ParseWorker {

	private static final String URL_LIVE_NATION = new String("http://www.livenation.pl/event/allevents?page=1");

	public LiveNation() {
		super();
		agencyName = "LIVENATION";

	}

	public void getData() throws IOException {
		boolean end = false;
		String adres = URL_LIVE_NATION;
		Document doc;
		while (!end) {
			doc = Jsoup.connect(adres).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
					.referrer("http://www.google.com")
					.get();
			Elements concertData = doc.select("div.tbl_r");
			for (Element concert : concertData) {
				String url = "http://www.livenation.pl" + concert.select("div.eventName").select("a").attr("href");
				String artist = concert.select("div.eventName").text();

				String data = concert.select("div.dateFrom").text();
				String[] date = data.split(" ");
				int day = Integer.parseInt(date[0]);
				int month = 0;

				switch (date[1]) {
				case ("sty"): {
					month = 1;
					break;
				}
				case ("lut"): {
					month = 2;
					break;
				}
				case ("mar"): {
					month = 3;
					break;
				}
				case ("kwi"): {
					month = 4;
					break;
				}
				case ("maj"): {
					month = 5;
					break;
				}
				case ("cze"): {
					month = 6;
					break;
				}
				case ("lip"): {
					month = 7;
					break;
				}
				case ("sie"): {
					month = 8;
					break;
				}
				case ("wrz"): {
					month = 9;
					break;
				}
				case ("paź"): {
					month = 10;
					break;
				}
				case ("lis"): {
					month = 11;
					break;
				}
				case ("gru"): {
					month = 12;
					break;
				}
				}
				data = null;
				int year = Integer.parseInt(date[2]);
				String place = concert.getElementsByClass("venueName").text();
				String city = concert.getElementsByClass("venueCity").text();

				addConcert(artist, city, place, day, month, year, agencyName, url);
			}
			Elements next = doc.getElementsByClass("next");
			String conUrl = next.select("a[href]").attr("href");
			if (conUrl.equals("")) {
				end = true;
			} else {
				adres = "http://www.livenation.pl" + conUrl;
			}
		}
	}
}
