package com.secureauth.productservice.dto;

import com.secureauth.productservice.entity.ServiceProvider;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceProviderResponse {

    private Long id;
    private String name;
    private ServiceProvider.Accreditation accreditation;
    private String address;
    private String country;
    private String contactPerson;
    private String phoneNumber;
    private String email;
    private String website;
    private String certificate;
    private String description;
    private ServiceProvider.ServiceType serviceType;
    private ServiceProvider.ServiceProviderType serviceProviderType;
    private String accreditationNumber;
    private LocalDate accreditationExpiryDate;
    private String serviceAreas;
    private ServiceProvider.ResponseTime responseTime;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    public static ServiceProviderResponse fromEntity(ServiceProvider serviceProvider) {
        return ServiceProviderResponse.builder()
                .id(serviceProvider.getId())
                .name(serviceProvider.getName())
                .accreditation(serviceProvider.getAccreditation())
                .address(serviceProvider.getAddress())
                .country(serviceProvider.getCountry())
                .contactPerson(serviceProvider.getContactPerson())
                .phoneNumber(serviceProvider.getPhoneNumber())
                .email(serviceProvider.getEmail())
                .website(serviceProvider.getWebsite())
                .certificate(serviceProvider.getCertificate())
                .description(serviceProvider.getDescription())
                .serviceType(serviceProvider.getServiceType())
                .serviceProviderType(serviceProvider.getServiceProviderType())
                .accreditationNumber(serviceProvider.getAccreditationNumber())
                .accreditationExpiryDate(serviceProvider.getAccreditationExpiryDate())
                .serviceAreas(serviceProvider.getServiceAreas())
                .responseTime(serviceProvider.getResponseTime())
                .createdAt(serviceProvider.getCreatedAt())
                .updatedAt(serviceProvider.getUpdatedAt())
                .build();
    }
} 