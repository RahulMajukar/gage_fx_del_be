package com.secureauth.productservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "calibration_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalibrationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gage_id", nullable = false)
    private Gage gage;

    @Column(nullable = false)
    private LocalDate calibrationDate;

    @Column(nullable = false)
    private LocalDate nextDueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalibrationStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String performedBy;

    @Column(columnDefinition = "BYTEA")
    private byte[] certificate; // PDF certificate as binary data

    // Audit fields
    @Column(nullable = false)
    private LocalDate createdAt;

    // Media attachments - fixed the relationship
    @OneToMany(mappedBy = "calibrationHistory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CalibrationMedia> mediaAttachments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
    }

    public enum CalibrationStatus {
        PASSED, FAILED, CONDITIONAL_PASS, OUT_OF_TOLERANCE
    }

    // Helper method to add media
    public void addMediaAttachment(CalibrationMedia media) {
        mediaAttachments.add(media);
        media.setCalibrationHistory(this);
    }

    // Helper method to remove media
    public void removeMediaAttachment(CalibrationMedia media) {
        mediaAttachments.remove(media);
        media.setCalibrationHistory(null);
    }
}