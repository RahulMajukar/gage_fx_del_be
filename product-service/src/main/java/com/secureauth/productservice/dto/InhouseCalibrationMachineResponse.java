package com.secureauth.productservice.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InhouseCalibrationMachineResponse {

    private Long id;
    private String machineName;
    private String instrumentName;
    private String instrumentCode;
    private String accuracy;
    private String resolution;
    private String location;
    private String status;
    private String manufacturer;
    private String machineEquipmentNumber;
    private LocalDate guaranteeExpiryDate;
    private Long gageTypeId;
    private String gageTypeName;
    private Long gageSubTypeId;
    private String gageSubTypeName;
}