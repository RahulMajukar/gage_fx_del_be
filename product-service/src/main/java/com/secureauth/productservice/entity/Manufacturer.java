package com.secureauth.productservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "manufacturers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Manufacturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false)
    private String country;

    private String contactPerson;

    private String phoneNumber;

    private String email;

    private String website;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ManufacturerType manufacturerType;

    private String certificationNumber;

    // In your Manufacturer.java entity
    @Column(name = "active", nullable = true) // Change to true temporarily
    private Boolean active = true;

    // One-to-Many relationship with Gage (one manufacturer can have many gages)
    @OneToMany(mappedBy = "manufacturer", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Gage> gages = new HashSet<>();

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

    // Manufacturer Type enum
    public enum ManufacturerType {
        ORIGINAL_EQUIPMENT_MANUFACTURER,
        THIRD_PARTY_MANUFACTURER,
        CUSTOM_MANUFACTURER,
        INTERNATIONAL_MANUFACTURER,
        LOCAL_MANUFACTURER
    }
}