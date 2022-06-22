package com.cst2335.finalproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import android.view.MenuItem;

/**
 * @Author Jordan Passant
 * @since 2021-04-08
 * CarTraderSelectionActivity is the 'Search' page for user to input
 * a vehicle make. It retrieves an XML object with pertinent model
 * listings and is displayed in a list for the user. S/he can then
 * click a model to view additional options/details in a different
 * activity.
 */

public class CarTraderSelectionActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    SharedPreferences prefs = null;

    private MyListAdapter myAdapter;

    public String makeRequest = "";
    public String tempMod = "";
    public EditText edittextEnterMake;
    public int clkCntr = 0;

    public static final String CAR_MAKE = "MAKE";
    public static final String CAR_MODEL = "MODEL";

    protected ArrayList< String> modelList = new ArrayList< String>( Arrays.asList());

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_trader_selection);

        /** Initialization of widgets */
        EditText enterMake = findViewById(R.id.edittextEnterMake);
        Button buttonModelSearch = findViewById(R.id.buttonModels);
        edittextEnterMake = findViewById(R.id.edittextEnterMake);
        ListView listviewModel = (ListView) findViewById(R.id.listviewModel);
        listviewModel.setAdapter(myAdapter = new MyListAdapter());

        // Shared Preferences
        prefs = getSharedPreferences("FileName", Context.MODE_PRIVATE);
        String savedString = prefs.getString("ReserveName", "Default Value");
        enterMake.setText(savedString);

        CarQuery req = new CarQuery();

        /** 'Search for models' Button */
        buttonModelSearch.setOnClickListener(bt -> {

            // Save search item in shared preferences
            saveSharedPrefs(enterMake.getText().toString());

            // If search button has been clicked, reset this activity to avoid crashing
            if( clkCntr > 0) {
                Intent nextActivity = new Intent( CarTraderSelectionActivity.this,
                        CarTraderSelectionActivity.class);
                startActivity( nextActivity);        //make the transition
            }
            // If first time clicking the search button, conduct ASYNC search
            else {
                // Take make from search bar
                makeRequest = edittextEnterMake.getText().toString();
                edittextEnterMake.onEditorAction(EditorInfo.IME_ACTION_DONE);

                /** Initiate ASYNC task */

                // If search field is empty, use 'Honda' as example make search
                if (makeRequest.equals("")) {
                    Toast.makeText(this,
                            getString(R.string.AT_Search_Toast),
                            Toast.LENGTH_LONG).show();
                    clkCntr++;
                    req.execute("https://vpic.nhtsa.dot.gov/api/vehicles/GetModelsForMake/honda?format=xml");
                } else {
                    clkCntr++;
                    req.execute("https://vpic.nhtsa.dot.gov/api/vehicles/GetModelsForMake/" + makeRequest + "?format=xml");
                }
            }
        });

        /** Send user to details(intent) upon clicking listview item */
        listviewModel.setOnItemClickListener(( list, items, position, id) -> {

            // If empty search, use Honda as the make to send as 'extra' info
            if( makeRequest.equals("")) { makeRequest = "Honda"; }
            tempMod = modelList.get(position);

            // Send make+model to intent (details page) with make/model
            Intent nextActivity = new Intent( CarTraderSelectionActivity.this,
                    CarTraderModelDetailsActivity.class);
            nextActivity.putExtra( CAR_MAKE, "" +makeRequest);
            nextActivity.putExtra( CAR_MODEL, "" +tempMod);
            clkCntr = 0;
            startActivity( nextActivity);        //make the transition
        });

        // Bottom Nav Bar
        BottomNavigationView bnv = findViewById(R.id.bnv);
        bnv.setOnNavigationItemSelectedListener(this);
    }

    /**
     * Inner class (MyListAdapter)
     *
     * @Author Jordan Passant
     * @Since 2021-04-08
     *
     * This class provides functionality of the listview, retrieves size
     * of list connected to the listview, items of the list, displays
     * list items in the listview, etc.
     */
    private class MyListAdapter extends BaseAdapter
    {
        /**
         * Getter method for no. of elements
         * @return number of items in the list of models
         */
        @Override
        public int getCount() {
            return modelList.size();
        }

        /**
         * Returns the item at specified position
         * @param i row number
         * @return The requested object
         */
        @Override
        public String getItem( int i) {
            return ""+modelList.get(i);
        }

        /**
         * Getter for the database id of the requested row's item
         * @param i row number
         * @return The database ID
         */
        @Override
        public long getItemId( int i) {
            return (long) i;
        }

        /**
         * Creates a view object (a row) to be placed in the listview
         * @param i Row number
         * @param view Object type to be inserted, (a row)
         * @param viewGroup Contains the other rows, (listview)
         * @return Returns the row for the listview (a model)
         */
        @Override
        public View getView( int i, View view, ViewGroup viewGroup) {
            // Constructor for creating an inflater for this class
            LayoutInflater inflater = getLayoutInflater();
            // Make a new row
            View modelRow = inflater.inflate(R.layout.car_trader_selection_model_row, viewGroup, false);
            // Set the row texts
            TextView listviewModel = modelRow.findViewById(R.id.listviewRow);
            listviewModel.setText( getItem( i).toString());
            return modelRow;
        }
    } // end of Inner (Adapter) class

    /** Innerclass : CarQuery
     *
     * @Author Jordan Passant
     * @Since 2021-04-08
     *
     * This class contains the ASYNC methods for the Async (thread) performing
     * the http request, grabs an XML object in the background, converts it to
     * a readable array format for parsing with key/value pairings
     */
    public class CarQuery extends AsyncTask< String, Integer, String> {

        String modelName;

        /**
         * Connects to the server, grabs the XML, parses the XML
         * adds each model to a list
         * @param args the URL
         * @return A (String)boolean for successful parse op
         */

        public String doInBackground( String... args) {
            try {
                //create a URL object of what server to contact:
                URL url = new URL(args[0]);

                //open the connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //wait for data:
                InputStream response = urlConnection.getInputStream();

                //Create a pull parser using factory pattern
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(response, "UTF-8");

                // Inspect the element
                int eventType = xpp.getEventType(); //The parser is currently at START_DOCUMENT

                while (eventType != XmlPullParser.END_DOCUMENT) {

                    if (eventType == XmlPullParser.START_TAG) {
                            if (xpp.getName().equals("Model_Name")) {

                                xpp.next(); // Because there are no 'values', have to call next event
                                modelName = xpp.getText();
                                //Log.i( "model", modelName);
                                modelList.add(modelName);
                            }
                    }
                    eventType = xpp.next(); //move to the next xml event and store it in a variable
                }
            }
            catch (Exception e) {
                Log.i("EXCEPTION", "Parsing exception");
            }
            return "Done";
        }

        /**
         * Not used. For Period GUI updates
         * @param args
         */
        //Type 2
        public void onProgressUpdate(Integer ... args) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * Final GUI Update, in this case, the listview of
         * models is filled in from the requested vehicle make
         * @param fromDoInBackground (success?)
         */
        //Type3
        public void onPostExecute(String fromDoInBackground) {

            // Redraw listview with updated list
            myAdapter.notifyDataSetChanged();
        }
    } // eo class ForecastQuery

    /** Needed for the OnNavigationItemSelected interface:
     *  Uses a switch to listen for clicked navigation bar
     *  items.
     *
     * @param item The clickable icon at bottom of screen
     * @return No purpose, satisfies the inherited method
     */
    @Override
    public boolean onNavigationItemSelected( MenuItem item) {

        switch(item.getItemId()) {

            // Go to Favorites if clicked
            case R.id.db_item:
                Intent nextActivity = new Intent( CarTraderSelectionActivity.this,
                        CarTraderFavoritesActivity.class);
                clkCntr = 0;
                startActivity( nextActivity);        //make the transition
                break;
            case R.id.help_item:

                /** Upon clicking the 'Help' button, an alert dialog box is launched */

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.AT_Instructions)

                    //What is the message:
                    .setMessage(R.string.AT_Search_Help)

                    .setPositiveButton(R.string.AT_Return, (click, arg) -> { })
                    .create().show();
            break;
        }
        return false;
    }

    /**
     * Opens the shared preferences object for read/write operations
     * @return the shared preferences object
     */
    protected SharedPreferences loadPreferences() {
        return getSharedPreferences("MakeName", Context.MODE_PRIVATE);
    }

    /**
     * Saves the user-entered edittext String in the sharedPrefs object
     * @param stringToSave The associated value for the key, 'ReserveName'
     */
    private void saveSharedPrefs(String stringToSave) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ReserveName", stringToSave);
        editor.commit();
    }

} // end of main class (CarTraderSelectionActivity.java)