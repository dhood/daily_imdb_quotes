package com.example.deanna.dailyimdbquotes.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


/**
 * Created by deanna on 9/3/14.
 */
public class QuoteProvider extends ContentProvider {

    //types of queries we support
    private static final int QUOTES = 100;
    private static final int QUOTE_FROM_TITLE_ID = 300;
    private static final int QUOTE_ID = 301;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private QuoteDbHelper mOpenHelper;

    private static final SQLiteQueryBuilder sQuotesByTitleQueryBuilder;

    //join the two tables by the location
    static {
        sQuotesByTitleQueryBuilder = new SQLiteQueryBuilder();
        sQuotesByTitleQueryBuilder.setTables(QuotesContract.TABLE_NAME);
    }

    private static final String sQuoteAtIndexFromTitleIdSelection =
            QuotesContract.COLUMN_TITLEID + " = ? AND " +
                    QuotesContract.COLUMN_QUOTEINDEX + " = ? ";

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(QuotesContract.CONTENT_AUTHORITY, QuotesContract.PATH_QUOTES, QUOTES);
        uriMatcher.addURI(QuotesContract.CONTENT_AUTHORITY, QuotesContract.PATH_QUOTES + "/*/#", QUOTE_FROM_TITLE_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new QuoteDbHelper(getContext());
        return true; //indicate successful creation of content provider
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case QUOTES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(QuotesContract.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }

    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case QUOTE_FROM_TITLE_ID:
                return QuotesContract.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor returnCursor;
        switch (sUriMatcher.match(uri)) {
            case QUOTES:
                returnCursor = mOpenHelper.getReadableDatabase().query(
                        QuotesContract.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case QUOTE_FROM_TITLE_ID:
                return getQuoteFromTitleId(uri, projection, sortOrder);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        //cursor will watch for changes (with content observer) on uri or descendants
        returnCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return returnCursor;
    }


    private Cursor getQuoteFromTitleId(Uri uri, String[] projection, String sortOrder) {
        String titleId = QuotesContract.getTitleIdFromUri(uri);
        long index = QuotesContract.getQuoteIndexFromUri(uri);

        String[] selectionArgs = new String[]{titleId, Long.toString(index)};
        String selection = sQuoteAtIndexFromTitleIdSelection;

        return sQuotesByTitleQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }


    @Override
    public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numberRowsAffected;
        switch (match) {
            // remember they said that this was supposed to be the base uri for the best observer performance
            case QUOTES:
                numberRowsAffected = db.update(
                        QuotesContract.TABLE_NAME,
                        values,
                        whereClause,
                        whereArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(numberRowsAffected > 0) {
            getContext().getContentResolver().notifyChange(uri, null); //notify ALL registered ContentObservers
        }
        return numberRowsAffected;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //For inserting, we only match the root of the URI because cursors will be registered as
        //notify for descendents, and therefore notifying on root uri will ensure all cursors will
        //get updated. If we did it on a descendent, cursors listening to root wouldn't update.
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case QUOTES:
                long _id = db.insert(QuotesContract.TABLE_NAME, null, values);
                if( _id > 0 ){
                    returnUri = QuotesContract.buildQuoteUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null); //notify ALL registered ContentObservers

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numberRowsAffected;
        switch (match) {
            case QUOTES:
                numberRowsAffected = db.delete(
                        QuotesContract.TABLE_NAME,
                        whereClause,
                        whereArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(whereClause == null || numberRowsAffected > 0) {
            getContext().getContentResolver().notifyChange(uri, null); //notify ALL registered ContentObservers
        }
        return numberRowsAffected;
    }


}
