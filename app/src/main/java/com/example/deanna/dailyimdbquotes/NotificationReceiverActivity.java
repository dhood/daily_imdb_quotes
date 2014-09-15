package com.example.deanna.dailyimdbquotes;


import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class NotificationReceiverActivity extends Activity {
    private static final String LOG_TAG = NotificationReceiverActivity.class.getName();
    private TextView mTextView;
    private ImageView mImageView;

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

        if(Utility.getNumberOfQuotesForCurrentTitle(this) > 0) {
            int index = Utility.getIndexOfQuotesForCurrentTitle(this);
            String titleId = Utility.getCurrentTitleId(this);

            if (index >= Utility.getNumberOfQuotesForCurrentTitle(this)) {
                Log.d(LOG_TAG, "Requested index is greater than the number of quotes available");
                index = 0;
            }

            Log.d(LOG_TAG, "Getting quote number " + Integer.toString(index) + " for title " + titleId);
            String[] quoteInfo = Utility.getQuoteAtIndexForTitle(this, index, titleId);

            String quote = quoteInfo[0];
            String quoteId = quoteInfo[1];

            if (quote != null) {
                mTextView.setText(quote);
                index++;
                Utility.setIndexOfQuotesForCurrentTitle(this, index);
            }
            if (quoteId != null) {
                int resID = getResources().getIdentifier(quoteId, "drawable", getPackageName());
                mImageView.setImageResource(resID);
            }

            MyAlarmManager.scheduleNewAlarm(this);
            if (MainActivity.instance != null) {
                MainActivity.instance.displayTimeTillNextAlarm();
            }
            Log.d(LOG_TAG, "Scheduled new alarm");
        }
        else{
            Log.e(LOG_TAG, "No quotes are available for this title - not rescheduling alarm.");
            mTextView.setText("No quotes are available for this title, sorry.");
            if (MainActivity.instance != null) {
                MainActivity.instance.displayNoQuotesAvailable();
            }
        }
    }

}

