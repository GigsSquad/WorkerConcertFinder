package com.humandevice.wrk.backend.workers;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Kayax extends ParseWorker {

//	private String URL_KAYAX = "http://kayax.net/kalendarz/0";
	String agencyName;

	public Kayax() {
		super();
		String agencyName = "KAYAX";
	}

	public void getData() throws IOException {
		Elements rows;
		int counter = -1;

		do {
			String urlKayax = "http://kayax.net/kalendarz/";
			counter++;
			//System.out.println("Counter"+counter);
			urlKayax=urlKayax+counter+"/";
			//System.out.println(urlKayax);
			Document doc = Jsoup.connect(urlKayax).timeout(10000).get();

			Element table = doc.getElementsByTag("table").get(0);
			rows = table.select("tr");

			String conName = "", conCity = "", conSpot = "";
			for (int i = 2; i < rows.size(); i++) {
				Element row = rows.get(i);
				String conArtistCity = row.getElementsByAttribute("style").first().text();
				// System.out.println(conCitySpotPlace);//awww yeeeh grzebanie w
				// gównie po pachy zabawa wolololo pozdrawiam

				conCity = "";
				conSpot = "";
				String[] conArtistCityArray = conArtistCity.split("-");
				//System.out.println(conArtistCityArray.length);
				if (conArtistCityArray.length == 3) {  // jak sa oddzielone myslnikami i są wszyskie 3
					conName = conArtistCityArray[0];
					conCity = conArtistCityArray[1];
					conSpot = conArtistCityArray[2];
				}
				
				if (conArtistCityArray.length == 2) // czasem nie ma klubu podanego
				{
					
					if(conArtistCityArray[0].contains(",")){// to jest Hey,łódź np.
						String[] conNameCity = conArtistCityArray[0].split(",");
						conName = conNameCity[0];
						conCity = conNameCity[1];
						conSpot = conArtistCityArray[1];
					}
					else if(!conArtistCityArray[1].toLowerCase().contains( //i czasem jest impreza zamknieta
								"impreza"))
					{
					conName = conArtistCityArray[0];
					conCity = conArtistCityArray[1];
					conSpot = "";
					
					}
					//System.out.println(conName );
						 
				}

			
				String conDate = row.getElementsByClass("event_date").text()
						.split(" ")[0];

				int conYear = Integer.valueOf(conDate.split("-")[0]);
				int conMonth = Integer.valueOf(conDate.split("-")[1]);
				int conDay = Integer.valueOf(conDate.split("-")[2]);
				
				
				String conUrl =row.getElementsByTag("a").first().attr("href");
				conUrl="http://kayax.net"+conUrl;
				// System.out.printf(conYear+"   "+conMonth+"   "+conDay+"\n");
				addConcert(conName, conCity, conSpot, conDay, conMonth, conYear, agencyName, conUrl);

			}
			
			// agencyName, conUrl);
		
		} while (rows.size() > 2);

	}

	public static void main(String[] args) {
		Kayax ky = new Kayax();
		try {
			ky.getData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
