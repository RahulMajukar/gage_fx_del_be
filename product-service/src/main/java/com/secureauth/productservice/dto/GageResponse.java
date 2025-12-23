package com.secureauth.productservice.dto;

import com.secureauth.productservice.entity.Gage;
import com.secureauth.productservice.entity.CalibrationHistory;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GageResponse {

    private Long id;

    // Main Gage Fields (exactly what frontend sends)
    private String serialNumber;
    private String modelNumber;
    private GageTypeResponse gageType;
    private GageSubTypeResponse gageSubType;
    private InhouseCalibrationMachineResponse inhouseCalibrationMachine; // Optional
    private Gage.UsageFrequency usageFrequency;
    private Gage.Criticality criticality;
    private Gage.Location location;
    private Gage.Status status;

    // Additional Fields (exactly what frontend sends)
    private String measurementRange;
    private String accuracy;
    private LocalDate purchaseDate;
    private String manufacturerId;
    private String manufacturerName;
    private Integer calibrationInterval;

    // New fields for enhanced gage management
    private LocalDate nextCalibrationDate;
    private Integer maxUsersNumber;
    private LocalDate pendingCalibrationDate; // Auto-calculated based on criticality
    private Integer remainingDays; // Daily countdown from today to next calibration date

    private String notes;

    // ✅ Updated: support multiple images
    private List<String> gageImages = new ArrayList<>();

    // ✅ New: optional videos
    private List<String> gageVideos = new ArrayList<>();

    // Manual (still single file)
    private String gageManual;

    // Calibration History
    private List<CalibrationHistoryResponse> calibrationHistory;

    private String barcodeImage;
    private String qrCodeImage;

    // Audit fields
    private LocalDate createdAt;
    private LocalDate updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalibrationHistoryResponse {
        private Long id;
        private LocalDate calibrationDate;
        private LocalDate nextDueDate;
        private CalibrationHistory.CalibrationStatus status;
        private String notes;
        private String performedBy;
        private String certificate; // Base64 encoded
        private LocalDate createdAt;
    }
}