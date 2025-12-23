package com.secureauth.productservice.repository;

import com.secureauth.productservice.entity.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {

    Optional<ServiceProvider> findByName(String name);
    
    List<ServiceProvider> findByCountry(String country);
    
    List<ServiceProvider> findByAccreditation(ServiceProvider.Accreditation accreditation);
    
    List<ServiceProvider> findByServiceType(ServiceProvider.ServiceType serviceType);
    
    List<ServiceProvider> findByResponseTime(ServiceProvider.ResponseTime responseTime);

    List<ServiceProvider> findByServiceProviderType(ServiceProvider.ServiceProviderType serviceProviderType);

    @Query("SELECT sp FROM ServiceProvider sp WHERE sp.accreditationExpiryDate <= :date")
    List<ServiceProvider> findExpiringAccreditations(@Param("date") LocalDate date);
    
    @Query("SELECT sp FROM ServiceProvider sp WHERE sp.name LIKE %:searchTerm% OR sp.country LIKE %:searchTerm% OR sp.contactPerson LIKE %:searchTerm%")
    List<ServiceProvider> searchServiceProviders(@Param("searchTerm") String searchTerm);
    
    boolean existsByName(String name);
    


} 