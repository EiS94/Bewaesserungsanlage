package com.schaubeck.watertrial;

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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class Login extends AppCompatActivity {

    //UI Element
    private TextView textTitle, textPassword, textServerAddress;
    private EditText serverAddress, password;
    private Button connect;
    //private ImageView gifLoad;
    private ProgressBar progressBar;

    //Variables
    public static String ipAdress = "192.168.178.25";
    public static int port = 5000;
    public static String CMD = "0";
    public static boolean valveStatus;
    JSONObject jsonHelper, json, plantNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textTitle = (TextView) findViewById(R.id.textTitle);
        textPassword = (TextView) findViewById(R.id.textPassword);
        textServerAddress = (TextView) findViewById(R.id.textServerAddress);
        serverAddress = (EditText) findViewById(R.id.serverAddress);
        password = (EditText) findViewById(R.id.password);
        connect = (Button) findViewById(R.id.btnConnect);
        //gifLoad = (ImageView) findViewById(R.id.gifLoad);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        getSupportActionBar().hide();

        setServerAddress();

        //read the JSON-DATA
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (true || Utility.getSHA(password.getText().toString()).equals(Utility.getPasswordHash())) {
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

    public class Socket_AsyncTask extends AsyncTask<Void, Void, Void> {

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
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class GetJSONTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return Utility.readJson("http://" + ipAdress + ":" + port + "/data.json");
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
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Verbindung zum Server nicht möglich", Toast.LENGTH_SHORT).show();
                //gifLoad.setVisibility(View.GONE);
                progressBar.setVisibility(View.INVISIBLE);
            } catch (NullPointerException e) {
                Toast.makeText(getApplicationContext(), "Verbindung zum Server nicht möglich", Toast.LENGTH_SHORT).show();
                //gifLoad.setVisibility(View.GONE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }

        void handlePostExecute() throws JSONException {
            //get JSON and write the Data to Textbox
            JSONArray jsonData = (JSONArray) jsonHelper.get("data");
            json = (JSONObject) jsonData.get(0);
            new GetPlantNames().execute();

        }
    }

    private class GetPlantNames extends AsyncTask<String, Void, String> {

        JSONArray helper;

        @Override
        protected String doInBackground(String... urls) {
            try {
                return Utility.readPlantNames("http://" + ipAdress + ":" + port + "/plantNames.json");
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
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Verbindung zum Server nicht möglich", Toast.LENGTH_SHORT).show();
                //gifLoad.setVisibility(View.GONE);
                progressBar.setVisibility(View.INVISIBLE);
            } catch (NullPointerException e) {
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
            b.putAll(Utility.jsonToBundle(json));
            b.putString("ipadress", ipAdress);
            b.putInt("port", port);
            homeIntent.putExtras(b);
            startActivity(homeIntent);
            finish();
        }

    }

}
