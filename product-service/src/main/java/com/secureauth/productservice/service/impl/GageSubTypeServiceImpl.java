package com.secureauth.productservice.service.impl;

import com.secureauth.productservice.dto.GageSubTypeRequest;
import com.secureauth.productservice.dto.GageSubTypeResponse;
import com.secureauth.productservice.entity.GageSubType;
import com.secureauth.productservice.repository.GageSubTypeRepository;
import com.secureauth.productservice.service.GageSubTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GageSubTypeServiceImpl implements GageSubTypeService {

    @Autowired
    private GageSubTypeRepository gageSubTypeRepository;

    @Override
    public GageSubTypeResponse createGageSubType(GageSubTypeRequest request) {
        GageSubType gageSubType = GageSubType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        gageSubType = gageSubTypeRepository.save(gageSubType);
        return mapToResponse(gageSubType);
    }

    @Override
    public GageSubTypeResponse getGageSubTypeById(Long id) {
        GageSubType gageSubType = gageSubTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GageSubType not found with id: " + id));
        return mapToResponse(gageSubType);
    }

    @Override
    public List<GageSubTypeResponse> getAllGageSubTypes() {
        return gageSubTypeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GageSubTypeResponse updateGageSubType(Long id, GageSubTypeRequest request) {
        GageSubType gageSubType = gageSubTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GageSubType not found with id: " + id));
        gageSubType.setName(request.getName());
        gageSubType.setDescription(request.getDescription());
        gageSubType = gageSubTypeRepository.save(gageSubType);
        return mapToResponse(gageSubType);
    }

    @Override
    public void deleteGageSubType(Long id) {
        if (!gageSubTypeRepository.existsById(id)) {
            throw new RuntimeException("GageSubType not found with id: " + id);
        }
        gageSubTypeRepository.deleteById(id);
    }

    @Override
    public boolean isNameUnique(String name) {
        return !gageSubTypeRepository.existsByName(name);
    }

    private GageSubTypeResponse mapToResponse(GageSubType gageSubType) {
        return GageSubTypeResponse.builder()
                .id(gageSubType.getId())
                .name(gageSubType.getName())
                .description(gageSubType.getDescription())
                .build();
    }
}

