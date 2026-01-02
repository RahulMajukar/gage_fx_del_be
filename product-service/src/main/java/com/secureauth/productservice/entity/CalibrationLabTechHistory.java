// src/main/java/com/secureauth/productservice/entity/CalibrationLabTechHistory.java
package com.secureauth.productservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "calibration_lab_tech_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalibrationLabTechHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gage_id", nullable = false)
    @JsonIgnoreProperties({
            "gageType",
            "calibrationHistories",
            "hibernateLazyInitializer",
            "handler"
    })
    private Gage gage;

    @Column(nullable = false)
    private String technician; // Assuming you have a User entity

    @Column(nullable = false)
    private LocalDate calibrationDate;

    @Column(nullable = false)
    private LocalDate nextCalibrationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalibrationResult result;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(nullable = false)
    private String calibratedBy;

    @Column(nullable = false)
    private String certificateNumber;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private Double calibrationDuration; // in hours

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id")
    @JsonIgnoreProperties({
            "calibrationHistories",
            "hibernateLazyInitializer",
            "handler"
    })
    private InhouseCalibrationMachine calibrationMachine;

    // Audit fields
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}