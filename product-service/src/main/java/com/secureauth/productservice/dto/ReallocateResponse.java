package com.secureauth.productservice.dto;

import com.secureauth.productservice.entity.Reallocate;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReallocateResponse {

    private Long id;
    private Long gageId;
    private String gageSerialNumber;
    private String gageModelNumber;
    private String gageTypeName;

    // Original allocation details
    private String originalDepartment;
    private String originalFunction;
    private String originalOperation;

    // Current allocation details
    private String currentDepartment;
    private String currentFunction;
    private String currentOperation;

    // Request details
    private String requestedBy;
    private String requestedByRole;
    private String requestedByFunction;
    private String requestedByOperation;

    // Approval details
    private String approvedBy;
    private LocalDateTime approvedAt;

    // Time limit settings
    private Reallocate.TimeLimit timeLimit;
    private LocalDateTime allocatedAt;
    private LocalDateTime expiresAt;

    // Status
    private Reallocate.Status status;

    // Additional information
    private String reason;
    private String notes;

    // Calculated fields
    private Long remainingMinutes;
    private Boolean isExpired;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
