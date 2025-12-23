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
public class JobRequest {

    @NotBlank(message = "Job number is required")
    private String jobNumber;

    @NotBlank(message = "Job description is required")
    private String jobDescription;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Status status;

    private Priority priority;

    @NotBlank(message = "Created by is required")
    private String createdBy;

    private String assignedTo;

    private LocalDate dueDate;

    private LocalDate startDate;

    private LocalDate endDate;

    // Cross-domain metadata
    private String department;
    private String functionName;
    private String operationName;

    // Job-specific fields
    @Min(value = 1, message = "Estimated duration must be at least 1 day")
    private Integer estimatedDuration;

    private Integer actualDuration;

    private String location;

    private String notes;

    // Gage usage fields (when job involves gage usage)
    private String gageType;
    private String gageSerialNumber;
    private Integer daysUsed;
    private Integer usesCount;
    private String operatorUsername;
    private String operatorRole;
    private String operatorFunction;
    private String operatorOperation;
    private LocalDate usageDate;
    private Integer usageCount;
    private String usageNotes;

    private java.util.List<String> tags;

    private java.util.List<String> attachments;

    public enum Status {
        OPEN, IN_PROGRESS, COMPLETED, CANCELLED, ON_HOLD
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
}
