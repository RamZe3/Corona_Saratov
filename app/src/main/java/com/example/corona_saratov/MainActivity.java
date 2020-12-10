package com.example.corona_saratov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private String mainTitle, infectedPeoples, plusOnDay, recoveredPeoples, activeInfections, deadPeoples;
    private boolean isHtmlWorked = false;

    private boolean isSendingMessage = false;
    private boolean isSendingToastOfMessage = false;

    Toolbar toolbar;

    TextView textViewMainTitle;
    TextView textViewInfectedPeoples;
    TextView textViewPlusOnDay;
    TextView textViewRecoveredPeoples;
    TextView textViewActiveInfections;
    TextView textViewDeadPeoples;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.update, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.send_message){
            sendMessage();
        }
        if (item.getItemId() == R.id.update){
            update();
        }
        return true;
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mainTitle", mainTitle);
        outState.putString("infectedPeoples", infectedPeoples);
        outState.putString("plusOnDay", plusOnDay);
        outState.putString("recoveredPeoples", recoveredPeoples);
        outState.putString("activeInfections", activeInfections);
        outState.putString("deadPeoples", deadPeoples);
        outState.putBoolean("isHtmlWorked", isHtmlWorked);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textViewMainTitle = findViewById(R.id.mainTitle);
        textViewInfectedPeoples = findViewById(R.id.infectedPeoples);
        textViewPlusOnDay = findViewById(R.id.plusOnDay);
        textViewRecoveredPeoples = findViewById(R.id.recoveredPeoples);
        textViewActiveInfections = findViewById(R.id.activeInfections);
        textViewDeadPeoples = findViewById(R.id.deadPeoples);

        if (savedInstanceState != null && savedInstanceState.getBoolean("isHtmlWorked")) {
            mainTitle = savedInstanceState.getString("mainTitle");
            infectedPeoples = savedInstanceState.getString("infectedPeoples");
            plusOnDay = savedInstanceState.getString("plusOnDay");
            recoveredPeoples = savedInstanceState.getString("recoveredPeoples");
            activeInfections = savedInstanceState.getString("activeInfections");
            deadPeoples = savedInstanceState.getString("deadPeoples");
            isHtmlWorked = savedInstanceState.getBoolean("isHtmlWorked");
        } else {
            init();
        }
        setTextViews();
    }


    private void init() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getInformation();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }


    private void getInformation() {
        try {
            Document doc = Jsoup.connect("https://coronavirus-control.ru/oembed/coronavirus-saratov-region/").data().get();
            Elements mainInformation = doc.getElementsByTag("div");
            mainTitle = doc.getElementsByAttributeValue("class", "entry-title").get(0).text();
            infectedPeoples = mainInformation.get(1).text().split(" ")[2];
            plusOnDay = doc.getElementsByAttributeValue("class", "plus").get(0).text();
            recoveredPeoples = mainInformation.get(4).text().split(" ")[1];
            activeInfections = mainInformation.get(2).text().split(" ")[2];
            deadPeoples = mainInformation.get(3).text().split(" ")[1];
            makeTitle();
            isHtmlWorked = true;
        } catch (IOException e) {
            init();
        }
    }


    public void makeTitle() {
        infectedPeoples = getString(R.string.infected_peoples) + infectedPeoples;
        plusOnDay = getString(R.string.plus_on_day) + plusOnDay;
        recoveredPeoples = getString(R.string.recovered) + recoveredPeoples;
        activeInfections = getString(R.string.active_infections) + activeInfections;
        deadPeoples = getString(R.string.dead_peoples) + deadPeoples;
    }


    private void setTextViews() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if (isHtmlWorked) {
                    textViewMainTitle.setText(mainTitle);
                    textViewInfectedPeoples.setText(infectedPeoples);
                    textViewPlusOnDay.setText(plusOnDay);
                    textViewRecoveredPeoples.setText(recoveredPeoples);
                    textViewActiveInfections.setText(activeInfections);
                    textViewDeadPeoples.setText(deadPeoples);
                } else {
                    handler.postDelayed(this, 500);
                }
            }
        });
    }


    public void update() {
        isHtmlWorked = false;
        init();
        setTextViews();
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isHtmlWorked) {
                    Toast.makeText(getApplicationContext(), R.string.updated, Toast.LENGTH_SHORT).show();
                } else {
                    handler.postDelayed(this, 500);
                }
            }
        });
    }


    // TODO придумать лучшую реализацую!!!
    public void sendMessage() {
        if (!isSendingMessage) {
            isSendingMessage = true;
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @SuppressLint("ShowToast")
                @Override
                public void run() {
                    if (isHtmlWorked) {
                        intent.putExtra(Intent.EXTRA_TEXT, getAllInformation());
                        startActivity(intent);
                        isSendingMessage = false;
                        isSendingToastOfMessage = false;
                    } else if (!isSendingToastOfMessage) {
                        Toast.makeText(getApplicationContext(), "Подождите", Toast.LENGTH_LONG).show();
                        isSendingToastOfMessage = true;
                        handler.postDelayed(this, 1000);
                    }
                    else {
                        handler.postDelayed(this, 1000);
                    }
                }
            });
        }
    }


    public String getAllInformation() {
        return String.format("%s\n%s\n%s\n%s\n%s\n%s", mainTitle, infectedPeoples,
                                        plusOnDay, recoveredPeoples, activeInfections, deadPeoples);
    }
}