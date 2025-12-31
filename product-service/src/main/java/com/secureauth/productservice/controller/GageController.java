package com.secureauth.productservice.controller;

import com.secureauth.productservice.dto.*;
import com.secureauth.productservice.entity.CalibrationHistory;
import com.secureauth.productservice.entity.Gage;
import com.secureauth.productservice.entity.ServiceProvider;
import com.secureauth.productservice.exception.ResourceNotFoundException;
import com.secureauth.productservice.service.GageService;
import com.secureauth.productservice.service.GageTypeService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gages")
public class GageController {

    @Autowired
    private GageService gageService;

    @Autowired
    private GageTypeService gageTypeService;

    @Autowired
    private com.secureauth.productservice.service.DailySchedulerService dailySchedulerService;

    // =============== BARCODE SCAN BY TEXT ===============
    @GetMapping("/scan/{serialNumber}")
    public ResponseEntity<GageScanResponse> getGageByBarcodeScan(@PathVariable String serialNumber) {
        GageScanResponse response = gageService.getGageDetailsByBarcodeScan(serialNumber);
        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // =============== BARCODE SCAN BY IMAGE UPLOAD ===============
    @PostMapping("/scan/upload")
    public ResponseEntity<GageScanResponse> getGageByBarcodeImage(
            @RequestParam("barcodeImage") MultipartFile barcodeImage) {
        if (barcodeImage == null || barcodeImage.isEmpty()) {
            GageScanResponse errorResponse = GageScanResponse.builder()
                    .success(false)
                    .message("No barcode image provided")
                    .scanTime(java.time.LocalDateTime.now())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }

        GageScanResponse response = gageService.getGageDetailsByBarcodeImage(barcodeImage);
        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // =============== CRUD OPERATIONS ===============
    @PostMapping
    public ResponseEntity<GageResponse> createGage(@Valid @RequestBody GageRequest gageRequest) {
        try {
            GageResponse createdGage = gageService.createGage(gageRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private Gage.CodeType parseCodeType(String codeTypeStr) {
        if (codeTypeStr == null || codeTypeStr.trim().isEmpty()) {
            return Gage.CodeType.BARCODE_ONLY;
        }
        try {
            return Gage.CodeType.valueOf(codeTypeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid codeType: " + codeTypeStr + ", defaulting to BARCODE_ONLY");
            return Gage.CodeType.BARCODE_ONLY;
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> createGageWithImages(
            @RequestParam("gageTypeName") String gageTypeName,
            @RequestParam("manufacturerId") String manufacturerId,
            @RequestParam("modelNumber") String modelNumber,
            @RequestParam("serialNumber") String serialNumber,
            @RequestParam("gageSubTypeId") Long gageSubTypeId,
            @RequestParam(value = "inhouseCalibrationMachineId", required = false) Long inhouseCalibrationMachineId,
            @RequestParam("measurementRange") String measurementRange,
            @RequestParam("accuracy") String accuracy,
            @RequestParam("purchaseDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate purchaseDate,
            @RequestParam("calibrationInterval") Integer calibrationInterval,
            @RequestParam("nextCalibrationDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextCalibrationDate,
            @RequestParam("maxUsersNumber") Integer maxUsersNumber,
            @RequestParam("usageFrequency") Gage.UsageFrequency usageFrequency,
            @RequestParam("criticality") Gage.Criticality criticality,
            @RequestParam("location") Gage.Location location,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "codeType", required = false) String codeTypeStr,
            @RequestParam(value = "gageImages", required = false) List<MultipartFile> gageImages,
            @RequestParam(value = "gageVideos", required = false) List<MultipartFile> gageVideos,
            @RequestParam(value = "gageManual", required = false) MultipartFile gageManual) {

        try {
            List<String> base64Images = null;
            if (gageImages != null && !gageImages.isEmpty()) {
                base64Images = gageImages.stream()
                        .filter(file -> !file.isEmpty())
                        .map(file -> {
                            try {
                                return Base64.getEncoder().encodeToString(file.getBytes());
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to read image file", e);
                            }
                        })
                        .collect(Collectors.toList());
            }

            // ✅ Process optional videos
            List<String> base64Videos = null;
            if (gageVideos != null && !gageVideos.isEmpty()) {
                base64Videos = gageVideos.stream()
                        .filter(file -> !file.isEmpty())
                        .map(file -> {
                            try {
                                return Base64.getEncoder().encodeToString(file.getBytes());
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to read video file", e);
                            }
                        })
                        .collect(Collectors.toList());
            }

            String base64Manual = null;
            if (gageManual != null && !gageManual.isEmpty()) {
                base64Manual = Base64.getEncoder().encodeToString(gageManual.getBytes());
            }

            Gage.CodeType selectedCodeType = parseCodeType(codeTypeStr);

            GageRequest gageRequest = GageRequest.builder()
                    .gageTypeName(gageTypeName)
                    .manufacturerId(manufacturerId)
                    .modelNumber(modelNumber)
                    .serialNumber(serialNumber)
                    .gageSubTypeId(gageSubTypeId)
                    .inhouseCalibrationMachineId(inhouseCalibrationMachineId)
                    .measurementRange(measurementRange)
                    .accuracy(accuracy)
                    .purchaseDate(purchaseDate)
                    .calibrationInterval(calibrationInterval)
                    .nextCalibrationDate(nextCalibrationDate)
                    .maxUsersNumber(maxUsersNumber)
                    .usageFrequency(usageFrequency)
                    .criticality(criticality)
                    .location(location)
                    .notes(notes)
                    .gageImages(base64Images) // ✅ Updated field
                    .gageVideos(base64Videos) // ✅ New video field
                    .gageManual(base64Manual)
                    .codeType(selectedCodeType)
                    .build();

            GageResponse createdGage = gageService.createGage(gageRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGage);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to process the uploaded files."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid request data: " + e.getMessage()));
        }
    }

    // =============== NEW ENDPOINT FROM CODE 2: Upload with Category ===============
    @PostMapping("/upload-with-category")
    public ResponseEntity<?> createGageWithImagesAndCategory(
            @RequestParam("gageTypeName") String gageTypeName,
            @RequestParam("manufacturerId") String manufacturerId,
            @RequestParam("modelNumber") String modelNumber,
            @RequestParam("serialNumber") String serialNumber,
            @RequestParam("measurementRange") String measurementRange,
            @RequestParam("accuracy") String accuracy,
            @RequestParam("purchaseDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate purchaseDate,
            @RequestParam("calibrationInterval") Integer calibrationInterval,
            @RequestParam("nextCalibrationDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextCalibrationDate,
            @RequestParam("maxUsersNumber") Integer maxUsersNumber,
            @RequestParam("usageFrequency") Gage.UsageFrequency usageFrequency,
            @RequestParam("criticality") Gage.Criticality criticality,
            @RequestParam("location") Gage.Location location,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "codeType", required = false) String codeTypeStr,
            @RequestParam(value = "gageImages", required = false) List<MultipartFile> gageImages,
            @RequestParam(value = "gageVideos", required = false) List<MultipartFile> gageVideos,
            @RequestParam(value = "gageManual", required = false) MultipartFile gageManual) {

        try {
            List<String> base64Images = null;
            if (gageImages != null && !gageImages.isEmpty()) {
                base64Images = gageImages.stream()
                        .filter(file -> !file.isEmpty())
                        .map(file -> {
                            try {
                                return Base64.getEncoder().encodeToString(file.getBytes());
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to read image file", e);
                            }
                        })
                        .collect(Collectors.toList());
            }

            // ✅ Process optional videos
            List<String> base64Videos = null;
            if (gageVideos != null && !gageVideos.isEmpty()) {
                base64Videos = gageVideos.stream()
                        .filter(file -> !file.isEmpty())
                        .map(file -> {
                            try {
                                return Base64.getEncoder().encodeToString(file.getBytes());
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to read video file", e);
                            }
                        })
                        .collect(Collectors.toList());
            }

            String base64Manual = null;
            if (gageManual != null && !gageManual.isEmpty()) {
                base64Manual = Base64.getEncoder().encodeToString(gageManual.getBytes());
            }

            Gage.CodeType selectedCodeType = parseCodeType(codeTypeStr);

            GageRequest gageRequest = GageRequest.builder()
                    .gageTypeName(gageTypeName)
                    .manufacturerId(manufacturerId)
                    .modelNumber(modelNumber)
                    .serialNumber(serialNumber)
                    .measurementRange(measurementRange)
                    .accuracy(accuracy)
                    .purchaseDate(purchaseDate)
                    .calibrationInterval(calibrationInterval)
                    .nextCalibrationDate(nextCalibrationDate)
                    .maxUsersNumber(maxUsersNumber)
                    .usageFrequency(usageFrequency)
                    .criticality(criticality)
                    .location(location)
                    .notes(notes)
                    .gageImages(base64Images)
                    .gageVideos(base64Videos)
                    .gageManual(base64Manual)
                    .codeType(selectedCodeType)
                    .build();

            GageResponse createdGage = gageService.createGage(gageRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGage);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to process the uploaded files."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid request data: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GageResponse> getGageById(@PathVariable Long id) {
        try {
            GageResponse gage = gageService.getGageById(id);
            return ResponseEntity.ok(gage);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/serial/{serialNumber}")
    public ResponseEntity<GageResponse> getGageBySerialNumber(@PathVariable String serialNumber) {
        try {
            GageResponse gage = gageService.getGageBySerialNumber(serialNumber);
            return ResponseEntity.ok(gage);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<GageResponse>> getAllGages() {
        List<GageResponse> gages = gageService.getAllGages();
        return ResponseEntity.ok(gages);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GageResponse> updateGage(@PathVariable Long id, @Valid @RequestBody GageRequest gageRequest) {
        try {
            GageResponse updatedGage = gageService.updateGage(id, gageRequest);
            return ResponseEntity.ok(updatedGage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGage(@PathVariable Long id) {
        try {
            gageService.deleteGage(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<GageResponse> updateGageStatus(
            @PathVariable Long id,
            @RequestParam Gage.Status status) {
        try {
            GageResponse updatedGage = gageService.updateGageStatus(id, status);
            return ResponseEntity.ok(updatedGage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/issue")
    public ResponseEntity<GageResponse> issueGage(@PathVariable Long id) {
        try {
            GageResponse updatedGage = gageService.updateGageStatus(id, Gage.Status.ISSUED);
            return ResponseEntity.ok(updatedGage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<GageResponse> returnGage(@PathVariable Long id) {
        try {
            GageResponse updatedGage = gageService.updateGageStatus(id, Gage.Status.ACTIVE);
            return ResponseEntity.ok(updatedGage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/issue-by-serial/{serialNumber}")
    public ResponseEntity<GageResponse> issueGageBySerialNumber(@PathVariable String serialNumber) {
        try {
            GageResponse updatedGage = gageService.issueGageBySerialNumber(serialNumber);
            return ResponseEntity.ok(updatedGage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissueGage(
            @RequestParam("gageTypeName") String gageTypeName,
            @RequestParam("manufacturerId") String manufacturerId,
            @RequestParam("modelNumber") String modelNumber,
            @RequestParam("serialNumber") String serialNumber,
            @RequestParam("gageSubTypeId") Long gageSubTypeId,
            @RequestParam(value = "inhouseCalibrationMachineId", required = false) Long inhouseCalibrationMachineId,
            @RequestParam("measurementRange") String measurementRange,
            @RequestParam("accuracy") String accuracy,
            @RequestParam("purchaseDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate purchaseDate,
            @RequestParam("calibrationInterval") Integer calibrationInterval,
            @RequestParam("nextCalibrationDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextCalibrationDate,
            @RequestParam("maxUsersNumber") Integer maxUsersNumber,
            @RequestParam("usageFrequency") Gage.UsageFrequency usageFrequency,
            @RequestParam("criticality") Gage.Criticality criticality,
            @RequestParam("location") Gage.Location location,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "codeType", required = false) String codeTypeStr,
            @RequestParam(value = "gageImages", required = false) List<MultipartFile> gageImages,
            @RequestParam(value = "gageVideos", required = false) List<MultipartFile> gageVideos,
            @RequestParam(value = "gageManual", required = false) MultipartFile gageManual) {

        try {
            List<String> base64Images = null;
            if (gageImages != null && !gageImages.isEmpty()) {
                base64Images = gageImages.stream()
                        .filter(file -> !file.isEmpty())
                        .map(file -> {
                            try {
                                return Base64.getEncoder().encodeToString(file.getBytes());
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to read image file", e);
                            }
                        })
                        .collect(Collectors.toList());
            }

            // ✅ Process optional videos for reissue
            List<String> base64Videos = null;
            if (gageVideos != null && !gageVideos.isEmpty()) {
                base64Videos = gageVideos.stream()
                        .filter(file -> !file.isEmpty())
                        .map(file -> {
                            try {
                                return Base64.getEncoder().encodeToString(file.getBytes());
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to read video file", e);
                            }
                        })
                        .collect(Collectors.toList());
            }

            String base64Manual = null;
            if (gageManual != null && !gageManual.isEmpty()) {
                base64Manual = Base64.getEncoder().encodeToString(gageManual.getBytes());
            }

            Gage.CodeType selectedCodeType = parseCodeType(codeTypeStr);

            GageRequest gageRequest = GageRequest.builder()
                    .gageTypeName(gageTypeName)
                    .manufacturerId(manufacturerId)
                    .modelNumber(modelNumber)
                    .serialNumber(serialNumber)
                    .gageSubTypeId(gageSubTypeId)
                    .inhouseCalibrationMachineId(inhouseCalibrationMachineId)
                    .measurementRange(measurementRange)
                    .accuracy(accuracy)
                    .purchaseDate(purchaseDate)
                    .calibrationInterval(calibrationInterval)
                    .nextCalibrationDate(nextCalibrationDate)
                    .maxUsersNumber(maxUsersNumber)
                    .usageFrequency(usageFrequency)
                    .criticality(criticality)
                    .location(location)
                    .notes(notes)
                    .gageImages(base64Images) // ✅ Updated field
                    .gageVideos(base64Videos) // ✅ New video field
                    .gageManual(base64Manual)
                    .codeType(selectedCodeType)
                    .status(Gage.Status.ISSUED)
                    .build();

            GageResponse reissuedGage = gageService.createGage(gageRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(reissuedGage);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to process the uploaded files."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid request data: " + e.getMessage()));
        }
    }

    // =============== NEW ENDPOINT FROM CODE 2: Reissue with Category ===============
    @PostMapping("/reissue-with-category")
    public ResponseEntity<?> reissueGageWithCategory(
            @RequestParam("gageTypeName") String gageTypeName,
            @RequestParam("manufacturerId") String manufacturerId,
            @RequestParam("modelNumber") String modelNumber,
            @RequestParam("serialNumber") String serialNumber,
            @RequestParam("measurementRange") String measurementRange,
            @RequestParam("accuracy") String accuracy,
            @RequestParam("purchaseDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate purchaseDate,
            @RequestParam("calibrationInterval") Integer calibrationInterval,
            @RequestParam("nextCalibrationDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextCalibrationDate,
            @RequestParam("maxUsersNumber") Integer maxUsersNumber,
            @RequestParam("usageFrequency") Gage.UsageFrequency usageFrequency,
            @RequestParam("criticality") Gage.Criticality criticality,
            @RequestParam("location") Gage.Location location,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "codeType", required = false) String codeTypeStr,
            @RequestParam(value = "gageImages", required = false) List<MultipartFile> gageImages,
            @RequestParam(value = "gageVideos", required = false) List<MultipartFile> gageVideos,
            @RequestParam(value = "gageManual", required = false) MultipartFile gageManual) {

        try {
            List<String> base64Images = null;
            if (gageImages != null && !gageImages.isEmpty()) {
                base64Images = gageImages.stream()
                        .filter(file -> !file.isEmpty())
                        .map(file -> {
                            try {
                                return Base64.getEncoder().encodeToString(file.getBytes());
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to read image file", e);
                            }
                        })
                        .collect(Collectors.toList());
            }

            // ✅ Process optional videos for reissue
            List<String> base64Videos = null;
            if (gageVideos != null && !gageVideos.isEmpty()) {
                base64Videos = gageVideos.stream()
                        .filter(file -> !file.isEmpty())
                        .map(file -> {
                            try {
                                return Base64.getEncoder().encodeToString(file.getBytes());
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to read video file", e);
                            }
                        })
                        .collect(Collectors.toList());
            }

            String base64Manual = null;
            if (gageManual != null && !gageManual.isEmpty()) {
                base64Manual = Base64.getEncoder().encodeToString(gageManual.getBytes());
            }

            Gage.CodeType selectedCodeType = parseCodeType(codeTypeStr);

            GageRequest gageRequest = GageRequest.builder()
                    .gageTypeName(gageTypeName)
                    .manufacturerId(manufacturerId)
                    .modelNumber(modelNumber)
                    .serialNumber(serialNumber)
                    .measurementRange(measurementRange)
                    .accuracy(accuracy)
                    .purchaseDate(purchaseDate)
                    .calibrationInterval(calibrationInterval)
                    .nextCalibrationDate(nextCalibrationDate)
                    .maxUsersNumber(maxUsersNumber)
                    .usageFrequency(usageFrequency)
                    .criticality(criticality)
                    .location(location)
                    .notes(notes)
                    .gageImages(base64Images)
                    .gageVideos(base64Videos)
                    .gageManual(base64Manual)
                    .codeType(selectedCodeType)
                    .status(Gage.Status.ISSUED)
                    .build();

            GageResponse reissuedGage = gageService.createGage(gageRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(reissuedGage);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to process the uploaded files."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid request data: " + e.getMessage()));
        }
    }

    // =============== SEARCH AND FILTER OPERATIONS ===============
    @GetMapping("/search")
    public ResponseEntity<List<GageResponse>> searchGages(@RequestParam String searchTerm) {
        List<GageResponse> gages = gageService.searchGages(searchTerm);
        return ResponseEntity.ok(gages);
    }

    @GetMapping("/type/{gageTypeId}")
    public ResponseEntity<List<GageResponse>> getGagesByType(@PathVariable Long gageTypeId) {
        List<GageResponse> gages = gageService.getGagesByType(gageTypeId);
        return ResponseEntity.ok(gages);
    }

    @GetMapping("/sub-type/{gageSubTypeId}")
    public ResponseEntity<List<GageResponse>> getGagesBySubType(@PathVariable Long gageSubTypeId) {
        List<GageResponse> gages = gageService.getGagesBySubType(gageSubTypeId);
        return ResponseEntity.ok(gages);
    }


    @GetMapping("/location/{location}")
    public ResponseEntity<List<GageResponse>> getGagesByLocation(@PathVariable Gage.Location location) {
        List<GageResponse> gages = gageService.getGagesByLocation(location);
        return ResponseEntity.ok(gages);
    }

    @GetMapping("/criticality/{criticality}")
    public ResponseEntity<List<GageResponse>> getGagesByCriticality(@PathVariable Gage.Criticality criticality) {
        List<GageResponse> gages = gageService.getGagesByCriticality(criticality);
        return ResponseEntity.ok(gages);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<GageResponse>> getGagesByStatus(@PathVariable Gage.Status status) {
        List<GageResponse> gages = gageService.getGagesByStatus(status);
        return ResponseEntity.ok(gages);
    }

    @GetMapping("/by-machine/{machineId}")
    public ResponseEntity<List<GageResponse>> getGagesByInhouseCalibrationMachine(@PathVariable Long machineId) {
        try {
            List<GageResponse> gages = gageService.getGagesByInhouseCalibrationMachine(machineId);
            return ResponseEntity.ok(gages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =============== VALIDATION ===============
    @GetMapping("/validate/serial-number")
    public ResponseEntity<Boolean> isSerialNumberUnique(@RequestParam String serialNumber) {
        boolean isUnique = gageService.isSerialNumberUnique(serialNumber);
        return ResponseEntity.ok(isUnique);
    }

    // =============== ENUM ENDPOINTS FOR FRONTEND DROPDOWNS ===============
    @GetMapping("/enums/gage-types")
    public ResponseEntity<List<GageTypeResponse>> getGageTypes() {
        return ResponseEntity.ok(gageTypeService.getAllGageTypes()); // ✅ use service
    }


    @GetMapping("/enums/usage-frequencies")
    public ResponseEntity<Gage.UsageFrequency[]> getUsageFrequencies() {
        return ResponseEntity.ok(Gage.UsageFrequency.values());
    }

    @GetMapping("/enums/criticalities")
    public ResponseEntity<Gage.Criticality[]> getCriticalities() {
        return ResponseEntity.ok(Gage.Criticality.values());
    }

    @GetMapping("/enums/locations")
    public ResponseEntity<Gage.Location[]> getLocations() {
        return ResponseEntity.ok(Gage.Location.values());
    }

    @GetMapping("/enums/statuses")
    public ResponseEntity<Gage.Status[]> getStatuses() {
        return ResponseEntity.ok(Gage.Status.values());
    }

    @GetMapping("/enums/accreditations")
    public ResponseEntity<ServiceProvider.Accreditation[]> getAccreditations() {
        return ResponseEntity.ok(ServiceProvider.Accreditation.values());
    }

    @GetMapping("/enums/calibration-statuses")
    public ResponseEntity<CalibrationHistory.CalibrationStatus[]> getCalibrationStatuses() {
        return ResponseEntity.ok(CalibrationHistory.CalibrationStatus.values());
    }

    @GetMapping("/enums/code-types")
    public ResponseEntity<Gage.CodeType[]> getCodeTypes() {
        return ResponseEntity.ok(Gage.CodeType.values());
    }

    // =============== MANUAL TRIGGER FOR DAILY UPDATE ===============
    @PostMapping("/admin/update-remaining-days")
    public ResponseEntity<?> manualUpdateRemainingDays() {
        try {
            dailySchedulerService.manualUpdateGagesRemainingDays();
            return ResponseEntity.ok(new SuccessResponse("Remaining days updated successfully for all gages"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to update remaining days: " + e.getMessage()));
        }
    }

    // =============== GAGE USAGE OPERATIONS ===============
    @GetMapping("/usage/validate")
    public ResponseEntity<GageUsageResponse> validateGageForUsage(
            @RequestParam String gageType,
            @RequestParam String serialNumber) {
        try {
            GageUsageResponse response = gageService.validateGageForUsage(gageType, serialNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GageUsageResponse.builder()
                            .serialNumber(serialNumber)
                            .gageType(gageType)
                            .isValidSerial(false)
                            .validationMessage("Error validating gage: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/usage/record")
    public ResponseEntity<GageUsageResponse> recordGageUsage(@Valid @RequestBody GageUsageRequest usageRequest) {
        try {
            GageUsageResponse response = gageService.recordGageUsage(usageRequest);
            if (response.getIsValidSerial()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GageUsageResponse.builder()
                            .serialNumber(usageRequest.getSerialNumber())
                            .isValidSerial(false)
                            .validationMessage("Error recording gage usage: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/by-type/{gageTypeName}")
    public ResponseEntity<List<GageResponse>> getGagesByTypeName(@PathVariable String gageTypeName) {
        try {
            List<GageResponse> gages = gageService.getGagesByType(gageTypeName);
            return ResponseEntity.ok(gages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/types/available")
    public ResponseEntity<List<GageTypeResponse>> getAvailableGageTypes() {
        try {
            List<GageTypeResponse> gageTypes = gageTypeService.getAllGageTypes();
            return ResponseEntity.ok(gageTypes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filtered")
    public ResponseEntity<List<GageResponse>> getFilteredGages(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String function,
            @RequestParam(required = false) String operation) {
        try {
            List<GageResponse> gages = gageService.getFilteredGages(department, function, operation);
            return ResponseEntity.ok(gages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =============== INNER CLASSES ===============
    @Data
    @AllArgsConstructor
    class ErrorResponse {
        private String message;
    }

    @Data
    @AllArgsConstructor
    class SuccessResponse {
        private String message;
    }

}