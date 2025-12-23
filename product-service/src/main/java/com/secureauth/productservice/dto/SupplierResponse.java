package com.secureauth.productservice.dto;

import com.secureauth.productservice.entity.Supplier;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierResponse {

    private Long id;
    private String name;
    private String address;
    private String country;
    private String contactPerson;
    private String phoneNumber;
    private String email;
    private String website;
    private String invoicePONumber;
    private String description;
    private Supplier.SupplierType supplierType;
    private String taxIdentificationNumber;
    private String businessLicenseNumber;
    private Supplier.PaymentTerms paymentTerms;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    
    public static SupplierResponse fromEntity(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .address(supplier.getAddress())
                .country(supplier.getCountry())
                .contactPerson(supplier.getContactPerson())
                .phoneNumber(supplier.getPhoneNumber())
                .email(supplier.getEmail())
                .website(supplier.getWebsite())
                .invoicePONumber(supplier.getInvoicePONumber())
                .description(supplier.getDescription())
                .supplierType(supplier.getSupplierType())
                .taxIdentificationNumber(supplier.getTaxIdentificationNumber())
                .businessLicenseNumber(supplier.getBusinessLicenseNumber())
                .paymentTerms(supplier.getPaymentTerms())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
} 