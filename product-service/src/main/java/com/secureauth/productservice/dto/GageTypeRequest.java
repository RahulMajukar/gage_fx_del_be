package com.secureauth.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GageTypeRequest {

    @NotBlank(message = "Gage type name is required")
    private String name;

    @NotNull(message = "Gage sub-type ID is required")
    private Long gageSubTypeId;

    private String description; // Optional, can be null
}
