package com.secureauth.productservice.service;

import com.secureauth.productservice.dto.*;
import com.secureauth.productservice.entity.Gage;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface GageService {

    // CRUD Operations
    GageResponse createGage(GageRequest gageRequest);
    GageResponse getGageById(Long id);
    GageResponse getGageBySerialNumber(String serialNumber);
    List<GageResponse> getAllGages();
    GageResponse updateGage(Long id, GageRequest gageRequest);
    void deleteGage(Long id);

    // Status Operations
    GageResponse updateGageStatus(Long id, Gage.Status status);
    GageResponse inwardupdateGageStatus(Long id, Gage.Status status);
    GageResponse issueGageBySerialNumber(String serialNumber);

    // Search and Filter Operations
    List<GageResponse> searchGages(String searchTerm);
    List<GageResponse> getGagesByType(Long gageTypeId);
    List<GageResponse> getGagesBySubType(Long gageSubTypeId);
    List<GageResponse> getGagesByStatus(Gage.Status status);
    List<GageResponse> getGagesByLocation(Gage.Location location);
    List<GageResponse> getGagesByCriticality(Gage.Criticality criticality);

    List<GageResponse> getGagesByTypeName(String gageTypeName);

    // Validation
    boolean isSerialNumberUnique(String serialNumber);

    // Daily Operations
    void updateAllGagesRemainingDays();

    // Gage Usage Operations
    GageUsageResponse validateGageForUsage(String gageType, String serialNumber);
    GageUsageResponse recordGageUsage(GageUsageRequest usageRequest);
    List<GageResponse> getGagesByType(String gageTypeName);

    // Filtering Operations
    List<GageResponse> getFilteredGages(String department, String function, String operation);

    // Barcode Scanning Operations
    GageScanResponse getGageDetailsByBarcodeImage(MultipartFile barcodeImage);
    GageScanResponse getGageDetailsByBarcodeScan(String serialNumber);
}