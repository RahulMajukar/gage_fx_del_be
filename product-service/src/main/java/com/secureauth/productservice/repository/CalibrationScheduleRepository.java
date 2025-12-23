package com.secureauth.productservice.repository;

import com.secureauth.productservice.entity.CalibrationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalibrationScheduleRepository extends JpaRepository<CalibrationSchedule, Long> {

    List<CalibrationSchedule> findByGageId(Long gageId);

    List<CalibrationSchedule> findByStatus(CalibrationSchedule.ScheduleStatus status);

    List<CalibrationSchedule> findByScheduledDateBetween(LocalDate startDate, LocalDate endDate);

    List<CalibrationSchedule> findByAssignedTo(String assignedTo);

    List<CalibrationSchedule> findByLaboratory(String laboratory);
}