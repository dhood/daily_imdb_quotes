package com.example.deanna.dailyimdbquotes.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the quotes database.
 */
public class QuotesContract implements BaseColumns {
    public static final String CONTENT_AUTHORITY = "com.example.deanna.dailyimdbquotes";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY); //how apps will contact content provider

    public static final String PATH_QUOTES = "quotes";
    public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_QUOTES).build();

    public static final String TABLE_NAME = "quotes";

    public static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/" + CONTENT_AUTHORITY;

    public static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/" + CONTENT_AUTHORITY;

    // Title of movie or whatever
    public static final String COLUMN_TITLETEXT = "title";

    // IMDB ID of title
    public static final String COLUMN_TITLEID = "title_id";

    // IMDB ID of quote
    public static final String COLUMN_QUOTEID = "quote_id";

    // Quote
    public static final String COLUMN_QUOTETEXT = "quote";

    // Quote index (as ordered in IMDB)
    public static final String COLUMN_QUOTEINDEX = "quote_index";

    public static Uri buildQuoteUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    public static Uri buildQuoteAtIndexForTitleUri(long index, String titleId){
        Uri uri = CONTENT_URI.buildUpon().appendPath(titleId).build();
        uri = ContentUris.withAppendedId(uri, index);
        return uri;
    }

    public static String getTitleIdFromUri(Uri uri) {
        return uri.getPathSegments().get(1);
    }

    public static long getQuoteIndexFromUri(Uri uri) {
        return ContentUris.parseId(uri);
    }


}
