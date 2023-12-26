package com.schaubeck.watertrial;

import android.annotation.SuppressLint;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class Utilities {


    /*
    public static String getPasswordHash() {
        return "6941bea706c4231b322a29b91fe6701c4d5e89f3e32f4c84f49b84caf0486b96";
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
     */

    public static Float getTime(String timestamp) {
        long tsLong = Long.parseLong(timestamp) * 1000L;
        Timestamp ts = new Timestamp(tsLong);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
        Date date = new Date(ts.getTime());
        float d = Float.parseFloat(sdf.format(date));
        if (d / 0.1 % 10 == 0.0) {
            d += 0.01F;
        }
        return d;
    }

    public static Bundle jsonToBundle(JSONObject jsonObject) throws JSONException {
        Bundle bundle = new Bundle();
        Iterator<String> iter = jsonObject.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            String value = jsonObject.getString(key);
            bundle.putString(key, value);
        }
        return bundle;
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
