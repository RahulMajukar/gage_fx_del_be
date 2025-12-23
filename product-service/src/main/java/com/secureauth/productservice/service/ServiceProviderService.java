package com.secureauth.productservice.service;

import com.secureauth.productservice.dto.ServiceProviderRequest;
import com.secureauth.productservice.dto.ServiceProviderResponse;
import com.secureauth.productservice.entity.ServiceProvider;

import java.time.LocalDate;
import java.util.List;

public interface ServiceProviderService {

    ServiceProviderResponse createServiceProvider(ServiceProviderRequest serviceProviderRequest);
    
    ServiceProviderResponse getServiceProviderById(Long id);
    
    ServiceProviderResponse getServiceProviderByName(String name);
    
    List<ServiceProviderResponse> getAllServiceProviders();
    
    List<ServiceProviderResponse> getServiceProvidersByCountry(String country);
    
    List<ServiceProviderResponse> getServiceProvidersByAccreditation(ServiceProvider.Accreditation accreditation);
    
    List<ServiceProviderResponse> getServiceProvidersByServiceType(ServiceProvider.ServiceType serviceType);
    
    List<ServiceProviderResponse> getServiceProvidersByResponseTime(ServiceProvider.ResponseTime responseTime);

    List<ServiceProviderResponse> getServiceProvidersByType(ServiceProvider.ServiceProviderType serviceProviderType);

    List<ServiceProviderResponse> getExpiringAccreditations(LocalDate date);
    
    List<ServiceProviderResponse> searchServiceProviders(String searchTerm);
    
    ServiceProviderResponse updateServiceProvider(Long id, ServiceProviderRequest serviceProviderRequest);
    
    void deleteServiceProvider(Long id);
    
    boolean isNameUnique(String name);
    

} 