package com.example.deanna.dailyimdbquotes;


import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.deanna.dailyimdbquotes.data.QuotesContract;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

class GetQuotesForTitleId extends AsyncTask< String, Long, Boolean > {
    private String LOG_TAG = this.getClass().toString();

    private MainActivity mMainActivity;
    private TextView mTextView;
    private Button mButton;
    private String mTitleId;
    private long mNumberOfQuotesForTitle = 0;

    public GetQuotesForTitleId(MainActivity mainActivity, TextView textView, Button button) {
        mMainActivity = mainActivity;
        mTextView = textView;
        mButton = button;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mTextView.setText("Getting quotes...");
        mTextView.setVisibility(View.VISIBLE);
        mButton.setVisibility(View.INVISIBLE);
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        mTitleId = strings[0];
        boolean success = false;

        try {
            String movieQuotesURL = "http://www.imdb.com/title/" + mTitleId + "/quotes";

            Log.d(LOG_TAG, "Connecting to [" + movieQuotesURL + "]");
            Document doc_quotes = Jsoup.connect(movieQuotesURL).get();
            Log.d(LOG_TAG, "Connected to [" + movieQuotesURL + "]");

            Elements quotes = doc_quotes.select("div.quote");

            mNumberOfQuotesForTitle = addQuotesToDatabase(quotes);

            Log.d(LOG_TAG, "Got " + Long.toString(mNumberOfQuotesForTitle) + " quotes!!");
            success = true;
        } catch (Throwable t) {
            t.printStackTrace();
            mNumberOfQuotesForTitle = 0;
        }
        return success;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        super.onProgressUpdate(values);
        Long numQuotes = values[0];

        mMainActivity.onQuoteGettingProgressUpdate(numQuotes);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);

        mMainActivity.onQuoteGettingFinished(success, mTitleId, mNumberOfQuotesForTitle);


    }

    private long addQuotesToDatabase_oneByOne(Elements quotes){
        List<String[]> quoteList = new ArrayList<String[]>(quotes.size());
        long quoteIndex = 0;
        for (Element quote : quotes) {
            String quoteId = quote.attr("id");
            Log.e(LOG_TAG,"ID of following quote: " + quoteId);
            Elements paragraphs = quote.select("p");
            StringBuffer quoteBuffer = new StringBuffer();
            for (Element paragraph : paragraphs) {
                quoteBuffer.append(paragraph.text() + "\r\n\r\n");
            }
            String quoteText = quoteBuffer.toString();
            Elements characters = quote.select("span.character");
            quoteList.add(new String[] {quoteText, quoteId});
            Log.d(LOG_TAG,quoteBuffer.toString());

            addQuote(mTitleId, "Asdf", quoteId, quoteText, quoteIndex++);
            publishProgress(quoteIndex);
        }
        return quoteIndex;
    }
    private long addQuotesToDatabase(Elements quotes){
        Vector<ContentValues> quotesVector = new Vector<ContentValues>(quotes.size());

        long quoteIndex = 0;
        for (Element quote : quotes) {
            String quoteId = quote.attr("id");
            //Log.e(LOG_TAG,"ID of following quote: " + quoteId);
            Elements paragraphs = quote.select("p");
            StringBuffer quoteBuffer = new StringBuffer();
            for (Element paragraph : paragraphs) {
                quoteBuffer.append(paragraph.text() + "\r\n\r\n");
            }
            String quoteText = quoteBuffer.toString();
            //Log.d(LOG_TAG,quoteText);
            quotesVector.add(makeDatabaseEntry(mTitleId, "Asdf", quoteId, quoteText, quoteIndex++));
            publishProgress(quoteIndex);
        }


        if (quotesVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[quotesVector.size()];
            quotesVector.toArray(cvArray);
            int rowsInserted = mMainActivity.getContentResolver()
                    .bulkInsert(QuotesContract.CONTENT_URI, cvArray);
            Log.v(LOG_TAG, "inserted " + rowsInserted + " quotes' data");
        }
        return quoteIndex;
    }

    private long addQuote(String titleId, String titleText, String quoteId, String quoteText, long quoteIndex){
        long returnVal = quoteExistsInDatabase(quoteId);

        if(returnVal != -1){
            Log.v(LOG_TAG,"Didn't find quote in database, adding now");
            Uri quoteInsertUri = mMainActivity.getContentResolver()
                    .insert(QuotesContract.CONTENT_URI, makeDatabaseEntry(titleId, titleText, quoteId, quoteText, quoteIndex));
            returnVal = ContentUris.parseId(quoteInsertUri);
        }
        return returnVal;
    }

    private long quoteExistsInDatabase(String quoteId){
        long returnVal = -1;

        Cursor cursor = mMainActivity.getContentResolver().query(QuotesContract.CONTENT_URI,
                new String[] {QuotesContract._ID},
                QuotesContract.COLUMN_QUOTEID + " = ?",
                new String[] {quoteId}, null);

        if(cursor.moveToFirst()){
            int locationIdIndex = cursor.getColumnIndex(QuotesContract._ID);
            returnVal = cursor.getLong(locationIdIndex);
        }
        cursor.close();
        return returnVal;
    }

    private ContentValues makeDatabaseEntry(String titleId, String titleText, String quoteId, String quoteText, long quoteIndex){
        ContentValues quoteValues = new ContentValues();
        quoteValues.put(QuotesContract.COLUMN_TITLEID, titleId);
        quoteValues.put(QuotesContract.COLUMN_TITLETEXT, titleText);
        quoteValues.put(QuotesContract.COLUMN_QUOTEID, quoteId);
        quoteValues.put(QuotesContract.COLUMN_QUOTETEXT, quoteText);
        quoteValues.put(QuotesContract.COLUMN_QUOTEINDEX, quoteIndex);

        return quoteValues;
    }

}
