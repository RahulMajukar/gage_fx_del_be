package com.secureauth.productservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DailySchedulerService {

    @Autowired
    private GageService gageService;

    /**
     * Scheduled task that runs daily at 12:00 AM (midnight)
     * Updates the remaining days count for all gages
     */
    @Scheduled(cron = "0 0 0 * * ?") // Runs at 00:00:00 every day
    public void updateGagesRemainingDaysDaily() {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("🕛 Daily scheduler triggered at: " + currentTime);
        
        try {
            gageService.updateAllGagesRemainingDays();
            System.out.println("✅ Daily gage update completed successfully at: " + currentTime);
        } catch (Exception e) {
            System.err.println("❌ Error during daily gage update at " + currentTime + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Manual trigger method for testing purposes
     * Can be called via REST endpoint if needed
     */
    public void manualUpdateGagesRemainingDays() {
        System.out.println("🔧 Manual trigger for gage remaining days update");
        gageService.updateAllGagesRemainingDays();
    }
}
