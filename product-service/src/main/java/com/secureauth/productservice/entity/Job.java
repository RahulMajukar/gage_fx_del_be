package com.secureauth.productservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String jobNumber;

    @Column(nullable = false)
    private String jobDescription;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(nullable = false)
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
    private Integer estimatedDuration; // in days
    private Integer actualDuration; // in days
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

    @ElementCollection
    @CollectionTable(name = "job_tags", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "job_attachments", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "attachment_name")
    private List<String> attachments = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = Status.OPEN;
        }
        if (priority == null) {
            priority = Priority.MEDIUM;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Status {
        OPEN, IN_PROGRESS, COMPLETED, CANCELLED, ON_HOLD
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
}
