package com.secureauth.productservice.entity;

import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

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
    
    private String invoicePONumber;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    private SupplierType supplierType;
    
    private String taxIdentificationNumber;
    
    private String businessLicenseNumber;
    
    @Enumerated(EnumType.STRING)
    private PaymentTerms paymentTerms;
    

    
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
    
    // Supplier Type enum
    public enum SupplierType {
        AUTHORIZED_DISTRIBUTOR,
        WHOLESALE_SUPPLIER,
        RETAIL_SUPPLIER,
        DIRECT_MANUFACTURER,
        THIRD_PARTY_SUPPLIER,
        INTERNATIONAL_SUPPLIER
    }
    
    // Payment Terms enum
    public enum PaymentTerms {
        NET_30,
        NET_60,
        NET_90,
        IMMEDIATE,
        ADVANCE_PAYMENT,
        LETTER_OF_CREDIT
    }
} 