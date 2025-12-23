package com.secureauth.productservice.dto;

import com.secureauth.productservice.entity.ServiceProvider;
import lombok.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceProviderRequest {

    @NotBlank(message = "Service provider name is required")
    private String name;
    
    private ServiceProvider.Accreditation accreditation;
    
    private String address;
    
    @NotBlank(message = "Country is required")
    private String country;
    
    private String contactPerson;
    
    private String phoneNumber;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String website;
    
    private String certificate; // Base64 encoded certificate
    
    private String description;
    
    private ServiceProvider.ServiceType serviceType;
    
    private String accreditationNumber;
    
    private LocalDate accreditationExpiryDate;
    
    private String serviceAreas;
    
    private ServiceProvider.ResponseTime responseTime;

    private ServiceProvider.ServiceProviderType serviceProviderType;

} 