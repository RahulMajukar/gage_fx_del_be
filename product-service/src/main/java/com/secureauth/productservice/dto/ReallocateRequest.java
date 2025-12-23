package com.secureauth.productservice.dto;

import com.secureauth.productservice.entity.Reallocate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReallocateRequest {

    @NotNull(message = "Gage ID is required")
    private Long gageId;

    @NotBlank(message = "Requested by is required")
    private String requestedBy;

    @NotBlank(message = "Requested by role is required")
    private String requestedByRole;

    @NotBlank(message = "Requested by function is required")
    private String requestedByFunction;

    @NotBlank(message = "Requested by operation is required")
    private String requestedByOperation;

    @NotNull(message = "Time limit is required")
    private Reallocate.TimeLimit timeLimit;

    private String reason;

    private String notes;
}
