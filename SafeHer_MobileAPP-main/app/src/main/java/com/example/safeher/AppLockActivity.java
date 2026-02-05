package com.example.safeher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AppLockActivity extends AppCompatActivity {

    private EditText pinEditText;
    private Button unlockButton;

    private static final String PREFS_NAME = "SafeHerPrefs";
    private static final String PIN_KEY = "AppLockPin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);

        pinEditText = findViewById(R.id.pinEditText);
        unlockButton = findViewById(R.id.unlockButton);

        unlockButton.setOnClickListener(v -> {
            String enteredPin = pinEditText.getText().toString();
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String savedPin = prefs.getString(PIN_KEY, null);

            if (savedPin != null && savedPin.equals(enteredPin)) {
                Intent intent = new Intent(AppLockActivity.this, HomeActivity.class);
                intent.putExtra("UNLOCKED", true); // Add this flag to signal a successful unlock
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
