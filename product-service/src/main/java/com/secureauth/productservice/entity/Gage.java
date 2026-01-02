package com.secureauth.productservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Main Fields - Only those sent by frontend
    @Column(unique = true, nullable = true)
    private String serialNumber;

    @Column(nullable = true)
    private String modelNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gage_type_id", nullable = false)
    private GageType gageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gage_sub_type_id", nullable = false)
    private GageSubType gageSubType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inhouse_calibration_machine_id", nullable = true)
    private InhouseCalibrationMachine inhouseCalibrationMachine;

    @Enumerated(EnumType.STRING)
    private UsageFrequency usageFrequency;

    @Enumerated(EnumType.STRING)
    private Criticality criticality;

    @Enumerated(EnumType.STRING)
    private Location location;

    // Measurement Details
    private String measurementRange;
    private String accuracy;
    private LocalDate purchaseDate;

    // Manufacturer relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id", nullable = true)
    private Manufacturer manufacturer;

    private Integer calibrationInterval;

    // New fields for enhanced gage management
    private LocalDate nextCalibrationDate;
    private Integer maxUsersNumber;
    private LocalDate pendingCalibrationDate; // Auto-calculated based on criticality
    private Integer remainingDays; // Daily countdown from today to next calibration date

    @Column(columnDefinition = "TEXT")
    private String notes;

    // ✅ MULTIPLE GAGE IMAGES (replaces single gageImage)
    @ElementCollection
    @CollectionTable(
            name = "gage_images",
            joinColumns = @JoinColumn(name = "gage_id")
    )
    @Column(name = "image_data", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> gageImages = new ArrayList<>();

    // ✅ OPTIONAL VIDEO UPLOAD
    @ElementCollection
    @CollectionTable(
            name = "gage_videos",
            joinColumns = @JoinColumn(name = "gage_id")
    )
    @Column(name = "video_data", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> gageVideos = new ArrayList<>();

    // Manual field (still single file)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String gageManual;

    // Calibration History relationship
    @OneToMany(mappedBy = "gage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CalibrationHistory> calibrationHistory = new ArrayList<>();

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (remainingDays == null) {
            // Initialize remaining days if not set
            calculateRemainingDays();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Recalculate remaining days on update
        calculateRemainingDays();
    }

    @Lob
    @Column(columnDefinition = "TEXT")
    private String barcodeImage; // Base64-encoded PNG of the barcode

    @Lob
    @Column(columnDefinition = "TEXT")
    private String qrCodeImage; // Base64 QR code

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CodeType codeType = CodeType.BARCODE_ONLY; // default

    // Helper method to calculate remaining days until next calibration
    private void calculateRemainingDays() {
        if (nextCalibrationDate != null) {
            LocalDate today = LocalDate.now();
            if (nextCalibrationDate.isAfter(today)) {
                this.remainingDays = (int) java.time.temporal.ChronoUnit.DAYS.between(today, nextCalibrationDate);
            } else {
                this.remainingDays = 0; // Overdue
            }
        }
    }

    // Helper method to add image
    public void addImage(String base64Image) {
        if (this.gageImages == null) {
            this.gageImages = new ArrayList<>();
        }
        this.gageImages.add(base64Image);
    }

    // Helper method to add video
    public void addVideo(String base64Video) {
        if (this.gageVideos == null) {
            this.gageVideos = new ArrayList<>();
        }
        this.gageVideos.add(base64Video);
    }

    // Helper method to add calibration history
    public void addCalibrationHistory(CalibrationHistory history) {
        if (this.calibrationHistory == null) {
            this.calibrationHistory = new ArrayList<>();
        }
        history.setGage(this);
        this.calibrationHistory.add(history);
    }

    // Business method to check if gage is due for calibration
    public boolean isDueForCalibration() {
        if (nextCalibrationDate == null) return false;
        return LocalDate.now().isAfter(nextCalibrationDate) ||
                LocalDate.now().equals(nextCalibrationDate);
    }

    // Business method to check if gage is overdue for calibration
    public boolean isOverdueForCalibration() {
        if (nextCalibrationDate == null) return false;
        return LocalDate.now().isAfter(nextCalibrationDate);
    }

    // Business method to get days overdue (negative if not overdue)
    public int getDaysOverdue() {
        if (nextCalibrationDate == null) return 0;
        if (LocalDate.now().isBefore(nextCalibrationDate)) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextCalibrationDate);
        } else {
            return -(int) java.time.temporal.ChronoUnit.DAYS.between(nextCalibrationDate, LocalDate.now());
        }
    }

    // Add enum inside Gage class
    public enum CodeType {
        BARCODE_ONLY,
        QR_ONLY,
        BOTH
    }

    // Category enum removed - now using GageSubType entity

    public enum UsageFrequency {
        DAILY,
        WEEKLY,
        MONTHLY,
        OCCASIONALLY
    }

    public enum Criticality {
        HIGH,
        MEDIUM,
        LOW
    }

    public enum Location {
        SHOP_FLOOR,
        LAB,
        WAREHOUSE,
        OFFICE,
        FIELD
    }

    public enum Status {
        ACTIVE,
        ISSUED,
        INACTIVE,
        SCHEDULED,
        OUT_OF_STORE,
        OUT_FOR_CALIBRATION,
        IN_USE
    }

    // toString method excluding large data fields for better logging
    @Override
    public String toString() {
        return "Gage{" +
                "id=" + id +
                ", serialNumber='" + serialNumber + '\'' +
                ", modelNumber='" + modelNumber + '\'' +
                ", gageType=" + (gageType != null ? gageType.getName() : "null") +
                ", gageSubType=" + (gageSubType != null ? gageSubType.getName() : "null") +
                ", status=" + status +
                ", usageFrequency=" + usageFrequency +
                ", criticality=" + criticality +
                ", location=" + location +
                ", nextCalibrationDate=" + nextCalibrationDate +
                ", remainingDays=" + remainingDays +
                '}';
    }
}