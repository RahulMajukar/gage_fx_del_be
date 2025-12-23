package com.secureauth.productservice.dto;

import com.secureauth.productservice.entity.Supplier;
import lombok.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    private String name;
    
    private String address;
    
    @NotBlank(message = "Country is required")
    private String country;
    
    private String contactPerson;
    
    private String phoneNumber;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String website;
    
    private String invoicePONumber;
    
    private String description;
    
    private Supplier.SupplierType supplierType;
    
    private String taxIdentificationNumber;
    
    private String businessLicenseNumber;
    
    private Supplier.PaymentTerms paymentTerms;
} 