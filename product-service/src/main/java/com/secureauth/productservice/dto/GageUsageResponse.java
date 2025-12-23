package com.secureauth.productservice.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GageUsageResponse {

    private Long id;
    private String gageType;
    private String serialNumber;
    private String modelNumber;
    
    // Current gage status from main gage table
    private LocalDate nextCalibrationDate;
    private LocalDate pendingCalibrationDate;
    private Integer remainingDays;
    private Integer maxUsersNumber;
    private Integer currentUsesCount; // Calculated remaining uses
    
    // Usage details
    private Integer daysUsed;
    private Integer usesCount;
    private String operatorUsername;
    private String operatorRole;
    private String operatorFunction;
    private String operatorOperation;
    private LocalDate usageDate;
    private String jobDescription;
    private String jobNumber;
    private Integer usageCount;
    private String usageNotes;
    
    // Validation status
    private Boolean isValidSerial;
    private String validationMessage;
    
    // Department/Function/Operation info
    private String department;
    private String functionName;
    private String operationName;
}
