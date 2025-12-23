package com.secureauth.productservice.service.impl;

import com.secureauth.productservice.dto.GageTypeRequest;
import com.secureauth.productservice.dto.GageTypeResponse;
import com.secureauth.productservice.entity.GageType;
import com.secureauth.productservice.entity.GageSubType;
import com.secureauth.productservice.repository.GageTypeRepository;
import com.secureauth.productservice.repository.GageSubTypeRepository;
import com.secureauth.productservice.service.GageTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GageTypeServiceImpl implements GageTypeService {

    @Autowired
    private GageTypeRepository gageTypeRepository;

    @Autowired
    private GageSubTypeRepository gageSubTypeRepository;

    @Override
    public GageTypeResponse createGageType(GageTypeRequest request) {
        GageSubType gageSubType = gageSubTypeRepository.findById(request.getGageSubTypeId())
                .orElseThrow(() -> new RuntimeException("GageSubType not found with id: " + request.getGageSubTypeId()));
        
        GageType gageType = GageType.builder()
                .name(request.getName())
                .gageSubType(gageSubType)
                .description(request.getDescription())
                .build();
        gageType = gageTypeRepository.save(gageType);
        return mapToResponse(gageType);
    }

    @Override
    public GageTypeResponse getGageTypeById(Long id) {
        GageType gageType = gageTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GageType not found with id: " + id));
        return mapToResponse(gageType);
    }

    @Override
    public List<GageTypeResponse> getAllGageTypes() {
        return gageTypeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GageTypeResponse updateGageType(Long id, GageTypeRequest request) {
        GageType gageType = gageTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GageType not found with id: " + id));
        GageSubType gageSubType = gageSubTypeRepository.findById(request.getGageSubTypeId())
                .orElseThrow(() -> new RuntimeException("GageSubType not found with id: " + request.getGageSubTypeId()));
        
        gageType.setName(request.getName());
        gageType.setGageSubType(gageSubType);
        gageType.setDescription(request.getDescription());
        gageType = gageTypeRepository.save(gageType);
        return mapToResponse(gageType);
    }

    @Override
    public void deleteGageType(Long id) {
        if (!gageTypeRepository.existsById(id)) {
            throw new RuntimeException("GageType not found with id: " + id);
        }
        gageTypeRepository.deleteById(id);
    }

    @Override
    public boolean isNameUnique(String name) {
        return !gageTypeRepository.existsByName(name);
    }

    private GageTypeResponse mapToResponse(GageType gageType) {
        return GageTypeResponse.builder()
                .id(gageType.getId())
                .name(gageType.getName())
                .gageSubTypeId(gageType.getGageSubType() != null ? gageType.getGageSubType().getId() : null)
                .gageSubTypeName(gageType.getGageSubType() != null ? gageType.getGageSubType().getName() : null)
                .description(gageType.getDescription())
                .build();
    }
}
