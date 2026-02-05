package com.example.safeher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SupportCircleActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SafeHerPrefs";
    private static final String CONTACTS_KEY = "SupportContacts";
    private static final int MAX_CONTACTS = 10; // Limit for safety

    private Button btnAddContact, btnClearAll;
    private RecyclerView recyclerViewContacts;

    private ArrayList<Contact> contactList;
    private ContactAdapter adapter;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_circle);

        btnAddContact = findViewById(R.id.btnAddContact);
        btnClearAll = findViewById(R.id.btnClearAll);
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        contactList = loadContacts();

        // CORRECTED: Initialize adapter with the correct constructor (context, list, listener)
        adapter = new ContactAdapter(this, contactList, contact -> {
            // Handle contact deletion
            int position = contactList.indexOf(contact);
            if (position != -1) {
                String deletedName = contact.getName();
                contactList.remove(position);
                saveContacts();
                adapter.notifyItemRemoved(position);

                // Verify deletion
                android.util.Log.d("SupportCircle", "Deleted contact: " + deletedName +
                        ", Remaining: " + contactList.size());

                Toast.makeText(this, "Removed " + deletedName, Toast.LENGTH_SHORT).show();
            }
        });

        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewContacts.setAdapter(adapter);

        // Open bottomsheet to add a contact
        btnAddContact.setOnClickListener(v -> showAddContactBottomSheet());

        // Clear all contacts with confirmation
        btnClearAll.setOnClickListener(v -> showClearAllConfirmation());
    }

    private void showClearAllConfirmation() {
        if (contactList.isEmpty()) {
            Toast.makeText(this, "No contacts to clear.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Clear All Contacts")
                .setMessage("Are you sure you want to remove all " + contactList.size() +
                        " contacts? This cannot be undone and data will be permanently deleted.")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    clearAllAppData(); // Use dedicated method
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "All contacts permanently deleted.", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showAddContactBottomSheet() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        bottomSheet.setContentView(R.layout.bottomsheet_add_contact);

        EditText etName = bottomSheet.findViewById(R.id.etName);
        EditText etPhone = bottomSheet.findViewById(R.id.etPhone);
        EditText etRelationship = bottomSheet.findViewById(R.id.etRelationship);
        SwitchMaterial switchPrimary = bottomSheet.findViewById(R.id.switchPrimary);
        Button btnSaveContact = bottomSheet.findViewById(R.id.btnSaveContact);

        if (btnSaveContact == null || etName == null || etPhone == null ||
                etRelationship == null || switchPrimary == null) {
            Toast.makeText(this, "Error loading contact form.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveContact.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String relationship = etRelationship.getText().toString().trim();
            boolean isPrimary = switchPrimary.isChecked();

            // Validation: Check contact limit
            if (contactList.size() >= MAX_CONTACTS) {
                Toast.makeText(this, "Maximum " + MAX_CONTACTS + " contacts allowed.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validation: Name
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a name.", Toast.LENGTH_SHORT).show();
                etName.requestFocus();
                return;
            }

            if (name.length() < 2) {
                Toast.makeText(this, "Name must be at least 2 characters.", Toast.LENGTH_SHORT).show();
                etName.requestFocus();
                return;
            }

            if (name.length() > 50) {
                Toast.makeText(this, "Name is too long (max 50 characters).", Toast.LENGTH_SHORT).show();
                etName.requestFocus();
                return;
            }

            // Validation: Phone
            if (phone.isEmpty()) {
                Toast.makeText(this, "Please enter a phone number.", Toast.LENGTH_SHORT).show();
                etPhone.requestFocus();
                return;
            }

            if (!Contact.isValidKenyanPhone(phone)) {
                Toast.makeText(this, "Enter a valid Kenyan mobile number (07XX or 01XX).", Toast.LENGTH_LONG).show();
                etPhone.requestFocus();
                return;
            }

            // Normalize phone number for storage and comparison
            String normalizedPhone = Contact.normalizePhoneNumber(phone);

            // Validation: Relationship
            if (relationship.isEmpty()) {
                Toast.makeText(this, "Please enter a relationship.", Toast.LENGTH_SHORT).show();
                etRelationship.requestFocus();
                return;
            }

            if (relationship.length() > 30) {
                Toast.makeText(this, "Relationship is too long (max 30 characters).", Toast.LENGTH_SHORT).show();
                etRelationship.requestFocus();
                return;
            }

            // Check if phone already exists (using normalized number)
            for (Contact contact : contactList) {
                String existingNormalized = Contact.normalizePhoneNumber(contact.getPhoneNumber());
                if (existingNormalized != null && existingNormalized.equals(normalizedPhone)) {
                    Toast.makeText(this, "Contact with this number already exists: " + contact.getName(),
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }

            // If setting as primary, unset other primary contacts
            if (isPrimary) {
                for (Contact contact : contactList) {
                    contact.setPrimary(false);
                }
            }

            // Add contact with normalized phone number
            Contact newContact = new Contact(name, normalizedPhone, relationship, isPrimary);
            contactList.add(newContact);
            saveContacts();
            adapter.notifyDataSetChanged(); // Use notifyDataSetChanged after modifying primary status

            Toast.makeText(this, "Contact added successfully!", Toast.LENGTH_SHORT).show();
            bottomSheet.dismiss();
        });

        bottomSheet.show();
    }

    private void saveContacts() {
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> contactJsonSet = new HashSet<>();
        for (Contact contact : contactList) {
            contactJsonSet.add(contact.toJson());
        }
        editor.putStringSet(CONTACTS_KEY, contactJsonSet);
        editor.apply();

        // Log for debugging
        android.util.Log.d("SupportCircle", "Saved " + contactList.size() + " contacts");
    }

    /**
     * Completely clears all app data from SharedPreferences
     * This ensures data is properly deleted and won't persist
     */
    private void clearAllAppData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        boolean success = editor.commit(); // Use commit() for immediate write

        if (success) {
            android.util.Log.d("SupportCircle", "All app data cleared successfully");
        } else {
            android.util.Log.e("SupportCircle", "Failed to clear app data");
        }

        // Also clear the in-memory list
        contactList.clear();
    }

    private ArrayList<Contact> loadContacts() {
        Set<String> contactJsonSet = prefs.getStringSet(CONTACTS_KEY, new HashSet<>());
        ArrayList<Contact> contacts = new ArrayList<>();
        if (contactJsonSet != null) {
            for (String json : contactJsonSet) {
                Contact contact = Contact.fromJson(json);
                if (contact != null) {
                    contacts.add(contact);
                }
            }
        }
        return contacts;
    }
}
