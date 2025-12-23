package com.secureauth.productservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gage_issues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GageIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "store", nullable = false)
    private String storeName;

    private String assignedTo;

    @ElementCollection
    @CollectionTable(name = "gage_issue_tags", joinColumns = @JoinColumn(name = "issue_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    // For now, store attachment filenames or simple identifiers (no blob storage here)
    @ElementCollection
    @CollectionTable(name = "gage_issue_attachments", joinColumns = @JoinColumn(name = "issue_id"))
    @Column(name = "attachment_name")
    private List<String> attachments = new ArrayList<>();

    private String serialNumber;

    // Cross-domain metadata captured as provided by frontend
    private String department;
    private String functionName;
    private String operationName;

    // Optional association to gage if serialNumber matches
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gage_id")
    private Gage gage;

    // Usage tracking fields for operator gage usage
    private String operatorUsername;     // Username of the operator who used the gage
    private String operatorRole;         // Role info (F for function, OT for operation)
    private String operatorFunction;     // Function code (F1, F2, etc.)
    private String operatorOperation;    // Operation code (OT1, OT2, etc.)
    private LocalDate usageDate;         // Date when gage was used
    private String jobDescription;       // Job description for the usage
    private String jobNumber;           // Job number reference
    private Integer usageCount;         // Count of items processed
    private String usageNotes;          // Additional notes for the usage

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = Status.OPEN;
        }
        if (storeName == null || storeName.isBlank()) {
            storeName = "UNKNOWN";
        }
    }

    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    public enum Status {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }

    @Transient
    public Priority getPriority() {
        if (gage != null && gage.getCriticality() != null) {
            return switch (gage.getCriticality()) {
                case HIGH -> Priority.HIGH;
                case MEDIUM -> Priority.MEDIUM;
                case LOW -> Priority.LOW;
            };
        }
        return null;
    }
}


