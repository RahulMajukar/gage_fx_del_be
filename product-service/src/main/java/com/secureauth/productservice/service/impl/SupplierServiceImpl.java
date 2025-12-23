package com.secureauth.productservice.service.impl;

import com.secureauth.productservice.dto.SupplierRequest;
import com.secureauth.productservice.dto.SupplierResponse;
import com.secureauth.productservice.entity.Supplier;
import com.secureauth.productservice.exception.ResourceNotFoundException;
import com.secureauth.productservice.repository.SupplierRepository;
import com.secureauth.productservice.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    public SupplierResponse createSupplier(SupplierRequest supplierRequest) {
        if (supplierRepository.existsByName(supplierRequest.getName())) {
            throw new IllegalArgumentException("Supplier with name '" + supplierRequest.getName() + "' already exists");
        }
        
        Supplier supplier = Supplier.builder()
                .name(supplierRequest.getName())
                .address(supplierRequest.getAddress())
                .country(supplierRequest.getCountry())
                .contactPerson(supplierRequest.getContactPerson())
                .phoneNumber(supplierRequest.getPhoneNumber())
                .email(supplierRequest.getEmail())
                .website(supplierRequest.getWebsite())
                .invoicePONumber(supplierRequest.getInvoicePONumber())
                .description(supplierRequest.getDescription())
                .supplierType(supplierRequest.getSupplierType())
                .taxIdentificationNumber(supplierRequest.getTaxIdentificationNumber())
                .businessLicenseNumber(supplierRequest.getBusinessLicenseNumber())
                .paymentTerms(supplierRequest.getPaymentTerms())
                .build();
        
        Supplier savedSupplier = supplierRepository.save(supplier);
        return SupplierResponse.fromEntity(savedSupplier);
    }

    @Override
    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return SupplierResponse.fromEntity(supplier);
    }

    @Override
    public SupplierResponse getSupplierByName(String name) {
        Supplier supplier = supplierRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with name: " + name));
        return SupplierResponse.fromEntity(supplier);
    }

    @Override
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(SupplierResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierResponse> getSuppliersByCountry(String country) {
        return supplierRepository.findByCountry(country).stream()
                .map(SupplierResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierResponse> getSuppliersByType(Supplier.SupplierType supplierType) {
        return supplierRepository.findBySupplierType(supplierType).stream()
                .map(SupplierResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierResponse> getSuppliersByPaymentTerms(Supplier.PaymentTerms paymentTerms) {
        return supplierRepository.findByPaymentTerms(paymentTerms).stream()
                .map(SupplierResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierResponse> searchSuppliers(String searchTerm) {
        return supplierRepository.searchSuppliers(searchTerm).stream()
                .map(SupplierResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public SupplierResponse updateSupplier(Long id, SupplierRequest supplierRequest) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        
        // Check if name is being changed and if it conflicts with existing name
        if (!existingSupplier.getName().equals(supplierRequest.getName()) && 
            supplierRepository.existsByName(supplierRequest.getName())) {
            throw new IllegalArgumentException("Supplier with name '" + supplierRequest.getName() + "' already exists");
        }
        
        existingSupplier.setName(supplierRequest.getName());
        existingSupplier.setAddress(supplierRequest.getAddress());
        existingSupplier.setCountry(supplierRequest.getCountry());
        existingSupplier.setContactPerson(supplierRequest.getContactPerson());
        existingSupplier.setPhoneNumber(supplierRequest.getPhoneNumber());
        existingSupplier.setEmail(supplierRequest.getEmail());
        existingSupplier.setWebsite(supplierRequest.getWebsite());
        existingSupplier.setInvoicePONumber(supplierRequest.getInvoicePONumber());
        existingSupplier.setDescription(supplierRequest.getDescription());
        existingSupplier.setSupplierType(supplierRequest.getSupplierType());
        existingSupplier.setTaxIdentificationNumber(supplierRequest.getTaxIdentificationNumber());
        existingSupplier.setBusinessLicenseNumber(supplierRequest.getBusinessLicenseNumber());
        existingSupplier.setPaymentTerms(supplierRequest.getPaymentTerms());
        
        Supplier updatedSupplier = supplierRepository.save(existingSupplier);
        return SupplierResponse.fromEntity(updatedSupplier);
    }

    @Override
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found with id: " + id);
        }
        supplierRepository.deleteById(id);
    }

    @Override
    public boolean isNameUnique(String name) {
        return !supplierRepository.existsByName(name);
    }


} 