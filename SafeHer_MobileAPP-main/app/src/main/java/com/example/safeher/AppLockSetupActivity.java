package com.example.safeher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AppLockSetupActivity extends AppCompatActivity {

    private EditText pinEditText, confirmPinEditText;
    private Button savePinButton;
    private ImageView backButton;

    private static final String PREFS_NAME = "SafeHerPrefs";
    private static final String PIN_KEY = "AppLockPin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock_setup);

        pinEditText = findViewById(R.id.pinEditText);
        confirmPinEditText = findViewById(R.id.confirmPinEditText);
        savePinButton = findViewById(R.id.savePinButton);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        savePinButton.setOnClickListener(v -> {
            String pin = pinEditText.getText().toString();
            String confirmPin = confirmPinEditText.getText().toString();

            if (pin.length() != 4) {
                Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pin.equals(confirmPin)) {
                Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(PIN_KEY, pin).apply();

            Toast.makeText(this, "PIN saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
