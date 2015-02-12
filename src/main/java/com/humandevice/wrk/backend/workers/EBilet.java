package com.humandevice.wrk.backend.workers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;

public class EBilet extends ParseWorker {

	private static final String URL_EBILET = new String("http://www.ebilet.pl/kat.php?ndzial=koncerty");

	public EBilet() {
		super();
		agencyName = "EBILET";
	}

	public void getData() throws IOException {
		String conArtist;
		String city;
		String club;
		int day = 0;
		int month = 0;
		int year = 0;
		String conUrl = null;
		Document doc;

		HashSet<String> sources = new HashSet<String>();
		doc = Jsoup.connect(URL_EBILET).get();

		Elements concertData = doc.body().getElementsByClass("act_act");

		for (Element el : concertData) {
			conUrl = ("http://ebilet.pl/" + el.select("a[href]").attr("href"));
			sources.add(conUrl);
		}
		for (String url : sources) {
			Document poddoc = Jsoup.connect(url).get();
			Element midcolumn = poddoc.body().getElementById("midcolumn");
			if (midcolumn != null) {
				conArtist = midcolumn.getElementsByClass("wTitle").text();
				if (!conArtist.equals("")) {
					Elements cdate = midcolumn.select("table[width=100%][cellpadding=0][cellspacing=0][border=0]");//.text().substring(0,10);
					Elements cplace = midcolumn.select("td[width][colspan][valign][align]");
					for (int i = 0; i < cdate.size() && i < cplace.size(); i++) {

						String conDate = null;
						try {
							conDate = cdate.get(i).text().substring(0, 10);

							if (conDate.charAt(0) == '2') {
								String[] data = conDate.split("-");
								year = Integer.parseInt(data[0]);
								month = Integer.parseInt(data[1]);
								day = Integer.parseInt(data[2]);
							}

						} catch (StringIndexOutOfBoundsException e) {
							parseError("conDate: " + conDate);
						} catch (NullPointerException npe) {
							parseError("conDate = null");
							//TODO coś z tym trzeba zrobić, bo nie może być tak że nie ma daty albo wpisuje nie wiadomo co ;)
						}

						String kod = cplace.get(i).html().split("<br>")[0].toString();
						String[] cl = kod.split(",");
						String[] ci = kod.split(" ");
						club = cl[0];
						city = ci[ci.length - 1];
						addConcert(conArtist, city, club, day, month, year, "EBILET", conUrl);

					}
				}
			}
			poddoc = null;
		}
	}

}
