package com.calendar.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ✅ Added timezone so date doesn't shift to previous day
    @Column(name = "start_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime start;

    @Column(name = "end_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime end;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory category = EventCategory.WORK;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventPriority priority = EventPriority.MEDIUM;

    @Column(name = "is_all_day")
    private Boolean isAllDay = false;

    @Column(name = "is_recurring")
    private Boolean isRecurring = false;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<EventAttendee> attendees;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<EventReminder> reminders;

    @Column(name = "created_by_email", nullable = false)
    private String createdByEmail;

    // 🔍 Optional: Helpful debugging log
    @PrePersist
    @PreUpdate
    private void logTimeForDebugging() {
        System.out.println("🕒 Event Debug Log → Start: " + start + ", End: " + end);
    }

    // Explicit getters/setters (Lombok already covers these, but kept for clarity)
    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public Boolean getAllDay() {
        return isAllDay;
    }

    public void setAllDay(Boolean allDay) {
        isAllDay = allDay;
    }

    public Boolean getRecurring() {
        return isRecurring;
    }

    public void setRecurring(Boolean recurring) {
        isRecurring = recurring;
    }
}
