package com.calendar.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventCategory {
    WORK("work"),
    PERSONAL("personal"),
    HEALTH("health"),
    EDUCATION("education");

    private final String value;

    EventCategory(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EventCategory fromValue(String value) {
        for (EventCategory category : EventCategory.values()) {
            if (category.value.equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown EventCategory: " + value);
    }
}
