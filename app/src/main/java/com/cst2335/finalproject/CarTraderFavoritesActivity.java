package com.cst2335.finalproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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
 * @Version 1.0
 *
 * This class is enacted once the user navigates to the Favorites
 * This class provides a list of database retrieved vehicles in a
 * listview object.
 * Functionality for this page includes options to remove the vehicle
 * from the Favorites database, or launch a browser to search the
 * vehcle on Google or AutoTrader
 * User can also perform another search from this activity as well.
 */

public class CarTraderFavoritesActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private MyListAdapter myAdapter;
    protected ArrayList< CarTraderCarActivity> modelList = new ArrayList<>();
    SQLiteDatabase db;

    /*public String makeRequest = "";
    public String tempMod = "";
    public EditText edittextEnterMake;
    TextView numModelResults;*/

    public static final String CAR_MAKE = "MAKE";
    public static final String CAR_MODEL = "MODEL";
    public static final String CAR_ID = "ID";

    /**
     * This method sets up the views in this activity
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_trader_favorites);

        /** Initialization of widgets */
        Button buttonModelSearch = findViewById(R.id.buttonModels);

        //Load data from DB (loads previously saved Message objects
        loadDataFromDatabase();
        ListView listviewModel = (ListView) findViewById(R.id.listviewModel);
        listviewModel.setAdapter(myAdapter = new MyListAdapter());

        //When a row in the list of favorited vehicles is long-clicked:
        listviewModel.setOnItemLongClickListener((p, b, pos, id) -> {

            //CarTraderCarActivity tempM = modelList.get( pos);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle( R.string.AT_Delete + modelList.get( pos).make +" "+
                    modelList.get( pos).model +R.string.AT_FromFavorites)

                    //what the Yes button does:
                    .setPositiveButton(R.string.AT_Yes, (click, arg) -> {
                        deleteCar( modelList.get( pos)); // This had to be removed as well
                        modelList.remove( pos); // remove from ListView
                        myAdapter.notifyDataSetChanged();
                    })
                    //What the No button does:
                    .setNegativeButton(R.string.AT_No, (click, arg) -> {
                    })

                    //Show the dialog
                    .create().show();
            return true;
        });

        // When a row is clicked in the listview:
        listviewModel.setOnItemClickListener(( list, items, pos, id) -> {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle( R.string.AT_SearchOnline+" \n"+ modelList.get( pos).make +" "+
                    modelList.get( pos).model +"':")

                    //what the Google button does:
                    .setPositiveButton("Google", (click, arg) -> {
                        final String url = "http://www.google.com/search?q="
                                +modelList.get( pos).make +"+"+
                                modelList.get( pos).model;
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity( browserIntent);        //make the transition
                    })
                    //What the AutoTrader button does:
                    .setNegativeButton("AutoTrader", (click, arg) -> {
                        final String url = "https://www.autotrader.ca/cars/?mdl="
                                + modelList.get( pos).model
                                + "&make="+modelList.get( pos).make+"&loc=K2G1V8";
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(url));
                        startActivity( browserIntent);        //make the transition
                    })

                    //Show the dialog
                    .create().show();
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
            return "" +modelList.get(i);
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
            View modelRow = inflater.inflate(R.layout.car_favorites_row, viewGroup, false);
            // Set the row texts
            TextView textviewMake = modelRow.findViewById(R.id.textviewRowMake);
            textviewMake.setText( modelList.get( i).getMake());
            TextView textviewModel = modelRow.findViewById(R.id.textviewRowModel);
            textviewModel.setText( modelList.get( i).getModel());

            return modelRow;
        }
    } // end of Inner (Adapter) class

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

        //String colCount = c.getColumnCount();
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

    /**
     * Method that alters an existing Car object's
     * model or make
     * @param c Car object
     */
    protected void updateCar(CarTraderCarActivity c)
    {
        //Create a ContentValues object to represent a database row:
        ContentValues updatedValues = new ContentValues();
        updatedValues.put( CarTraderOpenerActivity.COL_MAKE, c.getMake());
        updatedValues.put( CarTraderOpenerActivity.COL_MODEL, c.getModel());

        //now call the update function:
        db.update( CarTraderOpenerActivity.TABLE_NAME, updatedValues,
                CarTraderOpenerActivity.COL_ID + "= ?",
                new String[] {Long.toString( c.getId())});
    }

    /**
     * Deletes a car from the DB
     * @param c Car object
     */
    protected void deleteCar(CarTraderCarActivity c)
    {
        db.delete( CarTraderOpenerActivity.TABLE_NAME, CarTraderOpenerActivity.COL_ID + "= ?",
                new String[] {Long.toString(c.getId())});
    }

    /** Needed for the OnNavigationItemSelected interface:
     *  Uses a switch to listen for clicked navigation bar
     *  items SEARCH->Search page, HELP->Help dialog when
     *  clicked
     *
     * @param item The clickable icon at bottom of screen
     * @return No purpose, satisfies the inherited method
     */
    @Override
    public boolean onNavigationItemSelected( MenuItem item) {

        String message = null;

        switch(item.getItemId())
        {
            case R.id.search_item:
                Intent nextActivity = new Intent( CarTraderFavoritesActivity.this,
                        CarTraderSelectionActivity.class);
                startActivity( nextActivity);        //make the transition
                break;
            case R.id.help_item:

                /**
                 *   Upon clicking the 'Help' button, an alert dialog box is launched
                 */

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.AT_Instructions)

                        //What is the message:
                        .setMessage(R.string.AT_Favorited_Help)
                        /**
                         *   Return button launches a helpful toast 'message'
                         */
                        .setPositiveButton(R.string.AT_Return, (click, arg) -> { })
                        .create().show();
                break;
        }
        return false;
    }

} // end of main class (CarTraderSelectionActivity.java)