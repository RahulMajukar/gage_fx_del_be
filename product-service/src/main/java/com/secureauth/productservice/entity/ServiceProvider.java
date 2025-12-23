package com.secureauth.productservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "service_providers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;


    
    @Enumerated(EnumType.STRING)
    private Accreditation accreditation;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Column(nullable = false)
    private String country;
    
    private String contactPerson;
    
    private String phoneNumber;
    
    private String email;
    
    private String website;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String certificate; // Base64 encoded certificate
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;
    
    private String accreditationNumber;
    
    private LocalDate accreditationExpiryDate;
    
    private String serviceAreas;
    
    @Enumerated(EnumType.STRING)
    private ResponseTime responseTime;

    @Enumerated(EnumType.STRING)
    private ServiceProviderType serviceProviderType;

    
    // Audit fields
    @Column(nullable = false)
    private java.time.LocalDate createdAt;
    
    private java.time.LocalDate updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDate.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDate.now();
    }
    
    // Accreditation enum
    public enum Accreditation {
        ISO_IEC_17025, NABL, A2LA, UKAS, DAkkS, 
        CNAS, JAB, SAC, OTHER, NONE
    }
    
    // Service Type enum
    public enum ServiceType {
        CALIBRATION_SERVICE,
        REPAIR_SERVICE,
        MAINTENANCE_SERVICE,
        CERTIFICATION_SERVICE,
        CONSULTING_SERVICE,
        TRAINING_SERVICE,
        COMPREHENSIVE_SERVICE
    }
    
    // Response Time enum
    public enum ResponseTime {
        IMMEDIATE,
        WITHIN_24_HOURS,
        WITHIN_48_HOURS,
        WITHIN_1_WEEK,
        WITHIN_2_WEEKS,
        CUSTOM_SCHEDULE
    }
    public enum ServiceProviderType {
        AUTHORIZED,
        UNAUTHORIZED
    }
} 