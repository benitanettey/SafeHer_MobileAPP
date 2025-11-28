package com.example.safeher;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    private static final String SETTINGS_PREFS_NAME = "settings_prefs";
    public static final String CHANNEL_ID = "SAFEHER_DAILY_CHECKIN";

    private ImageView backButton;
    private MaterialCardView wellnessProfileCard;
    private Switch dailyRemindersSwitch, quickAccessSwitch;
    private TextView preferredTime;
    private TextView appLockSettings, alertHistory, about, privacyPolicy, helpAndSupport;

    private SharedPreferences settingsPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        createNotificationChannel();
        settingsPrefs = getSharedPreferences(SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);

        bindViews();
        loadSettings();
        setupClickListeners();
    }

    private void bindViews() {
        backButton = findViewById(R.id.backButton);
        wellnessProfileCard = findViewById(R.id.wellnessProfileCard);
        dailyRemindersSwitch = findViewById(R.id.dailyRemindersSwitch);
        quickAccessSwitch = findViewById(R.id.quickAccessSwitch);
        preferredTime = findViewById(R.id.preferredTime);
        appLockSettings = findViewById(R.id.appLockSettings);
        alertHistory = findViewById(R.id.alertHistory);
        about = findViewById(R.id.about);
        privacyPolicy = findViewById(R.id.privacyPolicy);
        helpAndSupport = findViewById(R.id.helpAndSupport);
    }

    private void loadSettings() {
        dailyRemindersSwitch.setChecked(settingsPrefs.getBoolean("daily_reminders", true));
        quickAccessSwitch.setChecked(settingsPrefs.getBoolean("quick_access", true));
        preferredTime.setText(settingsPrefs.getString("preferred_time", "09:00 AM"));
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        wellnessProfileCard.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        dailyRemindersSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsPrefs.edit().putBoolean("daily_reminders", isChecked).apply();
            if (isChecked) {
                scheduleNotification();
            } else {
                cancelNotification();
            }
        });

        quickAccessSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsPrefs.edit().putBoolean("quick_access", isChecked).apply();
        });

        preferredTime.setOnClickListener(v -> {
            showTimePickerDialog();
        });

        appLockSettings.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AppLockSetupActivity.class);
            startActivity(intent);
        });

        alertHistory.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AlertHistoryActivity.class);
            startActivity(intent);
        });

        about.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        privacyPolicy.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });

        helpAndSupport.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, HelpAndSupportActivity.class);
            startActivity(intent);
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SafeHer Daily Check-in";
            String description = "Channel for daily check-in reminders";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour, minute;
        String preferredTimeString = preferredTime.getText().toString();
        try {
            String[] timeParts = preferredTimeString.split("[: ]");
            hour = Integer.parseInt(timeParts[0]);
            minute = Integer.parseInt(timeParts[1]);
            if (preferredTimeString.endsWith("PM") && hour != 12) {
                hour += 12;
            }
            if (preferredTimeString.endsWith("AM") && hour == 12) {
                hour = 0;
            }
        } catch (Exception e) {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String amPm = hourOfDay >= 12 ? "PM" : "AM";
            int hourIn12Format = hourOfDay > 12 ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
            String time = String.format("%02d:%02d %s", hourIn12Format, minuteOfHour, amPm);
            preferredTime.setText(time);
            settingsPrefs.edit().putString("preferred_time", time).apply();
            if (dailyRemindersSwitch.isChecked()) {
                scheduleNotification();
            }
        }, hour, minute, false);

        timePickerDialog.show();
    }

    private void scheduleNotification() {
        Calendar calendar = Calendar.getInstance();
        String preferredTimeString = preferredTime.getText().toString();
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

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);

            Toast.makeText(this, "Reminder set for " + preferredTimeString, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Invalid time format", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
        Toast.makeText(this, "Reminder canceled", Toast.LENGTH_SHORT).show();
    }
}
