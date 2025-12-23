package com.secureauth.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GageUsageRequest {

    @NotBlank(message = "Gage type is required")
    private String gageType;

    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    @NotNull(message = "Days used is required")
    @Min(value = 1, message = "Days used must be at least 1")
    private Integer daysUsed;

    @NotNull(message = "Uses count is required")
    @Min(value = 1, message = "Uses count must be at least 1")
    private Integer usesCount;

    @NotBlank(message = "Operator username is required")
    private String operatorUsername;

    @NotBlank(message = "Operator role is required")
    private String operatorRole; // F for function, OT for operation

    private String operatorFunction; // F1, F2, etc.
    private String operatorOperation; // OT1, OT2, etc.

    @NotNull(message = "Usage date is required")
    private LocalDate usageDate;

    private String jobDescription;

    private String jobNumber;

    @Min(value = 1, message = "Usage count must be at least 1")
    private Integer usageCount;

    private String usageNotes;

    // Operator context for filtering
    private String department;
    private String functionName;
    private String operationName;
}
