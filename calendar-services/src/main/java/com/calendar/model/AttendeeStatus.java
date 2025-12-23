package com.calendar.model;

public enum AttendeeStatus {
    PENDING("pending"),
    ACCEPTED("accepted"),
    DECLINED("declined"),
    TENTATIVE("tentative");
    
    private final String value;
    
    AttendeeStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}

