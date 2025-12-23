package com.secureauth.productservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reallocates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reallocate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to the gage being reallocated
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gage_id", nullable = false)
    private Gage gage;

    // Original allocation details (stored when gage is first issued)
    @Column(nullable = false)
    private String originalDepartment;

    @Column(nullable = false)
    private String originalFunction;

    @Column(nullable = false)
    private String originalOperation;

    // Current allocation details (can be changed by Plant HOD)
    @Column(nullable = false)
    private String currentDepartment;

    @Column(nullable = false)
    private String currentFunction;

    @Column(nullable = false)
    private String currentOperation;

    // Request details
    @Column(nullable = false)
    private String requestedBy; // Username of the operator who requested

    @Column(nullable = false)
    private String requestedByRole; // Role of the requester (F for function, OT for operation)

    @Column(nullable = false)
    private String requestedByFunction; // Function code (F1, F2, etc.)

    @Column(nullable = false)
    private String requestedByOperation; // Operation code (OT1, OT2, etc.)

    // Approval details
    private String approvedBy; // Username of Plant HOD who approved

    private LocalDateTime approvedAt;

    // Time limit settings
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeLimit timeLimit;

    private LocalDateTime allocatedAt; // When the reallocation was approved

    private LocalDateTime expiresAt; // When the gage should automatically return

    // Status tracking
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    // Additional information
    @Column(columnDefinition = "TEXT")
    private String reason; // Reason for reallocation request

    @Column(columnDefinition = "TEXT")
    private String notes; // Additional notes from Plant HOD

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enums
    public enum TimeLimit {
        TWO_HOURS(2, "2 Hours"),
        ONE_DAY(1, "1 Day"),
        ONE_WEEK(7, "1 Week"),
        ONE_MONTH(30, "1 Month"),
        CUSTOM(0, "Custom");

        private final int days;
        private final String displayName;

        TimeLimit(int days, String displayName) {
            this.days = days;
            this.displayName = displayName;
        }

        public int getDays() {
            return days;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Status {
        PENDING_APPROVAL,    // Waiting for Plant HOD approval
        APPROVED,           // Approved and active
        EXPIRED,            // Time limit exceeded, should return
        RETURNED,           // Manually returned
        COMPLETED,          // Completed cycle - gage available for new requests
        CANCELLED           // Request cancelled
    }

    // Helper method to calculate expiry time
    public void calculateExpiryTime() {
        if (allocatedAt != null && timeLimit != null) {
            if (timeLimit == TimeLimit.TWO_HOURS) {
                expiresAt = allocatedAt.plusHours(2);
            } else if (timeLimit == TimeLimit.ONE_DAY) {
                expiresAt = allocatedAt.plusDays(1);
            } else if (timeLimit == TimeLimit.ONE_WEEK) {
                expiresAt = allocatedAt.plusWeeks(1);
            } else if (timeLimit == TimeLimit.ONE_MONTH) {
                expiresAt = allocatedAt.plusMonths(1);
            }
            // For CUSTOM, expiryAt should be set manually
        }
    }

    // Helper method to check if allocation is expired
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    // Helper method to get remaining time in minutes
    public Long getRemainingMinutes() {
        if (expiresAt == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiresAt)) {
            return 0L;
        }
        return java.time.Duration.between(now, expiresAt).toMinutes();
    }
}
