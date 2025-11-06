package com.example.safeher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SupportCircleActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SafeHerPrefs";
    private static final String CONTACTS_KEY = "SupportContacts";

    private EditText etPhoneNumber;
    private Button btnAddContact, btnClearAll;
    private ListView listViewContacts;

    private ArrayList<String> contactList;
    private ArrayAdapter<String> adapter;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_circle);

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnClearAll = findViewById(R.id.btnClearAll);
        listViewContacts = findViewById(R.id.listViewContacts);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        contactList = new ArrayList<>(prefs.getStringSet(CONTACTS_KEY, new HashSet<>()));

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactList);
        listViewContacts.setAdapter(adapter);

        // Add a contact
        btnAddContact.setOnClickListener(v -> {
            String number = etPhoneNumber.getText().toString().trim();

            if (number.isEmpty()) {
                Toast.makeText(this, "Enter a phone number.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!number.matches("^(?:\\+?254|0)?[17]\\d{8}$")) {
                Toast.makeText(this, "Enter a valid Kenyan phone number.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!contactList.contains(number)) {
                contactList.add(number);
                saveContacts();
                adapter.notifyDataSetChanged();
                etPhoneNumber.setText("");
                Toast.makeText(this, "Contact added.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Number already added.", Toast.LENGTH_SHORT).show();
            }
        });

        // Remove a contact on click
        listViewContacts.setOnItemClickListener((parent, view, position, id) -> {
            String number = contactList.get(position);
            contactList.remove(position);
            saveContacts();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Removed " + number, Toast.LENGTH_SHORT).show();
        });

        // Clear all contacts
        btnClearAll.setOnClickListener(v -> {
            contactList.clear();
            saveContacts();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "All contacts cleared.", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveContacts() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(CONTACTS_KEY, new HashSet<>(contactList));
        editor.apply();
    }
}
