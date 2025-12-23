package com.secureauth.productservice.controller;

import com.secureauth.productservice.dto.SupplierRequest;
import com.secureauth.productservice.dto.SupplierResponse;
import com.secureauth.productservice.entity.Supplier;
import com.secureauth.productservice.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    // CRUD Operations
    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody SupplierRequest supplierRequest) {
        try {
            SupplierResponse createdSupplier = supplierService.createSupplier(supplierRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSupplier);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable Long id) {
        try {
            SupplierResponse supplier = supplierService.getSupplierById(id);
            return ResponseEntity.ok(supplier);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<SupplierResponse> getSupplierByName(@PathVariable String name) {
        try {
            SupplierResponse supplier = supplierService.getSupplierByName(name);
            return ResponseEntity.ok(supplier);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        List<SupplierResponse> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierRequest supplierRequest) {
        try {
            SupplierResponse updatedSupplier = supplierService.updateSupplier(id, supplierRequest);
            return ResponseEntity.ok(updatedSupplier);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        try {
            supplierService.deleteSupplier(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Search and Filter Operations
    @GetMapping("/search")
    public ResponseEntity<List<SupplierResponse>> searchSuppliers(@RequestParam String searchTerm) {
        List<SupplierResponse> suppliers = supplierService.searchSuppliers(searchTerm);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<List<SupplierResponse>> getSuppliersByCountry(@PathVariable String country) {
        List<SupplierResponse> suppliers = supplierService.getSuppliersByCountry(country);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/type/{supplierType}")
    public ResponseEntity<List<SupplierResponse>> getSuppliersByType(@PathVariable Supplier.SupplierType supplierType) {
        List<SupplierResponse> suppliers = supplierService.getSuppliersByType(supplierType);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/payment-terms/{paymentTerms}")
    public ResponseEntity<List<SupplierResponse>> getSuppliersByPaymentTerms(@PathVariable Supplier.PaymentTerms paymentTerms) {
        List<SupplierResponse> suppliers = supplierService.getSuppliersByPaymentTerms(paymentTerms);
        return ResponseEntity.ok(suppliers);
    }



    // Validation
    @GetMapping("/validate/name")
    public ResponseEntity<Boolean> isNameUnique(@RequestParam String name) {
        boolean isUnique = supplierService.isNameUnique(name);
        return ResponseEntity.ok(isUnique);
    }

    // Enum endpoints for frontend dropdowns
    @GetMapping("/enums/supplier-types")
    public ResponseEntity<Supplier.SupplierType[]> getSupplierTypes() {
        return ResponseEntity.ok(Supplier.SupplierType.values());
    }

    @GetMapping("/enums/payment-terms")
    public ResponseEntity<Supplier.PaymentTerms[]> getPaymentTerms() {
        return ResponseEntity.ok(Supplier.PaymentTerms.values());
    }
} 