package com.example.deanna.dailyimdbquotes;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class QuoteDisplayActivity extends Activity {
    private static final String LOG_TAG = QuoteDisplayActivity.class.getName();
    private TextView mTextView;
    private ImageView mImageView;

    private ShareActionProvider mShareActionProvider;


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //we don't actually want to change anything if the screen gets rotated, but this is
        //necessary to prevent the activity from being restarted (and showing a new quote) when rotated
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Received");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        mTextView = (TextView) findViewById(R.id.textView1);
        mImageView = (ImageView) findViewById(R.id.imageView);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            if (extras.containsKey("TextToDisplay")) {
                String textToDisplay = extras.getString("TextToDisplay");
                mTextView.setText(textToDisplay);
                Log.d(LOG_TAG, "Received quote " + textToDisplay);

                setShareIntent(textToDisplay);
            }
            if (extras.containsKey("ImageToDisplay")) {
                String imageToDisplay = extras.getString("ImageToDisplay");
                int resID = getResources().getIdentifier(imageToDisplay, "drawable", getPackageName());
                mImageView.setImageResource(resID);
            }
        }

        Utility.quoteShown(this, extras);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.quote, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        // Return true to display menu
        return true;
    }

    // Call to update the share intent
    private void setShareIntent(String text) {
        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, '"' + text + '"');
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

}

