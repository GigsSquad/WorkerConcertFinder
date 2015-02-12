package com.humandevice.wrk.backend.others;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Kuba on 10/02/2015.
 */
public class Normalizer {

    private static String [] PL = {"Warszawa","Kraków"};
    private static String [] EN = {"Warsaw","Cracow"};
    private static HashMap<String,String> dict = new HashMap<String,String>();
    private static HashMap<Character,Character> lettersPL = new HashMap<Character,Character>();
    private static HashMap<String,String> cities = new HashMap<String,String>();

    static{
        //wypełnianie słownika
        for(int i = 0; i<EN.length; i++){
            dict.put(EN[i],PL[i]);
        }
        //odpowiedniki liter
        lettersPL.put('ą','a');
        lettersPL.put('ć','c');
        lettersPL.put('ę','e');
        lettersPL.put('ł','l');
        lettersPL.put('ń','n');
        lettersPL.put('ó','o');
        lettersPL.put('ś','s');
        lettersPL.put('ż','z');
        lettersPL.put('ź','z');
        lettersPL.put('Ą','A');
        lettersPL.put('Ć','C');
        lettersPL.put('Ę','E');
        lettersPL.put('Ł','L');
        lettersPL.put('Ń','N');
        lettersPL.put('Ó','O');
        lettersPL.put('Ś','S');
        lettersPL.put('Ż','Z');
        lettersPL.put('Ź','Z');

        //słownik: polskie znaki - brak polskich znaków
        try {
            BufferedReader br = new BufferedReader(new FileReader("src/main/resources/miasta.txt"));
            String currentLine;
            while( (currentLine = br.readLine()) != null){
                currentLine = currentLine.split("\\(")[0].trim();
                cities.put(convertLetter(currentLine),currentLine);
            }
            br.close();
        }catch (FileNotFoundException e){
            System.out.println("Nie znaleziono pliku miasta.txt");
        }catch (IOException e){
            System.out.println("Uszkodzony plik miasta.txt");
        }


    }

    /*
    główna metoda -  zwraca miasto w unormowanej postaci
     */
    public static String normalizeCity(String s){
        String res = "";
        for(String part : grbgDel(s).split(" "))
            res+= normalizeCase(part)+" ";
        res = res.trim();
        if(dict.containsKey(res))
            return dict.get(res);
        if(cities.containsKey(res))
            return cities.get(res);
        return res;
    }

    /*
    ogarniacz czcionki
     */
    private static String normalizeCase(String s){
        return (s.charAt(0)+"").toUpperCase()+s.substring(1).toLowerCase();
    }

    /*
    wywala wszystkie kropki, przecinki i inny syf
     */
    private static String grbgDel(String s){
        StringBuilder res = new StringBuilder(s.trim().replace("  "," "));
        for(int i=0; i<res.length();i++){
            char c = res.charAt(i);
            System.out.println("sprawdzam: "+c);
            if(c>=33 && c<=47 || c>=58 && c<=64) {
                System.out.println("wywalam: "+c);
                res.replace(i, i + 1, "");
                i-=1;
            }
        }
        return res.toString();
    }

    /*
    Zamienia polskie znaki na "niepolskie"
     */
    private static String convertLetter(String s){
        StringBuilder res = new StringBuilder();
        for(int i = 0; i<s.length(); i++) {
            char c = s.charAt(i);
            res.append(lettersPL.containsKey(c) ? lettersPL.get(c) : c);
        }
        return res.toString();
    }

    public static void main (String[] args){
       System.out.println(normalizeCity("WARSAW../,./,/?"));
    }
}
