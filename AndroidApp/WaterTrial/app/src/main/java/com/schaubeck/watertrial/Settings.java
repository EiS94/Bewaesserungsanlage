package com.schaubeck.watertrial;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class Settings extends AppCompatActivity {

    //UI Elements
    EditText plant1, plant2, plant3, plant4, plant5;
    Button btnSave, btnDefault;

    //Variables
    JSONObject json = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        plant1 = (EditText) findViewById(R.id.plant1Name);
        plant2 = (EditText) findViewById(R.id.plant2Name);
        plant3 = (EditText) findViewById(R.id.plant3Name);
        plant4 = (EditText) findViewById(R.id.plant4Name);
        plant5 = (EditText) findViewById(R.id.plant5Name);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnDefault = (Button) findViewById(R.id.btnDefault);

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

        //set current plantNames
        try {
            setHint();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String plant1Name = plant1.getText().toString();
                if (plant1Name.equals("")) {
                    plant1Name = plant1.getHint().toString();
                }
                String plant2Name = plant2.getText().toString();
                if (plant2Name.equals("")) {
                    plant2Name = plant2.getHint().toString();
                }
                String plant3Name = plant3.getText().toString();
                if (plant3Name.equals("")) {
                    plant3Name = plant3.getHint().toString();
                }
                String plant4Name = plant4.getText().toString();
                if (plant4Name.equals("")) {
                    plant4Name = plant4.getHint().toString();
                }
                String plant5Name = plant5.getText().toString();
                if (plant5Name.equals("")) {
                    plant5Name = plant5.getHint().toString();
                }

                new ValveChanger().execute("http://" + Login.ipAdress + ":" + Login.port +
                        "/plantNames?plant1=" + plant1Name + "&plant2=" + plant2Name + "&plant3="
                        + plant3Name + "&plant4=" + plant4Name + "&plant5=" + plant5Name +
                        "&plant6=Birnbaum&plant7=Kraeuterbeet");

                try {
                    updateJson();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent homeIntent = new Intent(Settings.this, Main.class);
                try {
                    homeIntent.putExtras(Utilities.jsonToBundle(json));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(homeIntent);
                finish();
            }
        });

        btnDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plant1.setHint("Blumen");
                plant2.setHint("Lavendel");
                plant3.setHint("Knoblauch");
                plant4.setHint("Chillis");
                plant5.setHint("Erdbeeren");
            }
        });

    }

    void updateJson() throws JSONException {
        String plant1Name = plant1.getText().toString();
        if (plant1Name.equals("")) {
            plant1Name = plant1.getHint().toString();
        }
        String plant2Name = plant2.getText().toString();
        if (plant2Name.equals("")) {
            plant2Name = plant2.getHint().toString();
        }
        String plant3Name = plant3.getText().toString();
        if (plant3Name.equals("")) {
            plant3Name = plant3.getHint().toString();
        }
        String plant4Name = plant4.getText().toString();
        if (plant4Name.equals("")) {
            plant4Name = plant4.getHint().toString();
        }
        String plant5Name = plant5.getText().toString();
        if (plant5Name.equals("")) {
            plant5Name = plant5.getHint().toString();
        }
        json.put("plant1Name", plant1Name);
        json.put("plant2Name", plant2Name);
        json.put("plant3Name", plant3Name);
        json.put("plant4Name", plant4Name);
        json.put("plant5Name", plant5Name);
    }

    void setHint() throws JSONException {
        plant1.setHint((String) json.get("plant1Name"));
        plant2.setHint((String) json.get("plant2Name"));
        plant3.setHint((String) json.get("plant3Name"));
        plant4.setHint((String) json.get("plant4Name"));
        plant5.setHint((String) json.get("plant5Name"));
    }

}
