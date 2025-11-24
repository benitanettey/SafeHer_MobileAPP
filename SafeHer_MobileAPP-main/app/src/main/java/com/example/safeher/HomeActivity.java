package com.example.safeher;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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
    private TextView btnManageSupport, tvDateTime;
    private EditText etJournal;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String PREFS_NAME = "SafeHerPrefs";
    private static final String CONTACTS_KEY = "SupportContacts";

    // Flag to retry alert after permission grant
    private boolean pendingAlertSend = false;
    private boolean pendingJournalSend = false;
    private String pendingJournalMessage = "";

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

        // Set current date and time
        updateDateTime();

        // Handle "I'm Not Okay" button
        btnNotOkay.setOnClickListener(v -> {
            // Check if support circle is empty
            if (getSupportContactCount() == 0) {
                Toast.makeText(this, "Please add contacts to your Support Circle first.",
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent(HomeActivity.this, SupportCircleActivity.class);
                startActivity(intent);
                return;
            }
            sendAlertMessage();
        });

        // Handle "Save Entry"
        btnSaveEntry.setOnClickListener(v -> {
            String message = etJournal.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Please write a message first.", Toast.LENGTH_SHORT).show();
                etJournal.requestFocus();
            } else if (getSupportContactCount() == 0) {
                Toast.makeText(this, "Please add contacts to your Support Circle first.",
                        Toast.LENGTH_LONG).show();
            } else {
                pendingJournalMessage = message;
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

    private int getSupportContactCount() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> contactJsonSet = prefs.getStringSet(CONTACTS_KEY, null);
        return (contactJsonSet == null) ? 0 : contactJsonSet.size();
    }

    // Method to dynamically display contacts
    private void displayTopContacts() {
        LinearLayout contactContainer = findViewById(R.id.layoutSupportList);
        contactContainer.removeAllViews();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> contactJsonSet = prefs.getStringSet(CONTACTS_KEY, null);

        if (contactJsonSet != null && !contactJsonSet.isEmpty()) {
            int count = 0;
            for (String contactJson : contactJsonSet) {
                Contact contact = Contact.fromJson(contactJson);
                if (contact != null) {
                    // Create a TextView for each contact
                    TextView tv = new TextView(this);

                    // Format: "Name - Relationship • Phone"
                    String displayText = contact.getName() + " - " +
                                        contact.getRelationship() + " • " +
                                        contact.getPhoneNumber();

                    tv.setText(displayText);
                    tv.setTextColor(0xFF212121); // Black color
                    tv.setTextSize(14);
                    tv.setPadding(8, 8, 8, 8);

                    // Add margin between items
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 0, 8); // 8dp bottom margin
                    tv.setLayoutParams(params);

                    contactContainer.addView(tv);
                    count++;
                }
            }

            // If no valid contacts were parsed
            if (count == 0) {
                TextView empty = new TextView(this);
                empty.setText("No contacts added yet.");
                empty.setTextColor(0xFF757575); // Gray color
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

        private void sendAlertMessage() {
        if (!checkPermissions()) {
            pendingAlertSend = true; // Retry after permission grant
            return;
        }

        // Check if we have location permission specifically
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Location permission required for emergency alerts.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            String coordinates = "Location unavailable";
            if (location != null) {
                coordinates = String.format(Locale.getDefault(),
                        "https://maps.google.com/?q=%.5f,%.5f",
                        location.getLatitude(), location.getLongitude());
            } else {
                coordinates = "Location data not available. Please ensure GPS is enabled.";
            }

            String timestamp = new SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())
                    .format(new Date());
            String alertMessage = "🚨 EMERGENCY ALERT from SafeHer 🚨\n\n" +
                    "HELP IS NEEDED!\n" +
                    "Time: " + timestamp + "\n" +
                    "Location: " + coordinates + "\n\n" +
                    "This is an automated emergency alert. Please check on this person immediately.";

            sendSMSToSupport(alertMessage);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to get location: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();

            // Send alert anyway without location
            String timestamp = new SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())
                    .format(new Date());
            String alertMessage = "🚨 EMERGENCY ALERT from SafeHer 🚨\n\n" +
                    "HELP IS NEEDED!\n" +
                    "Time: " + timestamp + "\n" +
                    "Location: Could not determine location\n\n" +
                    "This is an automated emergency alert. Please check on this person immediately.";

            sendSMSToSupport(alertMessage);
        });
    }

    private void sendHybridSafetyMessage(String message) {
        if (!checkPermissions()) {
            pendingJournalSend = true;
            return;
        }

        String timestamp = new SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())
                .format(new Date());
        String finalMessage = "💜 SafeHer Wellness Message\n" +
                "Time: " + timestamp + "\n\n" +
                message + "\n\n" +
                "Sent via SafeHer wellness journal";

        sendSMSToSupport(finalMessage);
    }

    private void sendSMSToSupport(String message) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> contactJsonSet = prefs.getStringSet(CONTACTS_KEY, null);

        if (contactJsonSet == null || contactJsonSet.isEmpty()) {
            Toast.makeText(this, "No contacts in Support Circle.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build phone numbers list
        StringBuilder phoneNumbers = new StringBuilder();
        int contactCount = 0;

        for (String contactJson : contactJsonSet) {
            Contact contact = Contact.fromJson(contactJson);
            if (contact != null) {
                String phoneNumber = contact.getPhoneNumber();

                // Convert to local format for SMS
                if (phoneNumber.startsWith("254") && !phoneNumber.startsWith("+")) {
                    phoneNumber = "0" + phoneNumber.substring(3);
                }

                // Build list for SMS Intent
                if (contactCount > 0) {
                    phoneNumbers.append(";");
                }
                phoneNumbers.append(phoneNumber);
                contactCount++;
            }
        }

        if (contactCount == 0) {
            Toast.makeText(this, "No valid contacts found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Open SMS app with message pre-filled (most reliable method)
        try {
            String uriString = "sms:" + phoneNumbers.toString() + "?body=" + Uri.encode(message);
            Intent smsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));

            startActivity(smsIntent);

            Toast.makeText(this, "Please click SEND to alert " + contactCount + " contact(s).",
                    Toast.LENGTH_LONG).show();

            // Clear journal entry after opening SMS app
            if (etJournal != null && !pendingJournalMessage.isEmpty()) {
                etJournal.setText("");
                pendingJournalMessage = "";
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to open SMS app: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

            if (allGranted) {
                Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show();

                // Retry pending actions
                if (pendingAlertSend) {
                    pendingAlertSend = false;
                    sendAlertMessage();
                } else if (pendingJournalSend && !pendingJournalMessage.isEmpty()) {
                    pendingJournalSend = false;
                    sendHybridSafetyMessage(pendingJournalMessage);
                }
            } else {
                Toast.makeText(this, "Permissions are required for emergency alerts and location sharing.",
                        Toast.LENGTH_LONG).show();

                // Clear pending flags
                pendingAlertSend = false;
                pendingJournalSend = false;
                pendingJournalMessage = "";
            }
        }
    }
}
