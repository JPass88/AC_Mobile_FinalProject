package com.cst2335.finalproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @Author Jordan Passant
 * @Since 2021-04-08
 *
 * This class holds the functionality and organization of the embedded
 * device database called 'CarsDB', the single table holds columns for
 * object attributes (make,model,id)
 */

public class CarTraderOpenerActivity extends SQLiteOpenHelper {

    protected final static String DATABASE_NAME = "CarsDB";
    protected final static int VERSION_NUM = 3;
    public final static String TABLE_NAME = "CARS";
    public final static String COL_MAKE = "MAKE";
    public final static String COL_MODEL = "MODEL";
    public final static String COL_ID = "_id";

    /**
     * Creates a DB with the given name and version number
     * @param ctx Supplied context, this app instance
     */

    public CarTraderOpenerActivity(Context ctx) {
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
    }

    /**
     *  This function gets called if no database file exists.
     *  Look on your device in the /data/data/package-name/database directory.
     *
     * @param db Empty schema to create entities/attributes in
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_MAKE + " text,"
                + COL_MODEL  + " text);");  // add or remove columns
    }

    /** This function gets called if the database version on your device is lower than VERSION_NUM
     *
     * @param db DB to update
     * @param oldVersion a version number
     * @param newVersion the new version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop the old table:
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);
        //Create the new table:
        onCreate(db);
    }

    /**
     * This function gets called if the database version on your device is higher than VERSION_NUM
     *
     * @param db DB to downgrade
     * @param oldVersion current db version
     * @param newVersion version number to label new db
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop the old table:
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);
        //Create the new table:
        onCreate(db);
    }
}