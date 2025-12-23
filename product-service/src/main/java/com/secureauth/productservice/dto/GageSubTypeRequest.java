package com.secureauth.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GageSubTypeRequest {

    @NotBlank(message = "Gage sub-type name is required")
    private String name;

    private String description; // Optional, can be null
}

