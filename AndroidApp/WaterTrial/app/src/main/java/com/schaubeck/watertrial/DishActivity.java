package com.schaubeck.watertrial;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class DishActivity extends AppCompatActivity {

    String[] headers = {"Name", "veggy"};
    String[][] dishesStringList;
    List<Dish> dishList = new ArrayList<>();
    List<String> file = new ArrayList<>();
    boolean addDishActive = false;
    String currentClicked;

    //UI Elements
    TableView<String[]> tb;
    CheckBox veggyChecker, inputVeggy;
    Button btnRandom, btnCloseDish, btnAddNewDish, btnEditDish;
    ImageButton btnAddDish, btnEdit, btnDelete;
    ImageView dishBackground;
    TextView dishName, dishText, textNewName, textNewZutaten, textNewUrl;
    EditText inputName, inputZutaten, inputUrl, searchBar;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dishes);

        Objects.requireNonNull(getSupportActionBar()).hide();

        tb = findViewById(R.id.tableView);
        veggyChecker = findViewById(R.id.veggyChecker);
        inputVeggy = findViewById(R.id.inputVeggy);
        btnRandom = findViewById(R.id.btnRandom);
        btnCloseDish = findViewById(R.id.btnCloseDish);
        btnAddNewDish = findViewById(R.id.btnAddNewDish);
        btnEditDish = findViewById(R.id.btnEditDish);
        btnAddDish = findViewById(R.id.btnAddDish);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnTrash);
        dishBackground = findViewById(R.id.dishBackground);
        dishName = findViewById(R.id.dishName);
        dishText = findViewById(R.id.dishText);
        textNewName = findViewById(R.id.textNewName);
        textNewZutaten = findViewById(R.id.textNewZutaten);
        textNewUrl = findViewById(R.id.textNewUrl);
        inputName = findViewById(R.id.inputName);
        inputZutaten = findViewById(R.id.inputZutaten);
        inputUrl = findViewById(R.id.inputUrl);
        searchBar = findViewById(R.id.inputSearch);

        tb.setColumnCount(2);
        tb.setColumnWeight(0, 4);

        new OnlineFileReader().execute("http://" + Login.ipAddress + ":" + Login.port + "/dishes.csv");

        tb.addDataClickListener((rowIndex, clickedData) -> {
            currentClicked = clickedData[0];
            if (!addDishActive) {
                addDishActive = true;
                Dish selectedDish = null;
                for (Dish d : dishList) {
                    if (d.getName().equals(clickedData[0])) selectedDish = d;
                }
                assert selectedDish != null;
                String text = selectedDish.getName();
                SpannableString content = new SpannableString(text);
                content.setSpan(new UnderlineSpan(), 0, text.length(), 0);
                dishName.setText(content);

                String others = "";
                if (selectedDish.isVeggy()) others += "vegetarisch\n\n";
                else others += "nicht vegetarisch\n\n";
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
                btnEdit.setVisibility(View.VISIBLE);
                btnEdit.bringToFront();
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.bringToFront();
            }
        });

        veggyChecker.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showOnlyVeggie();
            } else showAll();
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterDishesBySearch(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnRandom.setOnClickListener(v -> {
            Random rd = new Random();
            Dish d = dishList.get(rd.nextInt(dishList.size()));
            currentClicked = d.getName();
            SpannableString content = new SpannableString(currentClicked);
            content.setSpan(new UnderlineSpan(), 0, currentClicked.length(), 0);
            dishName.setText(content);

            String others = "";
            if (d.isVeggy()) others += "vegetarisch\n\n";
            else others += "nicht vegetarisch\n\n";
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
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.bringToFront();
            btnEdit.setVisibility(View.VISIBLE);
            btnEdit.bringToFront();
        });

        btnCloseDish.setOnClickListener(v -> {
            dishBackground.setVisibility(View.INVISIBLE);
            dishName.setVisibility(View.INVISIBLE);
            dishText.setVisibility(View.INVISIBLE);
            btnCloseDish.setVisibility(View.INVISIBLE);
            btnEdit.setVisibility(View.INVISIBLE);
            btnDelete.setVisibility(View.INVISIBLE);

            addDishActive = false;
        });

        btnAddDish.setOnClickListener(v -> {
            if (!addDishActive) showNewDishMenu();
        });

        btnAddNewDish.setOnClickListener(v -> handleAddNewDish());

        btnDelete.setOnClickListener(v -> {
            new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                    "/dishHandler?mode=remove&name=" + currentClicked);
            dishBackground.setVisibility(View.INVISIBLE);
            dishName.setVisibility(View.INVISIBLE);
            dishText.setVisibility(View.INVISIBLE);
            btnCloseDish.setVisibility(View.INVISIBLE);
            btnEdit.setVisibility(View.INVISIBLE);
            btnDelete.setVisibility(View.INVISIBLE);

            addDishActive = false;

            new OnlineFileReader().execute("http://" + Login.ipAddress + ":" + Login.port + "/dishes.csv");

            Toast.makeText(getApplicationContext(), currentClicked + " gelöscht", Toast.LENGTH_SHORT).show();
        });

        btnEdit.setOnClickListener(v -> editDish());

        btnEditDish.setOnClickListener(v -> handleEditDish());

    }

    @Override
    public void onBackPressed() {
        Intent home = new Intent(DishActivity.this, Main.class);
        home.putExtras(Objects.requireNonNull(getIntent().getExtras()));
        startActivity(home);
        finish();
    }

    private void showAll() {

        // add 3 empty lines add the end for better usability
        dishesStringList = new String[dishList.size() + 3][2];

        for (int i = 0; i < dishList.size(); i++) {
            Dish d = dishList.get(i);
            dishesStringList[i][0] = d.getName();
            if (d.isVeggy()) dishesStringList[i][1] = "ja";
            else dishesStringList[i][1] = "nein";
        }

        // set the last 3 lines to empty string to display empty lines in the app
        for (int i = 1; i < 4; i++) {
            dishesStringList[dishesStringList.length - i][0] = "";
            dishesStringList[dishesStringList.length - i][1] = "";
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

    private void filterDishesBySearch(String searchText) {
        List<Dish> list = new ArrayList<>();

        for (Dish d : dishList) {
            if (d.name.toLowerCase().contains(searchText.toLowerCase())) list.add(d);

            if (!list.contains(d) && d.zutaten != null
                    && d.zutaten.toLowerCase().contains(searchText.toLowerCase()))
                list.add(d);
        }

        // add 3 empty lines add the end for better usability
        dishesStringList = new String[dishList.size() + 3][2];

        for (int i = 0; i < list.size(); i++) {
            Dish d = list.get(i);
            dishesStringList[i][0] = d.getName();
            if (d.isVeggy()) dishesStringList[i][1] = "ja";
            else dishesStringList[i][1] = "nein";
        }

        // set the last 3 lines to empty string to display empty lines in the app
        for (int i = 1; i < 4; i++) {
            dishesStringList[dishesStringList.length - i][0] = "";
            dishesStringList[dishesStringList.length - i][1] = "";
        }

        tb.setDataAdapter(new SimpleTableDataAdapter(this, dishesStringList));
        tb.invalidate();
        tb.invalidateOutline();
        tb.postInvalidate();

    }

    @SuppressLint("StaticFieldLeak")
    public class OnlineFileReader extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            file.clear();

            URL url = null;
            try {
                url = new URL(strings[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection con = null;
            try {
                assert url != null;
                con = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                assert con != null;
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
                        assert reader != null;
                        if ((buffer = reader.readLine()) == null) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    file.add(buffer.replace("\\n", "\n"));
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
            dishList.clear();
            for (String s : file) {
                s = s.replaceAll(",", ";");
                String[] dishString = s.split(";");
                Dish dish;
                if (dishString[1].equals("yes")) dish = new Dish(dishString[0], true);
                else dish = new Dish(dishString[0], false);
                if (!dishString[2].equals("null")) dish.setUrl(dishString[2]);
                if (!dishString[3].equals("null")) dish.setZutaten(dishString[3]);
                dishList.add(dish);
            }

            // add 3 empty lines add the end for better usability
            dishesStringList = new String[dishList.size() + 3][2];
            for (int i = 0; i < dishList.size(); i++) {
                Dish d = dishList.get(i);
                dishesStringList[i][0] = d.getName();
                if (d.isVeggy()) dishesStringList[i][1] = "ja";
                else dishesStringList[i][1] = "nein";
            }

            // set the last 3 lines to empty string to display empty lines in the app
            for (int i = 1; i < 4; i++) {
                dishesStringList[dishesStringList.length - i][0] = "";
                dishesStringList[dishesStringList.length - i][1] = "";
            }

            setData();
        }
    }

    private void setData() {
        tb.setHeaderAdapter(new SimpleTableHeaderAdapter(this, headers));
        tb.setDataAdapter(new SimpleTableDataAdapter(this, dishesStringList));
        tb.invalidate();
        tb.invalidateOutline();
        tb.postInvalidate();
    }

    private void showNewDishMenu() {
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

        inputName.setText("");
        inputVeggy.setChecked(false);
        inputUrl.setText("");
        inputZutaten.setText("");

        addDishActive = true;
    }

    private void editDish() {
        dishName.setVisibility(View.INVISIBLE);
        dishText.setVisibility(View.INVISIBLE);
        btnCloseDish.setVisibility(View.INVISIBLE);
        btnEdit.setVisibility(View.INVISIBLE);
        btnDelete.setVisibility(View.INVISIBLE);

        addDishActive = false;

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
        btnEditDish.setVisibility(View.VISIBLE);
        btnEditDish.bringToFront();
        btnRandom.setVisibility(View.INVISIBLE);

        Dish dish = null;
        for (Dish d : dishList) {
            if (d.getName().equals(currentClicked))
                dish = d;
        }

        assert dish != null;
        inputName.setText(dish.getName());
        inputVeggy.setChecked(dish.isVeggy());

        if (dish.getUrl() != null) inputUrl.setText(dish.getUrl());
        else inputUrl.setText("");

        if (dish.getZutaten() != null) inputZutaten.setText(dish.getZutaten());
        else inputZutaten.setText("");

        addDishActive = true;
    }

    private void closeNewDishMenu() {
        dishBackground.setVisibility(View.INVISIBLE);
        inputName.setVisibility(View.INVISIBLE);
        inputUrl.setVisibility(View.INVISIBLE);
        inputZutaten.setVisibility(View.INVISIBLE);
        inputVeggy.setVisibility(View.INVISIBLE);
        textNewName.setVisibility(View.INVISIBLE);
        textNewUrl.setVisibility(View.INVISIBLE);
        textNewZutaten.setVisibility(View.INVISIBLE);
        btnAddNewDish.setVisibility(View.INVISIBLE);
        btnEditDish.setVisibility(View.INVISIBLE);
        btnRandom.setVisibility(View.VISIBLE);

        addDishActive = false;
    }

    private void handleEditDish() {
        String name = inputName.getText().toString();
        String url = inputUrl.getText().toString();
        String zutaten = inputZutaten.getText().toString().replace("\n", "\\n");
        boolean veggy = inputVeggy.isChecked();

        if (!url.equals("") && !zutaten.equals("") && !name.equals("")) {
            if (veggy) {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=edit&name=" + name + "&veggy=yes&url=" + url
                        + "&ingredients=" + zutaten + "&oldName=" + currentClicked);
            } else {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=edit&name=" + name + "&veggy=no&url=" + url
                        + "&ingredients=" + zutaten + "&oldName=" + currentClicked);
            }
        } else if (name.equals("")) {
            Toast.makeText(getApplicationContext(), "Name darf nicht leer sein", Toast.LENGTH_SHORT).show();
        } else if (!url.equals("")) {
            if (veggy) {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=edit&name=" + name + "&veggy=yes&url=" + url
                        + "&oldName=" + currentClicked);
            } else {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=edit&name=" + name + "&veggy=no&url=" + url
                        + "&oldName=" + currentClicked);
            }
        } else if (!zutaten.equals("")) {
            if (veggy) {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=edit&name=" + name + "&veggy=yes&ingredients=" + zutaten
                        + "&oldName=" + currentClicked);
            } else {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=edit&name=" + name + "&veggy=no&&ingredients=" + zutaten
                        + "&oldName=" + currentClicked);
            }
        } else {
            if (veggy) {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=edit&name=" + name + "&veggy=yes"
                        + "&oldName=" + currentClicked);
            } else {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=edit&name=" + name + "&veggy=no"
                        + "&oldName=" + currentClicked);
            }
        }
        new OnlineFileReader().execute("http://" + Login.ipAddress + ":" + Login.port + "/dishes.csv");
        closeNewDishMenu();
    }

    private void handleAddNewDish() {
        String name = inputName.getText().toString();
        String url = inputUrl.getText().toString();
        String zutaten = inputZutaten.getText().toString().replace("\n", "\\n");
        boolean veggy = inputVeggy.isChecked();

        if (!url.equals("") && !zutaten.equals("") && !name.equals("")) {
            if (veggy) {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=new&name=" + name + "&veggy=yes&url=" + url
                        + "&ingredients=" + zutaten);
            } else {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=new&name=" + name + "&veggy=no&url=" + url
                        + "&ingredients=" + zutaten);
            }
        } else if (name.equals("")) {
            Toast.makeText(getApplicationContext(), "Name darf nicht leer sein", Toast.LENGTH_SHORT).show();
        } else if (!url.equals("")) {
            if (veggy) {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=new&name=" + name + "&veggy=yes&url=" + url);
            } else {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=new&name=" + name + "&veggy=no&url=" + url);
            }
        } else if (!zutaten.equals("")) {
            if (veggy) {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=new&name=" + name + "&veggy=yes&ingredients=" + zutaten);
            } else {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=new&name=" + name + "&veggy=no&&ingredients=" + zutaten);
            }
        } else {
            if (veggy) {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=new&name=" + name + "&veggy=yes");
            } else {
                new ValveChanger().execute("http://" + Login.ipAddress + ":" + Login.port +
                        "/dishHandler?mode=new&name=" + name + "&veggy=no");
            }
        }
        new OnlineFileReader().execute("http://" + Login.ipAddress + ":" + Login.port + "/dishes.csv");
        closeNewDishMenu();
    }


}