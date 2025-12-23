package com.secureauth.productservice.service;

import com.secureauth.productservice.dto.GageResponse;
import com.secureauth.productservice.dto.ScheduleCalibrationRequest;
import com.secureauth.productservice.dto.ScheduleCalibrationResponse;
import com.secureauth.productservice.entity.CalibrationHistory;
import com.secureauth.productservice.entity.CalibrationSchedule;

import java.util.List;

public interface CalibrationService {

    CalibrationHistory addCalibrationRecord(Long gageId, CalibrationHistory calibrationHistory);

    List<CalibrationHistory> getCalibrationHistory(Long gageId);

    GageResponse completeCalibration(Long gageId, String performedBy, String notes, String certificate);

    ScheduleCalibrationResponse scheduleCalibration(Long gageId, ScheduleCalibrationRequest request);

    ScheduleCalibrationResponse scheduleCalibration(Long gageId, ScheduleCalibrationRequest request, Long userId, String userEmail);

    List<ScheduleCalibrationResponse> getGageSchedules(Long gageId);

    List<ScheduleCalibrationResponse> getUpcomingSchedules();

    ScheduleCalibrationResponse updateScheduleStatus(Long scheduleId, CalibrationSchedule.ScheduleStatus status);
}

