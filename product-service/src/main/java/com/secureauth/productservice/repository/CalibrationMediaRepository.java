package com.secureauth.productservice.repository;

import com.secureauth.productservice.entity.CalibrationMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalibrationMediaRepository extends JpaRepository<CalibrationMedia, Long> {
    List<CalibrationMedia> findByCalibrationHistoryId(Long calibrationHistoryId);
}