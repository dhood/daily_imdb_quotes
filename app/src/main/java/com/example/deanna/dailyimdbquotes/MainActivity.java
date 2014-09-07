package com.example.deanna.dailyimdbquotes;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends Activity {
    public static MainActivity instance = null;
    private static String LOG_TAG = "QuotesMain";
    CountDownTimer mCountDownTimer;
    TextView mTextView;
    Button mNewQuoteButton;
    ImageView mImageView;
    double prevVal_hoursBetweenQuotes;
    String prevVal_titleText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG,"onCreate called");

        instance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTextView = (TextView) findViewById(R.id.timeTillNextQuote);
        mImageView = (ImageView) findViewById(R.id.imageView1);

        mNewQuoteButton = (Button)findViewById(R.id.getNotification);
        mNewQuoteButton.setOnClickListener(mCreateNotificationListener);

        mNewQuoteButton.setVisibility(View.INVISIBLE);
        mTextView.setVisibility(View.INVISIBLE);

        // FORCE THIS WHILE DEVELOPING
        //MyAlarmManager.cancelAlarm(this);
        //Utility.setCurrentTitleId(this, null);

        storeCurrentPreferenceValues();

        String currentTitleId = Utility.getCurrentTitleId(this);
        if(currentTitleId == null){
            getQuotes();
        }else{
            if(Utility.getCurrentTitleText(this).equalsIgnoreCase(getString(R.string.pref_titleText_default))){ //special case for when Office Space
                mImageView.setImageResource(R.drawable.milton);
                mImageView.setScaleType(ImageView.ScaleType.CENTER);
            }

            MyAlarmManager.rescheduleAlarm(this);
            displayTimeTillNextAlarm();
            getImageForTitleId();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfPreferencesChanged();
    }

    private void storeCurrentPreferenceValues(){
        prevVal_hoursBetweenQuotes = Utility.getHoursBetweenQuotes(this);
        prevVal_titleText = Utility.getCurrentTitleText(this);
    }

    public void checkIfPreferencesChanged() {
        // number of hours between quotes
        double currVal_hoursBetweenQuotes = Utility.getHoursBetweenQuotes(this);
        if (currVal_hoursBetweenQuotes != prevVal_hoursBetweenQuotes) {
            prevVal_hoursBetweenQuotes = currVal_hoursBetweenQuotes;
            if (currVal_hoursBetweenQuotes < 1.0) {
                Utility.setHoursBetweenQuotes(this, 1.0);
            }
            Log.d(LOG_TAG, "Updating alarm for hours_between_quotes change");
            MyAlarmManager.cancelAlarm(this);
            MyAlarmManager.scheduleNewAlarm(this);
            displayTimeTillNextAlarm();
        }

        //title to show quotes for
        String currVal_titleText = Utility.getCurrentTitleText(this);
        if (!currVal_titleText.equals(prevVal_titleText)) {
            prevVal_titleText = currVal_titleText;
            Log.d(LOG_TAG, "Getting new quotes for title since it changed");
            getQuotes();
        }
    }

    private View.OnClickListener mCreateNotificationListener = new View.OnClickListener() {
        public void onClick(View v) {
            startQuotes();
        }
    };

    private void startQuotes(){
        MyAlarmManager.createNotification(getBaseContext());
        displayNotificationWaiting();
        makeTextBoxVisible();
    }

    public void displayNotificationWaiting(){
        if(mCountDownTimer != null) {
            mCountDownTimer.cancel(); //cancel previous timer
        }

        makeTextBoxVisible();
        mTextView.setText("Quote is waiting for you in the notification bar!");

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String ret = settings.getString("time_nextAlarm", null);
        if(ret == null ){ //no alarms scheduled, allow new requests
            Log.d(LOG_TAG,"No previous alarms when saying notification waiting");
        }else{
            Log.d(LOG_TAG,"Alarm scheduled for "+ Long.toString(Long.parseLong(ret))+ " when saying notification waiting");
        }
    }

    public void displayTimeTillNextAlarm(){

        if(mCountDownTimer != null) {
            mCountDownTimer.cancel(); //cancel previous timer
        }

        //get time of next alarm (which may have passed)
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String ret = settings.getString("time_nextAlarm", null);

        if(ret == null){ //no alarms scheduled
            if(Utility.getCurrentTitleId(this) != null){ //quotes should be available
                //allow new requests
                makeButtonVisible();
            }else{
                mTextView.setText("No quotes available yet");
                makeTextBoxVisible();
            }
        }else if(Long.parseLong(ret) <= System.currentTimeMillis()) { //alarm was scheduled but notification hasn't been seen
            displayNotificationWaiting();
        }
        else{ //alarm is in future
            makeTextBoxVisible();
            long timeTillNextAlarm = Long.parseLong(ret) - System.currentTimeMillis();
            mCountDownTimer = new myCountDownTimer(timeTillNextAlarm, 1000).start();
        }
    }

    private class myCountDownTimer extends CountDownTimer{

        public myCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        StringBuilder time = new StringBuilder();
        @Override
        public void onFinish() {
            mTextView.setText(DateUtils.formatElapsedTime(0));
        }

        @Override
        public void onTick(long millisUntilFinished) {
            time.setLength(0);
            // Use days if appropriate
            if(millisUntilFinished > DateUtils.DAY_IN_MILLIS) {
                long count = millisUntilFinished / DateUtils.DAY_IN_MILLIS;
                if(count > 1)
                    time.append(count).append(" days ");
                else
                    time.append(count).append(" day ");

                millisUntilFinished %= DateUtils.DAY_IN_MILLIS;
            }

            time.append(DateUtils.formatElapsedTime(Math.round(millisUntilFinished / 1000d)));
            time.append(" until next quote!");
            mTextView.setText(time.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_reset) {
            softReset();
            return true;
        }
        if (id == R.id.action_get_quotes) {
            getQuotes();
            return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            //registerPreferenceListener();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void softReset(){

        if(mCountDownTimer != null) {
            mCountDownTimer.cancel(); //cancel previous timer
        }
        MyAlarmManager.cancelAlarm(this);
        makeButtonVisible();
    }

    public void makeButtonVisible(){

        mTextView.setVisibility(View.INVISIBLE);
        mNewQuoteButton.setVisibility(View.VISIBLE);
    }
    public void makeTextBoxVisible(){

        mTextView.setVisibility(View.VISIBLE);
        mNewQuoteButton.setVisibility(View.INVISIBLE);
    }

    public void displayNoQuotesAvailable(){
        makeTextBoxVisible();
        mTextView.setText("No quotes are available for " + Utility.getCurrentTitleText(this));
    }

    public void getQuotes(){
        makeTextBoxVisible();
        if(mCountDownTimer != null) {
            mCountDownTimer.cancel(); //cancel previous timer
        }
        mTextView.setText("Fetching quotes...");

        String currentTitleText = Utility.getCurrentTitleText(this);
        GetTitleIdFromTitleText getTitleIdFromTitleText = new GetTitleIdFromTitleText(this);
        getTitleIdFromTitleText.execute(currentTitleText);
    }

    public void onQuoteGettingProgressUpdate(long numQuotes){
        mTextView.setText(Long.toString(numQuotes) + " quotes retrieved...");
    }

    public void onQuoteGettingFinished(boolean success, String titleId, long numberOfQuotes){
        if(success) {
            //update the current movie
            Utility.setCurrentTitleId(this, titleId);

            if(MyAlarmManager.getTimeOfNextAlarm(this) == -1) {
                //allow quote requests
                makeButtonVisible();
            }
            else{
                makeTextBoxVisible();
                MyAlarmManager.rescheduleAlarm(this);
                displayTimeTillNextAlarm();
            }

            if(numberOfQuotes == 0){
                makeTextBoxVisible();
                displayNoQuotesAvailable();
            }
        }
        else{
            mTextView.setText("There has been an error. Please check your internet and retry with the menu button.");
        }
        Utility.setNumberOfQuotesForCurrentTitle(this, numberOfQuotes);
    }

    public void titleIdFromTitleTextFinished(String titleId, String titleText_imdb_withYear, String titleText_imdb){
        String currentTitleId = Utility.getCurrentTitleId(this);

        Utility.setCurrentTitleShortText(this, titleText_imdb);

        if(titleText_imdb_withYear != null){
            //update the options value to explain which title we found from search
            Utility.setCurrentTitleText(this, titleText_imdb_withYear);
            prevVal_titleText = titleText_imdb_withYear;
        }

        if(titleId == null){
            //something went wrong getting titleId
            mTextView.setText("Something went wrong. Please check your internet connection and try again using the menu option.");
            makeTextBoxVisible();
        }
        else {
            if (!titleId.equalsIgnoreCase(currentTitleId)) {
                //update the current movie
                Utility.setCurrentTitleId(this, titleId);
                Utility.setIndexOfQuotesForCurrentTitle(this, 0);
                getQuotesForTitleId(titleId);
                getImageForTitleId();
            }
            else{
                //this is already the current movie....
                onQuoteGettingFinished(true, titleId, Utility.getNumberOfQuotesForCurrentTitle(this));

            }
        }

    }

    public void getQuotesForTitleId(String titleId){
        GetQuotesForTitleId getQuotesForTitleId = new GetQuotesForTitleId(this, mTextView, mNewQuoteButton);
        getQuotesForTitleId.execute(titleId);

    }

    public void getImageForTitleId(){
        GetPrimaryImageForTitleId getPrimaryImageForTitleId = new GetPrimaryImageForTitleId(this);
        getPrimaryImageForTitleId.execute(Utility.getCurrentTitleId(this));
    }

    public void onGettingPrimaryImageFinished(String titleId, Bitmap primaryImage){
        if(primaryImage != null){
            Log.d(LOG_TAG,"Got primary image!");

            if(titleId.equalsIgnoreCase("tt0151804")){ //special case for when Office Space
                mImageView.setImageResource(R.drawable.milton);
                mImageView.setScaleType(ImageView.ScaleType.CENTER);
            }
            else{
                // set the result as the background
                mImageView.setImageDrawable(new BitmapDrawable(primaryImage));
                mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        }
        else{
            Log.d(LOG_TAG,"No primary image found.");
        }
    }


/*
    public void onPreferencesChanged(String key){
        Log.d(LOG_TAG,"Preference listener called for "+key);
        if (key.equals(getString(R.string.pref_hours_between_quotes_key))) {
            if(Utility.getHoursBetweenQuotes(getBaseContext()) < 1.0){
                Utility.setHoursBetweenQuotes(getBaseContext(), 1.0);
            }
            Log.d(LOG_TAG, "Updating alarm to have number of hours changed to");
            startQuotes();
        }
        if (key.equals(getString(R.string.pref_titleText_key))){
            Log.d(LOG_TAG,"Getting new quotes for title since it changed");
            getQuotes();
        }
    }


    private void createPreferenceListener() {
        mPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    Log.d("PrefListener","Preference listener called");
                    if (key.equals(R.string.pref_hours_between_quotes_key)) {
                        if(Utility.getHoursBetweenQuotes(getBaseContext()) < 1.0){
                            Utility.setHoursBetweenQuotes(getBaseContext(), 1.0);
                        }

                        Log.d(LOG_TAG,"Updating alarm to have number of hours changed to");
                        MyAlarmManager.cancelAlarm(getBaseContext());
                        MyAlarmManager.scheduleNewAlarm(getBaseContext());
                    }
                    if (key.equals(R.string.pref_titleText_key)){
                        Log.d(LOG_TAG,"Getting new quotes for title since it changed");
                        getQuotes();
                    }
                }
            };
        Log.d(LOG_TAG,"Created preference listener");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterPreferenceListener();
    }
    private void unregisterPreferenceListener(){
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        preferences.unregisterOnSharedPreferenceChangeListener(mPrefListener);
        Log.d(LOG_TAG,"Unregistered preference listener");
    }
    private void registerPreferenceListener(){

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(mPrefListener);
        Log.d(LOG_TAG,"Registered preference listener");
        if(mPrefListener == null){
            Log.d(LOG_TAG,"But it's NULLLLL");

        }
    }*/

}
