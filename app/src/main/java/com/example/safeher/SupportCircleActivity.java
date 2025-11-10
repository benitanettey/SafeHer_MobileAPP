package com.example.safeher;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SupportCircleActivity extends AppCompatActivity implements AddContactBottomSheet.AddContactListener {

    private Button btnAddContact, btnClearAll;
    private RecyclerView recyclerViewContacts;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList;
    private ContactDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_circle);

        btnAddContact = findViewById(R.id.btnAddContact);
        btnClearAll = findViewById(R.id.btnClearAll);
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);

        dbHelper = new ContactDatabaseHelper(this);
        contactList = dbHelper.getAllContacts();
        if (contactList == null) contactList = new ArrayList<>();

        contactAdapter = new ContactAdapter(contactList, contact -> {
            dbHelper.deleteContact(contact.getPhoneNumber());
            contactList.remove(contact);
            contactAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Removed " + contact.getPhoneNumber(), Toast.LENGTH_SHORT).show();
        });

        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewContacts.setAdapter(contactAdapter);

        btnAddContact.setOnClickListener(v -> {
            AddContactBottomSheet sheet = new AddContactBottomSheet();
            sheet.setAddContactListener(this);
            sheet.show(getSupportFragmentManager(), "AddContactBottomSheet");
        });

        btnClearAll.setOnClickListener(v -> {
            dbHelper.clearAllContacts();
            contactList.clear();
            contactAdapter.notifyDataSetChanged();
            Toast.makeText(this, "All contacts cleared.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onContactAdded() {
        // reload from DB
        List<Contact> updated = dbHelper.getAllContacts();
        contactList.clear();
        if (updated != null) contactList.addAll(updated);
        contactAdapter.notifyDataSetChanged();
    }
}
