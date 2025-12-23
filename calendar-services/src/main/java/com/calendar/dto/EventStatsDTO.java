package com.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventStatsDTO {

    // Total count
    private Long total;

    // By category (flexible map)
    private Map<String, Long> byCategory;

    // By ownership
    private Map<String, Long> byOwnership;

    // By status (for attendee events)
    private Map<String, Long> byStatus;

    // Time-based
    private Long upcomingEvents;
    private Long todayEvents;

    // Legacy fields (for backwards compatibility)
    private Long workEvents;
    private Long personalEvents;
    private Long healthEvents;
    private Long educationEvents;
    private Long createdEvents;
    private Long attendingEvents;

    // Helper method to set category counts from map
    public void setByCategory(Map<String, Long> byCategory) {
        this.byCategory = byCategory;
        if (byCategory != null) {
            this.workEvents = byCategory.getOrDefault("work", 0L);
            this.personalEvents = byCategory.getOrDefault("personal", 0L);
            this.healthEvents = byCategory.getOrDefault("health", 0L);
            this.educationEvents = byCategory.getOrDefault("education", 0L);
        }
    }

    // Helper method to set ownership counts from map
    public void setByOwnership(Map<String, Long> byOwnership) {
        this.byOwnership = byOwnership;
        if (byOwnership != null) {
            this.createdEvents = byOwnership.getOrDefault("owned", 0L);
            this.attendingEvents = byOwnership.getOrDefault("attending", 0L);
        }
    }
}