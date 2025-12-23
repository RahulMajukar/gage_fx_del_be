package com.secureauth.productservice.dto;

import com.secureauth.productservice.entity.Gage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GageScanResponse {
    private Long id;
    private String serialNumber;
    private String modelNumber;
    private GageTypeResponse gageType;
    private String gageSubType; // GageSubType name
    private Gage.UsageFrequency usageFrequency;
    private Gage.Criticality criticality;
    private Gage.Location location;
    private Gage.Status status;
    private String measurementRange;
    private String accuracy;
    private LocalDate purchaseDate;
    private String manufacturerName;
    private Integer calibrationInterval;
    private LocalDate nextCalibrationDate;
    private Integer maxUsersNumber;
    private LocalDate pendingCalibrationDate;
    private Integer remainingDays;
    private String notes;

    // ✅ Updated: support multiple images
    private List<String> gageImages = new ArrayList<>();

    // ✅ New: optional videos
    private List<String> gageVideos = new ArrayList<>();

    // Optional: keep gageImage for backward compatibility (but it will be null/unused)
    // private String gageImage;

    private String barcodeImage;
    private String qrCodeImage;
    private LocalDateTime scanTime;

    @Builder.Default
    private Boolean success = true;

    private String message;
}