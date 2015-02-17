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
//    public static String[] getCoordinatesByAddress(String houseNumber, String street, String city,String country{
//        return getCoordinates(houseNumber+" "+street,city,country);
//    }


    private static String[] getCoordinates(String params){
        params = params.replace(" ","+") + (email!= null? email : "");
        JSONObject jso;
        try {
            jso = getJSON(params).getJSONObject(0);
        }catch (Exception e){
            return new String[]{"not found","not found"};
        }
        return new String[]{jso.getString("lat"),jso.getString("lon")};
    }

    /*
    @ params: nazwa klubu (od biedy(!) adres w jednym stringu), miasto, kraj
    @ return: tablica ze współrzędnymi [String]
    */
    public static String[] getCoordinates(String club, String city,String country){
        String params = club+ ", " + Normalizer.grbgDel(city) + ", "+country+ "&format=" +format;
        return getCoordinates(params);
    }
    //bez kraju
    public static String[] getCoordinates(String place,String city){
        String params = place+ ", " + Normalizer.grbgDel(city) + "&format=" +format;
        return getCoordinates(params);
    }
    //
    public static String[] getCityCoordinates(String city){
        String params = Normalizer.grbgDel(city) + "&format=" +format;
        return getCoordinates(params);
    }

    private static JSONArray getJSON(String params) throws IOException {
        Document doc = Jsoup.connect(url_front+params).ignoreContentType(true).get();
        String docContent = doc.toString().split("<body>")[1].split("</body>")[0];
        return docContent=="[]"? null : new JSONArray(docContent);
    }

    public static void main(String[] args) throws IOException {
        for(String s : getCityCoordinates("Wrocław"))
            System.out.println(s);
    }

}
