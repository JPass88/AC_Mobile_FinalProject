package com.cst2335.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public final String ACTIVITY_TITLE = "CST2335 Final Project - Main Page";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_alternate);

        //Set the toolbar for this activity with title
        Toolbar tBar = findViewById(R.id.main_toolbar_layout);
        setSupportActionBar(tBar);
        setTitle(R.string.project_title);

        //Set the Navigation Layout and listeners
        DrawerLayout drawer = findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, tBar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navView = findViewById(R.id.songsterr_nav_view);
        navView.setNavigationItemSelectedListener(this);

        // Trivia App Launch Button
        ImageButton triviaButton = findViewById(R.id.trivia_button);
        triviaButton.setOnClickListener(v-> {
            Intent goToTrivia = new Intent(MainActivity.this, TriviaMainActivity.class);
            startActivity(goToTrivia);
        });

        // Songster App Launch Button
        ImageButton songsterButton = findViewById(R.id.songsterr_button);
        songsterButton.setOnClickListener(v-> {
            Intent goToSongster = new Intent(MainActivity.this, SongsterrMainActivity.class);
            startActivityForResult(goToSongster, 20);
        });

        // Car Trader App Launch Button
        ImageButton carTraderButton = findViewById(R.id.cartrader_button);
        carTraderButton.setOnClickListener(v-> {
            Intent goToCarTrader = new Intent(MainActivity.this, CarTraderMainActivity.class);
            startActivity(goToCarTrader);
        });

        // Soccer App Launch Button
        ImageButton soccerButton = findViewById(R.id.soccer_button);
        soccerButton.setOnClickListener(v-> {
            Intent goToSoccer = new Intent(MainActivity.this, SoccerMainActivity.class);
            startActivity(goToSoccer);
        });
    }



    /**
     * Instantiates a view for the toolbar and sets the title in the drawer
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar_menu, menu);

        TextView navTitle = findViewById(R.id.main_nav_title);
        navTitle.setText(ACTIVITY_TITLE);

        return true;
    }

    /**
     * Defines that actions taken when an option in the toolbar is selected
     * @param item The toolbar item being selected
     * @return boolean - continues checking for matching item in menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if(item.getItemId() == R.id.main_help) {

            View alertView = getLayoutInflater().inflate(R.layout.main_help_layout, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.help);

            builder.setView(alertView);
            builder.setPositiveButton("Close", (click, arg) -> {});
            builder.create().show();
        }

        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.go_to_autotrader:
                Intent goToAuto = new Intent(this, CarTraderMainActivity.class);
                startActivity(goToAuto);
                break;
            case R.id.go_to_soccer:
                Intent goToSoccer = new Intent(this, SoccerMainActivity.class);
                startActivity(goToSoccer);
                break;
            case R.id.go_to_trivia:
                Intent goToTrivia = new Intent(this, TriviaMainActivity.class);
                startActivity(goToTrivia);
                break;
            case R.id.go_to_songsterr:

                break;
            case R.id.go_home:
                break;
        }
        return false;
    }
}