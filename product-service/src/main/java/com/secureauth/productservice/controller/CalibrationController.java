package com.secureauth.productservice.controller;

import com.secureauth.productservice.dto.*;
import com.secureauth.productservice.entity.CalibrationHistory;
import com.secureauth.productservice.entity.CalibrationLabTechHistory;
import com.secureauth.productservice.entity.CalibrationMedia;
import com.secureauth.productservice.entity.CalibrationSchedule;
import com.secureauth.productservice.entity.Gage;
import com.secureauth.productservice.entity.InhouseCalibrationMachine;
import com.secureauth.productservice.exception.ResourceNotFoundException;
import com.secureauth.productservice.repository.CalibrationHistoryRepository;
import com.secureauth.productservice.repository.CalibrationLabTechHistoryRepository;
import com.secureauth.productservice.repository.CalibrationScheduleRepository;
import com.secureauth.productservice.repository.GageRepository;
import com.secureauth.productservice.repository.InhouseCalibrationMachineRepository;
import com.secureauth.productservice.service.CalibrationHistoryService;
import com.secureauth.productservice.service.CalibrationService;
import com.secureauth.productservice.service.GageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calibration-manager")
public class CalibrationController {

    @Autowired
    private CalibrationService calibrationService;

    @Autowired
    private CalibrationHistoryService calibrationHistoryService;

    @Autowired
    private CalibrationHistoryRepository calibrationHistoryRepository;

    @Autowired
    private CalibrationScheduleRepository calibrationScheduleRepository;

    @Autowired
    private CalibrationLabTechHistoryRepository historyRepository;

    @Autowired
    private GageRepository gageRepository;

    @Autowired
    private InhouseCalibrationMachineRepository machineRepository;

    @Autowired
    private GageService gageService;

