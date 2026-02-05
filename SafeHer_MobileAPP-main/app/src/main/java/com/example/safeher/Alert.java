package com.example.safeher;

public class Alert {
    private String message;
    private long timestamp;

    public Alert(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
