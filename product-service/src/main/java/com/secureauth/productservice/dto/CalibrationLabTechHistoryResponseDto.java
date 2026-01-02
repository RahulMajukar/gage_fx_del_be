package com.secureauth.productservice.dto;

import com.secureauth.productservice.entity.CalibrationResult;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalibrationLabTechHistoryResponseDto {

    private Long id;

    // Gage
    private Long gageId;
    private String gageName;
    private String gageCode;

    // Calibration details
    private String technician;
    private LocalDate calibrationDate;
    private LocalDate nextCalibrationDate;
    private CalibrationResult result;
    private String remarks;
    private String calibratedBy;
    private String certificateNumber;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Double calibrationDuration;

    // Machine (optional)
    private Long machineId;
    private String machineName;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
