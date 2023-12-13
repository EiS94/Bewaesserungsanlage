package com.schaubeck.watertrial;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class Login extends AppCompatActivity {

    private EditText serverAddress, password;
    //private ImageView gifLoad;
    private ProgressBar progressBar;

    //Variables
    public static String ipAdress = "192.168.0.25";
    public static int port = 5000;
    public static String CMD = "0";
    public static boolean valveStatus;
    JSONObject jsonHelper, json, plantNames, weatherData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //UI Element
        TextView textTitle = (TextView) findViewById(R.id.textTitle);
        TextView textPassword = (TextView) findViewById(R.id.textPassword);
        TextView textServerAddress = (TextView) findViewById(R.id.textServerAddress);
        serverAddress = (EditText) findViewById(R.id.serverAddress);
        password = (EditText) findViewById(R.id.password);
        Button connect = (Button) findViewById(R.id.btnConnect);
        //gifLoad = (ImageView) findViewById(R.id.gifLoad);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        getSupportActionBar().hide();

        setServerAddress();

        //read the JSON-DATA
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (true || Utilities.getSHA(password.getText().toString()).equals(Utilities.getPasswordHash())) {
                        setServerAddress();
                        //gifLoad.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        new GetJSONTask().execute();
                    } else
                        Toast.makeText(getApplicationContext(), "Passwort falsch", Toast.LENGTH_SHORT).show();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    public void setServerAddress() {
        String[] split = serverAddress.getText().toString().split(":");
        ipAdress = split[0];
        port = Integer.parseInt(split[1]);
    }

    public static class Socket_AsyncTask extends AsyncTask<Void, Void, Void> {

        Socket socket;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                InetAddress inetAddress = InetAddress.getByName(Login.ipAdress);
                socket = new java.net.Socket(inetAddress, Login.port);
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeBytes(CMD);
                dataOutputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class WeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                return downloadURL(strings[0]);
                //Utility.readJson("http://" + ipAdress + ":" + port + "/api/weather");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String downloadURL(String urlString) throws IOException {
            InputStream inputStream = null;

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                inputStream = urlConnection.getInputStream();
                return readStream(inputStream);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }

        private String readStream(InputStream inputStream) throws IOException {
            StringBuilder result = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                weatherData = new JSONObject(result);
                handlePostExecute();
            } catch (JSONException | NullPointerException e) {
                Toast.makeText(getApplicationContext(), "Verbindung zum Server nicht möglich", Toast.LENGTH_SHORT).show();
                //gifLoad.setVisibility(View.GONE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }

        void handlePostExecute() throws JSONException {
            //get JSON and write the Data to Textbox
            json.put("weather", weatherData);
            new GetPlantNames().execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetJSONTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return Utilities.readJson("http://" + ipAdress + ":" + port + "/data.json");
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            //Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
            return null;
        }

        @Override
        protected void onPreExecute() {
            /*String url = "https://cdn.dribbble.com/users/1867579/screenshots/6580156/loadin_gif.gif";
            Glide.with(Login.this)
                    .load(url)
                    .centerCrop()
                    .into(gifLoad);*/
            super.onPreExecute();
        }

        // onPostExecute displays the results of the doInBackgroud and also we
        // can hide progress dialog.
        @Override
        protected void onPostExecute(String result) {
            //write String to JSONObject
            try {
                jsonHelper = new JSONObject(result);
                try {
                    handlePostExecute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                super.onPostExecute(result);
            } catch (JSONException | NullPointerException e) {
                Toast.makeText(getApplicationContext(), "Verbindung zum Server nicht möglich", Toast.LENGTH_SHORT).show();
                //gifLoad.setVisibility(View.GONE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }

        void handlePostExecute() throws JSONException {
            //get JSON and write the Data to Textbox
            JSONArray jsonData = (JSONArray) jsonHelper.get("data");
            json = (JSONObject) jsonData.get(0);
            new WeatherTask().execute("http://" + ipAdress + ":" + port + "/api/weather");
            //new GetPlantNames().execute();

        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetPlantNames extends AsyncTask<String, Void, String> {

        JSONArray helper;

        @Override
        protected String doInBackground(String... urls) {
            try {
                return Utilities.readPlantNames("http://" + ipAdress + ":" + port + "/plantNames.json");
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            //Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                helper = new JSONArray(result);
                handlePostExecute();
                super.onPostExecute(result);
            } catch (JSONException | NullPointerException e) {
                Toast.makeText(getApplicationContext(), "Verbindung zum Server nicht möglich", Toast.LENGTH_SHORT).show();
                //gifLoad.setVisibility(View.GONE);
                progressBar.setVisibility(View.INVISIBLE);
            }
            super.onPostExecute(result);
        }

        void handlePostExecute() throws JSONException {
            plantNames = (JSONObject) helper.get(0);
            Toast.makeText(getApplicationContext(), "Verbindung hergestellt", Toast.LENGTH_SHORT).show();

            //gifLoad.setVisibility(View.GONE);
            progressBar.setVisibility(View.INVISIBLE);

            for (int i = 1; i < 8; i++) {
                json.put("plant" + i + "Name", plantNames.get("plant" + i));
            }

            Intent homeIntent = new Intent(Login.this, Main.class);
            Bundle b = new Bundle();
            b.putAll(Utilities.jsonToBundle(json));
            b.putString("ipadress", ipAdress);
            b.putInt("port", port);
            homeIntent.putExtras(b);
            startActivity(homeIntent);
            finish();
        }

    }

}
