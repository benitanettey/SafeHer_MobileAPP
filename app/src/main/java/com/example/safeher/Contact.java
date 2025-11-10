package com.example.safeher;

public class Contact {
    private long id = -1;
    private String name;
    private String phoneNumber;
    private String relationship;
    private boolean isPrimary;

    // New constructor with id
    public Contact(long id, String name, String phoneNumber, String relationship, boolean isPrimary) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
        this.isPrimary = isPrimary;
    }

    // Existing constructor delegates to new one with id = -1
    public Contact(String name, String phoneNumber, String relationship, boolean isPrimary) {
        this(-1, name, phoneNumber, relationship, isPrimary);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
    }

    // For JSON serialization
    public String toJson() {
        return "{\"name\":\"" + name + "\",\"phoneNumber\":\"" + phoneNumber +
                "\",\"relationship\":\"" + relationship + "\",\"isPrimary\":" + isPrimary + "}";
    }

    // For JSON deserialization
    public static Contact fromJson(String json) {
        try {
            json = json.replace("{", "").replace("}", "");
            String[] pairs = json.split(",");
            String name = "", phoneNumber = "", relationship = "";
            boolean isPrimary = false;

            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                String key = keyValue[0].replace("\"", "").trim();
                String value = keyValue.length > 1 ? keyValue[1].replace("\"", "").trim() : "";

                switch (key) {
                    case "name":
                        name = value;
                        break;
                    case "phoneNumber":
                        phoneNumber = value;
                        break;
                    case "relationship":
                        relationship = value;
                        break;
                    case "isPrimary":
                        isPrimary = Boolean.parseBoolean(value);
                        break;
                }
            }
            return new Contact(name, phoneNumber, relationship, isPrimary);
        } catch (Exception e) {
            return null;
        }
    }
}
