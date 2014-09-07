package com.example.deanna.dailyimdbquotes;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by deanna on 9/4/14.
 */
public class GetTitleIdFromTitleText extends AsyncTask< String, Void, String > {
    private String LOG_TAG = this.getClass().toString();

    private MainActivity mActivity;
    private String mTitleText_imdb_withYear;
    private String mTitleText_imdb;

    public GetTitleIdFromTitleText(MainActivity mainActivity){
        mActivity = mainActivity;
    }

    @Override
    protected String doInBackground(String... titleText) {
        String movieTitle = titleText[0];
        String titleId = null;
        try {
            final String BASE_URL = "http://www.imdb.com/find";
            final String QUERY_PARAM = "q";
            final String CATEGORY_PARAM = "s";
            final String CATEGORY_TITLES = "tt";

            //movieTitle = java.net.URLEncoder.encode(movieTitle, "UTF-8"); //convert spaces to "+" (or uriBuilder will make "%20")
            Uri.Builder uriBuilder = Uri.parse(BASE_URL).buildUpon();
            uriBuilder.appendQueryParameter(QUERY_PARAM, movieTitle);
            uriBuilder.appendQueryParameter(CATEGORY_PARAM, CATEGORY_TITLES);
            Uri uri = uriBuilder.build();

            String url = uri.toString();
            url = url.replace("%20","+");
            Log.d(LOG_TAG, "Connecting to [" + url + "]");
            Document doc = Jsoup.connect(url).timeout(6000).get();
            Log.d(LOG_TAG, "Connected to [" + url + "]");

            //find link to movie's page
            Elements searchResults = doc.select("tr.findResult");
            Elements resultText = searchResults.first().select("td.result_text");
            mTitleText_imdb_withYear = resultText.text();

            Elements resultHref = resultText.select("a");
            mTitleText_imdb = resultHref.text();

            String resultRelativeLink = resultHref.first().attr("href");
            Log.d(LOG_TAG, "resultRelativeLink: " + resultRelativeLink);

            //extract movie's ID
            Pattern pattern = Pattern.compile("title/([\\w]*)/");
            Matcher matcher = pattern.matcher(resultRelativeLink);
            if (matcher.find()) {
                titleId = matcher.group(1);
                Log.d(LOG_TAG, "Movie ID code: " + titleId);
            } else {
                Log.e(LOG_TAG,"Wasn't able to extract movie code.");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return titleId;
    }

    @Override
    protected void onPostExecute(String titleId) {
        super.onPostExecute(titleId);

        // send the result
        mActivity.titleIdFromTitleTextFinished(titleId, mTitleText_imdb_withYear, mTitleText_imdb);
    }


}
