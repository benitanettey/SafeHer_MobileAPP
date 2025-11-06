package com.example.safeher;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private Button btnNotOkay, btnSaveEntry;
    private TextView btnManageSupport;
    private EditText etJournal;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String PREFS_NAME = "SafeHerPrefs";
    private static final String CONTACTS_KEY = "SupportContacts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        btnNotOkay = findViewById(R.id.btnNotOkay);
        btnSaveEntry = findViewById(R.id.btnSaveEntry);
        btnManageSupport = findViewById(R.id.btnManageSupport);
        etJournal = findViewById(R.id.etJournal);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Handle "I’m Not Okay" button
        btnNotOkay.setOnClickListener(v -> sendAlertMessage());

        // Handle "Save Entry" (Hybrid Safety)
        btnSaveEntry.setOnClickListener(v -> {
            String message = etJournal.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Please write a message first.", Toast.LENGTH_SHORT).show();
            } else {
                sendHybridSafetyMessage(message);
            }
        });

        // Handle "Manage Support Circle"
        btnManageSupport.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SupportCircleActivity.class);
            startActivity(intent);
        });

        // Display top contacts initially
        displayTopContacts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh top contacts when returning from SupportCircleActivity
        displayTopContacts();
    }

    // Method to dynamically display top 3 contacts
    private void displayTopContacts() {
        LinearLayout contactContainer = findViewById(R.id.layoutSupportList);
        contactContainer.removeAllViews();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> contacts = prefs.getStringSet(CONTACTS_KEY, null);

        if (contacts != null && !contacts.isEmpty()) {
            int count = 0;
            for (String contact : contacts) {
                if (count >= 3) break;

                TextView tv = new TextView(this);
                tv.setText(contact);
                tv.setTextSize(16);
                tv.setPadding(12, 8, 12, 8);

                contactContainer.addView(tv);
                count++;
            }
        } else {
            TextView empty = new TextView(this);
            empty.setText("No contacts added yet.");
            empty.setTextSize(16);
            empty.setPadding(12, 8, 12, 8);
            contactContainer.addView(empty);
        }
    }

    private void sendAlertMessage() {
        if (!checkPermissions()) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            String coordinates = "Location unavailable";
            if (location != null) {
                coordinates = String.format(Locale.getDefault(),
                        "Latitude: %.5f, Longitude: %.5f",
                        location.getLatitude(), location.getLongitude());
            }

            String timestamp = new SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(new Date());
            String alertMessage = " HELP IS NEEDED!\nTime: " + timestamp + "\n" + coordinates;

            sendSMSToSupport(alertMessage);
        });
    }

    private void sendHybridSafetyMessage(String message) {
        if (!checkPermissions()) return;

        String timestamp = new SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(new Date());
        String finalMessage = "🟣 SafeHer Message (" + timestamp + "):\n" + message;

        sendSMSToSupport(finalMessage);
    }

    private void sendSMSToSupport(String message) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> contacts = prefs.getStringSet(CONTACTS_KEY, null);

        if (contacts == null || contacts.isEmpty()) {
            Toast.makeText(this, "No contacts in Support Circle.", Toast.LENGTH_SHORT).show();
            return;
        }

        SmsManager smsManager = SmsManager.getDefault();
        for (String contact : contacts) {
            try {
                smsManager.sendTextMessage(contact, null, message, null, null);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to send SMS to " + contact, Toast.LENGTH_SHORT).show();
            }
        }

        Toast.makeText(this, "Message sent to your Support Circle.", Toast.LENGTH_LONG).show();
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "Permissions required for SMS and location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
