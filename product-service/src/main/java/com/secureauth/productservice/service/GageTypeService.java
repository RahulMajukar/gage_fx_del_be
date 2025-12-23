package com.secureauth.productservice.service;

import com.secureauth.productservice.dto.GageTypeRequest;
import com.secureauth.productservice.dto.GageTypeResponse;

import java.util.List;

public interface GageTypeService {

    GageTypeResponse createGageType(GageTypeRequest request);

    GageTypeResponse getGageTypeById(Long id);

    List<GageTypeResponse> getAllGageTypes();

    GageTypeResponse updateGageType(Long id, GageTypeRequest request);

    void deleteGageType(Long id);

    boolean isNameUnique(String name);
}
