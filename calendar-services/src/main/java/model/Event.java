package model;

import com.calendar.model.EventAttendee;
import com.calendar.model.EventCategory;
import com.calendar.model.EventPriority;
import com.calendar.model.EventReminder;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "start_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime start;
    
    @Column(name = "end_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
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
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // One-to-many relationship with attendees
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<EventAttendee> attendees;
    
    // One-to-many relationship with reminders
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<EventReminder> reminders;
}

