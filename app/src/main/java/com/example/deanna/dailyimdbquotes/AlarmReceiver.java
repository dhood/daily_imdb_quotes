package com.example.deanna.dailyimdbquotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by deanna on 8/30/14.
 */
public class AlarmReceiver extends BroadcastReceiver
{
    private static final String LOG_TAG = AlarmReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent)
    {

        Utility.createNotificationOfNextQuote(context);

        if(MainActivity.instance != null){
            MainActivity.instance.displayNotificationWaiting();
        }
        Log.d(LOG_TAG, "Alarm received");

    }

}