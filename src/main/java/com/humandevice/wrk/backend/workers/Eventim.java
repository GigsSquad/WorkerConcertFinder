package com.humandevice.wrk.backend.workers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Kuba on 28/02/2015.
 */
public class Eventim extends ParseWorker {

    private static String start = "http://www.eventim.pl/bilety.html?affiliate=PLE&doc=category&fun=kategorieliste&hkId=62&index=0&nurbuchbar=true&show=25&showFilter=yes&sort_by=name&sort_direction=asc";
    ArrayList<Document> bigUrls;
    int threadsWorking;

    public Eventim(){
        bigUrls = new ArrayList<Document>();
        threadsWorking = 0;
        String s = start;
        try {
            while(true) {
                Document doc = Jsoup.connect(s).get();
                bigUrls.add(doc);
                Element el = doc.getElementsByClass("next").first();
                if(el.getElementsByClass("arrowdisabled").size()!=0)
                    break;
                s = "http://www.eventim.pl/"+el.select("a").attr("href");
            }
        }catch (IOException e){
            bigUrls = null;
            e.printStackTrace();
        }
    }

    public void getData(){
        if(bigUrls!=null)
            for(Document doc : bigUrls) {
                new Thread(new Parser(doc)).start();
                threadsWorking++;
            }
        else System.out.println("bigUrls NULL");
    }

    private class Parser implements Runnable{

        Document doc;

        public Parser (Document doc){
            this.doc = doc;
        }

        public void run(){
            System.out.println(Thread.currentThread().getName()+" start");
            for(Element e : doc.getElementsByClass("taEvent")){
                try {
                    Document innerDoc = Jsoup.connect("http://www.eventim.pl/"+e.select("a").attr("href")+
                            "&includeOnlybookable=true").get();
                   // System.out.println("cur: "+innerDoc.location());
                    try {
                        for (Element el : innerDoc.select("tbody").first().select("tr")) {
                            String artist = el.getElementsByClass("col-event").first().select("span").text();
                            String city = el.getElementsByClass("col-location").first().getElementsByAttribute("itemprop")
                                    .first().attr("content");
                            String place = el.getElementsByClass("col-location").first().getElementsByAttribute("itemprop")
                                    .get(1).attr("content");
                            String [] date = el.getElementsByClass("col-date").first().select("meta").attr("content").split("-");
                            int d = Integer.valueOf(date[2]), m = Integer.valueOf(date[1]), y = Integer.valueOf(date[0]);
                            addConcert(artist,city,place,d,m,y,"EVENTIM",innerDoc.location());
                        }
                    }catch(NullPointerException npe){
                        System.out.println("npe: "+innerDoc.location());
                        npe.printStackTrace();
                    }
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
            System.out.println(Thread.currentThread().getName()+" end");
            threadsWorking--;
        }
    }

}
