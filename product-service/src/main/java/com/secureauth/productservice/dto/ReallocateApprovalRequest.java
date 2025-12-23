package com.secureauth.productservice.dto;

import com.secureauth.productservice.entity.Reallocate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReallocateApprovalRequest {

    @NotNull(message = "Reallocate ID is required")
    private Long reallocateId;

    @NotBlank(message = "Approved by is required")
    private String approvedBy;

    @NotNull(message = "Time limit is required")
    private Reallocate.TimeLimit timeLimit;

    // Optional: Change allocation details
    private String newDepartment;
    private String newFunction;
    private String newOperation;

    private String notes;
}
