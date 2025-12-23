package com.calendar.dto;

import com.calendar.model.EventCategory;
import com.calendar.model.EventPriority;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {

    // Basic event information
    private Long id;
    private String title;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime start;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime end;

    private String location;
    private EventCategory category;
    private EventPriority priority;

    // All-day and recurring flags
    private Boolean isAllDay;
    private Boolean isRecurring;

    // Creator information
    private Long createdBy;

    // Timestamps
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Related entities
    private List<EventAttendeeDTO> attendees;
    private List<EventReminderDTO> reminders;

    // ==========================================
    // USER RELATIONSHIP FIELDS (CRITICAL FOR FRONTEND)
    // ==========================================

    /**
     * Is the current user the owner/creator of this event?
     */
    private Boolean isOwner;

    /**
     * Is the current user an attendee of this event?
     */
    private Boolean isAttendee;

    /**
     * User's relationship to this event
     * Values: "owner", "attendee", "none"
     * Used for: Frontend color coding and permissions
     */
    private String userRelationship;

    /**
     * User's acceptance status (for attendees only)
     * Values: "accepted", "pending", "rejected"
     * Null for owners and non-attendees
     * Used for: Frontend color coding and status display
     */
    private String acceptanceStatus;
}