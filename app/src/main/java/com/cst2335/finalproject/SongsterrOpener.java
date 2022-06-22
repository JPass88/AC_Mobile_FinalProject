package com.cst2335.finalproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Create an manipulate locally stored Database
 * @author Gabriel Matte
 */
public class SongsterrOpener extends SQLiteOpenHelper {

    protected final static String DATABASE_NAME = "SongsterrFavouritesDB";
    protected final static int VERSION_NUM = 12;
    public final static String TABLE_NAME = "FAVOURITES";
    public final static String COL_SONG_ID = "SONG_ID";
    public final static String COL_ARTIST_ID = "ARTIST_ID";
    public final static String COL_TITLE = "TITLE";
    public final static String COL_ARTIST_NAME = "ARTIST_NAME";
    public final static String COL_ID = "_id";

    /**
     * Default constructor. Allows the Opener to interact with a database specific to this application
     * @param ctx
     */
    public SongsterrOpener(Context ctx) {
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
    }

    /**
     * Creates a Table for the specified Database
     * @param db Locally stored database instance
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + COL_SONG_ID + " INTEGER NOT NULL PRIMARY KEY, "
                + COL_ARTIST_ID + " INTEGER NOT NULL, "
                + COL_TITLE + " text NOT NULL, "
                + COL_ARTIST_NAME + " text NOT NULL);");
    }

    /**
     * Creates a new version of the database and removes the old version
     * @param db specified locally stored database
     * @param oldVersion integer representing the version number of the existing database
     * @param newVersion integer representing the version number of the new database to be created
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }
}
