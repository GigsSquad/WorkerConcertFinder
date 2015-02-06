package com.humandevice.wrk.backend.workers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.*;

public class TicketPro extends ParseWorker {
	

	public TicketPro() {
		super();
		agencyName = "TICKETPRO";
		
	}

	public void getData() throws IOException {

		String urlParse = "http://www.ticketpro.pl/jnp/muzyka/index.html?page=1";
		String urlParseName = ""; // potrzebne do parsowania podstron
		String conCity, conSpot;
		int conDay, conMonth, conYear;
		do {
			Document doc = Jsoup.connect(urlParse)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0").timeout(100000).get();
			Elements concertData = doc.getElementsByClass("eventInfo");

			// dbm.updateHash("TicketPro", currentHash);

			for (Element el : concertData) {
				String conName = el.getElementsByTag("a").first().text();

				String conUrl = el.getElementsByTag("a").first().attr("href");
				conUrl = "http://www.ticketpro.pl" + conUrl;

				String location = el.getElementsByClass("fn").text(); // City + spot
				if (!location.equals("")) // nie ma lokalizacji na gĹ‚Ăłwnej stronie->odwiedzamy szczegĂłĹ‚y
				{

					try { // jezeli coĹ› jest zapisane niestandardowo to omijamy
						conCity = location.split(",")[1];
						conSpot = location.split(",")[0];

						String conDate = el.getElementsByClass("dtstart").first().text();

						String[] conDateArray = conDate.split("\\.");

						conDay = Integer.parseInt(conDateArray[0]);
						conMonth = Integer.parseInt(conDateArray[1]);
						conYear = Integer.parseInt(conDateArray[2]);
					} catch (ArrayIndexOutOfBoundsException e) {
						System.err.println("Błąd parsowania");
						continue;
					}

					// System.out.printf("%s %s %s  %d  %d  %d %s %s \n",conName, conCity, conSpot, conDay, conMonth,
					// conYear, "TicketPro", conUrl);

					addConcert(conName, conCity, conSpot, conDay, conMonth, conYear, agencyName,conUrl);
				} else
					// jest wiecej niz jeden koncert
					// System.out.println(conName);
					getOtherLocalisation(conName, conUrl);
			}

			try {
				urlParse = doc.getElementsByClass("normal").last().attr("href");
				urlParse = "http://www.ticketpro.pl" + urlParse;
				urlParseName = doc.getElementsByClass("normal").last().text();
			} catch (Exception e) {
				break;
			}

		} while (urlParseName.equals("Następny"));
	}

	private void getOtherLocalisation(String conName, String detailInfo) throws IOException {
		Document doc = Jsoup.connect(detailInfo).timeout(1000000).get();
		Elements concertData = doc.getElementsByClass("info");

		for (Element el : concertData) {

			// url do szczegolow koncertu
			String conUrl = el.getElementsByTag("a").first().attr("href");
			conUrl = "http://www.ticketpro.pl" + conUrl;

			String conDate = el.getElementsByClass("date").first().text();

			if (conDate.split(" - ").length <= 1) //
			{

				try {
					String[] conDateArray = conDate.split("\\.");

					int conDay = Integer.parseInt(conDateArray[0]);
					int conMonth = Integer.parseInt(conDateArray[1]);
					int conYear = Integer.parseInt(conDateArray[2]);

					String conLocation = el.getElementsByTag("p").first().text();
					String[] conLocationArray = conLocation.split(",");
					String conSpot = conLocationArray[0];
					String conCity = conLocationArray[1];
					// System.out.printf("%s %s %s  %d  %d  %d %s %s \n",conName, conCity, conSpot, conDay, conMonth,
					// conYear, "TicketPro", conUrl);
					addConcert(conName, conCity, conSpot, conDay, conMonth, conYear, agencyName, conUrl);

				} catch (ArrayIndexOutOfBoundsException e) {
					System.err.println("parsing error");
					break;
				}
			}
		}
	}
}