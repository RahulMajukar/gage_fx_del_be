package com.secureauth.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCalibrationRequest {

    @NotNull(message = "Gage ID is required")
    private Long gageId; // 👈 ADDED

    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    private LocalTime scheduledTime;

    private String priority;

    @NotBlank(message = "Assigned technician is required")
    private String assignedTo;

    @NotBlank(message = "Laboratory is required")
    private String laboratory;

    private Integer estimatedDuration;

    private String notes;

    private Boolean notifyResponsible;

    private Boolean requiresSpecialEquipment;

    private String specialEquipment;

    // Email configuration
    private Boolean emailEnabled;
    private String emailSubject;
    private String emailTo;
    private List<String> emailCC;
    private String emailMessage;
    private List<String> attachments;

    private String serialNumberPhoto;
    private String serialNumberPhotoContentType;

    private String frontViewPhoto;
    private String frontViewPhotoContentType;

    private String backViewPhoto;
    private String backViewPhotoContentType;
}