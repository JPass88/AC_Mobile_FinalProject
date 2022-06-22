package com.cst2335.finalproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SoccerDBOpener extends SQLiteOpenHelper {

    protected static final String DATABASE_NAME = "SoccerDB";
    protected static final int VERSION_NUM = 4;
    protected static final String TABLE_NAME = "SoccerTable";
    protected static final String ARTICLE_TITLE = "ArticleTitle";
    protected static final String ARTICLE_URL = "ArticleURL";
    protected static final String IMAGE_URL = "ImageURL";
    protected static final String ARTICLE_DATE = "ArticleDate";
    protected static final String ARTICLE_DESC = "ArticleDesc";

    public SoccerDBOpener(Context ctx) {
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ARTICLE_URL + " TEXT," +
                IMAGE_URL + " TEXT," +
                ARTICLE_TITLE + " TEXT," +
                ARTICLE_DATE + " TEXT," +
                ARTICLE_DESC + " TEXT );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
