package com.secureauth.productservice.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalibrationHistoryResponse {
    private Long id;
    private LocalDate calibrationDate;
    private LocalDate nextDueDate;
    private String status;
    private String notes;
    private String performedBy;
    private LocalDate createdAt;
}
