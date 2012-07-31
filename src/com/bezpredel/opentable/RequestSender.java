package com.bezpredel.opentable;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: 1/12/12
 * Time: 3:23 PM
 */
public class RequestSender
{
    private static final Pattern AVAILABILITY_PATTERN = Pattern.compile("<li a=\"\\['([^']*)'[^\"]*\" [^>]*class=\"ti[^\"]*\">");
    private static final Pattern NAMES_PATTERN = Pattern.compile("arrAutoF = \\[(.*)\\];");
    private final SimpleDateFormat parseFormat = new SimpleDateFormat("M/d/yyyy h:mm:ss a");

    static {
        System.setProperty("http.agent", "");
    }

    public RequestSender() {
    }

    private String createURLString(int rid, int hourInput) {
        Date date = new Date();


        int hour = date.getHours() + 1 - 12;

        if(hour > hourInput) {
            date = new Date(System.currentTimeMillis() + 1000*60*60*24);
        }

        int day = date.getDate();
        int month = date.getMonth()+1;
        int year = date.getYear();

        String hourStr;
        String pmAm;
        if(hourInput < 12){
            hourStr = Integer.toString(hourInput);
            pmAm = "AM";
        } else if(hourInput == 12) {
            hourStr = "12";
            pmAm = "PM";
        } else {
            hourStr = Integer.toString(hourInput - 12);
            pmAm = "PM";
        }

        return "http://www.opentable.com/nextavailabletable.aspx?rid="+rid+"&d="+month+"%2f"+day+"%2f"+year+"+"+hourStr+"%3a00%3a00+"+pmAm+"&p=2";
    }

    private String getUrlContents(String urlString ) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Referer", urlString);
        connection.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:13.0) Gecko/20100101 Firefox/13.0.1");


        InputStream is = connection.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        char[] bb = new char[1024*64];
        StringBuilder res = new StringBuilder();
        while(true) {
            int cnt = isr.read(bb);
            if(cnt < 0) break;
            res.append(bb, 0, cnt);
        }

        is.close();

        return res.toString();
    }

    public List<Date> getAvailability(int rid, int hourPM) throws Exception {
        String input = getUrlContents(createURLString(rid, hourPM));
        Matcher matcher = AVAILABILITY_PATTERN.matcher(input);
        ArrayList<Date> res = new ArrayList<Date>();
        while(matcher.find()) {
            res.add(parseFormat.parse(matcher.group(1)));
        }

        if(res.isEmpty()) {
            if(input.indexOf("No Availability for") < 0) {
                throw new RuntimeException("Unexpected results");
            }
        }
        return res;
    }

    public RestaurantList requestRestaurants() throws IOException {
        String input = getUrlContents("http://www.opentable.com/new-york-city-restaurants");

        Matcher matcher = NAMES_PATTERN.matcher(input);

        if(matcher.find()) {
            String names = matcher.group(1);
            String[] nn = names.substring(1, names.length() - 1).split("\",\"");
            RestaurantList res = new RestaurantList();
            for (String s : nn) {
                String name = s.substring(0, s.lastIndexOf("^"));
                int id = Integer.parseInt(s.substring(s.lastIndexOf("^") + 1));
                res.list.put(id, name);
            }
            return res;
        } else {
            throw new RuntimeException("Failed to find arrAutoF");
        }

    }


    public static class RestaurantList {
        private final Map<Integer, String> list = new HashMap<Integer, String>();

        public String get(int id) {
            return list.get(id);
        }

        public Map<String, Integer> getAll() {
            TreeMap<String, Integer> map = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
            for (Map.Entry<Integer, String> entry : list.entrySet()) {
                map.put(entry.getValue(), entry.getKey());
            }
            return map;
        }
    }

}
