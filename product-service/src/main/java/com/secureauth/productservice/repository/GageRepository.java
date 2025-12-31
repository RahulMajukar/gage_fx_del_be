package com.secureauth.productservice.repository;

import com.secureauth.productservice.entity.Gage;
import com.secureauth.productservice.entity.GageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GageRepository extends JpaRepository<Gage, Long> {

    Optional<Gage> findBySerialNumber(String serialNumber);

    List<Gage> findByGageType(GageType gageType);

    List<Gage> findByGageSubTypeId(Long gageSubTypeId);

    List<Gage> findByLocation(Gage.Location location);

    List<Gage> findByCriticality(Gage.Criticality criticality);

    List<Gage> findByStatus(Gage.Status status);

    @Query("SELECT g FROM Gage g WHERE g.serialNumber LIKE %:searchTerm% OR g.modelNumber LIKE %:searchTerm%")
    List<Gage> searchGages(@Param("searchTerm") String searchTerm);

    boolean existsBySerialNumber(String serialNumber);

    // Additional methods needed by the service
    List<Gage> findByGageTypeId(Long gageTypeId);

    List<Gage> findByGageTypeName(String gageTypeName);

    // Methods for manufacturer usage check (from second file)
    boolean existsByManufacturerId(Long manufacturerId);

    long countByManufacturerId(Long manufacturerId);

    List<Gage> findByManufacturerId(Long manufacturerId);

    // Find gages mapped to an inhouse calibration machine
    List<Gage> findByInhouseCalibrationMachineId(Long inhouseCalibrationMachineId);
}