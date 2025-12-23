package com.secureauth.productservice.repository;

import com.secureauth.productservice.entity.CalibrationHistory;
import com.secureauth.productservice.entity.Gage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalibrationHistoryRepository extends JpaRepository<CalibrationHistory, Long> {
    
    List<CalibrationHistory> findByGageOrderByCalibrationDateDesc(Gage gage);
    
    List<CalibrationHistory> findByGageIdOrderByCalibrationDateDesc(Long gageId);
    
    @Query("SELECT ch FROM CalibrationHistory ch WHERE ch.calibrationDate BETWEEN :startDate AND :endDate")
    List<CalibrationHistory> findByCalibrationDateBetween(@Param("startDate") LocalDate startDate, 
                                                         @Param("endDate") LocalDate endDate);
    
    @Query("SELECT ch FROM CalibrationHistory ch WHERE ch.gage.id = :gageId AND ch.calibrationDate BETWEEN :startDate AND :endDate")
    List<CalibrationHistory> findByGageIdAndCalibrationDateBetween(@Param("gageId") Long gageId,
                                                                  @Param("startDate") LocalDate startDate,
                                                                  @Param("endDate") LocalDate endDate);
    
    List<CalibrationHistory> findByStatus(CalibrationHistory.CalibrationStatus status);
    
    List<CalibrationHistory> findByPerformedBy(String performedBy);
    
    List<CalibrationHistory> findByGageId(Long gageId);
} 