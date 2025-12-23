package com.secureauth.productservice.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResponse {

    private Long id;
    private String jobNumber;
    private String jobDescription;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
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
    
    private List<String> tags;
    private List<String> attachments;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status {
        OPEN, IN_PROGRESS, COMPLETED, CANCELLED, ON_HOLD
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
}
