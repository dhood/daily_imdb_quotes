package com.example.deanna.dailyimdbquotes.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by deanna on 9/3/14.
 */
public class QuoteDbHelper extends SQLiteOpenHelper

    {
        private static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "quotes.db";

        public QuoteDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

        @Override
        public void onCreate(SQLiteDatabase db) {
        // create a table to hold locations
        final String SQL_CREATE_QUOTES_TABLE = "CREATE TABLE " + QuotesContract.TABLE_NAME + " (" +
                QuotesContract._ID + " INTEGER PRIMARY KEY, " + // AUTOINCREMENT, " +
                QuotesContract.COLUMN_QUOTEID + " TEXT UNIQUE NOT NULL, " +
                QuotesContract.COLUMN_QUOTETEXT + " TEXT NOT NULL, " +
                QuotesContract.COLUMN_TITLEID + " TEXT NOT NULL, " +
                QuotesContract.COLUMN_TITLETEXT + " TEXT NOT NULL, " +
                QuotesContract.COLUMN_QUOTEINDEX + " INTEGER NOT NULL, " +
                " UNIQUE (" + QuotesContract.COLUMN_QUOTEID + ") ON CONFLICT IGNORE);";

        db.execSQL(SQL_CREATE_QUOTES_TABLE);

    }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //it's not really important data (like user-generated info) so just get rid of it
        db.execSQL("DROP TABLE IF EXISTS "+QuotesContract.TABLE_NAME);
        onCreate(db);
    }
    }
