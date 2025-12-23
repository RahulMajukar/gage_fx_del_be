package com.secureauth.productservice.service;

import com.secureauth.productservice.dto.SupplierRequest;
import com.secureauth.productservice.dto.SupplierResponse;
import com.secureauth.productservice.entity.Supplier;

import java.util.List;

public interface SupplierService {

    SupplierResponse createSupplier(SupplierRequest supplierRequest);
    
    SupplierResponse getSupplierById(Long id);
    
    SupplierResponse getSupplierByName(String name);
    
    List<SupplierResponse> getAllSuppliers();
    
    List<SupplierResponse> getSuppliersByCountry(String country);
    
    List<SupplierResponse> getSuppliersByType(Supplier.SupplierType supplierType);
    
    List<SupplierResponse> getSuppliersByPaymentTerms(Supplier.PaymentTerms paymentTerms);
    
    List<SupplierResponse> searchSuppliers(String searchTerm);
    
    SupplierResponse updateSupplier(Long id, SupplierRequest supplierRequest);
    
    void deleteSupplier(Long id);
    
    boolean isNameUnique(String name);
    

} 