package com.example.safeher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private Button btnNotOkay, btnSaveEntry;
    private TextView btnManageSupport, tvDateTime;
    private EditText etJournal;
    private FusedLocationProviderClient fusedLocationClient;

    private ContactDatabaseHelper dbHelper;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        btnNotOkay = findViewById(R.id.btnNotOkay);
        btnSaveEntry = findViewById(R.id.btnSaveEntry);
        btnManageSupport = findViewById(R.id.btnManageSupport);
        etJournal = findViewById(R.id.etJournal);
        tvDateTime = findViewById(R.id.tvDateTime);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        dbHelper = new ContactDatabaseHelper(this);

        // New: profile and settings buttons
        ImageView btnProfile = findViewById(R.id.btnProfile);
        ImageView btnSettings = findViewById(R.id.btnSettings);

        // Set current date and time
        updateDateTime();

        // Handle top-right buttons
        btnProfile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));

        // Handle "I'm Not Okay" button
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
        // Refresh date/time and top contacts when returning from SupportCircleActivity
        updateDateTime();
        displayTopContacts();
    }

    // Method to dynamically display contacts
    private void displayTopContacts() {
        LinearLayout contactContainer = findViewById(R.id.layoutSupportList);
        contactContainer.removeAllViews();

        List<Contact> contacts = dbHelper.getAllContacts();

        if (contacts != null && !contacts.isEmpty()) {
            int shown = 0;
            for (Contact contact : contacts) {
                if (shown >= 3) break;

                TextView tv = new TextView(this);
                String displayText = contact.getName() + " - " + contact.getRelationship() + " • " + contact.getPhoneNumber();
                tv.setText(displayText);
                tv.setTextColor(0xFF212121); // Black color
                tv.setTextSize(14);
                tv.setPadding(8, 8, 8, 8);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                // convert dp margin to px
                final float scale = getResources().getDisplayMetrics().density;
                int bottomMarginPx = (int) (8 * scale + 0.5f);
                params.setMargins(0, 0, 0, bottomMarginPx);
                tv.setLayoutParams(params);

                contactContainer.addView(tv);
                shown++;
            }

            if (shown == 0) {
                TextView empty = new TextView(this);
                empty.setText("No contacts added yet.");
                empty.setTextColor(0xFF757575);
                empty.setTextSize(14);
                empty.setPadding(8, 6, 8, 6);
                contactContainer.addView(empty);
            }
        } else {
            TextView empty = new TextView(this);
            empty.setText("No contacts added yet.");
            empty.setTextColor(0xFF757575); // Gray color
            empty.setTextSize(14);
            empty.setPadding(8, 6, 8, 6);
            contactContainer.addView(empty);
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
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
        List<Contact> contacts = dbHelper.getAllContacts();

        if (contacts == null || contacts.isEmpty()) {
            Toast.makeText(this, "No contacts in Support Circle.", Toast.LENGTH_SHORT).show();
            return;
        }

        SmsManager smsManager = SmsManager.getDefault();
        int sentCount = 0;

        for (Contact contact : contacts) {
            if (contact != null) {
                try {
                    smsManager.sendTextMessage(contact.getPhoneNumber(), null, message, null, null);
                    sentCount++;
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to send SMS to " + contact.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (sentCount > 0) {
            Toast.makeText(this, "Message sent to " + sentCount + " contact(s).", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to send messages.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d • h:mm a", Locale.getDefault());
        String currentDateTime = dateFormat.format(new Date());
        tvDateTime.setText(currentDateTime);
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