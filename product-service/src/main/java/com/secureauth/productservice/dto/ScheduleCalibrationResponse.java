package com.secureauth.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCalibrationResponse {
    private Long id;
    private Long gageId;
    private String serialNumber;
    private String gageName;
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private String priority;
    private String assignedTo;
    private String laboratory;
    private Integer estimatedDuration;
    private String notes;
    private String status;
    private Boolean emailSent;
    private LocalDateTime createdAt;
    private String message;
}