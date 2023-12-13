package com.schaubeck.watertrial;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Charts extends AppCompatActivity implements OnChartGestureListener, OnChartValueSelectedListener {

    private LineChart lineChart;
    private ArrayList<String> entrys = new ArrayList<>();
    private ArrayList<Entry> yValues = new ArrayList<>();

    Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        lineChart = (LineChart) findViewById(R.id.chart);
        back = (Button) findViewById(R.id.btnBack);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(Charts.this, Main.class);
                homeIntent.putExtras(getIntent().getExtras());
                startActivity(homeIntent);
                finish();
            }
        });

        lineChart.setOnChartGestureListener(Charts.this);
        lineChart.setOnChartValueSelectedListener(Charts.this);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        new OnlineFileReader().execute("http://" + Login.ipAdress + ":" + Login.port + "/wheaterData.txt");

    }

    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(Charts.this, Main.class);
        homeIntent.putExtras(getIntent().getExtras());
        startActivity(homeIntent);
        finish();
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

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
                    entrys.add(buffer);
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
            int counter = 0;
            for (String entry : entrys) {
                String[] split = entry.split(",");
                yValues.add(new Entry(Utilities.getTime(split[0]), Float.parseFloat(split[1])));
            }

            LineDataSet set1 = new LineDataSet(yValues, "Temperatur in °C");

            set1.setDrawIcons(false);
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.DKGRAY);
            set1.setCircleColor(Color.DKGRAY);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setDrawValues(false);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);
            set1.setFillColor(Color.DKGRAY);

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            LineData data = new LineData(dataSets);

            lineChart.setData(data);
            lineChart.invalidate();
        }
    }

}
