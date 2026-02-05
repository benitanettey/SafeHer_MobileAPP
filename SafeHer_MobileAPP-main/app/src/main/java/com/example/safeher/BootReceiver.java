package com.example.safeher;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences settingsPrefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
            boolean remindersEnabled = settingsPrefs.getBoolean("daily_reminders", false);

            if (remindersEnabled) {
                String preferredTimeString = settingsPrefs.getString("preferred_time", "09:00 AM");
                Calendar calendar = Calendar.getInstance();
                try {
                    String[] timeParts = preferredTimeString.split("[: ]");
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);
                    if (preferredTimeString.endsWith("PM") && hour != 12) {
                        hour += 12;
                    }
                    if (preferredTimeString.endsWith("AM") && hour == 12) {
                        hour = 0;
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);

                    if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                    }

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    Intent reminderIntent = new Intent(context, ReminderBroadcastReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, reminderIntent, PendingIntent.FLAG_IMMUTABLE);

                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY, pendingIntent);
                } catch (Exception e) {
                    // Handle exception
                }
            }
        }
    }
}
