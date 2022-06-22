package com.cst2335.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

/**
 * Shows the user's stored favourite songs and provides functionality to remove songs from database or to follow links to external Songsterr website
 * @author Gabriel Matte
 */
public class SongsterrFavourites extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public final String ACTIVITY_TITLE = "Songsterr Favourites Activity";

    private ListView favouritesResults;
    private ArrayList<Song> songList = new ArrayList<>();
    private MyListAdapter adapter;
    private boolean isTablet;
    private SongsterrDetailsFragment detailsFragment;
    private SQLiteDatabase database;

    /**
     * Sets the layout and loads data from the database
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songsterr_favourites);

        // Check if the app is running on a tablet
        if (findViewById(R.id.songsterr_searchResultsFragment) != null) {
            isTablet = true;
        } else {
            isTablet = false;
        }

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

        favouritesResults = findViewById(R.id.songsterr_favouritesResults);
        favouritesResults.setAdapter(adapter = new MyListAdapter());

        loadFromDatabase();

        favouritesResults.setOnItemClickListener( (p, b, pos, id) -> {

            Bundle songData = new Bundle();
            songData.putString("SongID", songList.get(pos).getSongID());
            songData.putString("ArtistID", songList.get(pos).getArtistID());
            songData.putString("Title", songList.get(pos).getTitle());
            songData.putString("ArtistName", songList.get(pos).getArtistName());
            songData.putBoolean("Favourite", songList.get(pos).getFavourite());

            FragmentManager fm = getSupportFragmentManager();

            if (isTablet) {
                detailsFragment = new SongsterrDetailsFragment();
                detailsFragment.setArguments(songData);
                fm.beginTransaction().replace(R.id.songsterr_searchResultsFragment, detailsFragment).commit();
            } else {
                Intent goToDetails = new Intent(SongsterrFavourites.this, SongsterrEmptyActivity.class);
                goToDetails.putExtras(songData);
                startActivity(goToDetails);
            }
        });


    }

    private long addToDatabase(String songId, String artistId, String title, String artistName) {
        ContentValues newRowValues = new ContentValues();

        newRowValues.put(SongsterrOpener.COL_SONG_ID, songId);
        newRowValues.put(SongsterrOpener.COL_ARTIST_ID, artistId);
        newRowValues.put(SongsterrOpener.COL_TITLE, title);
        newRowValues.put(SongsterrOpener.COL_ARTIST_NAME, artistName);

        return database.insert(SongsterrOpener.TABLE_NAME, null, newRowValues);
    }

    /**
     * Deletes the song from the 'Favourites' database
     * @param id Song ID - primary key in the database
     */
    private void deleteFromDatabase(String id) {
        database.delete(SongsterrOpener.TABLE_NAME, SongsterrOpener.COL_SONG_ID + " = ?", new String [] {id});
    }

    /**
     * This method pulls the song data saved in the 'Favourites' database to be displayed on screen
     * Sets the 'favourite' parameter of each song to 'true' because it is being loaded from the 'Favourites' database
     */
    private void loadFromDatabase() {

        SongsterrOpener dbOpener = new SongsterrOpener(this);
        database = dbOpener.getWritableDatabase();

        // Declare column names
        String [] columns = {SongsterrOpener.COL_SONG_ID, SongsterrOpener.COL_ARTIST_ID, SongsterrOpener.COL_TITLE, SongsterrOpener.COL_ARTIST_NAME};
        Cursor results = database.query(false, SongsterrOpener.TABLE_NAME, columns, null, null, null, null, null, null);

        int songIdColIndex = results.getColumnIndex(SongsterrOpener.COL_SONG_ID);
        int artistIdColIndex = results.getColumnIndex(SongsterrOpener.COL_ARTIST_ID);
        int titleColIndex = results.getColumnIndex(SongsterrOpener.COL_TITLE);
        int artistNameColIndex = results.getColumnIndex(SongsterrOpener.COL_ARTIST_NAME);

        while(results.moveToNext()) {
            String songId = Integer.toString(results.getInt(songIdColIndex));
            String artistId = Integer.toString(results.getInt(artistIdColIndex));
            String title = results.getString(titleColIndex);
            String artistName = results.getString(artistNameColIndex);

            songList.add(new Song(songId, artistId, title, artistName, true));
        }
    }

    /**
     * Details of a song obtained from Songsterr
     */
    class Song {
        private String songID;
        private String artistID;
        private String title;
        private String artistName;
        private boolean favourite;

        /**
         * Constructor for Song class. Parameters store details obtained from Songsterr
         * @param sID Song ID
         * @param aID Artist ID
         * @param title Song Title
         * @param artistName Artist Name
         * @param favourite Stored in 'Favourites' database - boolean
         */
        public Song(String sID, String aID, String title, String artistName, boolean favourite) {
            this.songID = sID;
            this.artistID = aID;
            this.title = title;
            this.artistName = artistName;
            this.favourite = favourite;
        }


        public String getSongID() { return songID; }
        public String getArtistID() { return artistID; }
        public String getTitle() { return title; }
        public String getArtistName() { return artistName; }
        public boolean getFavourite() { return favourite; }

        public void setFavourite(boolean fav) {
            this.favourite = fav;
        }
    }

    /**
     *
     */
    class MyListAdapter extends BaseAdapter {

        /**
         * Gets the number of items in the list
         * @return The number of items in the list
         */
        @Override
        public int getCount() {
            return songList.size();
        }

        /**
         * Gets the object in the list stored at the specified location
         * @param position Index of the element in the list
         * @return The element at specified index
         */
        @Override
        public Object getItem(int position) {
            return songList.get(position);
        }

        /**
         * Gets the database ID of the object in the list stored at the specified position
         * @param position Index of the element in the list
         * @return The database ID of the element
         */
        @Override
        public long getItemId(int position) {
            return 0;
        }

        /**
         * Sets how the element is displayed using LayoutInflater.
         *
         * @param position Index of the element in the list
         * @param convertView
         * @param parent
         * @return
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            Song s = (Song)getItem(position);

            View songElement = inflater.inflate(R.layout.songsterr_song_element, parent, false);
            TextView songTitle = songElement.findViewById(R.id.songsterr_song_element_title);
            songTitle.setText(s.getTitle());
            TextView artistName = songElement.findViewById(R.id.songsterr_song_element_artist);
            artistName.setVisibility(View.VISIBLE);
            artistName.setText(s.getArtistName());

            return songElement;
        }
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
                finish();
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
}