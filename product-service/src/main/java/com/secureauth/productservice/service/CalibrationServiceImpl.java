package com.secureauth.productservice.service;

import com.secureauth.productservice.client.EventClient;
import com.secureauth.productservice.client.dto.CreateEventRequest;
import com.secureauth.productservice.client.dto.EventDTO;
import com.secureauth.productservice.controller.MailController;
import com.secureauth.productservice.dto.GageResponse;
import com.secureauth.productservice.dto.ScheduleCalibrationRequest;
import com.secureauth.productservice.dto.ScheduleCalibrationResponse;
import com.secureauth.productservice.entity.CalibrationHistory;
import com.secureauth.productservice.entity.CalibrationSchedule;
import com.secureauth.productservice.entity.Gage;
import com.secureauth.productservice.exception.ResourceNotFoundException;
import com.secureauth.productservice.repository.CalibrationHistoryRepository;
import com.secureauth.productservice.repository.CalibrationScheduleRepository;
import com.secureauth.productservice.repository.GageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CalibrationServiceImpl implements CalibrationService {

    @Autowired
    private GageRepository gageRepository;

    @Autowired
    private CalibrationHistoryRepository calibrationHistoryRepository;

    @Autowired
    private CalibrationScheduleRepository calibrationScheduleRepository;

    @Autowired
    private MailController emailService;

    @Autowired
    private EventClient eventClient;

    @Autowired
    private GageService gageService;

    @Override
    public CalibrationHistory addCalibrationRecord(Long gageId, CalibrationHistory calibrationHistory) {
        Gage gage = gageRepository.findById(gageId)
                .orElseThrow(() -> new ResourceNotFoundException("Gage not found with id: " + gageId));
        calibrationHistory.setGage(gage);
        return calibrationHistoryRepository.save(calibrationHistory);
    }

    @Override
    public List<CalibrationHistory> getCalibrationHistory(Long gageId) {
        return calibrationHistoryRepository.findByGageId(gageId);
    }

    @Override
    public GageResponse completeCalibration(Long gageId, String performedBy, String notes, String certificate) {
        Gage gage = gageRepository.findById(gageId)
                .orElseThrow(() -> new RuntimeException("Gage not found with id: " + gageId));

        String certToStore = certificate;
        if (certToStore == null || certToStore.isBlank()) {
            certToStore = Base64.getEncoder().encodeToString(
                    ("Calibration Certificate for Gage SN: " + gage.getSerialNumber()
                            + " on " + LocalDate.now()).getBytes(StandardCharsets.UTF_8));
        }

        CalibrationHistory history = CalibrationHistory.builder()
                .gage(gage)
                .calibrationDate(LocalDate.now())
                .nextDueDate(gage.getNextCalibrationDate())
                .status(CalibrationHistory.CalibrationStatus.PASSED)
                .notes(notes != null ? notes : "Calibration completed via API")
                .performedBy(performedBy)
                .certificate(certToStore.getBytes())
                .build();

        calibrationHistoryRepository.save(history);

        if (gage.getCalibrationInterval() != null && gage.getCalibrationInterval() > 0) {
            gage.setNextCalibrationDate(LocalDate.now().plusDays(gage.getCalibrationInterval()));
        } else {
            gage.setNextCalibrationDate(LocalDate.now().plusYears(1));
        }

        gage.setRemainingDays((int) ChronoUnit.DAYS.between(LocalDate.now(), gage.getNextCalibrationDate()));
        gage.setStatus(Gage.Status.ACTIVE);
        gageRepository.save(gage);

        return gageService.getGageById(gageId);
    }

    @Override
    public ScheduleCalibrationResponse scheduleCalibration(Long gageId, ScheduleCalibrationRequest request) {
        return scheduleCalibration(gageId, request, null, null);
    }

    @Override
    public ScheduleCalibrationResponse scheduleCalibration(Long gageId,
                                                           ScheduleCalibrationRequest request,
                                                           Long userId,
                                                           String userEmail) {
        Gage gage = gageRepository.findById(gageId)
                .orElseThrow(() -> new ResourceNotFoundException("Gage not found with id: " + gageId));

        CalibrationSchedule schedule = CalibrationSchedule.builder()
                .gage(gage)
                .scheduledDate(request.getScheduledDate())
                .scheduledTime(request.getScheduledTime() != null ? request.getScheduledTime() : LocalTime.of(9, 0))
                .priority(mapPriority(request.getPriority()))
                .assignedTo(request.getAssignedTo())
                .laboratory(request.getLaboratory())
                .estimatedDuration(request.getEstimatedDuration() != null ? request.getEstimatedDuration() : 2)
                .notes(request.getNotes())
                .requiresSpecialEquipment(request.getRequiresSpecialEquipment())
                .specialEquipment(request.getSpecialEquipment())
                .status(CalibrationSchedule.ScheduleStatus.SCHEDULED)
                .emailSent(false)
                .serialNumberPhoto(decode(request.getSerialNumberPhoto()))
                .serialNumberPhotoContentType(request.getSerialNumberPhotoContentType())
                .frontViewPhoto(decode(request.getFrontViewPhoto()))
                .frontViewPhotoContentType(request.getFrontViewPhotoContentType())
                .backViewPhoto(decode(request.getBackViewPhoto()))
                .backViewPhotoContentType(request.getBackViewPhotoContentType())
                .build();

        CalibrationSchedule savedSchedule = calibrationScheduleRepository.save(schedule);

        gage.setStatus(Gage.Status.SCHEDULED);
        gageRepository.save(gage);

        try {
            CreateEventRequest eventReq = new CreateEventRequest();
            eventReq.setTitle("Calibration Scheduled for " + gage.getSerialNumber());
            eventReq.setDescription(
                    "Calibration scheduled on " + request.getScheduledDate()
                            + " at " + schedule.getScheduledTime());

            LocalDateTime start = LocalDateTime.of(request.getScheduledDate(), schedule.getScheduledTime());
            eventReq.setStart(start);
            eventReq.setEnd(start.plusHours(2));
            eventReq.setCategory("WORK");
            eventReq.setPriority("MEDIUM");

            EventDTO eventCreated = eventClient.createEvent(
                    eventReq,
                    userId,
                    userEmail != null ? userEmail : "system@company.com"
            );

            System.out.println("🎉 Event successfully created with ID: " + eventCreated.getId());

        } catch (Exception e) {
            System.err.println("⚠️ Failed to create event: " + e.getMessage());
        }

        if (Boolean.TRUE.equals(request.getEmailEnabled()) && request.getEmailTo() != null) {
            try {
                sendScheduleEmail(gage, savedSchedule, request);
                savedSchedule.setEmailSent(true);
                savedSchedule.setEmailSentAt(LocalDateTime.now());
                calibrationScheduleRepository.save(savedSchedule);
            } catch (Exception e) {
                System.err.println("❌ Failed to send email: " + e.getMessage());
            }
        }

        return mapToScheduleResponse(savedSchedule, gage);
    }

    @Override
    public List<ScheduleCalibrationResponse> getGageSchedules(Long gageId) {
        List<CalibrationSchedule> schedules = calibrationScheduleRepository.findByGageId(gageId);
        return schedules.stream()
                .map(schedule -> mapToScheduleResponse(schedule, schedule.getGage()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleCalibrationResponse> getUpcomingSchedules() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);
        List<CalibrationSchedule> schedules = calibrationScheduleRepository
                .findByScheduledDateBetween(today, thirtyDaysLater);
        return schedules.stream()
                .filter(schedule -> schedule.getStatus() == CalibrationSchedule.ScheduleStatus.SCHEDULED)
                .map(schedule -> mapToScheduleResponse(schedule, schedule.getGage()))
                .collect(Collectors.toList());
    }

    @Override
    public ScheduleCalibrationResponse updateScheduleStatus(Long scheduleId, CalibrationSchedule.ScheduleStatus status) {
        CalibrationSchedule schedule = calibrationScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));
        schedule.setStatus(status);
        CalibrationSchedule updatedSchedule = calibrationScheduleRepository.save(schedule);
        return mapToScheduleResponse(updatedSchedule, updatedSchedule.getGage());
    }

    private CalibrationSchedule.Priority mapPriority(String priority) {
        if (priority == null) {
            return CalibrationSchedule.Priority.MEDIUM;
        }
        return switch (priority.toLowerCase()) {
            case "high" -> CalibrationSchedule.Priority.HIGH;
            case "low" -> CalibrationSchedule.Priority.LOW;
            default -> CalibrationSchedule.Priority.MEDIUM;
        };
    }

    private byte[] decode(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        return Base64.getDecoder().decode(base64);
    }

    private void sendScheduleEmail(Gage gage, CalibrationSchedule schedule, ScheduleCalibrationRequest request) {
        try {
            String subject = request.getEmailSubject() != null
                    ? request.getEmailSubject()
                    : "Calibration Scheduled - " + gage.getSerialNumber();
            String htmlContent = buildScheduleEmailContent(gage, schedule, request);
            emailService.sendHtmlEmail(request.getEmailTo(), subject, htmlContent);
            if (request.getEmailCC() != null && !request.getEmailCC().isEmpty()) {
                for (String cc : request.getEmailCC()) {
                    if (cc != null && !cc.trim().isEmpty()) {
                        emailService.sendHtmlEmail(cc, subject, htmlContent);
                    }
                }
            }
            System.out.println("✅ Schedule email sent successfully to: " + request.getEmailTo());
        } catch (Exception e) {
            System.err.println("❌ Error sending schedule email: " + e.getMessage());
            throw new RuntimeException("Failed to send email notification", e);
        }
    }

    private String buildScheduleEmailContent(Gage gage, CalibrationSchedule schedule,
                                             ScheduleCalibrationRequest request) {
        String customMessage = request.getEmailMessage();
        if (customMessage != null && !customMessage.isBlank()) {
            return formatEmailAsHtml(customMessage);
        }

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append(
                "<html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Calibration Scheduled</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; background-color: #f4f6f8; margin: 0; padding: 0; }");
        html.append(
                ".email-container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 6px rgba(0,0,0,0.1); overflow: hidden; }");
        html.append(
                ".header { background-color: #007b55; color: white; padding: 15px 20px; font-size: 18px; font-weight: bold; display: flex; align items: center; justify-content: space-between; }"
                        .replace("align items", "align-items"));
        html.append(
                ".status { background-color: #e6f9ee; color: #1b7a38; font-weight: bold; padding: 8px 16px; border-radius: 4px; text-align: center; width: fit-content; margin: 20px auto 0 auto; }");
        html.append(".content { padding: 20px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 15px; }");
        html.append("th, td { text-align: left; padding: 10px; border-bottom: 1px solid #eee; }");
        html.append("th { background-color: #f9fafb; color: #333; width: 35%; }");
        html.append(
                ".notes { background-color: #f5f9ff; border-left: 4px solid #007bff; padding: 12px; margin-top: 10px; font-style: italic; }");
        html.append(
                ".footer { background-color: #fafafa; text-align: center; padding: 15px; font-size: 13px; color: #555; }");
        html.append("</style></head><body>");
        html.append("<div class='email-container'>");
        html.append("<div class='header'>");
        html.append("🔧 GageFX Calibration Scheduled");
        html.append("</div>");
        html.append("<div class='status'>✅ SCHEDULED</div>");
        html.append("<div class='content'>");
        html.append("<p>Dear <strong>").append(schedule.getAssignedTo()).append("</strong>,</p>");
        html.append("<p>A calibration has been scheduled for the following gage:</p>");
        html.append("<table>");
        html.append("<tr><th>Gage ID</th><td>").append(gage.getSerialNumber()).append("</td></tr>");
        html.append("<tr><th>Model</th><td>").append(gage.getModelNumber() != null ? gage.getModelNumber() : "N/A")
                .append("</td></tr>");
        html.append("<tr><th>Scheduled Date</th><td>").append(schedule.getScheduledDate()).append("</td></tr>");
        html.append("<tr><th>Time</th><td>").append(schedule.getScheduledTime()).append("</td></tr>");
        html.append("<tr><th>Laboratory</th><td>").append(schedule.getLaboratory()).append("</td></tr>");
        html.append("<tr><th>Estimated Duration</th><td>").append(schedule.getEstimatedDuration())
                .append(" hours</td></tr>");
        html.append("<tr><th>Priority</th><td><strong style='color:#d32f2f;'>").append(schedule.getPriority())
                .append("</strong></td></tr>");
        html.append("</table>");
        if (schedule.getNotes() != null && !schedule.getNotes().isBlank()) {
            html.append("<div class='notes'><strong>Additional Notes:</strong> ").append(schedule.getNotes())
                    .append("</div>");
        }
        html.append(
                "<p style='margin-top:20px;'>Please ensure you have the necessary equipment and documentation ready for the scheduled calibration.</p>");
        html.append("</div>");
        html.append("<div class='footer'>");
        html.append("<p>Thank you for your attention to this matter.<br>");
        html.append("Best regards,<br><strong>GageFX System</strong></p>");
        html.append(
                "<p style='color:#999; font-size:12px;'>This is an automated notification. Do not reply directly.</p>");
        html.append("</div>");
        html.append("</div></body></html>");
        return html.toString();
    }

    private String formatEmailAsHtml(String plainText) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; padding: 20px; }");
        html.append("</style></head><body>");
        html.append("<pre style='white-space: pre-wrap; font-family: Arial, sans-serif;'>");
        html.append(plainText.replace("<", "<").replace(">", ">"));
        html.append("</pre>");
        html.append("</body></html>");
        return html.toString();
    }

    private ScheduleCalibrationResponse mapToScheduleResponse(CalibrationSchedule schedule, Gage gage) {
        return ScheduleCalibrationResponse.builder()
                .id(schedule.getId())
                .gageId(gage.getId())
                .serialNumber(gage.getSerialNumber())
                .gageName(gage.getGageType() != null ? gage.getGageType().getName() : "Unknown")
                .scheduledDate(schedule.getScheduledDate())
                .scheduledTime(schedule.getScheduledTime())
                .priority(schedule.getPriority() != null ? schedule.getPriority().name() : "MEDIUM")
                .assignedTo(schedule.getAssignedTo())
                .laboratory(schedule.getLaboratory())
                .estimatedDuration(schedule.getEstimatedDuration())
                .notes(schedule.getNotes())
                .status(schedule.getStatus() != null ? schedule.getStatus().name() : "SCHEDULED")
                .emailSent(schedule.getEmailSent())
                .createdAt(schedule.getCreatedAt())
                .message("Calibration scheduled successfully")
                .build();
    }
}

