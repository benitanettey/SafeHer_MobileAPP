package com.example.safeher;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// NO LONGER IMPLEMENTS OnContactDeleteListener
public class HomeActivity extends AppCompatActivity {
    private Button btnNotOkay, btnSaveEntry;
    private TextView tvDateTime;
    private EditText etJournal;
    private ImageView btnProfile, btnAddContact;
    private FusedLocationProviderClient fusedLocationClient;
    private RecyclerView recyclerViewContacts;
    private ContactAdapter contactAdapter;
    private List<Contact> supportContactList = new ArrayList<>();

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String PREFS_NAME = "SafeHerPrefs";
    private static final String CONTACTS_KEY = "SupportContacts";
    private static final String ALERT_HISTORY_KEY = "AlertHistory";
    private static final String PIN_KEY = "AppLockPin";

    private boolean pendingAlertSend = false;
    private boolean pendingJournalSend = false;
    private String pendingJournalMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isUnlocked = getIntent().getBooleanExtra("UNLOCKED", false);

        if (prefs.getString(PIN_KEY, null) != null && !isUnlocked) {
            Intent intent = new Intent(this, AppLockActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Initialize views
        btnNotOkay = findViewById(R.id.btnNotOkay);
        btnSaveEntry = findViewById(R.id.btnSaveEntry);
        etJournal = findViewById(R.id.etJournal);
        tvDateTime = findViewById(R.id.tvDateTime);
        btnProfile = findViewById(R.id.btnProfile);
        btnAddContact = findViewById(R.id.btnAddContact);
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        updateDateTime();

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        btnAddContact.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SupportCircleActivity.class);
            startActivity(intent);
        });

        btnNotOkay.setOnClickListener(v -> {
            if (supportContactList.isEmpty()) {
                Toast.makeText(this, "Please add contacts to your Support Circle first.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(HomeActivity.this, SupportCircleActivity.class);
                startActivity(intent);
                return;
            }
            sendAlertMessage();
        });

        btnSaveEntry.setOnClickListener(v -> {
            String message = etJournal.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Please write a message first.", Toast.LENGTH_SHORT).show();
                etJournal.requestFocus();
            } else if (supportContactList.isEmpty()) {
                Toast.makeText(this, "Please add contacts to your Support Circle first.", Toast.LENGTH_LONG).show();
            } else {
                pendingJournalMessage = message;
                sendHybridSafetyMessage(message);
            }
        });

        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDateTime();
        loadContacts(); // Reload contacts every time the user returns
    }

    private void setupRecyclerView() {
        if (recyclerViewContacts == null) return; // Safety check
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        // CORRECTED: Pass null for the listener, as HomeActivity does not handle deletes.
        contactAdapter = new ContactAdapter(this, supportContactList, null);
        recyclerViewContacts.setAdapter(contactAdapter);
    }

    private void loadContacts() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> contactJsonSet = prefs.getStringSet(CONTACTS_KEY, new HashSet<>());
        supportContactList.clear();
        for (String json : contactJsonSet) {
            supportContactList.add(Contact.fromJson(json));
        }
        if (contactAdapter != null) {
            contactAdapter.notifyDataSetChanged();
        }
    }

    private void sendAlertMessage() {
        if (!checkPermissions()) {
            pendingAlertSend = true;
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required for emergency alerts.", Toast.LENGTH_SHORT).show();
            return;
        }

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken()).addOnSuccessListener(this, location -> {
            String coordinates;
            if (location != null) {
                coordinates = String.format(Locale.US,
                        "https://maps.google.com/?q=%.5f,%.5f",
                        location.getLatitude(), location.getLongitude());
            } else {
                coordinates = "Location data not available. Please ensure GPS is enabled.";
            }

            String timestamp = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
            String alertMessage = "SafeHer Alert: I need help. My location: " + coordinates + " (Sent at " + timestamp + ")";

            saveAlertToHistory(alertMessage);
            sendSMSToSupport(alertMessage);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to get location: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();

            String timestamp = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
            String alertMessage = "SafeHer Alert: I need help. Location could not be determined. (Sent at " + timestamp + ")";

            saveAlertToHistory(alertMessage);
            sendSMSToSupport(alertMessage);
        });
    }

    private void sendHybridSafetyMessage(String message) {
        if (!checkPermissions()) {
            pendingJournalSend = true;
            return;
        }

        String timestamp = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
        String finalMessage = "SafeHer Message (at " + timestamp + "): " + message;

        saveAlertToHistory(finalMessage);
        sendSMSToSupport(finalMessage);
    }

    private void sendSMSToSupport(String message) {
        if (supportContactList.isEmpty()) {
            Toast.makeText(this, "No contacts in Support Circle.", Toast.LENGTH_SHORT).show();
            return;
        }

        int contactsToSend = 0;
        for (Contact contact : supportContactList) {
            String phoneNumber = contact.getPhoneNumber();
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                try {
                    Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
                    smsIntent.putExtra("sms_body", message);
                    startActivity(smsIntent);
                    contactsToSend++;
                } catch (Exception e) {
                    Toast.makeText(this, "Could not open SMS app for " + contact.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (contactsToSend > 0) {
            Toast.makeText(this, "Opening SMS app to alert " + contactsToSend + " contact(s).", Toast.LENGTH_LONG).show();
            if (etJournal != null && !pendingJournalMessage.isEmpty()) {
                etJournal.setText("");
                pendingJournalMessage = "";
            }
        } else {
            Toast.makeText(this, "No valid contacts found to send alerts.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAlertToHistory(String message) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(ALERT_HISTORY_KEY, null);
        Type type = new TypeToken<ArrayList<Alert>>() {}.getType();
        List<Alert> alerts = new Gson().fromJson(json, type);
        if (alerts == null) {
            alerts = new ArrayList<>();
        }
        alerts.add(new Alert(message, System.currentTimeMillis()));
        String updatedJson = new Gson().toJson(alerts);
        prefs.edit().putString(ALERT_HISTORY_KEY, updatedJson).apply();
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
                if (pendingAlertSend) {
                    sendAlertMessage();
                    pendingAlertSend = false;
                } else if (pendingJournalSend) {
                    sendHybridSafetyMessage(pendingJournalMessage);
                    pendingJournalSend = false;
                    pendingJournalMessage = "";
                }
            } else {
                Toast.makeText(this, "Permissions are required to send alerts.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
