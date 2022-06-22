package com.cst2335.finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * This fragment displays information about a song and provides functionality to store a song in Favourites or to follow external links to the Songsterr website
 * @author Gabriel Matte
 */
public class SongsterrDetailsFragment extends Fragment {

    private AppCompatActivity parentActivity;
    private SQLiteDatabase database;
    private Bundle songData;
    private String songID;
    private String artistID;
    private String title;
    private String artistName;
    private Boolean isFavourite;



    /**
     * Default constructor
     */
    public SongsterrDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Creates the view to display song details
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        songData = getArguments();
        songID = songData.getString("SongID");
        artistID = songData.getString("ArtistID");
        title = songData.getString("Title");
        artistName = songData.getString("ArtistName");
        isFavourite = songData.getBoolean("Favourite");

        getDatabaseConnection();

        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_songsterr_details, container, false);

        // Set text according to the song information passed into the fragment
        TextView titleView = (TextView)result.findViewById(R.id.songsterr_details_songTitle);
        titleView.setText(title);

        TextView artistNameView = (TextView)result.findViewById(R.id.songsterr_details_artistName);
        artistNameView.setText(artistName);

        TextView songIdView = (TextView)result.findViewById(R.id.songsterr_details_songID);
        songIdView.setText(getString(R.string.songID) + " " + songID);

        TextView artistIdView = (TextView)result.findViewById(R.id.songsterr_details_artistID);
        artistIdView.setText(getString(R.string.artistID) + " " + artistID);

        Button visitSongPage = (Button)result.findViewById(R.id.songsterr_details_songLink);
        visitSongPage.setOnClickListener( click -> goToURL("song", songID));

        Button visitArtistPage = (Button)result.findViewById(R.id.songsterr_details_artistLink);
        visitArtistPage.setOnClickListener( click -> goToURL("artist", artistID));

        Button favouriteButton = (Button)result.findViewById(R.id.songsterr_details_favourites);
        setFavouriteButtonText(favouriteButton);

        favouriteButton.setOnClickListener( click -> {
            if (isFavourite) {
                deleteFromDatabase(songID);
                isFavourite = false;
            } else {
                addToDatabase(songID, artistID, title, artistName);
                isFavourite = true;
            }
            setFavouriteButtonText(favouriteButton);
        });



        return result;
    }

    /**
     * Attaches the Fragment to the parent
     * @param context The app's currently loaded context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        parentActivity = (AppCompatActivity)context;
    }

    /**
     * Changes the text of the 'Favourite' button to 'Add' or 'Remove' based on whether it is already in the DB or not
     * @param b The button for which text will be changed
     */
    public void setFavouriteButtonText(Button b) {
        if (isFavourite) {
            b.setText(getString(R.string.songsterr_removeFromFavourites));
        } else {
            b.setText(getString(R.string.songsterr_addToFavourites));
        }
    }

    public void goToURL(String pageToVisit, String id) {
        String url = "http://www.songsterr.com/a/wa/";
        url += pageToVisit;
        url += "?id=";
        url += id;

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    /**
     * Gets the local 'Favourites' database to be used in the activity
     */
    private void getDatabaseConnection() {
        SongsterrOpener dbOpener = new SongsterrOpener(parentActivity);
        database = dbOpener.getWritableDatabase();
    }

    private long addToDatabase(String songId, String artistId, String title, String artistName) {
        ContentValues newRowValues = new ContentValues();

        newRowValues.put(SongsterrOpener.COL_SONG_ID, songId);
        newRowValues.put(SongsterrOpener.COL_ARTIST_ID, artistId);
        newRowValues.put(SongsterrOpener.COL_TITLE, title);
        newRowValues.put(SongsterrOpener.COL_ARTIST_NAME, artistName);

        return database.insert(SongsterrOpener.TABLE_NAME, null, newRowValues);
    }

    private void deleteFromDatabase(String songId) {
        database.delete(SongsterrOpener.TABLE_NAME, SongsterrOpener.COL_SONG_ID + "= ?", new String [] {songId});
    }
}