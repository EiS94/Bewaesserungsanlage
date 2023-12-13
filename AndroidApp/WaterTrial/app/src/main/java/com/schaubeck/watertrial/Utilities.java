package com.schaubeck.watertrial;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeoutException;

public class Utilities {

    private static String passwordHash = "6941bea706c4231b322a29b91fe6701c4d5e89f3e32f4c84f49b84caf0486b96";

    public static String getPasswordHash() {
        return passwordHash;
    }

    public static String getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return toHexString(md.digest(input.getBytes(StandardCharsets.UTF_8)));
    }

    public static String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);

        StringBuilder sb = new StringBuilder(number.toString(16));
        while (sb.length() < 32) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }

    public static void changeValve(String urlAdress) {
        URL url = null;
        try {
            url = new URL(urlAdress);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            con.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        int status = 0;
        try {
            status = con.getResponseCode();
        } catch (IOException e) {
            try {
                throw new TimeoutException("Server unreachable");
            } catch (TimeoutException ex) {
                ex.printStackTrace();
            }
        }
    }


    public static String readJson(String urlAdress) throws TimeoutException {
        URL url = null;
        try {
            url = new URL(urlAdress);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            con.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        int status = 0;
        try {
            status = con.getResponseCode();
        } catch (IOException e) {
            throw new TimeoutException("Server unreachable");
        }
        StringBuilder sb = new StringBuilder();
        if (status < 299) {
            String buffer = "";
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(url.openStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    if (!((buffer = reader.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sb.append(buffer);
            }
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }
        else throw new TimeoutException("Server unreachable");
    }

    public static Float getTime(String timestamp) {
        Long tsLong = Long.parseLong(timestamp) * 1000L;
        Timestamp ts = new Timestamp(tsLong);
        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
        Date date = new Date(ts.getTime());
        Float d = Float.parseFloat(sdf.format(date));
        if (d / 0.1 % 10 == 0.0) {
            d += 0.01F;
        }
        return d;
    }



    public static Bundle jsonStringToBundle(String jsonString){
        try {
            JSONObject jsonObject = toJsonObject(jsonString);
            return jsonToBundle(jsonObject);
        } catch (JSONException ignored) {

        }
        return null;
    }
    public static JSONObject toJsonObject(String jsonString) throws JSONException {
        return new JSONObject(jsonString);
    }
    public static Bundle jsonToBundle(JSONObject jsonObject) throws JSONException {
        Bundle bundle = new Bundle();
        Iterator iter = jsonObject.keys();
        while(iter.hasNext()){
            String key = (String)iter.next();
            String value = jsonObject.getString(key);
            bundle.putString(key,value);
        }
        return bundle;
    }

    public static String readPlantNames(String urlAdress) throws TimeoutException {
        URL url = null;
        try {
            url = new URL(urlAdress);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            con.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        int status = 0;
        try {
            status = con.getResponseCode();
        } catch (IOException e) {
            throw new TimeoutException("Server unreachable");
        }
        StringBuilder sb = new StringBuilder();
        if (status < 299) {
            String buffer = "";
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(url.openStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    if (!((buffer = reader.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sb.append(buffer);
            }
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }
        else throw new TimeoutException("Server unreachable");
    }

    public static String convertRainIntensity(String rainString) {
        switch (rainString) {
            case "NONE":
                return "Kein Regen";
            case "DRIZZLE":
                return "Nieselregen";
            case "LIGHT_RAIN":
                return "leichter Regen";
            case "MODERATE_RAIN":
                return "durchgängiger Regen";
            case "HEAVY_RAIN":
                return "starker Regen";
            case "VERY_HEAVY_RAIN":
                return "sehr starker Regen";
            default:
                return "Unbekannt";
        }
    }

}
