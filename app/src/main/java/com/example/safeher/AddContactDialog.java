package com.example.safeher;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AddContactDialog extends DialogFragment {

    public interface AddContactListener {
        void onContactAdded(); // simplified, since we reload from DB
    }

    private AddContactListener listener;

    public void setAddContactListener(AddContactListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_contact, null);

        EditText etName = view.findViewById(R.id.etName);
        EditText etRelationship = view.findViewById(R.id.etRelationship);
        EditText etPhone = view.findViewById(R.id.etPhone);
        CheckBox cbPrimary = view.findViewById(R.id.cbPrimary);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSave = view.findViewById(R.id.btnSave);

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog)
                .setView(view)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String relationship = etRelationship.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            boolean isPrimary = cbPrimary.isChecked();

            // Validation
            if (TextUtils.isEmpty(name)) {
                etName.setError("Name required");
                return;
            }

            if (!phone.matches("^(?:\\+?254|0)?[17]\\d{8}$")) {
                etPhone.setError("Enter valid Kenyan phone number");
                return;
            }

            // Save to SQLite
            // Contact constructor expects: (name, phoneNumber, relationship, isPrimary)
            Contact newContact = new Contact(name, phone, relationship, isPrimary);
            ContactDatabaseHelper dbHelper = new ContactDatabaseHelper(getContext());

            long result = dbHelper.addContact(newContact);

            if (result != -1) {
                Toast.makeText(getContext(), "Contact saved", Toast.LENGTH_SHORT).show();

                if (listener != null) listener.onContactAdded(); // reload list
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Contact already exists", Toast.LENGTH_SHORT).show();
            }
        });

        return dialog;
    }
}
