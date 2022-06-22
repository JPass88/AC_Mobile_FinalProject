package com.cst2335.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

public class SongsterrEmptyActivity extends AppCompatActivity {

    /**
     * Create an empty activity so that a Fragment may be loaded.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songsterr_empty);

        Bundle songData = getIntent().getExtras();

        // Instantiate Fragment and FragmentManager, attach fragment to this empty activity
        FragmentManager fm = getSupportFragmentManager();
        SongsterrDetailsFragment detailsFragment = new SongsterrDetailsFragment();
        detailsFragment.setArguments(songData);
        fm.beginTransaction().replace(R.id.songsterr_searchResultsFragment, detailsFragment).commit();
    }
}