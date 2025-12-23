package com.secureauth.productservice.dto;

import com.secureauth.productservice.entity.Gage;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GageRequest {

    // ✅ DO NOT define CodeType here — use Gage.CodeType

    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    @NotBlank(message = "Model number is required")
    private String modelNumber;

    @NotBlank(message = "Gage type name is required")
    private String gageTypeName;

    @NotNull(message = "Gage sub-type ID is required")
    private Long gageSubTypeId;

    private Long inhouseCalibrationMachineId; // Optional

    @NotNull(message = "Usage frequency is required")
    private Gage.UsageFrequency usageFrequency;

    @NotNull(message = "Criticality is required")
    private Gage.Criticality criticality;

    @NotNull(message = "Location is required")
    private Gage.Location location;

    // ✅ Updated: support multiple images
    private List<String> gageImages = new ArrayList<>();

    // ✅ New: optional videos
    private List<String> gageVideos = new ArrayList<>();

    private String gageManual;

    @NotBlank(message = "Measurement range is required")
    private String measurementRange;

    private String accuracy;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    @NotBlank(message = "Manufacturer ID is required")
    private String manufacturerId;

    @NotNull(message = "Calibration interval is required")
    @Min(value = 1, message = "Calibration interval must be at least 1 month")
    private Integer calibrationInterval;

    @NotNull(message = "Next calibration date is required")
    private LocalDate nextCalibrationDate;

    @NotNull(message = "Max users number is required")
    @Min(value = 1, message = "Max users number must be at least 1")
    private Integer maxUsersNumber;

    private String notes;

    private Gage.Status status;

    private Gage.CodeType codeType;
}