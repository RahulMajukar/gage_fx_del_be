package com.secureauth.productservice.service;

import com.secureauth.productservice.service.ReallocateService;
import com.secureauth.productservice.service.ReallocateNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReallocateSchedulerService {

    private final ReallocateService reallocateService;
    private final ReallocateNotificationService notificationService;

    /**
     * Process expired reallocations every 30 minutes
     * This will automatically return gages that have exceeded their time limit
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes in milliseconds
    public void processExpiredReallocations() {
        try {
            log.info("Starting scheduled processing of expired reallocations");
            reallocateService.processAllExpiredReallocations();
            log.info("Completed scheduled processing of expired reallocations");
        } catch (Exception e) {
            log.error("Error in scheduled processing of expired reallocations", e);
        }
    }

    /**
     * Send expiration notifications every hour
     * This will notify users about gages that are expiring soon
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void sendExpirationNotifications() {
        try {
            log.info("Starting scheduled sending of expiration notifications");
            notificationService.sendDailySummary();
            log.info("Completed scheduled sending of expiration notifications");
        } catch (Exception e) {
            log.error("Error in scheduled sending of expiration notifications", e);
        }
    }

    /**
     * Process expired reallocations every 5 minutes during business hours
     * This provides more frequent processing during active hours
     */
    @Scheduled(cron = "0 */5 * * * MON-FRI") // Every 5 minutes, Monday to Friday
    public void processExpiredReallocationsBusinessHours() {
        try {
            log.info("Starting business hours processing of expired reallocations");
            reallocateService.processAllExpiredReallocations();
            log.info("Completed business hours processing of expired reallocations");
        } catch (Exception e) {
            log.error("Error in business hours processing of expired reallocations", e);
        }
    }

    /**
     * Send urgent notifications every 15 minutes
     * This will send notifications for gages expiring within the next hour
     */
    @Scheduled(fixedRate = 900000) // 15 minutes in milliseconds
    public void sendUrgentExpirationNotifications() {
        try {
            log.info("Starting urgent expiration notifications");
            // Send notifications for gages expiring soon
            reallocateService.getReallocatesExpiringSoon().forEach(notificationService::sendExpirationNotification);
            log.info("Completed urgent expiration notifications");
        } catch (Exception e) {
            log.error("Error in urgent expiration notifications", e);
        }
    }
}
