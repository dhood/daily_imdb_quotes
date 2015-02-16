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

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            if (extras.containsKey("TextToDisplay")) {
                String textToDisplay = extras.getString("TextToDisplay");
                mTextView.setText(textToDisplay);
                Log.d(LOG_TAG, "Received quote " + textToDisplay);
            }
            if (extras.containsKey("ImageToDisplay")) {
                String imageToDisplay = extras.getString("ImageToDisplay");
                int resID = getResources().getIdentifier(imageToDisplay, "drawable", getPackageName());
                mImageView.setImageResource(resID);
            }
        }

        Utility.notificationShown(this, extras);


    }

}

