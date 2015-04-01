package com.humandevice.wrk.backend.workers;

import com.humandevice.wrk.backend.others.MapMgr;
import com.humandevice.wrk.backend.others.Normalizer;
import com.humandevice.wrk.backend.pojos.LatLngPojo;
import com.mysql.jdbc.MysqlDataTruncation;
import com.mysql.jdbc.Statement;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ParseWorker extends Worker {

    protected long lastRun;
    protected PreparedStatement pstm = null;
    protected ResultSet resultSet = null;
    protected String agencyName;
    private static final String table = "gigs";

	/*
     * Abstract class to get data from agencies
	 */

    public abstract void getData() throws IOException;

    public void addConcert(String conArtist, String conCity, String conSpot, int conDay, int conMonth, int conYear, String agencyName, String conUrl) {

        try {
            conSpot = Normalizer.normalizeSpot(conSpot);
            conCity = Normalizer.normalizeCity(conCity);


            //-1. bierzemy wszystkie koncerty
            String getConcertsQuery = String.format(
                    "SELECT * FROM %s" +
                            " JOIN spots ON gigs.id_spot = spots.id_spot" +
                            " JOIN artists ON gigs.id_artist = artists.id_artist" +
                            " WHERE artist = ? AND city = ? AND date=?", table);

            pstm = connection.prepareStatement(getConcertsQuery);
            pstm.setString(1, conArtist);
            pstm.setString(2, conCity);
            pstm.setString(3, String.format("%d-%d-%d", conYear, conMonth, conDay));
            resultSet = pstm.executeQuery();
            String log;

            //0. jeśli istnieje to kończymy
            if (resultSet.next()) {
                log = String.format("ISTNIEJE %-4.4s %-12.12s %-20.20s %-40.40s", agencyName, conCity, conSpot, conArtist);
                System.out.println(log);
                return;
            }

            log = String.format("DODAJE %-4.4s %-12.12s %-20.20s %-40.40s", agencyName, conCity, conSpot, conArtist);
            System.out.println(log);

            //1. patrzymy czy jest artysta
            String getArtistsQuery = "SELECT id_artist FROM artists WHERE artist = ?";
            pstm = connection.prepareStatement(getArtistsQuery);
            pstm.setString(1, conArtist);
            resultSet = pstm.executeQuery();

            int id_artist = 0;
            if (!resultSet.next()) { // jezeli nie bylo artysty to go dodajemy
                String insertArtistQuery = "INSERT INTO artists(artist) VALUES(?)";
                pstm = connection.prepareStatement(insertArtistQuery, Statement.RETURN_GENERATED_KEYS);
                pstm.setString(1, conArtist);
                pstm.executeUpdate();

                ResultSet idSet = pstm.getGeneratedKeys(); //get last inserted id
                idSet.next();
                id_artist = idSet.getInt(1);
            } else { //jeśli był to pobieramy jego id z tableli artists
                id_artist = resultSet.getInt("id_artist");
            }

            //2. patrzymy czy jest konkretna (city + spot) lokalizacja
            String getSpotQuery = "SELECT id_spot FROM spots WHERE city = ? AND spot = ?";
            pstm = connection.prepareStatement(getSpotQuery);
            pstm.setString(1, conCity);
            pstm.setString(2, conSpot);
            resultSet = pstm.executeQuery();

            int id_spot = 0;
            if (!resultSet.next()) { // jezeli nie ma spot to dodajemy

                String tmpCity = java.text.Normalizer.normalize(conCity, java.text.Normalizer.Form.NFD);
                LatLngPojo latlng = MapMgr.getCityCoordinates(tmpCity); // szukamy latlng miasta


                /* TUTAJ RZEŹBILEM  */
//
//                //teraz szukamy samego miasta w bazie, ot tak żeby zajebać bazę i zajebać sie w ifach, pozdrawiam Wasz Jakub
//                String getCityQuery = "SELECT id_spot FROM spots WHERE city = ? OR lat = 0 OR lon = 0";
//                pstm = connection.prepareStatement(getSpotQuery);
//                pstm.setString(1, conCity);
//                resultSet = pstm.executeQuery();
//                int id_city = 0;
//                if(!resultSet.next()) // nie ma takiego miasta więc szukamy latlng
//                {
//                    LatLngPojo latlng = MapMgr.getCityCoordinates(conCity); // szukamy latlng miasta
//
//                    ResultSet idSet = pstm.getGeneratedKeys();//get last inserted id
//                    idSet.next();
//                    id_city = idSet.getInt(1);
//                } else { //jeśli jest to pobieramy id
//                    id_city = resultSet.getInt("id_spot");
//                }

                String insertSpotQuery = "INSERT IGNORE INTO spots(city, spot, lat, lon, country) VALUES(?, ?, ?, ?, ?)";
                pstm = connection.prepareStatement(insertSpotQuery, Statement.RETURN_GENERATED_KEYS);
                pstm.setString(1, conCity);
                pstm.setString(2, conSpot);
                pstm.setString(3, latlng.getLatitude());
                pstm.setString(4, latlng.getLongitude());
                pstm.setString(5, "PL"); //TODO HEHEHEHEHEHEH
                pstm.executeUpdate();

                ResultSet idSet = pstm.getGeneratedKeys();//get last inserted id
                idSet.next();
                id_spot = idSet.getInt(1);
            } else { //jeśli jest to pobieramy id
                id_spot = resultSet.getInt("id_spot");
            }


            //3. insertujemy wszystko razem (plusik za profejonalne slownictwo)
            String insertToGigs = String.format("INSERT IGNORE INTO %s (id_artist, id_spot, date, agency, url, updated) VALUES(?, ?, DATE ?, ?, ?, now())", table);
            pstm = connection.prepareStatement(insertToGigs);
            pstm.setInt(1, id_artist);
            pstm.setInt(2, id_spot);
            pstm.setString(3, String.format("%d-%d-%d", conYear, conMonth, conDay));
            pstm.setString(4, agencyName);
            pstm.setString(5, conUrl);
            pstm.executeUpdate();

        } catch (MysqlDataTruncation trnce) {
            sqlError("za długa kolumna " + conArtist);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } catch (NullPointerException npe) {
            System.err.println("NULL POINTER" + conArtist); // to bedzie do wyjebania //mówisz?
        }
    }

    /**
     * Metoda odpowiedzialna za wykonywania działań konkretnego workera
     */
    public void process() {
        //metoda odpowiedzialna za wykonywanie zadan konkretnego workera

        lastRun = System.currentTimeMillis();
        try {
            updateLatLng(false);
            getData();
        } catch (IOException | SQLException ioe) {
            ioe.printStackTrace();
        }

    }

    /**
     * Bierze wszystkie koncerty pokolei które mają lat = 0 lub lon = 0 i próbuje do nich znaleźć poprawne współrzędne
     *
     * @param forceUpdate - aktualizuje wszystkie współrzędne z bazy, a nie tylko brakujące
     * @throws SQLException
     */
    public void updateLatLng(boolean forceUpdate) throws SQLException {
        String select = "SELECT * FROM spots";

        if (!forceUpdate) {
            select = String.format("%s WHERE lat = 0 OR lon = 0", select);
        }

        pstm = connection.prepareStatement(select);
        resultSet = pstm.executeQuery();

        while (resultSet.next()) {
            String update = "UPDATE spots SET lat = ?, lon = ? WHERE id_spot = ?";
            pstm = connection.prepareStatement(update);

            LatLngPojo latlon = MapMgr.getCoordinates(resultSet.getString(3), resultSet.getString(4));
            if (!latlon.isEmpty()) {
                pstm.setString(1, latlon.getLatitude());
                pstm.setString(2, latlon.getLongitude());
                pstm.setInt(3, resultSet.getInt(1));
                pstm.executeUpdate();
            }
            System.out.println(
                    "UPDATE LatLng: " + resultSet.getString(3) + " " + resultSet.getString(4) + " - " + latlon.getLatitude() + " / " + latlon.getLongitude());
        }

    }

    public boolean checkConditions() {
        long currentTime = System.currentTimeMillis();

        return (currentTime - lastRun) > 900000; //15 minut
    }

    protected void parseError(String err) {
        System.err.println(agencyName + ": błąd parsowania (" + err + ")");
    }

    private void sqlError(String err) {
        System.err.println(err);
    }

}
