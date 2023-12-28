package com.schaubeck.watertrial;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.schaubeck.watertrial.utils.NetworkUtils;
import com.schaubeck.watertrial.utils.Utilities;
import com.sdsmdg.harjot.vectormaster.VectorMasterView;
import com.sdsmdg.harjot.vectormaster.models.PathModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends AppCompatActivity {

    //UI Element
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchWaterOnOff;
    private TextView textData;
    private ProgressBar progressBar;
    //private RichPathView richPathView;
    ImageView garden;

    //Variables
    public static boolean valveStatus;
    private JSONObject data;
    private JSONObject json = new JSONObject();
    List<String> plantNames = new ArrayList<>();

    @SuppressLint({"MissingInflatedId", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchWaterOnOff = findViewById(R.id.switchWaterValve);
        textData = findViewById(R.id.textData);
        Button chartActivity = findViewById(R.id.btnChartActivity);
        Button dishesActivity = findViewById(R.id.btnDishesActivity);
        Button settings = findViewById(R.id.settings);
        ImageButton update = findViewById(R.id.btnUpdate2);
        progressBar = findViewById(R.id.progressBarMain);
        //richPathView = findViewById(R.id.gardenNew);
        //garden = findViewById(R.id.garden);

        final MediaPlayer schorschVoice = MediaPlayer.create(this, R.raw.schorsch);
        final MediaPlayer heidiVoice = MediaPlayer.create(this, R.raw.heidi);
        final MediaPlayer wandaVoice = MediaPlayer.create(this, R.raw.wanda);

        Objects.requireNonNull(getSupportActionBar()).hide();

        //fit garden picture to display size
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        final float scale = getResources().getDisplayMetrics().density;
        int dpWidthInPx = (int) (dpWidth * scale);
        int dpHeightInPx = (int) (dpHeight * scale);
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(dpWidthInPx, dpHeightInPx);
        //richPathView.setLayoutParams(lp);
        //richPathView.setVectorDrawable(R.id.gardenNew);
        switchWaterOnOff.bringToFront();
        update.bringToFront();
        settings.bringToFront();
        chartActivity.bringToFront();

        Bundle bundle = getIntent().getExtras();

        //write Bundle items to json
        assert bundle != null;
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            try {
                json.put(key, JSONObject.wrap(bundle.get(key)));
            } catch (JSONException e) {
                //Handle exception here
            }
        }

        // write weather-data to separate jsonObject
        JSONObject weatherData;
        try {
            weatherData = new JSONObject((String) json.get("weather"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        //set dataText with the info of json
        String dataText = null;
        try {
            JSONObject actual = weatherData.getJSONObject("actual");
            dataText = getActualWeatherAsString(actual);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        textData.setText(dataText);
        //set plantNames
        for (int i = 1; i < 8; i++) {
            try {
                plantNames.add((String) json.get("plant" + i + "Name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //set SwitchButton weather if valve is on or off
        try {
            valveStatus = json.get("valve").equals("an");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        switchWaterOnOff.setChecked(valveStatus);

        //change colors of garden picture
        for (int i = 1; i <= 5; i++) {
            int color;
            Object status = null;
            try {
                status = json.get("plant" + i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            assert status != null;
            if (status.equals("ausreichend bewässert")) color = 2;
            else if (status.equals("braucht Wasser")) color = 1;
            else color = 3;
            //changeColor(i, color);
        }
        changeSchorschColor();
        changeWandaColor();


        /*
        richPathView.setOnPathClickListener(richPath -> {
            String status;
            try {
                try {
                    status = plantNames.get(Integer.parseInt(richPath.getName()) - 1) + ": " + json.get("plant" + richPath.getName())
                            + " (" + json.get("p" + richPath.getName() + "Value") + ")";
                    Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
                } catch (ClassCastException e) {
                    status = plantNames.get(Integer.parseInt(richPath.getName()) - 1) + ": " + json.get("plant" + richPath.getName())
                            + " (" + json.get("p" + richPath.getName() + "Value") + ")";
                    Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    if (richPath.getName().equals("schorsch")) {
                        if (richPath.getFillColor() == Color.BLACK) {
                            Toast.makeText(getApplicationContext(), "Schorsch", Toast.LENGTH_SHORT).show();
                            schorschVoice.start();
                        } else {
                            Toast.makeText(getApplicationContext(), "Heidi", Toast.LENGTH_SHORT).show();
                            heidiVoice.start();
                        }
                    } else if (richPath.getName().equals("wanda")) {
                        if (richPath.getFillColor() == Color.BLACK) {
                            Toast.makeText(getApplicationContext(), "Wanda", Toast.LENGTH_SHORT).show();
                            wandaVoice.start();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (richPath.getName().equals("schorsch")) {
                    if (richPath.getFillColor() == Color.BLACK) {
                        Toast.makeText(getApplicationContext(), "Schorsch", Toast.LENGTH_SHORT).show();
                        schorschVoice.start();
                    } else {
                        Toast.makeText(getApplicationContext(), "Heidi", Toast.LENGTH_SHORT).show();
                        heidiVoice.start();
                    }
                } else if (richPath.getName().equals("wanda")) {
                    if (richPath.getFillColor() == Color.BLACK) {
                        Toast.makeText(getApplicationContext(), "Wanda", Toast.LENGTH_SHORT).show();
                        wandaVoice.start();
                    }
                }
            }
        });

         */


        switchWaterOnOff.setOnClickListener(v -> {
            if (switchWaterOnOff.isChecked()) {
                valveStatus = true;
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port + "/v?valve=on");
            } else {
                valveStatus = false;
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port + "/v?valve=off");
            }
        });

        update.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            final Map<String, String> backgroundTaskResults = new HashMap<>();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                backgroundTaskResults.put("data", NetworkUtils.getRequest("http://" + Login.ipAddress + ":" + Login.port + "/data.json"));
                backgroundTaskResults.put("plantNames", NetworkUtils.getRequest("http://" + Login.ipAddress + ":" + Login.port + "/plantNames.json"));
                backgroundTaskResults.put("weather", NetworkUtils.getRequest("http://" + Login.ipAddress + ":" + Login.port + "/api/weather"));

                runOnUiThread(() -> {
                            try {
                                data = new JSONObject(Objects.requireNonNull(backgroundTaskResults.get("data")));
                                JSONArray jsonData = (JSONArray) data.get("data");
                                json = (JSONObject) jsonData.get(0);
                                valveStatus = json.get("valve").equals("an");
                                switchWaterOnOff.setChecked(valveStatus);

                                //set actual weather data
                                JSONObject wd = new JSONObject(Objects.requireNonNull(backgroundTaskResults.get("weather")));
                                try {
                                    JSONObject actual = wd.getJSONObject("actual");
                                    String weatherText = getActualWeatherAsString(actual);
                                    textData.setText(weatherText);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                //change Colors of the Plants
                                for (int i = 1; i <= 5; i++) {
                                    int color;
                                    Object status = json.get("plant" + i);
                                    if (status.equals("ausreichend bewässert")) color = 2;
                                    else if (status.equals("braucht Wasser")) color = 1;
                                    else color = 3;
                                    //changeColor(i, color);
                                }
                                changeSchorschColor();
                                changeWandaColor();

                                progressBar.setVisibility(View.INVISIBLE);

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
            });
        });

        chartActivity.setOnClickListener(v -> {
            Intent homeIntent = new Intent(Main.this, Charts.class);
            try {
                homeIntent.putExtras(Utilities.jsonToBundle(json));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            startActivity(homeIntent);
            finish();
        });

        dishesActivity.setOnClickListener(v -> {
            Intent homeIntent = new Intent(Main.this, DishActivity.class);
            try {
                homeIntent.putExtras(Utilities.jsonToBundle(json));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            startActivity(homeIntent);
            finish();
        });

        settings.setOnClickListener(v -> {
            Intent homeIntent = new Intent(Main.this, Settings.class);
            try {
                homeIntent.putExtras(Utilities.jsonToBundle(json));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            startActivity(homeIntent);
            finish();
        });


    }

    public String getActualWeatherAsString(JSONObject actual) throws JSONException {
        String dataText;
        dataText = "Temperatur: " + actual.get("temperature") + "°C\n";
        dataText += "Beschreibung: " + actual.get("description") + "\n";
        dataText += "Windgeschwindigkeit: " + actual.get("wind") + " km/h\n";
        dataText += "Regen in letzter Stunde: " + Utilities.convertRainIntensity((String) actual.get("rain_1h")) + "\n";
        dataText += "Regen in letzten 24 Stunden: " + Utilities.convertRainIntensity((String) actual.get("rain_24h"));
        return dataText;
    }

    public void changeSchorschColor() {
        VectorMasterView vectorMasterView = findViewById(R.id.garden);
        PathModel schorsch = vectorMasterView.getPathModelByName("schorsch");
        double rd = Math.random();
        if (rd > 0.8) schorsch.setFillColor(Color.WHITE);
        else schorsch.setFillColor(Color.BLACK);
        vectorMasterView.update();
    }

    public void changeWandaColor() {
        VectorMasterView vectorMasterView = findViewById(R.id.garden);
        PathModel wanda = vectorMasterView.getPathModelByName("wanda");
        double rd = Math.random();
        if (rd > 0.5) wanda.setFillColor(Color.parseColor("#d3f1cb"));
        else wanda.setFillColor(Color.BLACK);
        vectorMasterView.update();
    }
/*
    public void changeColor(int plant, int color) {
        RichPath p = richPathView.findRichPathByName(Integer.toString(plant));
        if (color == 1) {
            assert p != null;
            p.setFillColor(Color.RED);
        } else if (color == 2) {
            assert p != null;
            p.setFillColor(Color.YELLOW);
        } else {
            assert p != null;
            p.setFillColor(Color.parseColor("#0c6d00"));
        }
    }

    private Path findPathByName(String name) {
        try {
            @SuppressLint("ResourceType") XmlResourceParser parser = getResources().getXml(R.drawable.ic_garden_update);
            //@SuppressLint("ResourceType") XmlPullParser parser = getResources().getXml(R.drawable.ic_garden_update);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.getName().equals("path")) {
                    String pathName = parser.getAttributeValue(0);
                    if (pathName != null && pathName.equals(name)) {
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            String attributeName = parser.getAttributeName(i);
                            if (attributeName.equals("pathData")) {
                                String pathData = parser.getAttributeValue(i);
                                return PathParser.createPathFromPathData(pathData);
                            }
                        }

                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

     */

}