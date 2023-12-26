package com.schaubeck.watertrial;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static String getRequest(String url) {
        HttpURLConnection urlConnection = null;
        try {
            URL urlObj = new URL(url);
            urlConnection = (HttpURLConnection) urlObj.openConnection();
            urlConnection.setRequestMethod("GET");

            if (urlConnection.getResponseCode() == 200 || urlConnection.getResponseCode() == 201) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                return sb.toString();
            } else {
                throw new IOException("Unexpected Response Code " + urlConnection.getResponseCode());
            }

        } catch (IOException e) {
            Log.e(TAG, "Error during GET request", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    public static String postRequest(String urlString, String data) {
        HttpURLConnection urlConnection;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);

            // send data
            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.write(data.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            // read answer
            if (urlConnection.getResponseCode() == 200 || urlConnection.getResponseCode() == 201) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                return sb.toString();
            } else {
                throw new IOException("Unexpected Response Code " + urlConnection.getResponseCode());
            }

        } catch (IOException e) {
            Log.e(TAG, "Error during POST request", e);
        }

        return null;
    }


}
