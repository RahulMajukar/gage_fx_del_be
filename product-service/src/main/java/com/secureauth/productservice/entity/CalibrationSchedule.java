package com.secureauth.productservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "calibration_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalibrationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gage_id", nullable = false)
    private Gage gage;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    private LocalTime scheduledTime;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(nullable = false)
    private String assignedTo;

    @Column(nullable = false)
    private String laboratory;

    private Integer estimatedDuration; // in hours

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Boolean requiresSpecialEquipment;

    @Column(columnDefinition = "TEXT")
    private String specialEquipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus status;

    private Boolean emailSent;

    private LocalDateTime emailSentAt;

    // Audit fields
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Lob
    @Column(name = "serial_number_photo")
    private byte[] serialNumberPhoto;

    @Lob
    @Column(name = "front_view_photo")
    private byte[] frontViewPhoto;

    @Lob
    @Column(name = "back_view_photo")
    private byte[] backViewPhoto;

    @Column(name = "serial_number_photo_content_type", length = 255)
    private String serialNumberPhotoContentType;

    @Column(name = "front_view_photo_content_type", length = 255)
    private String frontViewPhotoContentType;

    @Column(name = "back_view_photo_content_type", length = 255)
    private String backViewPhotoContentType;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = ScheduleStatus.SCHEDULED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Priority {
        HIGH, MEDIUM, LOW
    }

    public enum ScheduleStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, RESCHEDULED
    }
}