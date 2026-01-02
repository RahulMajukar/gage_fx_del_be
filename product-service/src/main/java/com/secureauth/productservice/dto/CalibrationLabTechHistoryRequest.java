package com.secureauth.productservice.dto;

import com.secureauth.productservice.entity.CalibrationResult;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CalibrationLabTechHistoryRequest {

    private Long gageId;
    private Long machineId;

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
}