    @PostMapping("/gages/{gageId}/records")
    public ResponseEntity<CalibrationHistory> addCalibrationRecord(
            @PathVariable Long gageId,
            @Valid @RequestBody CalibrationHistory calibrationHistory) {
        try {
            CalibrationHistory savedRecord = calibrationService.addCalibrationRecord(gageId, calibrationHistory);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRecord);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/gages/{gageId}/history")
    public ResponseEntity<List<CalibrationHistoryResponse>> getCalibrationHistory(@PathVariable Long gageId) {
        List<CalibrationHistory> histories = calibrationService.getCalibrationHistory(gageId);

        List<CalibrationHistoryResponse> responseList = histories.stream()
                .map(history -> CalibrationHistoryResponse.builder()
                        .id(history.getId())
                        .calibrationDate(history.getCalibrationDate())
                        .nextDueDate(history.getNextDueDate())
                        .status(history.getStatus() != null ? history.getStatus().name() : "UNKNOWN")
                        .notes(history.getNotes())
                        .performedBy(history.getPerformedBy())
                        .createdAt(history.getCreatedAt())
                        .build())
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/history")
    public ResponseEntity<List<CalibrationHistoryResponse>> getAllCalibrationHistory() {
        List<CalibrationHistory> histories = calibrationHistoryRepository.findAll();

        List<CalibrationHistoryResponse> responseList = histories.stream()
                .map(history -> CalibrationHistoryResponse.builder()
                        .id(history.getId())
                        .calibrationDate(history.getCalibrationDate())
                        .nextDueDate(history.getNextDueDate())
                        .status(history.getStatus() != null ? history.getStatus().name() : "UNKNOWN")
                        .notes(history.getNotes())
                        .performedBy(history.getPerformedBy())
                        .createdAt(history.getCreatedAt())
                        .build())
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @PostMapping("/gages/{gageId}/complete")
    public ResponseEntity<GageResponse> completeCalibration(
            @PathVariable Long gageId,
            @RequestBody(required = false) CompleteCalibrationRequest request) {
        try {
            String performedBy = (request != null && request.getPerformedBy() != null)
                    ? request.getPerformedBy()
                    : "System";

            GageResponse updatedGage = calibrationService.completeCalibration(
                    gageId,
                    performedBy,
                    request != null ? request.getNotes() : null,
                    request != null ? request.getCertificate() : null);
            return ResponseEntity.ok(updatedGage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/gages/{gageId}/schedule")
    public ResponseEntity<ScheduleCalibrationResponse> scheduleCalibration(
            @PathVariable Long gageId,
            @Valid @RequestBody ScheduleCalibrationRequest request) {
        try {
            ScheduleCalibrationResponse response = calibrationService.scheduleCalibration(gageId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ScheduleCalibrationResponse.builder()
                            .message("Failed to schedule calibration: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/gages/{gageId}/schedule-enhanced")
    public ResponseEntity<ScheduleCalibrationResponse> scheduleCalibrationEnhanced(
            @PathVariable Long gageId,
            @Valid @RequestBody ScheduleCalibrationRequest request,
            @RequestHeader(value = "User-ID", required = false) Long userId,
            @RequestHeader(value = "User-Email", required = false) String userEmail) {

        try {
            if (userId == null) {
                userId = 1L;
            }
            if (userEmail == null) {
                userEmail = "system@company.com";
            }

            ScheduleCalibrationResponse response = calibrationService.scheduleCalibration(gageId, request, userId,
                    userEmail);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ScheduleCalibrationResponse.builder()
                            .message("Failed to schedule calibration: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/schedules/{id}/serial-number-photo")
    public ResponseEntity<byte[]> getSerialNumberPhoto(@PathVariable Long id) {
        CalibrationSchedule schedule = calibrationScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (schedule.getSerialNumberPhoto() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, schedule.getSerialNumberPhotoContentType())
                .body(schedule.getSerialNumberPhoto());
    }

    @GetMapping("/schedules/{id}/front-view-photo")
    public ResponseEntity<byte[]> getFrontViewPhoto(@PathVariable Long id) {
        CalibrationSchedule schedule = calibrationScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (schedule.getFrontViewPhoto() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, schedule.getFrontViewPhotoContentType())
                .body(schedule.getFrontViewPhoto());
    }

    @GetMapping("/schedules/{id}/back-view-photo")
    public ResponseEntity<byte[]> getBackViewPhoto(@PathVariable Long id) {
        CalibrationSchedule schedule = calibrationScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (schedule.getBackViewPhoto() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, schedule.getBackViewPhotoContentType())
                .body(schedule.getBackViewPhoto());
    }

    @PostMapping("/gages/{gageId}/send")
    public ResponseEntity<String> sendGage(@PathVariable Long gageId) {
        try {
            gageService.updateGageStatus(gageId, Gage.Status.OUT_FOR_CALIBRATION);
            return ResponseEntity.ok("Gage status changed to OUT_FOR_CALIBRATION successfully.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Gage not found with ID: " + gageId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update status: " + e.getMessage());
        }
    }

    @PostMapping(value = "/gages/{gageId}/inward", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> inwardGauge(
            @PathVariable Long gageId,
            @ModelAttribute InwardRequest request) {

        try {
            if (request.getNextDueDate() == null) {
                request.setNextDueDate(LocalDate.now().plusDays(30));
            }

            if (request.getCalibrationDate() == null) {
                request.setCalibrationDate(LocalDate.now());
            }
            if (request.getStatus() == null) {
                request.setStatus(InwardRequest.CalibrationStatus.PASSED);
            }
            if (request.getPerformedBy() == null || request.getPerformedBy().trim().isEmpty()) {
                request.setPerformedBy("System");
            }
            if (request.getNotes() == null || request.getNotes().trim().isEmpty()) {
                request.setNotes("Gage received back from calibration with media documentation.");
            }

            CalibrationHistory history = calibrationHistoryService.processInward(gageId, request);

            int totalMedia = getTotalMediaCount(request);
            String message = "✅ Gauge inward processed successfully and status set to ACTIVE." +
                    (totalMedia > 0 ? " " + totalMedia + " media files attached." : "");

            return ResponseEntity.ok(new ApiResponse("success", message, history.getId()));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("error", "❌ Gage not found with ID: " + gageId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("error", "❌ Failed to process inward: " + e.getMessage()));
        }
    }

    @GetMapping("/history/{historyId}/media")
    public ResponseEntity<List<MediaResponse>> getMediaByHistory(@PathVariable Long historyId) {
        List<CalibrationMedia> mediaList = calibrationHistoryService.getMediaByHistoryId(historyId);

        List<MediaResponse> response = mediaList.stream()
                .map(media -> new MediaResponse(
                        media.getId(),
                        media.getFileName(),
                        "/api/calibration-manager/media/" + media.getId() + "/download",
                        media.getMimeType(),
                        media.getFileType()))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/media/{mediaId}/download")
    public ResponseEntity<byte[]> downloadMedia(@PathVariable Long mediaId) {
        CalibrationMedia media = calibrationHistoryService.getMediaById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id " + mediaId));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + media.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(media.getMimeType()))
                .body(media.getFileData());
    }

    @GetMapping("/gages/{gageId}/schedules")
    public ResponseEntity<List<ScheduleCalibrationResponse>> getGageSchedules(@PathVariable Long gageId) {
        try {
            List<ScheduleCalibrationResponse> schedules = calibrationService.getGageSchedules(gageId);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/schedules/upcoming")
    public ResponseEntity<List<ScheduleCalibrationResponse>> getUpcomingSchedules() {
        try {
            List<ScheduleCalibrationResponse> schedules = calibrationService.getUpcomingSchedules();
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/schedules/{scheduleId}/status")
    public ResponseEntity<ScheduleCalibrationResponse> updateScheduleStatus(
            @PathVariable Long scheduleId,
            @RequestParam CalibrationSchedule.ScheduleStatus status) {
        try {
            ScheduleCalibrationResponse response = calibrationService.updateScheduleStatus(scheduleId, status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // CREATE
    // @PostMapping("/lab-calibration-history")
    // public ResponseEntity<CalibrationLabTechHistory> create(
    // @RequestBody CalibrationLabTechHistory request) {

    // Gage gage = gageRepository.findById(request.getGage().getId())
    // .orElseThrow(() -> new RuntimeException("Gage not found"));

    // request.setGage(gage);

    // if (request.getCalibrationMachine() != null &&
    // request.getCalibrationMachine().getId() != null) {

    // InhouseCalibrationMachine machine =
    // machineRepository.findById(request.getCalibrationMachine().getId())
    // .orElseThrow(() -> new RuntimeException("Machine not found"));

    // request.setCalibrationMachine(machine);
    // }

    // return ResponseEntity.ok(historyRepository.save(request));
    // }

    @PostMapping("/lab-calibration-history")
    public ResponseEntity<ApiResponse> create(
            @RequestBody CalibrationLabTechHistory request) {

        Gage gage = gageRepository.findById(request.getGage().getId())
                .orElseThrow(() -> new RuntimeException("Gage not found"));
        request.setGage(gage);

        if (request.getCalibrationMachine() != null &&
                request.getCalibrationMachine().getId() != null) {

            InhouseCalibrationMachine machine = machineRepository.findById(request.getCalibrationMachine().getId())
                    .orElseThrow(() -> new RuntimeException("Machine not found"));
            request.setCalibrationMachine(machine);
        }

        CalibrationLabTechHistory saved = historyRepository.save(request);

        return ResponseEntity.ok(
                new ApiResponse(
                        "SUCCESS",
                        "Calibration history created successfully",
                        toDto(saved)));
    }

    // GET BY ID
    // @GetMapping("/lab-calibration-history/{id}")
    // public ResponseEntity<CalibrationLabTechHistory> getById(@PathVariable Long
    // id) {
    // return ResponseEntity.ok(
    // historyRepository.findById(id)
    // .orElseThrow(() -> new RuntimeException("History not found")));
    // }

    @GetMapping("/lab-calibration-history/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable Long id) {

        CalibrationLabTechHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("History not found"));

        return ResponseEntity.ok(
                new ApiResponse(
                        "SUCCESS",
                        "Calibration history fetched successfully",
                        toDto(history)));
    }

    // GET BY GAGE
    // @GetMapping("/lab-calibration-history/gage/{gageId}")
    // public ResponseEntity<List<CalibrationLabTechHistory>> getByGage(
    // @PathVariable Long gageId) {
    // return ResponseEntity.ok(historyRepository.findByGage_Id(gageId));
    // }

    @GetMapping("/lab-calibration-history/gage/{gageId}")
    public ResponseEntity<ApiResponse> getByGage(@PathVariable Long gageId) {

        List<CalibrationLabTechHistoryResponseDto> data = historyRepository.findByGage_Id(gageId)
                .stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse(
                        "SUCCESS",
                        "Calibration history list fetched",
                        data));
    }

    // GET ALL
    @GetMapping("/lab-calibration-history")
    public ResponseEntity<ApiResponse> getAll() {

        List<CalibrationLabTechHistoryResponseDto> data = historyRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse(
                        "SUCCESS",
                        "All calibration history fetched",
                        data));
    }

    // DELETE
    @DeleteMapping("/lab-calibration-history/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {

        historyRepository.deleteById(id);

        return ResponseEntity.ok(
                new ApiResponse(
                        "SUCCESS",
                        "Calibration history deleted successfully"));
    }

    private int getTotalMediaCount(InwardRequest request) {
        int count = 0;
        if (request.getDocuments() != null)
            count += request.getDocuments().size();
        if (request.getImages() != null)
            count += request.getImages().size();
        if (request.getVideos() != null)
            count += request.getVideos().size();
        return count;
    }

    public static class ApiResponse {
        private String status;
        private String message;
        private Object data;

        public ApiResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public ApiResponse(String status, String message, Object data) {
            this.status = status;
            this.message = message;
            this.data = data;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }
    }

    private CalibrationLabTechHistoryResponseDto toDto(CalibrationLabTechHistory entity) {

        return CalibrationLabTechHistoryResponseDto.builder()
                .id(entity.getId())

                // Gage
                .gageId(entity.getGage().getId())

                // Calibration details
                .technician(entity.getTechnician())
                .calibrationDate(entity.getCalibrationDate())
                .nextCalibrationDate(entity.getNextCalibrationDate())
                .result(entity.getResult())
                .remarks(entity.getRemarks())
                .calibratedBy(entity.getCalibratedBy())
                .certificateNumber(entity.getCertificateNumber())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .calibrationDuration(entity.getCalibrationDuration())

                // Machine (nullable)
                .machineId(
                        entity.getCalibrationMachine() != null
                                ? entity.getCalibrationMachine().getId()
                                : null)
                .machineName(
                        entity.getCalibrationMachine() != null
                                ? entity.getCalibrationMachine().getMachineName()
                                : null)

                // Audit
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
