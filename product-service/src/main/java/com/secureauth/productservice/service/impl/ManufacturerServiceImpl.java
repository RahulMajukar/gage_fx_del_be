package com.secureauth.productservice.service.impl;

import com.secureauth.productservice.dto.ManufacturerRequest;
import com.secureauth.productservice.dto.ManufacturerResponse;
import com.secureauth.productservice.entity.Manufacturer;
import com.secureauth.productservice.exception.ResourceNotFoundException;
import com.secureauth.productservice.repository.ManufacturerRepository;
import com.secureauth.productservice.repository.GageRepository;
import com.secureauth.productservice.service.ManufacturerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManufacturerServiceImpl implements ManufacturerService {

    private static final Logger logger = LoggerFactory.getLogger(ManufacturerServiceImpl.class);

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private GageRepository gageRepository;

    @Override
    public ManufacturerResponse createManufacturer(ManufacturerRequest manufacturerRequest) {
        if (manufacturerRepository.existsByName(manufacturerRequest.getName())) {
            throw new IllegalArgumentException("Manufacturer with name '" + manufacturerRequest.getName() + "' already exists");
        }

        Manufacturer manufacturer = Manufacturer.builder()
                .name(manufacturerRequest.getName())
                .address(manufacturerRequest.getAddress())
                .country(manufacturerRequest.getCountry())
                .contactPerson(manufacturerRequest.getContactPerson())
                .phoneNumber(manufacturerRequest.getPhoneNumber())
                .email(manufacturerRequest.getEmail())
                .website(manufacturerRequest.getWebsite())
                .description(manufacturerRequest.getDescription())
                .manufacturerType(manufacturerRequest.getManufacturerType())
                .certificationNumber(manufacturerRequest.getCertificationNumber())
                .build();

        Manufacturer savedManufacturer = manufacturerRepository.save(manufacturer);
        return ManufacturerResponse.fromEntity(savedManufacturer);
    }

    @Override
    public ManufacturerResponse getManufacturerById(Long id) {
        Manufacturer manufacturer = manufacturerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found with id: " + id));
        return ManufacturerResponse.fromEntity(manufacturer);
    }

    @Override
    public ManufacturerResponse getManufacturerByName(String name) {
        Manufacturer manufacturer = manufacturerRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found with name: " + name));
        return ManufacturerResponse.fromEntity(manufacturer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManufacturerResponse> getAllManufacturers() {
        try {
            logger.info("Starting to fetch all manufacturers from database...");
            List<Manufacturer> manufacturers = manufacturerRepository.findAll();
            logger.info("Found {} manufacturers in database", manufacturers.size());

            List<ManufacturerResponse> responses = manufacturers.stream()
                    .map(ManufacturerResponse::fromEntity)
                    .collect(Collectors.toList());

            logger.info("Successfully converted {} manufacturers to response objects", responses.size());
            return responses;
        } catch (Exception e) {
            logger.error("Error fetching all manufacturers: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch manufacturers: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ManufacturerResponse> getManufacturersByCountry(String country) {
        return manufacturerRepository.findByCountry(country).stream()
                .map(ManufacturerResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ManufacturerResponse> getManufacturersByType(Manufacturer.ManufacturerType manufacturerType) {
        return manufacturerRepository.findByManufacturerType(manufacturerType).stream()
                .map(ManufacturerResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ManufacturerResponse> searchManufacturers(String searchTerm) {
        return manufacturerRepository.searchManufacturers(searchTerm).stream()
                .map(ManufacturerResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ManufacturerResponse updateManufacturer(Long id, ManufacturerRequest manufacturerRequest) {
        Manufacturer existingManufacturer = manufacturerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found with id: " + id));

        // Check if name is being changed and conflicts with existing name
        if (!existingManufacturer.getName().equals(manufacturerRequest.getName()) &&
                manufacturerRepository.existsByName(manufacturerRequest.getName())) {
            throw new IllegalArgumentException("Manufacturer with name '" + manufacturerRequest.getName() + "' already exists");
        }

        existingManufacturer.setName(manufacturerRequest.getName());
        existingManufacturer.setAddress(manufacturerRequest.getAddress());
        existingManufacturer.setCountry(manufacturerRequest.getCountry());
        existingManufacturer.setContactPerson(manufacturerRequest.getContactPerson());
        existingManufacturer.setPhoneNumber(manufacturerRequest.getPhoneNumber());
        existingManufacturer.setEmail(manufacturerRequest.getEmail());
        existingManufacturer.setWebsite(manufacturerRequest.getWebsite());
        existingManufacturer.setDescription(manufacturerRequest.getDescription());
        existingManufacturer.setManufacturerType(manufacturerRequest.getManufacturerType());
        existingManufacturer.setCertificationNumber(manufacturerRequest.getCertificationNumber());

        Manufacturer updatedManufacturer = manufacturerRepository.save(existingManufacturer);
        return ManufacturerResponse.fromEntity(updatedManufacturer);
    }

    @Override
    @Transactional
    public void deleteManufacturer(Long id) {
        Manufacturer manufacturer = manufacturerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found with id: " + id));

        // Check if manufacturer is used by any gages
        if (isManufacturerUsed(id)) {
            long count = gageRepository.countByManufacturerId(id);
            throw new IllegalStateException("Cannot delete manufacturer: it is currently used by " + count + " gage(s)");
        }

        try {
            manufacturerRepository.delete(manufacturer);
            manufacturerRepository.flush();
            logger.info("Manufacturer {} deleted successfully", id);
        } catch (DataIntegrityViolationException ex) {
            logger.error("Failed to delete manufacturer {} due to constraint violation: {}", id, ex.getMessage());
            throw new IllegalStateException("Cannot delete manufacturer due to database constraint violation.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isManufacturerUsed(Long manufacturerId) {
        // Efficient existence check
        return gageRepository.existsByManufacturerId(manufacturerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.secureauth.productservice.entity.Gage> getGagesUsingManufacturer(Long manufacturerId) {
        return gageRepository.findByManufacturerId(manufacturerId);
    }

    @Override
    public boolean isNameUnique(String name) {
        return !manufacturerRepository.existsByName(name);
    }

    @Override
    public List<ManufacturerResponse> getManufacturersByGageId(Long gageId) {
        return manufacturerRepository.findByGageId(gageId).stream()
                .map(ManufacturerResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
