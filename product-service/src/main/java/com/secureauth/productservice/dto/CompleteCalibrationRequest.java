package com.secureauth.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteCalibrationRequest {
    private String performedBy;
    private String notes;
    private String certificate; // Base64 encoded certificate
}