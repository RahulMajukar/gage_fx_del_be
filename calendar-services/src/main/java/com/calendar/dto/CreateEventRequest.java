package com.calendar.dto;


import com.calendar.model.EventCategory;
import com.calendar.model.EventPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateEventRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String username; // this is your email

    private String description;

    @NotNull(message = "Start date is required")
    private LocalDateTime start;

    @NotNull(message = "End date is required")
    private LocalDateTime end;

    private String location;
    private EventCategory category = EventCategory.WORK;
    private EventPriority priority = EventPriority.MEDIUM;
    private Boolean isAllDay = false;
    private Boolean isRecurring = false;

    private List<String> attendees;
    private List<EventReminderRequest> reminders;
}
