package com.example.safeher;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddContactBottomSheet extends BottomSheetDialogFragment {

    public interface AddContactListener {
        void onContactAdded();
    }

    private AddContactListener listener;

    public void setAddContactListener(AddContactListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_add_contact, container, false);

        EditText etName = view.findViewById(R.id.etName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etRelationship = view.findViewById(R.id.etRelationship);
        Button btnSave = view.findViewById(R.id.btnSaveContact);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String relationship = etRelationship.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("Name required");
                return;
            }

            if (!phone.matches("^(?:\\+?254|0)?[17]\\d{8}$")) {
                etPhone.setError("Enter valid Kenyan phone number");
                return;
            }

            Contact newContact = new Contact(name, phone, relationship, false);
            ContactDatabaseHelper dbHelper = new ContactDatabaseHelper(requireContext());
            long id = dbHelper.addContact(newContact);

            if (id != -1) {
                Toast.makeText(getContext(), "Contact saved", Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onContactAdded();
                dismiss();
            } else {
                Toast.makeText(getContext(), "Contact already exists", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}

