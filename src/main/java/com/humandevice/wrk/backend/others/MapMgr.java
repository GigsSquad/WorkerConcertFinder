package com.humandevice.wrk.backend.others;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by Kuba on 13/02/2015.
 */
public class MapMgr {

    private static String url_front = "http://nominatim.openstreetmap.org/search?q=";
    private static String format = "json";
    private static String email = null; // do kontaktu z nominatim jakby coś się zjebało


    /*
    @ params: numer domu, ulica, miasto, kraj
    @ return: tablica ze współrzędnymi [String]
     */
    public static Double[] getCoordinatesByAddress(String houseNumber, String street, String city,String country) throws IOException {
        return getCoordinates(houseNumber+" "+street,city,country);
    }

    /*
    @ params: nazwa klubu, miasto, kraj
    @ return: tablica ze współrzędnymi [String]
 */
    public static Double[] getCoordinates(String club, String city,String country) throws IOException {
        String params = club+ ", " + Normalizer.grbgDel(city) +
                ", "+country+ "&format=" +format;
        params = params.replace(" ","+") + (email!= null? email : "");
        JSONObject jso;
        try {
            jso = getJSON(params).getJSONObject(0);
        }catch (Exception e){
            return new Double[]{0.0,0.0};
        }
        return new Double[]{Double.valueOf(jso.getString("lat")),Double.valueOf(jso.getString("lon"))};
    }

    private static JSONArray getJSON(String params) throws IOException {
        Document doc = Jsoup.connect(url_front+params).ignoreContentType(true).get();
        String docContent = doc.toString().split("<body>")[1].split("</body>")[0];
        //System.out.println("haloo: "+docContent);
        return docContent=="[]"? null : new JSONArray(docContent);
    }

    public static void main(String[] args) throws IOException {
        for(Double d : getCoordinates("Alibi","Wrocław","")) {
            System.out.println(d);
        }
    }

}
