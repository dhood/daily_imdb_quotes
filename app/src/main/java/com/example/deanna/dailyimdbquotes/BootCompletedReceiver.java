package com.example.deanna.dailyimdbquotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = BootCompletedReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(LOG_TAG, "Boot completed");

        MyAlarmManager.rescheduleAlarm(context);


    }

}
