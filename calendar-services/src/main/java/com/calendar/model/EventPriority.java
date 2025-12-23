package com.calendar.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Event priority levels
 * Can be used for visual indicators in the frontend
 */
public enum EventPriority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    private final String value;

    EventPriority(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EventPriority fromValue(String value) {
        for (EventPriority priority : EventPriority.values()) {
            if (priority.value.equalsIgnoreCase(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown priority: " + value);
    }
}