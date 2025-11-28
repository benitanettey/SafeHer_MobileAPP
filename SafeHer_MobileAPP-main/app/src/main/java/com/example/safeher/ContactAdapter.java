package com.example.safeher;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    public interface OnContactDeleteListener {
        void onContactDelete(Contact contact);
    }

    private final Context context;
    private final List<Contact> contactList;
    private final OnContactDeleteListener deleteListener;
    private final int[] avatarColors;

    public ContactAdapter(Context context, List<Contact> contactList, OnContactDeleteListener deleteListener) {
        this.context = context;
        this.contactList = contactList;
        this.deleteListener = deleteListener;
        this.avatarColors = context.getResources().getIntArray(R.array.avatar_colors);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);

        String contactName = contact.getName() != null ? contact.getName() : "Unknown";

        holder.contactName.setText(contactName);
        holder.contactRelationship.setText(contact.getRelationship() != null ? contact.getRelationship() : "");
        holder.contactInitials.setText(getInitials(contactName));

        // Set avatar background color with safety check
        if (avatarColors.length > 0) {
            int color = avatarColors[Math.abs(contactName.hashCode()) % avatarColors.length];
            GradientDrawable drawable = (GradientDrawable) holder.initialsContainer.getBackground();
            if (drawable != null) {
                drawable.setColor(color);
            }
        }

        // Handle delete button
        if (deleteListener != null) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> deleteListener.onContactDelete(contact));
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        if (parts.length > 0 && !parts[0].isEmpty()) {
            initials.append(parts[0].charAt(0));
            if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
                initials.append(parts[parts.length - 1].charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }

    public void updateContactList(List<Contact> newList) {
        this.contactList.clear();
        this.contactList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactName, contactInitials, contactRelationship;
        View initialsContainer;
        ImageView deleteButton;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.contactName);
            contactInitials = itemView.findViewById(R.id.contactInitials);
            contactRelationship = itemView.findViewById(R.id.contactRelationship);
            initialsContainer = itemView.findViewById(R.id.initialsContainer);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
