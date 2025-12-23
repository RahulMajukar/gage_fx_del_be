package com.secureauth.productservice.dto;

import com.secureauth.productservice.entity.Manufacturer;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManufacturerResponse {

    private Long id;
    private String name;
    private String address;
    private String country;
    private String contactPerson;
    private String phoneNumber;
    private String email;
    private String website;
    private String description;
    private Manufacturer.ManufacturerType manufacturerType;
    private String certificationNumber;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Set<Long> gageIds; // Only IDs to avoid circular references
    
    public static ManufacturerResponse fromEntity(Manufacturer manufacturer) {
        Set<Long> gageIds = null;
        try {
            // Safely handle the gages relationship to prevent LazyInitializationException
            if (manufacturer.getGages() != null && !manufacturer.getGages().isEmpty()) {
                gageIds = manufacturer.getGages().stream()
                    .map(gage -> gage.getId())
                    .collect(Collectors.toSet());
            }
        } catch (Exception e) {
            // If we can't access gages (e.g., LazyInitializationException), set to null
            gageIds = null;
        }
        
        return ManufacturerResponse.builder()
                .id(manufacturer.getId())
                .name(manufacturer.getName())
                .address(manufacturer.getAddress())
                .country(manufacturer.getCountry())
                .contactPerson(manufacturer.getContactPerson())
                .phoneNumber(manufacturer.getPhoneNumber())
                .email(manufacturer.getEmail())
                .website(manufacturer.getWebsite())
                .description(manufacturer.getDescription())
                .manufacturerType(manufacturer.getManufacturerType())
                .certificationNumber(manufacturer.getCertificationNumber())
                .createdAt(manufacturer.getCreatedAt())
                .updatedAt(manufacturer.getUpdatedAt())
                .gageIds(gageIds)
                .build();
    }
} 