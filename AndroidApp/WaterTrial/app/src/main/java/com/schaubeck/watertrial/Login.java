package com.schaubeck.watertrial;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.schaubeck.watertrial.utils.NetworkUtils;
import com.schaubeck.watertrial.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Login extends AppCompatActivity {

    private EditText serverAddress;
    private ProgressBar progressBar;

    //Variables
    public static String ipAddress = "192.168.0.25";
    public static int port = 5000;
    JSONObject data, json, plantNames, weatherData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //UI Element
        //TextView textTitle = (TextView) findViewById(R.id.textTitle);
        //TextView textPassword = findViewById(R.id.textPassword);
        //TextView textServerAddress = findViewById(R.id.textServerAddress);
        serverAddress = findViewById(R.id.serverAddress);
        //EditText password = findViewById(R.id.password);
        Button connect = findViewById(R.id.btnConnect);
        progressBar = findViewById(R.id.progressBar);


        Objects.requireNonNull(getSupportActionBar()).hide();

        //read the JSON-DATA
        connect.setOnClickListener(view -> {
            //try {
            //if (true || Utilities.getSHA(password.getText().toString()).equals(Utilities.getPasswordHash())) {
            setServerAddress();
            final Map<String, String> backgroundTaskResults = new HashMap<>();

            // after android skd 30 no more AsyncTasks are supported
            // these are replaced by Executors
            progressBar.setVisibility(View.VISIBLE);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                // old inBackground Process
                backgroundTaskResults.put("data", NetworkUtils.getRequest("http://" + ipAddress + ":" + port + "/data.json"));
                backgroundTaskResults.put("plantNames", NetworkUtils.getRequest("http://" + ipAddress + ":" + port + "/plantNames.json"));
                backgroundTaskResults.put("weather", NetworkUtils.getRequest("http://" + ipAddress + ":" + port + "/api/weather"));

                // old postExecuteProcess
                runOnUiThread(() -> {
                    try {
                        data = new JSONObject(Objects.requireNonNull(backgroundTaskResults.get("data")));
                        // TODO change plantNames.json to a JSONObject instead of JSONArray
                        JSONArray plantNamesArray = new JSONArray(backgroundTaskResults.get("plantNames"));
                        plantNames = (JSONObject) plantNamesArray.get(0);
                        // plantNames = new JSONObject(Objects.requireNonNull(backgroundTaskResults.get("plantNames")));
                        weatherData = new JSONObject(Objects.requireNonNull(backgroundTaskResults.get("weather")));

                        try {
                            // perform old postExecuteProcess for data
                            //get JSON and write the Data to Textbox
                            JSONArray jsonData = (JSONArray) data.get("data");
                            json = (JSONObject) jsonData.get(0);

                            // perform old postExecuteProcess for weather
                            //get JSON and write the Data to Textbox
                            json.put("weather", weatherData);

                            // perform old postExecuteProcess for plantNames
                            for (int i = 1; i < 8; i++) {
                                json.put("plant" + i + "Name", plantNames.get("plant" + i));
                            }

                            // change to Main Activity
                            Toast.makeText(getApplicationContext(), "Verbindung hergestellt", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            Intent homeIntent = new Intent(Login.this, Main.class);
                            Bundle b = new Bundle();
                            b.putAll(Utilities.jsonToBundle(json));
                            b.putString("ipadress", ipAddress);
                            b.putInt("port", port);
                            homeIntent.putExtras(b);
                            startActivity(homeIntent);
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException | NullPointerException e) {
                        Toast.makeText(getApplicationContext(), "Verbindung zum Server nicht möglich", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            });

            //} else
            //    Toast.makeText(getApplicationContext(), "Passwort falsch", Toast.LENGTH_SHORT).show();
            //} catch (NoSuchAlgorithmException e) {
            //    e.printStackTrace();
            //}
        });

    }

    public void setServerAddress() {
        String[] split = serverAddress.getText().toString().split(":");
        ipAddress = split[0];
        port = Integer.parseInt(split[1]);
    }
}