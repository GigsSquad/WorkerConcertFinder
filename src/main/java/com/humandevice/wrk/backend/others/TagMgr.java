package com.humandevice.wrk.backend.others;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

/**
 * Created by Kuba on 25/03/2015.
 */
public class TagMgr {

    private static final String lfmUrl = "http://www.lastfm.pl/music/";
    private static final boolean ASC = true;
    private static final boolean DESC = true;

    /*
        @howMany - zaleca się max 5, powyżej trzeba już realizować odrobinę bardziej złożony kod
     */
    public static String[] getTags(String artist, int howMany) throws IOException {
        if(howMany>5) {
            Map<String, Integer> tm = getTags(artist); // zwraca posrtowaną mapę wszystkich tagów
            String [] tags = new String[tm.keySet().size()];
            tm.keySet().toArray(tags);
            String [] res = new String[howMany];
            for(int i = 0; i<howMany; i++) {
                res[i] = tags[i];
            }
            return res;
        }
        String [] res = new String[howMany];
        artist = artist.trim().replaceAll(" ","+");
        final String url = lfmUrl+artist;
        Document doc = Jsoup.connect(url).get();
        Elements tagElems = doc.getElementsByClass("tags").first().getElementsByAttributeValue("rel", "tag");
        for(int  i = 0; i < howMany; i++)
            res[i] = tagElems.get(i).text();
        return res;
    }

    // nie wywoływać poza metodą wyżej!
    // zwraca tagi od najbardziej do najmniej znaczącego ("tag-weight" na laście)
    private static Map<String, Integer> getTags(String artist) throws IOException{
        artist = artist.trim().replaceAll(" ","+");
        final String url = lfmUrl+artist+"/+tags";
        Document doc = Jsoup.connect(url).get();

        TreeMap<String,Integer> tagsAndWeights = new TreeMap<String,Integer>();
        Elements tagElems = doc.getElementsByClass("tags--weighted").first().getElementsByAttributeValue("rel", "tag");

        for(Element el : tagElems){
            int weight = Integer.valueOf(el.attr("class").replace("tags-weight-", ""));
            String tag = el.text();
         // System.out.println("tag: "+tag+",wieght: "+weight);
            tagsAndWeights.put(tag,weight);
        }

        return sortByComparator(tagsAndWeights,false);
    }

    public static void main(String[] args) throws IOException {
        for(String s : getTags("Slayer",7)){
            System.out.println(s);
        }
    }

    //sortuje mapy po values, true - rosnąco, false - malejąco
    private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean order)
    {

        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>()
        {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

}
