package model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReminderType {
    POPUP("popup"),
    EMAIL("email"),
    SMS("sms");

    private final String value;

    ReminderType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ReminderType fromValue(String value) {
        for (ReminderType type : ReminderType.values()) {
            if (type.value.equalsIgnoreCase(value)) {  // case-insensitive
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ReminderType: " + value);
    }
}
