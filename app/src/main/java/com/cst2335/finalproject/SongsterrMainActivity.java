package com.cst2335.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

/**
 * @title SongterrMainActivity
 * @author Gabriel Matte
 * This is the main page for the Songsterr search activity.
 * It allows the user to search for an artist or band, or navigate to saved favourite songs.
 *
 */
public class SongsterrMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public final String ACTIVITY_TITLE = "Songsterr Main Activity";

    //Declare View variables
    private EditText searchText;
    private Button search;
    private Button favourites;

    // Declare Intents + SharedPrefs
    private Intent searchPage;
    private Intent favouritesPage;
    private SharedPreferences sp;

    /**
     * Sets the view for the SongsterrMainActivity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songster_main);

        //Set the toolbar for this activity with title
        Toolbar tBar = findViewById(R.id.songsterr_toolbar_layout);
        setSupportActionBar(tBar);
        setTitle(R.string.songsterr);

        //Set the Navigation Layout and listeners
        DrawerLayout drawer = findViewById(R.id.songsterr_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, tBar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navView = findViewById(R.id.songsterr_nav_view);
        navView.setNavigationItemSelectedListener(this);

        //Assign View variables to layout elements
        searchText = findViewById(R.id.songsterr_searchBar);
        search = findViewById(R.id.songsterr_searchButton);
        favourites = findViewById(R.id.songsterr_favourites);

        // Set Search Intent and button functionality
        searchPage = new Intent(this, SongsterrSearchActivity.class);
        search.setOnClickListener(click -> songsterrSearch());
        // Set Favourites Intent and button functionality
        favouritesPage = new Intent(this, SongsterrFavourites.class);
        favourites.setOnClickListener(click -> startActivityForResult(favouritesPage, 21));

        // Populates the 'Search' textbox with the last search performed
        sp = getSharedPreferences("spSearch", Context.MODE_PRIVATE);
        searchText.setText(sp.getString("lastSearch", ""));

    }

    /**
     * Instantiates a view for the toolbar and sets the title in the drawer
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.songsterr_toolbar_menu, menu);

        TextView navTitle = findViewById(R.id.songsterr_nav_title);
        navTitle.setText(ACTIVITY_TITLE);

        return true;
    }

    // Starts the Search Activity
    // Validates that user has entered a term in the search bar

    /**
     * This method starts the 'search' activity. Validates that search input is not null before moving to the search activity.
     */
    public void songsterrSearch() {
        String searchParam = searchText.getText().toString().trim();

        if (searchParam.isEmpty()) {
            String toastText = this.getResources().getString(R.string.songsterr_emptySearch);
            Toast.makeText(SongsterrMainActivity.this, toastText, Toast.LENGTH_SHORT).show();
        } else {
            searchPage.putExtra("searchParam", searchParam);
            startActivityForResult(searchPage, 22);
        }
    }

    /**
     * Stores the 'Search' input as SharedPreference
     */
    @Override
    protected void onPause() {
        super.onPause();

        sp = getSharedPreferences("spSearch", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("lastSearch", searchText.getText().toString());
        editor.commit();
    }


    /**
     * Defines that actions taken when an option in the toolbar is selected
     * @param item The toolbar item being selected
     * @return boolean - continues checking for matching item in menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.songsterr_toolbar_favourites) {
            Intent goToFavourites = new Intent(this, SongsterrFavourites.class);
            startActivity(goToFavourites);
        }

        if(item.getItemId() == R.id.songsterr_help) {

            View alertView = getLayoutInflater().inflate(R.layout.songsterr_alertdialog_layout, null);

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
                setResult(999);
                finish();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 999) {
            finish();
        }
    }


//                            Snackbar.make(searchResults, "Added to favourites", Snackbar.LENGTH_LONG)
//                                    .setAction("UNDO", clickUndo -> {
//                                    Log.e("UNDO", "Undo the add/ remove favourite");
//                                    })
//                                    .show();
}