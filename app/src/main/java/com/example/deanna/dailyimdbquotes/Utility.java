package com.example.deanna.dailyimdbquotes;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.deanna.dailyimdbquotes.data.QuotesContract;

/**
 * Created by deanna on 9/4/14.
 */
public class Utility {

    public static String getCurrentTitleId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String location = prefs.getString(context.getString(R.string.current_titleId), null);
        if (null == location) {
            Log.e("getPreferredLocation", "No value for titleId in shared preferences...");
        }
        return location;
    }

    public static void setCurrentTitleId(Context context, String titleId) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(context.getString(R.string.current_titleId), titleId);
        editor.commit();
    }

    public static String getCurrentTitleText(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String location = prefs.getString(context.getString(R.string.pref_titleText_key),
                context.getString(R.string.pref_titleText_default));
        if (null == location) {
            Log.e("getCurrentTitleText", "No value for titleText in shared preferences...");
        }
        return location;
    }

    public static void setCurrentTitleShortText(Context context, String titleText_short) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(context.getString(R.string.current_titleText_short), titleText_short);
        editor.commit();
    }

    public static String getCurrentTitleShortText(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String location = prefs.getString(context.getString(R.string.current_titleText_short),
                "");
        return location;
    }
    public static void setCurrentTitleText(Context context, String titleText) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(context.getString(R.string.pref_titleText_key), titleText);
        editor.commit();
    }

    public static int getIndexOfQuotesForCurrentTitle(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String ret = settings.getString(context.getString(R.string.index_of_quotes_for_current_title), "0");
        return Integer.parseInt(ret);
    }

    public static void setIndexOfQuotesForCurrentTitle(Context context, int index) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(context.getString(R.string.index_of_quotes_for_current_title), Integer.toString(index));
        editor.commit();
    }

    public static double getHoursBetweenQuotes(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String ret = settings.getString(context.getString(R.string.pref_hours_between_quotes_key),
                context.getString(R.string.pref_hours_between_quotes_default));
        try {
            return Double.parseDouble(ret);
        }
        catch(Throwable t) {
            t.printStackTrace();
            return -1;
        }
    }

    public static void setHoursBetweenQuotes(Context context, double numHours) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(context.getString(R.string.pref_hours_between_quotes_key), Double.toString(numHours));
        editor.commit();
    }

    public static long getNumberOfQuotesForCurrentTitle(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String ret = settings.getString(context.getString(R.string.number_of_quotes_for_current_title), "0");
        return Long.parseLong(ret);
    }

    public static void setNumberOfQuotesForCurrentTitle(Context context, Long numberQuotes) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(context.getString(R.string.number_of_quotes_for_current_title), Long.toString(numberQuotes));
        editor.commit();
    }

    public static String[] getQuoteAtIndexForTitle(Context context, int index, String titleId) {
        String quote = null;
        String quoteId = null;

        Cursor quoteCursor = context.getContentResolver().query(QuotesContract.buildQuoteAtIndexForTitleUri(index, titleId), null, null, null, null);
        if (quoteCursor.moveToFirst())

        {
            int idx = quoteCursor.getColumnIndex(QuotesContract.COLUMN_QUOTETEXT);
            if (idx != -1) {
                quote = quoteCursor.getString(idx);

                idx = quoteCursor.getColumnIndex(QuotesContract.COLUMN_QUOTEID);
                if (idx != -1) {
                    quoteId = quoteCursor.getString(idx);
                }
            }
        }

        quoteCursor.close();
        return new String[] {quote, quoteId};
    }

    public static void createNotificationOfNextQuote(Context context) {
        int index = Utility.getIndexOfQuotesForCurrentTitle(context);
        Bundle extras = createExtrasForQuoteIndex(context, index);
        extras.putBoolean("ScheduleAlarmAfterDisplaying",true);
        MyAlarmManager.createNotification(context, extras, index);
    }

    public static void launchPrevQuote(Context context) {
        int index = Utility.getIndexOfQuotesForCurrentTitle(context);
        if (index > 0) {
            Bundle extras = createExtrasForQuoteIndex(context, index - 1);
            extras.putBoolean("ScheduleAlarmAfterDisplaying", false);
            launchQuote(context, extras);
        }
    }

    public static Bundle createExtrasForQuoteIndex(Context context, int index){
        Bundle extras = new Bundle();

        if(Utility.getNumberOfQuotesForCurrentTitle(context) > 0) {

            String titleId = Utility.getCurrentTitleId(context);

            if (index >= Utility.getNumberOfQuotesForCurrentTitle(context)) {
                Log.d(context.getClass().getName(), "Requested index is greater than the number of quotes available");
                index = 0;
            }

            Log.d(context.getClass().getName(), "Getting quote number " + Integer.toString(index) + " for title " + titleId);
            String[] quoteInfo = Utility.getQuoteAtIndexForTitle(context, index, titleId);

            String quote = quoteInfo[0];
            String quoteId = quoteInfo[1];
            extras.putInt("Index", index);
            if (quote != null) {
                extras.putString("TextToDisplay", quote);
                Log.d(context.getClass().getName(), "Quote: " + quote);
            }
            if (quoteId != null) {
                extras.putString("ImageToDisplay", quoteId);
            }


        } else {
            Log.e(context.getClass().getName(), "No quotes are available for this title - not rescheduling alarm.");
        }

        return extras;
    }

    public static void quoteShown(Context context, Bundle extras) {
        if (extras != null) {
            if (extras.containsKey("Index")) {
                int indexOfNotification = extras.getInt("Index");

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(indexOfNotification);

                int currentIndex = Utility.getIndexOfQuotesForCurrentTitle(context);
                if (currentIndex == indexOfNotification) {
                    currentIndex++;
                    Utility.setIndexOfQuotesForCurrentTitle(context, currentIndex);
                    if (MainActivity.instance != null) { // enable the previous quote menu option if it wasn't already
                        MainActivity.instance.invalidateOptionsMenu();
                    }
                }

            if (extras.containsKey("ScheduleAlarmAfterDisplaying")
                    && extras.getBoolean("ScheduleAlarmAfterDisplaying")){
                    MyAlarmManager.scheduleNewAlarm(context);
                    if (MainActivity.instance != null) {
                        MainActivity.instance.displayTimeTillNextAlarm();
                    }
                    Log.d(context.getClass().getName(), "Scheduled new alarm");
                }
            }
        }
    }


    private static void launchQuote(Context context, Bundle extras) {

        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(context, QuoteDisplayActivity.class);
        intent.putExtras(extras); // pass along the extras
        context.startActivity(intent);
    }

}

