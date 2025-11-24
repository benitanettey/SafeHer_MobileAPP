package com.example.safeher;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Contact {
    private static final String TAG = "Contact";
    private String name;
    private String phoneNumber;
    private String relationship;
    private boolean isPrimary;

    private static final Gson gson = new Gson();

    public Contact(String name, String phoneNumber, String relationship, boolean isPrimary) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
        this.isPrimary = isPrimary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public String getInitials() {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
    }

    /**
     * Normalizes Kenyan phone numbers to standard format (+254XXXXXXXXX)
     * Handles: 0712345678, +254712345678, 254712345678, 0712 345 678
     */
    public static String normalizePhoneNumber(String phone) {
        if (phone == null) return "";

        // Remove all whitespace, dashes, and parentheses
        String cleaned = phone.replaceAll("[\\s\\-()]", "");

        // Remove leading + if present
        if (cleaned.startsWith("+")) {
            cleaned = cleaned.substring(1);
        }

        // Convert 0712345678 to 254712345678
        if (cleaned.startsWith("0") && cleaned.length() == 10) {
            cleaned = "254" + cleaned.substring(1);
        }

        // Ensure it starts with 254
        if (!cleaned.startsWith("254") && cleaned.length() == 9) {
            cleaned = "254" + cleaned;
        }

        return cleaned;
    }

    /**
     * Validates if phone number is a valid Kenyan mobile number
     */
    public static boolean isValidKenyanPhone(String phone) {
        String normalized = normalizePhoneNumber(phone);
        // Kenyan mobile numbers: 254 7XX XXX XXX or 254 1XX XXX XXX
        return normalized.matches("^254[71]\\d{8}$");
    }

    // For JSON serialization using Gson
    public String toJson() {
        return gson.toJson(this);
    }

    // For JSON deserialization using Gson
    public static Contact fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(json, Contact.class);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Failed to parse contact JSON: " + json, e);
            return null;
        }
    }
}

