package com.secureauth.productservice.client.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateEventRequest {
    private String title;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private String location;
    private String username;     // used for User-Email fallback
    private String category;
    private String priority;
    private Boolean isAllDay;
    private Boolean isRecurring;

    private List<String> attendees;

    private List<ReminderRequest> reminders;

    @Data
    public static class ReminderRequest {
        private String type;
        private Integer minutes;
    }
}
