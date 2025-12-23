package com.secureauth.productservice.service.impl;

import com.secureauth.productservice.dto.ServiceProviderRequest;
import com.secureauth.productservice.dto.ServiceProviderResponse;
import com.secureauth.productservice.entity.ServiceProvider;
import com.secureauth.productservice.exception.ResourceNotFoundException;
import com.secureauth.productservice.repository.ServiceProviderRepository;
import com.secureauth.productservice.service.ServiceProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceProviderServiceImpl implements ServiceProviderService {

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Override
    public ServiceProviderResponse createServiceProvider(ServiceProviderRequest serviceProviderRequest) {
        if (serviceProviderRepository.existsByName(serviceProviderRequest.getName())) {
            throw new IllegalArgumentException("Service Provider with name '" + serviceProviderRequest.getName() + "' already exists");
        }

        ServiceProvider serviceProvider = ServiceProvider.builder()
                .name(serviceProviderRequest.getName())
                .accreditation(serviceProviderRequest.getAccreditation())
                .address(serviceProviderRequest.getAddress())
                .country(serviceProviderRequest.getCountry())
                .contactPerson(serviceProviderRequest.getContactPerson())
                .phoneNumber(serviceProviderRequest.getPhoneNumber())
                .email(serviceProviderRequest.getEmail())
                .website(serviceProviderRequest.getWebsite())
                .certificate(serviceProviderRequest.getCertificate())
                .description(serviceProviderRequest.getDescription())
                .serviceType(serviceProviderRequest.getServiceType())
                .accreditationNumber(serviceProviderRequest.getAccreditationNumber())
                .accreditationExpiryDate(serviceProviderRequest.getAccreditationExpiryDate())
                .serviceAreas(serviceProviderRequest.getServiceAreas())
                .responseTime(serviceProviderRequest.getResponseTime())
                .serviceProviderType(serviceProviderRequest.getServiceProviderType())
                .build();

        ServiceProvider savedServiceProvider = serviceProviderRepository.save(serviceProvider);
        return ServiceProviderResponse.fromEntity(savedServiceProvider);
    }

    @Override
    public ServiceProviderResponse getServiceProviderById(Long id) {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service Provider not found with id: " + id));
        return ServiceProviderResponse.fromEntity(serviceProvider);
    }

    @Override
    public ServiceProviderResponse getServiceProviderByName(String name) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Service Provider not found with name: " + name));
        return ServiceProviderResponse.fromEntity(serviceProvider);
    }

    @Override
    public List<ServiceProviderResponse> getAllServiceProviders() {
        return serviceProviderRepository.findAll().stream()
                .map(ServiceProviderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceProviderResponse> getServiceProvidersByCountry(String country) {
        return serviceProviderRepository.findByCountry(country).stream()
                .map(ServiceProviderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceProviderResponse> getServiceProvidersByAccreditation(ServiceProvider.Accreditation accreditation) {
        return serviceProviderRepository.findByAccreditation(accreditation).stream()
                .map(ServiceProviderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceProviderResponse> getServiceProvidersByServiceType(ServiceProvider.ServiceType serviceType) {
        return serviceProviderRepository.findByServiceType(serviceType).stream()
                .map(ServiceProviderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceProviderResponse> getServiceProvidersByResponseTime(ServiceProvider.ResponseTime responseTime) {
        return serviceProviderRepository.findByResponseTime(responseTime).stream()
                .map(ServiceProviderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceProviderResponse> getServiceProvidersByType(ServiceProvider.ServiceProviderType serviceProviderType) {
        return serviceProviderRepository.findByServiceProviderType(serviceProviderType).stream()
                .map(ServiceProviderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceProviderResponse> getExpiringAccreditations(LocalDate date) {
        return serviceProviderRepository.findExpiringAccreditations(date).stream()
                .map(ServiceProviderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceProviderResponse> searchServiceProviders(String searchTerm) {
        return serviceProviderRepository.searchServiceProviders(searchTerm).stream()
                .map(ServiceProviderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ServiceProviderResponse updateServiceProvider(Long id, ServiceProviderRequest serviceProviderRequest) {
        ServiceProvider existingServiceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service Provider not found with id: " + id));

        // Check if name is being changed and if it conflicts with existing name
        if (!existingServiceProvider.getName().equals(serviceProviderRequest.getName()) &&
            serviceProviderRepository.existsByName(serviceProviderRequest.getName())) {
            throw new IllegalArgumentException("Service Provider with name '" + serviceProviderRequest.getName() + "' already exists");
        }

        existingServiceProvider.setName(serviceProviderRequest.getName());
        existingServiceProvider.setAccreditation(serviceProviderRequest.getAccreditation());
        existingServiceProvider.setAddress(serviceProviderRequest.getAddress());
        existingServiceProvider.setCountry(serviceProviderRequest.getCountry());
        existingServiceProvider.setContactPerson(serviceProviderRequest.getContactPerson());
        existingServiceProvider.setPhoneNumber(serviceProviderRequest.getPhoneNumber());
        existingServiceProvider.setEmail(serviceProviderRequest.getEmail());
        existingServiceProvider.setWebsite(serviceProviderRequest.getWebsite());
        existingServiceProvider.setCertificate(serviceProviderRequest.getCertificate());
        existingServiceProvider.setDescription(serviceProviderRequest.getDescription());
        existingServiceProvider.setServiceType(serviceProviderRequest.getServiceType());
        existingServiceProvider.setAccreditationNumber(serviceProviderRequest.getAccreditationNumber());
        existingServiceProvider.setAccreditationExpiryDate(serviceProviderRequest.getAccreditationExpiryDate());
        existingServiceProvider.setServiceAreas(serviceProviderRequest.getServiceAreas());
        existingServiceProvider.setResponseTime(serviceProviderRequest.getResponseTime());
        existingServiceProvider.setServiceProviderType(serviceProviderRequest.getServiceProviderType());

        ServiceProvider updatedServiceProvider = serviceProviderRepository.save(existingServiceProvider);
        return ServiceProviderResponse.fromEntity(updatedServiceProvider);
    }

    @Override
    public void deleteServiceProvider(Long id) {
        if (!serviceProviderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service Provider not found with id: " + id);
        }
        serviceProviderRepository.deleteById(id);
    }

    @Override
    public boolean isNameUnique(String name) {
        return !serviceProviderRepository.existsByName(name);
    }


}