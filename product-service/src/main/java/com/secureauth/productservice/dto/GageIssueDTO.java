package com.secureauth.productservice.dto;

import com.secureauth.productservice.entity.GageIssue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GageIssueDTO {

    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    // Optional: if not provided, backend derives from gage criticality
    private GageIssue.Priority priority;
    private GageIssue.Status status;

    private String store;
    private String assignedTo;
    private List<String> attachments;
    private List<String> tags;
    private String serialNumber;
    private String dept;
    private String func;
    private String operation;
    
    // Usage tracking fields
    private String operatorUsername;
    private String operatorRole;
    private String operatorFunction;
    private String operatorOperation;
    private LocalDate usageDate;
    private String jobDescription;
    private String jobNumber;
    private Integer usageCount;
    private String usageNotes;
    
    private LocalDateTime createdAt;
}


