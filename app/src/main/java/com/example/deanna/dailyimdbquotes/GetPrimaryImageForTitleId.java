package com.example.deanna.dailyimdbquotes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by deanna on 9/4/14.
 */
public class GetPrimaryImageForTitleId extends AsyncTask< String, Void, Bitmap > {
    private String LOG_TAG = this.getClass().toString();

    private MainActivity mActivity;
    private String mTitleId;

    public GetPrimaryImageForTitleId(MainActivity mainActivity){
        mActivity = mainActivity;
    }

    @Override
    protected Bitmap doInBackground(String... titleId) {
        mTitleId = titleId[0];
        Bitmap primaryImage = null;
        try {
            String titlePageURL = "http://www.imdb.com/title/" + mTitleId;

            Log.d(LOG_TAG, "Connecting to [" + titlePageURL + "]");
            Document doc_quotes = Jsoup.connect(titlePageURL).get();
            Log.d(LOG_TAG, "Connected to [" + titlePageURL + "]");

            Elements images = doc_quotes.select("div.image");
            if(images.size() > 0) { //not every page has a primary image
                String primaryImageUrl = images.get(0).select("img").attr("src");
                primaryImage = getBitmapFromURL(primaryImageUrl);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return primaryImage;
    }

    @Override
    protected void onPostExecute(Bitmap primaryImage) {
        super.onPostExecute(primaryImage);

        mActivity.onGettingPrimaryImageFinished(mTitleId, primaryImage);
    }


    public Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
