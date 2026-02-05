package com.example.safeher;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.FrameLayout;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatButton;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "profile_prefs";

    private ImageView avatarImageView;
    private FrameLayout cameraButton;

    private TextView nameTextView;
    private ImageView editNameButton;

    private TextView bioTextView;
    private TextView editBioButton;

    private TextView statusBadgeText;
    private AppCompatButton setActiveButton;

    private ImageView settingsButton;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // ---- BIND VIEWS ----
        avatarImageView = findViewById(R.id.avatarImageView);
        cameraButton = findViewById(R.id.cameraButton);

        nameTextView = findViewById(R.id.nameTextView);
        editNameButton = findViewById(R.id.editNameButton);

        bioTextView = findViewById(R.id.bioTextView);
        editBioButton = findViewById(R.id.editBioButton);

        statusBadgeText = findViewById(R.id.statusBadgeText);
        setActiveButton = findViewById(R.id.setActiveButton);

        settingsButton = findViewById(R.id.settingsButton);

        // ---- LOAD SAVED DATA ----
        loadProfileData();

        // ---- IMAGE PICKER SETUP ----
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        Bitmap bitmap = null;

                        if (imageUri != null) {
                            try {
                                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                                bitmap = BitmapFactory.decodeStream(imageStream);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Bundle extras = result.getData().getExtras();
                            if (extras != null) {
                                bitmap = (Bitmap) extras.get("data");
                            }
                        }

                        if (bitmap != null) {
                            avatarImageView.setImageBitmap(bitmap);
                            saveAvatarToPrefs(bitmap);
                        }
                    }
                }
        );

        // ---- BUTTON LISTENERS ----
        cameraButton.setOnClickListener(v -> requestImagePermissions());
        editNameButton.setOnClickListener(v -> showEditDialog("Edit Name", nameTextView));
        editBioButton.setOnClickListener(v -> showEditDialog("Edit Bio", bioTextView));
        setActiveButton.setOnClickListener(v -> toggleStatus());
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    // ---- IMAGE PERMISSION HANDLING ----
    private void requestImagePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pickImage();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100
                );
            } else pickImage();
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    // ---- GENERIC EDIT DIALOG ----
    private void showEditDialog(String title, TextView targetView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(targetView.getText().toString());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            targetView.setText(input.getText().toString());
            saveTextToPrefs(targetView);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // ---- STATUS HANDLER ----
    private void toggleStatus() {
        if (statusBadgeText.getText().toString().equals("Not Active")) {
            statusBadgeText.setText("Active");
            Toast.makeText(this, "You are now Active", Toast.LENGTH_SHORT).show();
        } else {
            statusBadgeText.setText("Not Active");
            Toast.makeText(this, "Status set to Not Active", Toast.LENGTH_SHORT).show();
        }
        prefs.edit().putString("status", statusBadgeText.getText().toString()).apply();
    }

    // ---- SHARED PREFERENCES ----
    private void saveTextToPrefs(TextView textView) {
        SharedPreferences.Editor editor = prefs.edit();
        if (textView == nameTextView) editor.putString("name", textView.getText().toString());
        else if (textView == bioTextView) editor.putString("bio", textView.getText().toString());
        editor.apply();
    }

    private void saveAvatarToPrefs(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        prefs.edit().putString("avatar", encodedImage).apply();
    }

    private void loadProfileData() {
        String name = prefs.getString("name", "Emily Johnson");
        String bio = prefs.getString("bio", "Taking care of my mental wellness one day at a time 🌸");
        String status = prefs.getString("status", "Not Active");
        String avatarEncoded = prefs.getString("avatar", null);

        nameTextView.setText(name);
        bioTextView.setText(bio);
        statusBadgeText.setText(status);

        if (avatarEncoded != null) {
            byte[] decodedBytes = Base64.decode(avatarEncoded, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            avatarImageView.setImageBitmap(bitmap);
        }
    }
}
