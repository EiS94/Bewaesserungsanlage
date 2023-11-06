package com.schaubeck.watertrial;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.devs.vectorchildfinder.VectorChildFinder;
import com.richpath.RichPath;
import com.richpath.RichPathView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class Main extends AppCompatActivity {

    //UI Element
    private Switch switchWaterOnOff;
    private TextView textOn, textOff, textWater, textTitle, textData, textPassword, textServerAddress, textGarden;
    private Button chartActivity, changeColor, settings, dishesActivity;
    private ImageButton update2;
    //private ImageView garden;
    private ProgressBar progressBar;
    private RichPathView richPathView;

    //Variables
    public static String CMD = "0";
    public static boolean valveStatus;
    VectorChildFinder vector;
    private JSONObject jsonHelper;
    private JSONObject json = new JSONObject();
    List<String> plantNames = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        switchWaterOnOff = (Switch) findViewById(R.id.switchWaterValve);
        textOn = (TextView) findViewById(R.id.textOn);
        textOff = (TextView) findViewById(R.id.textOff);
        textWater = (TextView) findViewById(R.id.textWater);
        textTitle = (TextView) findViewById(R.id.textTitle);
        textData = (TextView) findViewById(R.id.textData);
        textPassword = (TextView) findViewById(R.id.textPassword);
        textServerAddress = (TextView) findViewById(R.id.textServerAddress);
        textGarden = (TextView) findViewById(R.id.textGarden);
        chartActivity = (Button) findViewById(R.id.btnChartActivity);
        dishesActivity = (Button) findViewById(R.id.btnDishesActivity);
        settings = (Button) findViewById(R.id.settings);
        update2 = (ImageButton) findViewById(R.id.btnUpdate2);
        //gifLoad = (ImageView) findViewById(R.id.gifLoad);
        //garden = (ImageView) findViewById(R.id.garden);
        progressBar = (ProgressBar) findViewById(R.id.progressBarMain);
        richPathView = (RichPathView) findViewById(R.id.gardenNew);

        final MediaPlayer schorschVoice = MediaPlayer.create(this, R.raw.schorsch);
        final MediaPlayer heidiVoice = MediaPlayer.create(this, R.raw.heidi);
        final MediaPlayer wandaVoice = MediaPlayer.create(this, R.raw.wanda);

        getSupportActionBar().hide();

        //fit garden picture to display size
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        final float scale = getResources().getDisplayMetrics().density;
        int dpWidthInPx = (int) (dpWidth * scale);
        int dpHeightInPx = (int) (dpHeight * scale);
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(dpWidthInPx, dpHeightInPx);
        richPathView.setLayoutParams(lp);
        switchWaterOnOff.bringToFront();
        update2.bringToFront();
        settings.bringToFront();
        chartActivity.bringToFront();

        //vector = new VectorChildFinder(this, R.drawable.ic_allplantsok, garden);

        Bundle bundle = getIntent().getExtras();

        //write Bundle items to json
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            try {
                json.put(key, JSONObject.wrap(bundle.get(key)));
            } catch (JSONException e) {
                //Handle exception here
            }
        }

        //set dataText with the info of json
        String dataText = null;
        try {
            dataText = "Temperatur: " + json.get("temperatur") + "°C\nLuftfeuchtigkeit: " +
                    json.get("wetness") + " %" + "\nRegen: " + json.get("rain")  + "\nHelligkeit : " +
                    json.get("illuminance") + " lx" + "\nZeit : "
                    + Utility.getTime(((String) json.get("timestamp")));
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

        //set SwitchButton wheater if valve is on or off
        try {
            if (json.get("valve").equals("an")) valveStatus = true;
            else valveStatus = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (valveStatus) switchWaterOnOff.setChecked(true);
        else switchWaterOnOff.setChecked(false);

        //change colors of garden picture
        for (int i = 1; i <= 5; i++) {
            int color;
            Object status = null;
            try {
                status = (String) json.get("plant" + i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (status.equals("ausreichend bewässert")) color = 2;
            else if (status.equals("braucht Wasser")) color = 1;
            else color = 3;
            changeColor(i, color);
        }
        changeSchorschColor();
        changeWandaColor();
        //garden.invalidate();


        richPathView.setOnPathClickListener(new RichPath.OnPathClickListener() {
            @Override
            public void onClick(RichPath richPath) {
                String status = null;
                String pValue = null;
                int alertnativeValue = 0;
                try {
                    try {
                        status = plantNames.get(Integer.parseInt(richPath.getName())-1) + ": " + (String) json.get("plant" + richPath.getName())
                                + " (" + (String) json.get("p" + richPath.getName() + "Value") + ")";
                        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
                    } catch (ClassCastException e) {
                        status = plantNames.get(Integer.parseInt(richPath.getName())-1) + ": " + (String) json.get("plant" + richPath.getName())
                                + " (" + (Integer) json.get("p" + richPath.getName() + "Value") + ")";
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
            }
        });

        switchWaterOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchWaterOnOff.isChecked()) {
                    valveStatus = true;
                    new ValveChanger().execute("http://" + Login.ipAdress + ":" + Login.port + "/v?valve=on");
                } else {
                    valveStatus = false;
                    new ValveChanger().execute("http://" + Login.ipAdress + ":" + Login.port + "/v?valve=off");
                }
            }
        });

        /*switchWaterOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!valveStatus) {
                    valveStatus = true;
                    new ValveChanger().execute("http://" + ipAdress + ":" + port + "/v?valve=on");
                } else {
                    valveStatus = false;
                    new ValveChanger().execute("http://" + ipAdress + ":" + port + "/v?valve=off");
                }

            }
        });*/

        update2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //updateVector();
                //gifLoad.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                new GetJSONTask().execute();
            }
        });

        chartActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(Main.this, Charts.class);
                try {
                    homeIntent.putExtras(Utility.jsonToBundle(json));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(homeIntent);
                finish();
            }
        });

        dishesActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(Main.this, DishActivity.class);
                try {
                    homeIntent.putExtras(Utility.jsonToBundle(json));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(homeIntent);
                finish();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(Main.this, Settings.class);
                try {
                    homeIntent.putExtras(Utility.jsonToBundle(json));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(homeIntent);
                finish();
            }
        });


    }

    /*public void updateVector() {
        vector = new VectorChildFinder(this, R.drawable.ic_allplantsok, garden);
    }
    */

    public void changeSchorschColor() {
        //VectorDrawableCompat.VFullPath path1 = vector.findPathByName("schorsch");
        RichPath schorsch = richPathView.findRichPathByName("schorsch");
        Double rd = Math.random();
        if (rd > 0.8) schorsch.setFillColor(Color.WHITE);
        else schorsch.setFillColor(Color.BLACK);
        //if (rd > 0.8) path1.setFillColor(Color.WHITE);
        //else path1.setFillColor(Color.BLACK);
    }

    public void changeWandaColor() {
        //VectorDrawableCompat.VFullPath path1 = vector.findPathByName("wanda");
        RichPath wanda = richPathView.findRichPathByName("wanda");
        Double rd = Math.random();
        if (rd > 0.5) wanda.setFillColor(Color.BLACK);
        else wanda.setFillColor(Color.parseColor("#d3f1cb"));
        //if (rd > 0.5) path1.setFillColor(Color.BLACK);
    }

    public void changeColor(int plant, int color) {
        //VectorDrawableCompat.VFullPath path1 = vector.findPathByName(Integer.toString(plant));
        RichPath p = richPathView.findRichPathByName(Integer.toString(plant));
        if (color == 1) p.setFillColor(Color.RED);
        else if (color == 2) p.setFillColor(Color.YELLOW);
        else p.setFillColor(Color.parseColor("#0c6d00"));
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
                return Utility.readJson("http://" + Login.ipAdress + ":" + Login.port + "/data.json");
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            //Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
            return null;
        }

        @Override
        protected void onPreExecute() {
            /*String url = "https://cdn.dribbble.com/users/1867579/screenshots/6580156/loadin_gif.gif";
            Glide.with(Main.this)
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
                Toast.makeText(getApplicationContext(), "Verbindung hergestellt", Toast.LENGTH_SHORT).show();
                super.onPostExecute(result);
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Verbindung zum Server nicht möglich", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                // gifLoad.setVisibility(View.GONE);
            } catch (NullPointerException e) {
                Toast.makeText(getApplicationContext(), "Verbindung zum Server nicht möglich", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                //gifLoad.setVisibility(View.GONE);
            }
        }

        void handlePostExecute() throws JSONException {
            //get JSON and write the Data to Textbox
            JSONArray jsonData = (JSONArray) jsonHelper.get("data");
            json = (JSONObject) jsonData.get(0);
            String dataText = "Temperatur: " + json.get("temperatur") + "°C\nLuftfeuchtigkeit: " +
                    json.get("wetness") + " %" + "\nRegen: " + json.get("rain") + "\nHelligkeit : " +
                    json.get("illuminance") + " lx" + "\nZeit : "
                    + Utility.getTime(String.valueOf((Integer) json.get("timestamp")));
            textData.setText(dataText);
            if (json.get("valve").equals("an")) valveStatus = true;
            else valveStatus = false;
            if (valveStatus) switchWaterOnOff.setChecked(true);
            else switchWaterOnOff.setChecked(false);

            //change Colors of the Plants
            for (int i = 1; i <= 5; i++) {
                int color;
                Object status = (String) json.get("plant" + i);
                if (status.equals("ausreichend bewässert")) color = 2;
                else if (status.equals("braucht Wasser")) color = 1;
                else color = 3;
                changeColor(i, color);
            }
            changeSchorschColor();
            changeWandaColor();
            //garden.invalidate();

            progressBar.setVisibility(View.INVISIBLE);
            //gifLoad.setVisibility(View.GONE);
        }
    }
}
