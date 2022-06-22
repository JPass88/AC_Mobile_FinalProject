package com.cst2335.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static org.xmlpull.v1.XmlPullParser.END_TAG;

/**
 * @title SongsterrSearchActivity
 * @author Gabriel Matte
 * This is the search activity that displays the results of the user-specified search.
 */
public class SongsterrSearchActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public final String ACTIVITY_TITLE = "Songsterr Search Activity";

    private Intent getSearchParam;
    private ListView searchResults;
    private ArrayList<Song> songList = new ArrayList<>();
    private MyListAdapter adapter;
    private String urlString;
    private ProgressBar progBar;
    private boolean isTablet;
    private SongsterrDetailsFragment detailsFragment;
    private SQLiteDatabase database;


    /**
     * Instantiates the view elements and executes the search. Gets the user input from SongsterrMainActivity to include in URL to be searched.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songsterr_search);

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

        // Find the progress bar and set visibility to visible
        progBar = findViewById(R.id.songsterr_searchProgress);
        progBar.setVisibility(progBar.VISIBLE);

        // Get the user-inputted search text from the previous page
        getSearchParam = getIntent();
        String searchParam = getSearchParam.getStringExtra("searchParam");

        // Find the ListView in Layout and set the adapter
        searchResults = (ListView)findViewById(R.id.songsterr_searchResults);
        searchResults.setAdapter(adapter = new MyListAdapter());

        // Append the user-input to the songsterr song search URL
        urlString = "https://www.songsterr.com/a/ra/songs.xml?pattern=" + searchParam;
        SearchQuery sQuery = new SearchQuery();
        sQuery.execute(urlString);

        // Set click listener to display song details of item in the list
        // Display is dependent on display size (tablet vs phone)
        searchResults.setOnItemClickListener( (p, b, pos, id) -> {

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
                Intent goToDetails = new Intent(SongsterrSearchActivity.this, SongsterrEmptyActivity.class);
                goToDetails.putExtras(songData);
                startActivity(goToDetails);
            }
        });

        // *** NOTE ***
        // Need to implement the add / remove favourite function. Need to translate the strings and add them to res.
        searchResults.setOnItemLongClickListener( (p, b, pos, id) -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(songList.get(pos).getTitle())
                    .setMessage("Artist: " + songList.get(pos).getArtistName() + "\n" + "Songsterr song ID: " + songList.get(pos).getSongID() + "\n" + "Songsterr Artist ID: " + songList.get(pos).getArtistID())
                    .setPositiveButton("Add to favourites", (click, arg) -> {
                        Snackbar.make(searchResults, "Added to favourites", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", clickUndo -> {
                                    Log.e("UNDO", "Undo the add/ remove favourite");
                                })
                                .show();

                    })
                    .setNeutralButton("Close", (click, arg) -> {});

            builder.create().show();
            return true;
        });

        // TEST LIST FUNCTIONALITY
//        Song s = new Song("1", "11", "TEST");
//
//        songList.add(s);
//
//        adapter.notifyDataSetChanged();

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
         * @return The number of items in the list
         */
        @Override
        public int getCount() {
            return songList.size();
        }

        /**
         * @param position Index of the element in the list
         * @return The element at specified index
         */
        @Override
        public Object getItem(int position) {
            return songList.get(position);
        }

        /**
         *
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

            return songElement;
        }
    }

    /**
     * Searches the specified URL for song details and stores them in a list.
     * This is an Asynchronous task that will run on a separate thread.
     */
    class SearchQuery extends AsyncTask< String, Integer, String> {

        private ArrayList<Song> songListResult = new ArrayList<>();
        private String songID;
        private String artistID;
        private String songTitle;
        private String artistName;

        /**
         * This method will search the specified URL line-by-line and store data for each song as a Song object in a list.
         *
         * @param args The URL string representation to be searched
         * @return String notifying that the method has finished running
         */
        @Override
        public String doInBackground(String... args) {

            try {
                URL url = new URL(args[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                InputStream response = connection.getInputStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(response, "UTF-8");

                int eventType = xpp.getEventType();
                int progress = 0;

                // Continues iterating through the XML file until end of document is found
                while (eventType != XmlPullParser.END_DOCUMENT) {

                    if (eventType == XmlPullParser.START_TAG) {

                        // When a 'Song' tag is found, ID is stored and a new loop begins until all relevant song details are found.
                        if (xpp.getName().equals("Song")) {
                            songID = xpp.getAttributeValue(null, "id");

                            int found = 1;

                            //
                            while (found <4) {

                                if (eventType == XmlPullParser.START_TAG) {

                                    if (xpp.getName().equals("title")) {
                                        xpp.next();
                                        songTitle = xpp.getText();
                                        found++;

                                    } else if (xpp.getName().equals("artist")) {
                                        artistID = xpp.getAttributeValue(null, "id");
                                        found++;
                                    } else if (xpp.getName().equals("name")) {
                                        xpp.next();
                                        artistName = xpp.getText();
                                        found++;
                                    }

                                }

                                eventType = xpp.next();

                            }

                            // Creates a new Song object and adds it to a list
                            // Publish progress to update progress bar
                            Song s = new Song(songID, artistID, songTitle, artistName, false);
                            songListResult.add(s);
                            progress += 5;
                            publishProgress(progress);

                        }

                    }
                    eventType = xpp.next();
                }

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
                Log.e("EXCEPTION", "Something went wrong in the XML Reading");
            }
            return "Search Complete";
        }

        /**
         * Updates the progress bar on the page as song data is found
         *
         * @param args The number (%) of progress made
         */
        public void onProgressUpdate(Integer ... args) {
            progBar.setProgress(args[0]);
        }

        // Passes the list of search results to the main thread to be displayed in the list

        /**
         * Retrieves the temporary song list from the background thread and adds each element to the list in the main thread.
         *
         * @param resultMessage Message that indicates the background task is complete
         */
        public void onPostExecute(String resultMessage) {

            progBar.setProgress(100);

            getDatabaseConnection();

            for(int i=0; i < songListResult.size(); i++) {
                String songId = songListResult.get(i).getSongID();
                songListResult.get(i).setFavourite(checkIsFavourite(songId));
                songList.add(songListResult.get(i));
            }
            adapter.notifyDataSetChanged();

            progBar.setVisibility(progBar.INVISIBLE);
            Log.i("QUERY RESPONSE", resultMessage);
        }
    }

    /**
     * Gets the local 'Favourites' database to be used in the activity
     */
    public void getDatabaseConnection() {
        SongsterrOpener dbOpener = new SongsterrOpener(this);
        database = dbOpener.getWritableDatabase();
    }

    /**
     * Checks the local 'Favourites' database to determine if the song with the given ID is stored
     * @param songId Song ID of a song obtained from Songsterr
     * @return Song stored in local database - boolean
     */
    public boolean checkIsFavourite(String songId) {
        String [] columns = {SongsterrOpener.COL_SONG_ID};
        Cursor results = database.query(false, SongsterrOpener.TABLE_NAME, columns, SongsterrOpener.COL_SONG_ID + " =?", new String[] {songId}, null, null, null, null);
        if (results.moveToNext()) {
            return true;
        } else {
            return false;
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