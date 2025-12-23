package com.calendar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.ReminderType;

@Entity
@Table(name = "event_reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventReminder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderType type = ReminderType.POPUP;
    
    @Column(nullable = false)
    private Integer minutes;
    
    @Column(name = "is_sent")
    private Boolean isSent = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}
