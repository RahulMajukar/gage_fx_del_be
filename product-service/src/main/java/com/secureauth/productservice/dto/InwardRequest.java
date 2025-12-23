package com.secureauth.productservice.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
public class InwardRequest {
    private LocalDate calibrationDate;
    private LocalDate nextDueDate;
    private String notes;
    private String performedBy;
    private CalibrationStatus status;
    private List<MultipartFile> documents;
    private List<MultipartFile> images;
    private List<MultipartFile> videos;

    public enum CalibrationStatus {
        PASSED, FAILED, CONDITIONAL_PASS, OUT_OF_TOLERANCE
    }
}