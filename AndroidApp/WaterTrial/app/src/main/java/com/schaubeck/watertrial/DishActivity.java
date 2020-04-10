package com.schaubeck.watertrial;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class DishActivity extends AppCompatActivity {

    String[] headers = {"Name", "veggy"};
    String[][] dishesStringList;
    List<Dish> dishList = new ArrayList<>();
    List<String> file = new ArrayList<>();

    //UI Elements
    TableView<String[]> tb;
    CheckBox veggyChecker, inputVeggy;
    Button btnRandom, btnCloseDish, btnAddNewDish;
    ImageButton btnAddDish;
    ImageView dishBackground;
    TextView dishName, dishText, textNewName, textNewZutaten, textNewUrl;
    EditText inputName, inputZutaten, inputUrl;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dishes);

        tb = (TableView<String[]>) findViewById(R.id.tableView);
        veggyChecker = (CheckBox) findViewById(R.id.veggyChecker);
        inputVeggy = (CheckBox) findViewById(R.id.inputVeggy);
        btnRandom = (Button) findViewById(R.id.btnRandom);
        btnCloseDish = (Button) findViewById(R.id.btnCloseDish);
        btnAddNewDish = (Button) findViewById(R.id.btnAddNewDish);
        btnAddDish = (ImageButton) findViewById(R.id.btnAddDish);
        dishBackground = (ImageView) findViewById(R.id.dishBackground);
        dishName = (TextView) findViewById(R.id.dishName);
        dishText = (TextView) findViewById(R.id.dishText);
        textNewName = (TextView) findViewById(R.id.textNewName);
        textNewZutaten = (TextView) findViewById(R.id.textNewZutaten);
        textNewUrl = (TextView) findViewById(R.id.textNewUrl);
        inputName = (EditText) findViewById(R.id.inputName);
        inputZutaten = (EditText) findViewById(R.id.inputZutaten);
        inputUrl = (EditText) findViewById(R.id.inputUrl);

        tb.setColumnCount(2);
        tb.setColumnWeight(0, 4);

        new OnlineFileReader().execute("http://" + Login.ipAdress + ":" + Login.port + "/dishes.csv");

        tb.addDataClickListener(new TableDataClickListener<String[]>() {
            @Override
            public void onDataClicked(int rowIndex, String[] clickedData) {
                Dish selectedDish = null;
                for (Dish d : dishList) {
                    if (d.getName().equals(clickedData[0])) selectedDish = d;
                }
                String text= selectedDish.getName();
                SpannableString content = new SpannableString(text);
                content.setSpan(new UnderlineSpan(), 0, text.length(), 0);
                dishName.setText(content);

                String others = "";
                if (selectedDish.getZutaten() != null) {
                    others += selectedDish.getZutaten();
                }
                if (selectedDish.getUrl() != null) {
                    others += "\n\n" + selectedDish.getUrl();
                }
                dishText.setText(others);

                dishBackground.setVisibility(View.VISIBLE);
                dishBackground.bringToFront();
                dishName.setVisibility(View.VISIBLE);
                dishName.bringToFront();
                dishText.setVisibility(View.VISIBLE);
                dishText.bringToFront();
                btnCloseDish.setVisibility(View.VISIBLE);
            }
        });

        veggyChecker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showOnlyVeggie();
                } else showAll();
            }
        });

        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random rd = new Random();
                Dish d = dishList.get(rd.nextInt(dishList.size()));
                String text= d.getName();
                SpannableString content = new SpannableString(text);
                content.setSpan(new UnderlineSpan(), 0, text.length(), 0);
                dishName.setText(content);

                String others = "";
                if (d.getZutaten() != null) {
                    others += d.getZutaten();
                }
                if (d.getUrl() != null) {
                    others += "\n\n" + d.getUrl();
                }
                dishText.setText(others);

                dishBackground.setVisibility(View.VISIBLE);
                dishBackground.bringToFront();
                dishName.setVisibility(View.VISIBLE);
                dishName.bringToFront();
                dishText.setVisibility(View.VISIBLE);
                dishText.bringToFront();
                btnCloseDish.setVisibility(View.VISIBLE);
            }
        });

        btnCloseDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dishBackground.setVisibility(View.INVISIBLE);
                dishName.setVisibility(View.INVISIBLE);
                dishText.setVisibility(View.INVISIBLE);
                btnCloseDish.setVisibility(View.INVISIBLE);
            }
        });

        btnAddDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddNewDish();
            }
        });

    }

    private void showAll() {

        dishesStringList = new String[dishList.size()][2];

        for (int i = 0; i < dishList.size(); i++) {
            Dish d = dishList.get(i);
            dishesStringList[i][0] = d.getName();
            if (d.isVeggy()) dishesStringList[i][1] = "ja";
            else dishesStringList[i][1] = "nein";
        }

        tb.setDataAdapter(new SimpleTableDataAdapter(this, dishesStringList));
        tb.invalidate();
        tb.invalidateOutline();
        tb.postInvalidate();

    }

    private void showOnlyVeggie() {

        List<Dish> list = new ArrayList<>();

        for (Dish d : dishList) {
            if (d.isVeggy()) list.add(d);
        }

        dishesStringList = new String[dishList.size()][2];

        for (int i = 0; i < list.size(); i++) {
            Dish d = list.get(i);
            dishesStringList[i][0] = d.getName();
            if (d.isVeggy()) dishesStringList[i][1] = "ja";
            else dishesStringList[i][1] = "nein";
        }

        tb.setDataAdapter(new SimpleTableDataAdapter(this, dishesStringList));
        tb.invalidate();
        tb.invalidateOutline();
        tb.postInvalidate();

    }

    public class OnlineFileReader extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            ArrayList<String> lines = null;

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
                    file.add(buffer);
                }
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            for (String s : file) {
                String[] dishString = s.split(";");
                Dish dish = null;
                if (dishString[1].equals("yes")) dish = new Dish(dishString[0], true);
                else dish = new Dish(dishString[0], false);
                if (!dishString[2].equals("null")) dish.setUrl(dishString[2]);
                if (!dishString[3].equals("null")) dish.setZutaten(dishString[3]);
                dishList.add(dish);
            }

            dishesStringList = new String[dishList.size()][2];
            for (int i = 0; i < dishList.size(); i++) {
                Dish d = dishList.get(i);
                dishesStringList[i][0] = d.getName();
                if (d.isVeggy()) dishesStringList[i][1] = "ja";
                else dishesStringList[i][1] = "nein";
            }

            setData();
        }
    }

    private void setData() {
        tb.setHeaderAdapter(new SimpleTableHeaderAdapter(this, headers));
        tb.setDataAdapter(new SimpleTableDataAdapter(this, dishesStringList));
    }

    private void handleAddNewDish() {
        dishBackground.setVisibility(View.VISIBLE);
        dishBackground.bringToFront();
        inputName.setVisibility(View.VISIBLE);
        inputName.bringToFront();
        inputUrl.setVisibility(View.VISIBLE);
        inputUrl.bringToFront();
        inputZutaten.setVisibility(View.VISIBLE);
        inputZutaten.bringToFront();
        inputVeggy.setVisibility(View.VISIBLE);
        inputVeggy.bringToFront();
        textNewName.setVisibility(View.VISIBLE);
        textNewName.bringToFront();
        textNewZutaten.setVisibility(View.VISIBLE);
        textNewZutaten.bringToFront();
        textNewUrl.setVisibility(View.VISIBLE);
        textNewUrl.bringToFront();
        btnAddNewDish.setVisibility(View.VISIBLE);
        btnAddNewDish.bringToFront();
        btnRandom.setVisibility(View.INVISIBLE);
    }


}
