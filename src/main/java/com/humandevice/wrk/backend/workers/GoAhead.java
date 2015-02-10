package com.humandevice.wrk.backend.workers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class GoAhead extends ParseWorker {

	private final static String URL_GOAHEAD = new String("http://www.go-ahead.pl/pl/koncerty.html");

	public GoAhead() {
		super();
		agencyName = "GOAHEAD";

	}

	@Override
	public boolean checkConditions() {
		long currentTime = System.currentTimeMillis();

		return (currentTime - lastRun) > 50 * 1000;
	}

	public void getData() throws IOException {
		Document doc = Jsoup.connect(URL_GOAHEAD).get();
		Elements concertData = doc.getElementsByClass("b_c");

		for (Element el : concertData) {
			String conUrl = el.attr("href");
			String conName = el.getElementsByClass("b_c_b").first().text();
			String conPlace = el.getElementsByClass("b_c_cp").first().text();
			String conDate = el.getElementsByClass("b_c_d").first().text();
			String conCity = conPlace.split(" ")[0];
			String conSpot = conPlace.substring(conPlace.indexOf(" ") + 1);// jesli jakims cudem nie ma spota to
			// wezmie nazwe miasta
			//Log.i("GOAHEAD", conSpot);
			int conDay = Integer.valueOf(conDate.split(" ")[0]);
			String[] months = { "st", "lu", "mar", "kw", "maj", "cz", "lip", "si", "wr", "pa", "lis", "gr" };
			int conMonth = 0;
			while (!conDate.split(" ")[1].startsWith(months[conMonth]))
				conMonth++;
			conMonth++;
			int conYear = Integer.valueOf(conDate.split(" ")[2]);
			addConcert(conName, conCity, conSpot, conDay, conMonth, conYear, agencyName, conUrl);
		}

	}
}
