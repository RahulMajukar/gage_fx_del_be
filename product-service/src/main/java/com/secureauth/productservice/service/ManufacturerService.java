package com.secureauth.productservice.service;

import com.secureauth.productservice.dto.ManufacturerRequest;
import com.secureauth.productservice.dto.ManufacturerResponse;
import com.secureauth.productservice.entity.Manufacturer;

import java.util.List;

public interface ManufacturerService {

    ManufacturerResponse createManufacturer(ManufacturerRequest manufacturerRequest);

    ManufacturerResponse getManufacturerById(Long id);

    ManufacturerResponse getManufacturerByName(String name);

    List<ManufacturerResponse> getAllManufacturers();

    List<ManufacturerResponse> getManufacturersByCountry(String country);

    List<ManufacturerResponse> getManufacturersByType(Manufacturer.ManufacturerType manufacturerType);

    List<ManufacturerResponse> searchManufacturers(String searchTerm);

    ManufacturerResponse updateManufacturer(Long id, ManufacturerRequest manufacturerRequest);

    void deleteManufacturer(Long id);

    boolean isNameUnique(String name);

    List<ManufacturerResponse> getManufacturersByGageId(Long gageId);

    // Add these new methods to the interface
    boolean isManufacturerUsed(Long manufacturerId);

    List<com.secureauth.productservice.entity.Gage> getGagesUsingManufacturer(Long manufacturerId);
}