package com.secureauth.productservice.controller;

import com.secureauth.productservice.dto.ManufacturerRequest;
import com.secureauth.productservice.dto.ManufacturerResponse;
import com.secureauth.productservice.entity.Manufacturer;
import com.secureauth.productservice.entity.Gage; // Add this import if not present
import com.secureauth.productservice.service.ManufacturerService;
import com.secureauth.productservice.exception.ResourceNotFoundException; // Add this import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.secureauth.productservice.repository.ManufacturerRepository;
import com.secureauth.productservice.repository.GageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/manufacturers")
public class ManufacturerController {

    private static final Logger logger = LoggerFactory.getLogger(ManufacturerController.class);

    @Autowired
    private ManufacturerService manufacturerService;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private GageRepository gageRepository; // Add this field

    // CRUD Operations
    @PostMapping
    public ResponseEntity<ManufacturerResponse> createManufacturer(@Valid @RequestBody ManufacturerRequest manufacturerRequest) {
        try {
            ManufacturerResponse createdManufacturer = manufacturerService.createManufacturer(manufacturerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdManufacturer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManufacturerResponse> getManufacturerById(@PathVariable Long id) {
        try {
            ManufacturerResponse manufacturer = manufacturerService.getManufacturerById(id);
            return ResponseEntity.ok(manufacturer);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ManufacturerResponse> getManufacturerByName(@PathVariable String name) {
        try {
            ManufacturerResponse manufacturer = manufacturerService.getManufacturerByName(name);
            return ResponseEntity.ok(manufacturer);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ManufacturerResponse>> getAllManufacturers() {
        try {
            logger.info("Fetching all manufacturers...");
            List<ManufacturerResponse> manufacturers = manufacturerService.getAllManufacturers();
            logger.info("Successfully fetched {} manufacturers", manufacturers.size());
            return ResponseEntity.ok(manufacturers);
        } catch (Exception e) {
            logger.error("Error fetching all manufacturers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ManufacturerResponse> updateManufacturer(@PathVariable Long id, @Valid @RequestBody ManufacturerRequest manufacturerRequest) {
        try {
            ManufacturerResponse updatedManufacturer = manufacturerService.updateManufacturer(id, manufacturerRequest);
            return ResponseEntity.ok(updatedManufacturer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Updated delete method with usage check
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteManufacturer(@PathVariable Long id) {
        try {
            // Check if manufacturer is being used by any gages
            boolean isUsed = manufacturerService.isManufacturerUsed(id);
            if (isUsed) {
                long gageCount = gageRepository.countByManufacturerId(id);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new DeleteResponse(false, "Cannot delete manufacturer: it is currently used by " + gageCount + " gage(s)", gageCount));
            }

            // If not used, proceed with deletion
            manufacturerService.deleteManufacturer(id);
            return ResponseEntity.noContent().build();

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new DeleteResponse(false, e.getMessage(), 0));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DeleteResponse(false, "Failed to delete manufacturer: " + e.getMessage(), 0));
        }
    }

    // Search and Filter Operations
    @GetMapping("/search")
    public ResponseEntity<List<ManufacturerResponse>> searchManufacturers(@RequestParam String searchTerm) {
        List<ManufacturerResponse> manufacturers = manufacturerService.searchManufacturers(searchTerm);
        return ResponseEntity.ok(manufacturers);
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<List<ManufacturerResponse>> getManufacturersByCountry(@PathVariable String country) {
        List<ManufacturerResponse> manufacturers = manufacturerService.getManufacturersByCountry(country);
        return ResponseEntity.ok(manufacturers);
    }

    @GetMapping("/type/{manufacturerType}")
    public ResponseEntity<List<ManufacturerResponse>> getManufacturersByType(@PathVariable Manufacturer.ManufacturerType manufacturerType) {
        List<ManufacturerResponse> manufacturers = manufacturerService.getManufacturersByType(manufacturerType);
        return ResponseEntity.ok(manufacturers);
    }

    @GetMapping("/gage/{gageId}")
    public ResponseEntity<List<ManufacturerResponse>> getManufacturersByGageId(@PathVariable Long gageId) {
        List<ManufacturerResponse> manufacturers = manufacturerService.getManufacturersByGageId(gageId);
        return ResponseEntity.ok(manufacturers);
    }

    // Validation
    @GetMapping("/validate/name")
    public ResponseEntity<Boolean> isNameUnique(@RequestParam String name) {
        boolean isUnique = manufacturerService.isNameUnique(name);
        return ResponseEntity.ok(isUnique);
    }

    // New endpoints for manufacturer usage check
    @GetMapping("/{id}/can-delete")
    public ResponseEntity<?> canDeleteManufacturer(@PathVariable Long id) {
        try {
            boolean isUsed = manufacturerService.isManufacturerUsed(id);
            long gageCount = isUsed ? gageRepository.countByManufacturerId(id) : 0;

            return ResponseEntity.ok(new DeleteCheckResponse(!isUsed, isUsed, gageCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/used-by-gages")
    public ResponseEntity<?> getGagesUsingManufacturer(@PathVariable Long id) {
        try {
            List<com.secureauth.productservice.entity.Gage> gages = manufacturerService.getGagesUsingManufacturer(id);
            if (gages.isEmpty()) {
                return ResponseEntity.ok(new GagesUsageResponse(false, 0, new ArrayList<>()));
            } else {
                List<GageInfo> gageInfos = gages.stream().map(gage -> new GageInfo(
                        gage.getId(),
                        gage.getSerialNumber(),
                        gage.getModelNumber(),
                        gage.getStatus()
                )).collect(Collectors.toList());

                return ResponseEntity.ok(new GagesUsageResponse(true, gages.size(), gageInfos));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Enum endpoints for frontend dropdowns
    @GetMapping("/enums/manufacturer-types")
    public ResponseEntity<Manufacturer.ManufacturerType[]> getManufacturerTypes() {
        return ResponseEntity.ok(Manufacturer.ManufacturerType.values());
    }

    // Debug endpoint to check database status
    @GetMapping("/debug/status")
    public ResponseEntity<String> getDebugStatus() {
        try {
            long count = manufacturerRepository.count();
            logger.info("Database contains {} manufacturers", count);
            return ResponseEntity.ok("Database contains " + count + " manufacturers");
        } catch (Exception e) {
            logger.error("Error checking database status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking database: " + e.getMessage());
        }
    }

    // Inner classes for response objects
    @lombok.Data
    @lombok.AllArgsConstructor
    static class DeleteResponse {
        private boolean success;
        private String message;
        private long gageCount;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class DeleteCheckResponse {
        private boolean canDelete;
        private boolean isUsed;
        private long gageCount;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class GagesUsageResponse {
        private boolean usedByGages;
        private long gageCount;
        private List<GageInfo> gages;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class GageInfo {
        private Long id;
        private String serialNumber;
        private String modelNumber;
        private com.secureauth.productservice.entity.Gage.Status status;
    }
}