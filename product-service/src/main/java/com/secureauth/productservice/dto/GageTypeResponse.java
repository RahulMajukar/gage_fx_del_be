package com.secureauth.productservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GageTypeResponse {

    private Long id;          // Database ID of the GageType
    private String name;      // Name of the GageType
    private Long gageSubTypeId;
    private String gageSubTypeName;
    private String description; // Optional description
}
