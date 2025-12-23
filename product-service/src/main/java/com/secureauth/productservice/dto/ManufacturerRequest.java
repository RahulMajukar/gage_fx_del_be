package com.secureauth.productservice.dto;

import com.secureauth.productservice.entity.Manufacturer;
import lombok.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManufacturerRequest {

    @NotBlank(message = "Manufacturer name is required")
    private String name;
    
    private String address;
    
    @NotBlank(message = "Country is required")
    private String country;
    
    private String contactPerson;
    
    private String phoneNumber;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String website;
    
    private String description;
    
    private Manufacturer.ManufacturerType manufacturerType;
    
    private String certificationNumber;
} 