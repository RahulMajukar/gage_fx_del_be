package com.secureauth.productservice.service;

import com.secureauth.productservice.dto.InwardRequest;
import com.secureauth.productservice.entity.CalibrationHistory;
import com.secureauth.productservice.entity.CalibrationMedia;
import com.secureauth.productservice.entity.Gage;
import com.secureauth.productservice.repository.CalibrationHistoryRepository;
import com.secureauth.productservice.repository.CalibrationMediaRepository;
import com.secureauth.productservice.repository.GageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class CalibrationHistoryService {

    private final CalibrationHistoryRepository historyRepository;
    private final CalibrationMediaRepository mediaRepository;
    private final GageRepository gageRepository;

    public CalibrationHistory processInward(Long gageId, InwardRequest request) {
        // 1. Update gage status to ACTIVE
        Gage gage = gageRepository.findById(gageId)
                .orElseThrow(() -> new RuntimeException("Gage not found with ID: " + gageId));

        gage.setStatus(Gage.Status.ACTIVE);
        gageRepository.save(gage);

        // 2. Create calibration history record
        // CalibrationHistory history = CalibrationHistory.builder()
        // .gage(gage)
        // .calibrationDate(request.getCalibrationDate() != null ?
        // request.getCalibrationDate() : LocalDate.now())
        // .nextDueDate(request.getNextDueDate())
        // .status(CalibrationHistory.CalibrationStatus.valueOf(request.getStatus().name()))
        // .notes(request.getNotes() != null ? request.getNotes() : "Gauge received back
        // from calibration.")
        // .performedBy(request.getPerformedBy() != null ? request.getPerformedBy() :
        // "System")
        // .build();
        CalibrationHistory history = CalibrationHistory.builder()
                .gage(gage)
                .calibrationDate(
                        request.getCalibrationDate() != null
                                ? request.getCalibrationDate()
                                : LocalDate.now())
                .nextDueDate(
                        request.getNextDueDate() != null
                                ? request.getNextDueDate()
                                : LocalDate.now().plusMonths(6))
                .status(
                        request.getStatus() != null
                                ? CalibrationHistory.CalibrationStatus.valueOf(request.getStatus().name())
                                : CalibrationHistory.CalibrationStatus.PASSED)
                .notes(
                        request.getNotes() != null && !request.getNotes().isEmpty()
                                ? request.getNotes()
                                : "Gauge received back from calibration.")
                .performedBy(
                        request.getPerformedBy() != null && !request.getPerformedBy().isEmpty()
                                ? request.getPerformedBy()
                                : getCurrentUsername())
                .build();

        CalibrationHistory savedHistory = historyRepository.save(history);

        // 3. Process and save media files
        processMediaFiles(savedHistory, request);

        // 4. Generate and store PDF certificate
        byte[] certificatePdf = generateCertificate(savedHistory, request);
        savedHistory.setCertificate(certificatePdf);

        return historyRepository.save(savedHistory);
    }

    // Helper method
    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "System";
        } catch (Exception e) {
            return "System";
        }
    }

    public Optional<CalibrationMedia> getMediaById(Long mediaId) {
        return mediaRepository.findById(mediaId);
    }

    private void processMediaFiles(CalibrationHistory history, InwardRequest request) {
        List<CalibrationMedia> mediaList = new ArrayList<>();

        // Process documents
        if (request.getDocuments() != null) {
            for (MultipartFile file : request.getDocuments()) {
                if (!file.isEmpty()) {
                    mediaList.add(createMediaEntity(history, file, "document"));
                }
            }
        }

        // Process images
        if (request.getImages() != null) {
            for (MultipartFile file : request.getImages()) {
                if (!file.isEmpty()) {
                    mediaList.add(createMediaEntity(history, file, "image"));
                }
            }
        }

        // Process videos
        if (request.getVideos() != null) {
            for (MultipartFile file : request.getVideos()) {
                if (!file.isEmpty()) {
                    mediaList.add(createMediaEntity(history, file, "video"));
                }
            }
        }

        if (!mediaList.isEmpty()) {
            mediaRepository.saveAll(mediaList);
        }
    }

    private CalibrationMedia createMediaEntity(CalibrationHistory history, MultipartFile file, String fileType) {
        try {
            return CalibrationMedia.builder()
                    .calibrationHistory(history)
                    .fileName(file.getOriginalFilename())
                    .fileType(fileType)
                    .mimeType(file.getContentType())
                    .fileData(file.getBytes())
                    .fileSize(file.getSize())
                    .description("") // Can be updated later
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to process file: " + file.getOriginalFilename(), e);
        }
    }

    private byte[] generateCertificate(CalibrationHistory history, InwardRequest request) {
        // Implement PDF generation logic here
        try {
            String certificateContent = generateCertificateContent(history, request);
            return certificateContent.getBytes(); // Convert to actual PDF in production
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate certificate", e);
        }
    }

    private String generateCertificateContent(CalibrationHistory history, InwardRequest request) {
        // Generate certificate content - implement your actual PDF generation logic
        return String.format(
                "Calibration Certificate\nInstrument: %s\nSerial: %s\nCalibration Date: %s\nNext Due: %s\nStatus: %s",
                history.getGage().getSerialNumber(),
                history.getGage().getSerialNumber(),
                history.getCalibrationDate(),
                history.getNextDueDate(),
                history.getStatus());
    }

    public List<CalibrationMedia> getMediaByHistoryId(Long historyId) {
        return mediaRepository.findByCalibrationHistoryId(historyId);
    }
}