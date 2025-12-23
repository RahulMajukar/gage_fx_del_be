package com.secureauth.productservice.repository;

import com.secureauth.productservice.entity.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {

    Optional<Manufacturer> findByName(String name);
    
    List<Manufacturer> findByCountry(String country);
    
    List<Manufacturer> findByManufacturerType(Manufacturer.ManufacturerType manufacturerType);
    
    @Query("SELECT m FROM Manufacturer m WHERE m.name LIKE %:searchTerm% OR m.country LIKE %:searchTerm% OR m.contactPerson LIKE %:searchTerm%")
    List<Manufacturer> searchManufacturers(@Param("searchTerm") String searchTerm);
    
    boolean existsByName(String name);
    
    @Query("SELECT m FROM Manufacturer m JOIN m.gages g WHERE g.id = :gageId")
    List<Manufacturer> findByGageId(@Param("gageId") Long gageId);
} 