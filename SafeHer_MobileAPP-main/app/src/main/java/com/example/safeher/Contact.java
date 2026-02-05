package com.example.safeher;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.regex.Pattern;

public class Contact implements Serializable {
    private String name;
    private String phoneNumber;
    private String relationship;
    private boolean isPrimary; // <-- ADD THIS FIELD

    // This empty constructor is important for Gson
    public Contact() {}

    // Updated constructor to include isPrimary
    public Contact(String name, String phoneNumber, String relationship, boolean isPrimary) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
        this.isPrimary = isPrimary; // <-- ADD THIS LINE
    }

    // --- GETTERS ---
    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getRelationship() {
        return relationship;
    }

    public boolean isPrimary() { // <-- ADD THIS GETTER
        return isPrimary;
    }

    // --- SETTERS ---
    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public void setPrimary(boolean primary) { // <-- ADD THIS SETTER
        isPrimary = primary;
    }

    // --- JSON CONVERSION METHODS ---
    public String toJson() {
        return new Gson().toJson(this);
    }

    public static Contact fromJson(String json) {
        return new Gson().fromJson(json, Contact.class);
    }

    // --- VALIDATION METHODS ---
    public static boolean isValidKenyanPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return Pattern.matches("^(0(7|1))\\d{8}$", phone.trim());
    }

    public static String normalizePhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }
        String trimmedPhone = phone.trim();
        if (trimmedPhone.startsWith("0")) {
            return "+254" + trimmedPhone.substring(1);
        } else if (trimmedPhone.startsWith("254")) {
            return "+" + trimmedPhone;
        } else if (trimmedPhone.startsWith("+254")) {
            return trimmedPhone;
        }
        return trimmedPhone;
    }
}
