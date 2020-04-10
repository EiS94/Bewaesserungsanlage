package com.schaubeck.watertrial;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.TimeoutException;

public class SettingsChanger extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... strings) {
        URL url = null;
        try {
            url = new URL(strings[0]);
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
        return null;
    }

}
