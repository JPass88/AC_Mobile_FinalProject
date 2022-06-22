package com.cst2335.finalproject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @Author Jordan Passant
 * @Version 1.0
 *
 * This class is enacted once the user clicks a model in the list of the Search
 * page (Selection Activity).
 * It receives a make/model to display for the user to see while deciding what
 * to do: add it to favorites, search it on google or autotrader
 * User can also perform another search from this activity as well.
 */

public class CarTraderModelDetailsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private ArrayList< CarTraderCarActivity> modelList = new ArrayList<>();
    //private AppCompatActivity parentActivity;
    int openDrawerContentDescRes;
    int closeDrawerContentDescRes;
    public static String MAKE = "Honda";
    public static String MODEL = "Civic";
    SQLiteDatabase db;
    TextView tvCar;

    /**
     * Sets up the GUI elements/views for the rest of the class
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_details);

        Intent fromSelection = getIntent();
        MAKE = fromSelection.getStringExtra("MAKE");
        MODEL = fromSelection.getStringExtra("MODEL");

        tvCar = findViewById(R.id.textviewDetailCar);
        tvCar.setText(""+MAKE+" ("+MODEL+")");

        // Load DB into RAM
        loadDataFromDatabase();

        //This gets the toolbar from the layout:
        Toolbar tBar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(tBar);

        //For NavigationDrawer:
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, tBar,
                openDrawerContentDescRes,
                closeDrawerContentDescRes);
        toggle.syncState();

        //Find navigation object, set a listener to it
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Bottom Nav Bar
        BottomNavigationView bnv = findViewById(R.id.bnv);
        bnv.setOnNavigationItemSelectedListener(this);
    }

    /**
     * Called to fill the toolbar
     * @param menu The menu/layout to inflate
     * @return placeholder*
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.help_menu, menu);
        return true;
    }

    /**
     * Toolbar item "listener", uses a switch structure to listen
     * for all of its items' state change.
     *
     * @param item User-selected toolbar icon
     * @return placeholder value
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId())
        {
            // Toolbar (HELP) button
            case R.id.help_item:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.AT_Instructions)

                        //What is the message:
                        .setMessage(R.string.AT_Details_Help)
                        .setPositiveButton(R.string.AT_Return, (click, arg) -> { })
                        .create().show();
                break;
        }
        return true;
    }

    /**
     * Needed for the OnNavigationItemSelected interface:
     * Redirects user to other pages, opens the browser with
     * a google/autotrader search and adds a vehicle to the
     * device's DB of 'Favorites'
     *
     * @param item the navigation bar item that was clicked
     * @return placeholder*
     */

    @Override
    public boolean onNavigationItemSelected( MenuItem item) {

        String url = "";
        Intent browserIntent; // = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        switch(item.getItemId())
        {
            case R.id.search_item:
                Intent goToSearch = new Intent(this, CarTraderSelectionActivity.class);
                startActivity(goToSearch);
                break;
            case R.id.db_item:
                // Intent that goes to chat page
                Intent goToFavorites = new Intent(this, CarTraderFavoritesActivity.class);
                startActivity(goToFavorites);
                break;
            case R.id.fav_item:
                /** Add to DB upon click */
                ContentValues newRowValues = new ContentValues();
                /** Put Strings in columns */
                newRowValues.put( CarTraderOpenerActivity.COL_MAKE, MAKE);
                newRowValues.put( CarTraderOpenerActivity.COL_MODEL, MODEL);
                /** Insert in db */
                long newId = db.insert( CarTraderOpenerActivity.TABLE_NAME, null, newRowValues);
                /** Create contact object with newId */
                CarTraderCarActivity newCar = new CarTraderCarActivity( MAKE, MODEL, newId);
                Toast.makeText(this, MAKE+" "+MODEL+" "+"added to favorites/ajouté aux Favoris", Toast.LENGTH_LONG).show();
                break;
            //Launch a make/model query at autotrader.ca
            case R.id.at_item:
                url = "https://www.autotrader.ca/cars/?mdl="
                        + MODEL
                        + "&make=" +MAKE+"&loc=K2G1V8";
                browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(url));
                Snackbar snackbar = Snackbar
                        .make( tvCar, "Click OK to launch browser", Snackbar.LENGTH_LONG)
                        .setAction("OK", click -> {
                            startActivity( browserIntent); //make the transition
                        });
                        snackbar.show();
                break;
            //Launch a google search for the make/model
            case R.id.google_item:
                url = "http://www.google.com/search?q="
                        +MAKE +"+"+MODEL;
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                Snackbar snackbar2 = Snackbar
                        .make( tvCar, "Click OK to launch browser", Snackbar.LENGTH_LONG)
                        .setAction("OK", click -> {
                            startActivity( browserIntent); //make the transition
                        });
                        snackbar2.show();
                break;

        }

        //Bottom navigation drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

        return false;
    }

    /**
     * Sets up and loads the DB for use in the activity
     * Establishes a connection with the DB, (creates if necessary),
     * reads the columns into a table for use in current session
     */
    private void loadDataFromDatabase()
    {
        //get a database connection:
        CarTraderOpenerActivity dbOpener = new CarTraderOpenerActivity(this);
        db = dbOpener.getWritableDatabase(); //This calls onCreate() if you've never built the table before, or onUpgrade if the version here is newer

        // We want to get all of the columns. Look at MyOpener.java for the definitions:
        String [] columns = { CarTraderOpenerActivity.COL_ID, CarTraderOpenerActivity.COL_MODEL, CarTraderOpenerActivity.COL_MAKE };
        //query all the results from the database:
        Cursor results = db.query(false, CarTraderOpenerActivity.TABLE_NAME, columns, null, null, null, null, null, null);

        //Now the results object has rows of results that match the query.
        //find the column indices:
        int makeColIndex = results.getColumnIndex(CarTraderOpenerActivity.COL_MAKE);
        int modelColumnIndex = results.getColumnIndex(CarTraderOpenerActivity.COL_MODEL);
        int idColIndex = results.getColumnIndex(CarTraderOpenerActivity.COL_ID);

        //iterate over the results, return true if there is a next item:
        while(results.moveToNext())
        {
            String make = results.getString(makeColIndex);
            String model = results.getString(modelColumnIndex);
            long id = results.getLong(idColIndex);

            //add the new Message to the array list:
            modelList.add(new CarTraderCarActivity(make, model, id));
        }

        printCursor(results, CarTraderOpenerActivity.VERSION_NUM);
        //At this point, the modelList array has loaded every row from the cursor.
    }

    /**
     * Method to log DB contents
     *
     * @param c result set of device DB query
     * @param version current version of the DB
     */
    public void printCursor(Cursor c, int version) {

        //DB version:
        Log.i("Version No", "   " + db.getVersion());
        Log.i("No of rows", "   " + c.getCount());
        Log.i("No of columns", "" + c.getColumnCount());
        Log.i("Names of the columns", "" + Arrays.toString(c.getColumnNames()));
        Log.i("DB Contents"," ");
        c.moveToFirst();
        if (c.moveToFirst()) {
            String id;
            if (c.getString(c.getColumnIndex("_id")).length() < 2)
            {
                id = " " + c.getString(c.getColumnIndex("_id"));
            }
            else {
                id = c.getString(c.getColumnIndex("_id"));
            }
            String make = c.getString(c.getColumnIndex("MAKE"));
            String model = c.getString(c.getColumnIndex("MODEL"));
            Log.i("", ""+id+"|"+make+"|"+model);
        }
        while (c.moveToNext())
        {
            String id = c.getString(c.getColumnIndex("_id"));
            String make = c.getString(c.getColumnIndex("MAKE"));
            String model = c.getString(c.getColumnIndex("MODEL"));
            Log.i("", ""+id+"|"+make+"|"+model);
        }
        c.close();
    }
}
