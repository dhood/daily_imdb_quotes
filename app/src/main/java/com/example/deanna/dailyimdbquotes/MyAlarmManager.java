package com.example.deanna.dailyimdbquotes;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;

/**
 * Created by deanna on 8/31/14.
 */
public class MyAlarmManager {
    private static String LOG_TAG = "MyAlarmManager";
    private static long TIME_BETWEEN_ALARMS_IN_MILLIS =
            DateUtils.DAY_IN_MILLIS * 0 +
                    DateUtils.HOUR_IN_MILLIS * 0 +
                    DateUtils.MINUTE_IN_MILLIS * 0 +
                    DateUtils.SECOND_IN_MILLIS * 10;

    public static void setTimeBetweenAlarms(long time){
        TIME_BETWEEN_ALARMS_IN_MILLIS = time;
    }

    public static void scheduleNewAlarm(Context context) {
        Double hoursBetweenAlarms = Utility.getHoursBetweenQuotes(context);
        long timeBetweenAlarms_millis = (long) (DateUtils.HOUR_IN_MILLIS * hoursBetweenAlarms);

        //schedule new alarm
        long time_nextAlarm = System.currentTimeMillis();
        time_nextAlarm += timeBetweenAlarms_millis;

        scheduleAlarm(time_nextAlarm, context);
    }

    public static void rescheduleAlarm(Context context) {
        //Cancel any previous alarm
        descheduleAlarm(context);

        //get time of next alarm (which may have been interrupted by shutdown)
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String ret = settings.getString("time_nextAlarm", null);
        if(ret != null) {
            long time_nextAlarm = Long.parseLong(ret);
            Log.d(LOG_TAG, "Time of previously scheduled alarm: " + ret);

            //if alarm has happened already schedule it now
            if (time_nextAlarm < System.currentTimeMillis()) {
                time_nextAlarm = System.currentTimeMillis() + DateUtils.SECOND_IN_MILLIS;
                Log.d(LOG_TAG, "Recheduled alarm for: " + Long.toString(time_nextAlarm));
            }
            scheduleAlarm(time_nextAlarm, context);
        }else{
            Log.d(LOG_TAG,"No previously scheduled alarm");
        }
    }
    public static long getTimeOfNextAlarm(Context context){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String ret = settings.getString("time_nextAlarm", null);
        if(ret != null) {
            return Long.parseLong(ret);
        }
        else{
            return -1;
        }
    }

    private static void scheduleAlarm(long time_nextAlarm, Context context){

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context,
                0, intent, 0);

        // Schedule the alarm!
        android.app.AlarmManager am = (android.app.AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.set(android.app.AlarmManager.RTC_WAKEUP,
                time_nextAlarm, sender);

        //store next time in case phone is shutdown in the meantime
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("time_nextAlarm", Long.toString(time_nextAlarm));
        editor.commit();
        Log.d(LOG_TAG, "Current time: " + Long.toString(System.currentTimeMillis()));
        Log.d(LOG_TAG,"Time of currently scheduled alarm: "+Long.toString(time_nextAlarm));

    }

    public static void descheduleAlarm(Context context){
        // Create the same intent, and thus a matching IntentSender, for
        // the one that was scheduled.
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context,
                0, intent, 0);

        // And cancel the alarm.
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);

        Log.d(LOG_TAG,"Descheduling alarm");
    }

    public static void cancelAlarm(Context context){
        descheduleAlarm(context);
        removeAlarmTimeFromPreferences(context);
    }

    public static void removeAlarmTimeFromPreferences(Context context){
        //remove schedule time of alarm so no one thinks we are still waiting for it and reschedules
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("time_nextAlarm");
        editor.commit();
        Log.d(LOG_TAG, "Removing alarm time from preferences");
    }

    public static void createNotification(Context context, Bundle extras, int notificationID) {

        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(context, QuoteDisplayActivity.class);
        intent.putExtras(extras); // pass along the extras
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pIntent = PendingIntent.getActivity(context, notificationID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Build notification
        Notification noti = new NotificationCompat.Builder(context)
                .setContentTitle("New "+ Utility.getCurrentTitleShortText(context) + " quote!")
                .setContentText("Click to view").setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent).build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        noti.flags |= Notification.FLAG_NO_CLEAR;
        noti.defaults = Notification.DEFAULT_ALL;

        notificationManager.notify(notificationID, noti);

        Log.d(LOG_TAG,"Making notification");
    }


}
