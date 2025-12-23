package com.secureauth.productservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GageSubTypeResponse {

    private Long id;          // Database ID of the GageSubType
    private String name;      // Name of the GageSubType
    private String description; // Optional description
}

