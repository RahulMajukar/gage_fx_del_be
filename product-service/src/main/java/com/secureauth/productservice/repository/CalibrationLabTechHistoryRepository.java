package com.secureauth.productservice.repository;

import com.secureauth.productservice.entity.CalibrationLabTechHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalibrationLabTechHistoryRepository extends JpaRepository<CalibrationLabTechHistory, Long> {

    List<CalibrationLabTechHistory> findByGage_Id(Long gageId);
}
