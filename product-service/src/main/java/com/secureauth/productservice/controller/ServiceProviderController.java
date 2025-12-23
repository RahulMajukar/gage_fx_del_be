package com.secureauth.productservice.controller;

import com.secureauth.productservice.dto.ServiceProviderRequest;
import com.secureauth.productservice.dto.ServiceProviderResponse;
import com.secureauth.productservice.entity.ServiceProvider;
import com.secureauth.productservice.service.ServiceProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/service-providers")
public class ServiceProviderController {

    @Autowired
    private ServiceProviderService serviceProviderService;

    // CRUD Operations
    @PostMapping
    public ResponseEntity<ServiceProviderResponse> createServiceProvider(@Valid @RequestBody ServiceProviderRequest serviceProviderRequest) {
        try {
            ServiceProviderResponse createdServiceProvider = serviceProviderService.createServiceProvider(serviceProviderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdServiceProvider);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceProviderResponse> getServiceProviderById(@PathVariable Long id) {
        try {
            ServiceProviderResponse serviceProvider = serviceProviderService.getServiceProviderById(id);
            return ResponseEntity.ok(serviceProvider);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ServiceProviderResponse> getServiceProviderByName(@PathVariable String name) {
        try {
            ServiceProviderResponse serviceProvider = serviceProviderService.getServiceProviderByName(name);
            return ResponseEntity.ok(serviceProvider);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ServiceProviderResponse>> getAllServiceProviders() {
        List<ServiceProviderResponse> serviceProviders = serviceProviderService.getAllServiceProviders();
        return ResponseEntity.ok(serviceProviders);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceProviderResponse> updateServiceProvider(@PathVariable Long id, @Valid @RequestBody ServiceProviderRequest serviceProviderRequest) {
        try {
            ServiceProviderResponse updatedServiceProvider = serviceProviderService.updateServiceProvider(id, serviceProviderRequest);
            return ResponseEntity.ok(updatedServiceProvider);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceProvider(@PathVariable Long id) {
        try {
            serviceProviderService.deleteServiceProvider(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Search and Filter Operations
    @GetMapping("/search")
    public ResponseEntity<List<ServiceProviderResponse>> searchServiceProviders(@RequestParam String searchTerm) {
        List<ServiceProviderResponse> serviceProviders = serviceProviderService.searchServiceProviders(searchTerm);
        return ResponseEntity.ok(serviceProviders);
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<List<ServiceProviderResponse>> getServiceProvidersByCountry(@PathVariable String country) {
        List<ServiceProviderResponse> serviceProviders = serviceProviderService.getServiceProvidersByCountry(country);
        return ResponseEntity.ok(serviceProviders);
    }

    @GetMapping("/accreditation/{accreditation}")
    public ResponseEntity<List<ServiceProviderResponse>> getServiceProvidersByAccreditation(@PathVariable ServiceProvider.Accreditation accreditation) {
        List<ServiceProviderResponse> serviceProviders = serviceProviderService.getServiceProvidersByAccreditation(accreditation);
        return ResponseEntity.ok(serviceProviders);
    }

    @GetMapping("/service-type/{serviceType}")
    public ResponseEntity<List<ServiceProviderResponse>> getServiceProvidersByServiceType(@PathVariable ServiceProvider.ServiceType serviceType) {
        List<ServiceProviderResponse> serviceProviders = serviceProviderService.getServiceProvidersByServiceType(serviceType);
        return ResponseEntity.ok(serviceProviders);
    }

    @GetMapping("/response-time/{responseTime}")
    public ResponseEntity<List<ServiceProviderResponse>> getServiceProvidersByResponseTime(@PathVariable ServiceProvider.ResponseTime responseTime) {
        List<ServiceProviderResponse> serviceProviders = serviceProviderService.getServiceProvidersByResponseTime(responseTime);
        return ResponseEntity.ok(serviceProviders);
    }

    @GetMapping("/expiring-accreditations")
    public ResponseEntity<List<ServiceProviderResponse>> getExpiringAccreditations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ServiceProviderResponse> serviceProviders = serviceProviderService.getExpiringAccreditations(date);
        return ResponseEntity.ok(serviceProviders);
    }



    // Validation
    @GetMapping("/validate/name")
    public ResponseEntity<Boolean> isNameUnique(@RequestParam String name) {
        boolean isUnique = serviceProviderService.isNameUnique(name);
        return ResponseEntity.ok(isUnique);
    }

    // Enum endpoints for frontend dropdowns
    @GetMapping("/enums/accreditations")
    public ResponseEntity<ServiceProvider.Accreditation[]> getAccreditations() {
        return ResponseEntity.ok(ServiceProvider.Accreditation.values());
    }

    @GetMapping("/enums/service-types")
    public ResponseEntity<ServiceProvider.ServiceType[]> getServiceTypes() {
        return ResponseEntity.ok(ServiceProvider.ServiceType.values());
    }

    @GetMapping("/enums/response-times")
    public ResponseEntity<ServiceProvider.ResponseTime[]> getResponseTimes() {
        return ResponseEntity.ok(ServiceProvider.ResponseTime.values());
    }

    @GetMapping("/enums/service-provider-types")
    public ResponseEntity<ServiceProvider.ServiceProviderType[]> getServiceProviderTypes() {
        return ResponseEntity.ok(ServiceProvider.ServiceProviderType.values());
    }
} 