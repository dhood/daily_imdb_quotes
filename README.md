daily_imdb_quotes
=================

An Android app to get quotes from your favourite movie/series every few hours/days.

###Dependencies
`jsoup` for parsing HTML

##Classes

`MainActivity.java`: Main user interface (set title, see how long till next quote, etc)

`MyAlarmManager.java`: Class for scheduling/descheduling alarms

`AlarmReceiver.java`: BroadcastReceiver for creating notification when alarm goes off

`NotificationReceiverActivity.java`: Activity which displays quotes when notification is opened

`BootCompletedReceiver.java`: BroadcastReceiver for rescheduling alarms after device resets

`GetTitleIdFromTitleText.java`: AsyncTask for performing imdb search on user-requested title and retrieving imdb ID of first result

`GetQuotesForTitleId.java`: AsyncTask for retrieving quotes from imdb for title, based on imdb ID

`data/*`: Database for storing quotes retrieved from imdb

`GetPrimaryImageForTitleId.java`: AsyncTask for retrieving the primary image on the imdb page for the title

`SettingsActivity.java`: Page for user-modifiable settings

`Utility.java`: Helper methods (e.g. accessing current values of user preferences)
