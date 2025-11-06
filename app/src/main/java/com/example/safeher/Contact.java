package com.example.safeher;

public class Contact {

    private String name;
    private String phoneNumber;
    private String relationship;
    private boolean isPrimary;
    private int avatarColor; // dynamic color (lavender/pink)

    // Constructor
    public Contact(String name, String phoneNumber, String relationship, boolean isPrimary, int avatarColor) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
        this.isPrimary = isPrimary;
        this.avatarColor = avatarColor;
    }

    // Getters
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getRelationship() { return relationship; }
    public boolean isPrimary() { return isPrimary; }
    public int getAvatarColor() { return avatarColor; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public void setPrimary(boolean primary) { isPrimary = primary; }
    public void setAvatarColor(int avatarColor) { this.avatarColor = avatarColor; }

    public String getInitials() {
        if (name == null || name.isEmpty()) return "";
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) initials.append(part.charAt(0));
        }
        return initials.length() > 2 ? initials.substring(0, 2).toUpperCase() : initials.toString().toUpperCase();
    }
}
