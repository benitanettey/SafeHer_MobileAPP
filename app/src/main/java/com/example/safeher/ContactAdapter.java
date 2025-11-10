package com.example.safeher;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private final List<Contact> contactList;
    private final OnContactDeleteListener deleteListener;

    // Alternate colors for initials circle background
    private final int[] avatarColors = {
            0xFFB39DDB, // Soft Purple
            0xFFF48FB1, // Soft Pink
            0xFF81D4FA, // Light Blue
            0xFFA5D6A7, // Mint Green
            0xFFFFE082  // Soft Yellow
    };

    // Constructor
    public ContactAdapter(List<Contact> contactList, OnContactDeleteListener deleteListener) {
        this.contactList = contactList;
        this.deleteListener = deleteListener;
    }

    // Interface for delete action
    public interface OnContactDeleteListener {
        void onDelete(Contact contact);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);

        // Bind text fields
        holder.tvContactName.setText(contact.getName());
        holder.tvRelationship.setText(contact.getRelationship());
        holder.tvPhoneNumber.setText(contact.getPhoneNumber());
        holder.tvInitials.setText(getInitials(contact.getName()));

        // Tint circular background dynamically
        Drawable background = holder.tvInitials.getBackground();
        if (background != null) {
            Drawable wrapped = DrawableCompat.wrap(background.mutate());
            int color = avatarColors[position % avatarColors.length];
            DrawableCompat.setTint(wrapped, color);
            holder.tvInitials.setBackground(wrapped);
        }

        // Handle primary badge visibility
        holder.tvPrimaryBadge.setVisibility(contact.isPrimary() ? View.VISIBLE : View.GONE);

        // Handle delete click
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    // ViewHolder class
    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvContactName, tvRelationship, tvPhoneNumber, tvPrimaryBadge;
        ImageButton btnDelete;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tvInitials);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvRelationship = itemView.findViewById(R.id.tvRelationship);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            tvPrimaryBadge = itemView.findViewById(R.id.tvPrimaryBadge);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // Helper: get initials from name
    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) initials.append(Character.toUpperCase(part.charAt(0)));
        }
        return initials.length() > 2 ? initials.substring(0, 2) : initials.toString();
    }
}
