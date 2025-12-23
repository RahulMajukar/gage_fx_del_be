package com.calendar.model;

/**
 * Types of reminders for events
 */
public enum ReminderType {
    POPUP("popup"),
    EMAIL("email"),
    SMS("sms"),
    NOTIFICATION("notification");

    private final String value;

    ReminderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}