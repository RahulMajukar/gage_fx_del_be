package com.secureauth.productservice.client.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private String location;
    private String category;
    private String priority;
    private Boolean isAllDay;
    private Boolean isRecurring;
    private String createdByEmail;
    private Long createdBy;

    private List<AttendeeDTO> attendees;
    private List<ReminderDTO> reminders;

    @Data
    public static class AttendeeDTO {
        private Long id;
        private String username;
        private String status;
    }

    @Data
    public static class ReminderDTO {
        private Long id;
        private String type;
        private Integer minutes;
        private Boolean isSent;
    }
}
