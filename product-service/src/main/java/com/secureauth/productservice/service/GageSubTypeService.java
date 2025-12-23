package com.secureauth.productservice.service;

import com.secureauth.productservice.dto.GageSubTypeRequest;
import com.secureauth.productservice.dto.GageSubTypeResponse;

import java.util.List;

public interface GageSubTypeService {

    GageSubTypeResponse createGageSubType(GageSubTypeRequest request);

    GageSubTypeResponse getGageSubTypeById(Long id);

    List<GageSubTypeResponse> getAllGageSubTypes();

    GageSubTypeResponse updateGageSubType(Long id, GageSubTypeRequest request);

    void deleteGageSubType(Long id);

    boolean isNameUnique(String name);
}

