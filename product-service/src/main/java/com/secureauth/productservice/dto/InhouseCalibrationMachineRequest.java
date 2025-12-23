package com.secureauth.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InhouseCalibrationMachineRequest {

    @NotBlank(message = "Machine name is required")
    private String machineName;

    @NotBlank(message = "Instrument name is required")
    private String instrumentName;

    @NotBlank(message = "Instrument code is required")
    private String instrumentCode;

    @NotBlank(message = "Accuracy is required")
    private String accuracy;

    @NotBlank(message = "Resolution is required")
    private String resolution;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Status is required")
    private String status;

    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;

    @NotBlank(message = "Machine/Equipment number is required")
    private String machineEquipmentNumber;

    @NotNull(message = "Guarantee expiry date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate guaranteeExpiryDate;

    @NotNull(message = "Gage type ID is required")
    private Long gageTypeId;

    @NotNull(message = "Gage sub-type ID is required")
    private Long gageSubTypeId;
}