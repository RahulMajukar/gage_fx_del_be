package com.secureauth.productservice.service;

import com.secureauth.productservice.dto.ReallocateResponse;
import com.secureauth.productservice.entity.Reallocate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReallocateNotificationService {

    private final JavaMailSender mailSender;
    
    @Autowired
    @Lazy
    private ReallocateService reallocateService;

    @Value("${spring.mail.username:noreply@gagefx.com}")
    private String fromEmail;

    @Value("${app.plant.hod.email:plant.hod@gagefx.com}")
    private String plantHodEmail;

    /**
     * Send notification to Plant HOD about new reallocation request
     */
    public void sendReallocationRequestNotification(ReallocateResponse reallocate) {
        try {
            String subject = "New Gage Reallocation Request - " + reallocate.getGageSerialNumber();
            String message = buildReallocationRequestMessage(reallocate);

            sendEmail(plantHodEmail, subject, message);
            log.info("Reallocation request notification sent to Plant HOD for request ID: {}", reallocate.getId());
        } catch (Exception e) {
            log.error("Error sending reallocation request notification", e);
        }
    }

    /**
     * Send notification to operator about approval/rejection.
     * Returns list of operator usernames that were notified (useful for dashboard notes).
     */
    public java.util.List<String> sendApprovalNotification(ReallocateResponse reallocate, boolean approved) {
        java.util.List<String> notified = new java.util.ArrayList<>();
        try {
            String subject = approved ?
                "Gage Reallocation Request Approved - " + reallocate.getGageSerialNumber() :
                "Gage Reallocation Request Rejected - " + reallocate.getGageSerialNumber();

            String message = buildApprovalMessage(reallocate, approved);

            // Notify the requester first
            String operatorEmail = getOperatorEmail(reallocate.getRequestedBy());
            sendEmail(operatorEmail, subject, message);
            notified.add(reallocate.getRequestedBy());
            log.info("Approval notification sent to requester {} for request ID: {}",
                    reallocate.getRequestedBy(), reallocate.getId());

            // Attempt to notify all operators in the current allocation (dept/function/operation)
            try {
                java.util.List<java.util.Map<String, Object>> users = fetchAllUsers();
                for (java.util.Map<String, Object> user : users) {
                    try {
                        String role = (user.getOrDefault("role", "")).toString().toUpperCase();
                        if (!(role.contains("OPERATOR") || role.contains("F"))) continue;

                        String userDept = (user.getOrDefault("departments", "")).toString();
                        String userFunc = (user.getOrDefault("functions", "")).toString();
                        String userOp = (user.getOrDefault("operations", "")).toString();

                        boolean matchDept = reallocate.getCurrentDepartment() == null || reallocate.getCurrentDepartment().isBlank() || userDept.contains(reallocate.getCurrentDepartment());
                        boolean matchFunc = reallocate.getCurrentFunction() == null || reallocate.getCurrentFunction().isBlank() || userFunc.contains(reallocate.getCurrentFunction());
                        boolean matchOp = reallocate.getCurrentOperation() == null || reallocate.getCurrentOperation().isBlank() || userOp.contains(reallocate.getCurrentOperation());

                        if (matchDept && matchFunc && matchOp) {
                            String uName = (user.getOrDefault("username", user.getOrDefault("email", ""))).toString();
                            String uEmail = (user.getOrDefault("email", uName + "@gagefx.com")).toString();
                            sendEmail(uEmail, subject, message);
                            if (!notified.contains(uName) && uName != null && !uName.isBlank()) notified.add(uName);
                        }
                    } catch (Exception x) {
                        log.debug("Skipping user during notification matching: {}", x.getMessage());
                    }
                }
            } catch (Exception ex) {
                log.warn("Could not fetch users for additional operator notifications: {}", ex.getMessage());
            }

        } catch (Exception e) {
            log.error("Error sending approval notification", e);
        }

        return notified;
    }

    /**
     * Send notification about gage expiration
     */
    public void sendExpirationNotification(ReallocateResponse reallocate) {
        try {
            String subject = "Gage Reallocation Expiring Soon - " + reallocate.getGageSerialNumber();
            String message = buildExpirationMessage(reallocate);

            // Send to operator
            String operatorEmail = getOperatorEmail(reallocate.getRequestedBy());
            sendEmail(operatorEmail, subject, message);

            // Send to Plant HOD
            sendEmail(plantHodEmail, subject, message);

            log.info("Expiration notification sent for reallocation ID: {}", reallocate.getId());
        } catch (Exception e) {
            log.error("Error sending expiration notification", e);
        }
    }

    /**
     * Send notification about automatic return
     */
    public void sendAutoReturnNotification(ReallocateResponse reallocate) {
        try {
            String subject = "Gage Automatically Returned - " + reallocate.getGageSerialNumber();
            String message = buildAutoReturnMessage(reallocate);

            // Send to operator
            String operatorEmail = getOperatorEmail(reallocate.getRequestedBy());
            sendEmail(operatorEmail, subject, message);

            // Send to Plant HOD
            sendEmail(plantHodEmail, subject, message);

            log.info("Auto return notification sent for reallocation ID: {}", reallocate.getId());
        } catch (Exception e) {
            log.error("Error sending auto return notification", e);
        }
    }

    /**
     * Send daily summary to Plant HOD
     */
    public void sendDailySummary() {
        try {
            List<ReallocateResponse> pendingApprovals = reallocateService.getReallocatesByStatus(Reallocate.Status.PENDING_APPROVAL);
            List<ReallocateResponse> expiringSoon = reallocateService.getReallocatesExpiringSoon();

            String subject = "Daily Gage Reallocation Summary - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String message = buildDailySummaryMessage(pendingApprovals, expiringSoon);

            sendEmail(plantHodEmail, subject, message);
            log.info("Daily summary sent to Plant HOD");
        } catch (Exception e) {
            log.error("Error sending daily summary", e);
        }
    }

    private void sendEmail(String to, String subject, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            mailSender.send(mailMessage);
            log.debug("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Error sending email to {}: {}", to, e.getMessage());
        }
    }

    private String buildReallocationRequestMessage(ReallocateResponse reallocate) {
        return String.format("""
            New Gage Reallocation Request
            
            Request Details:
            - Request ID: %d
            - Gage Serial Number: %s
            - Gage Model: %s
            - Gage Type: %s
            - Requested By: %s (%s)
            - Function: %s
            - Operation: %s
            - Time Limit: %s
            - Reason: %s
            - Requested At: %s
            
            Original Allocation:
            - Department: %s
            - Function: %s
            - Operation: %s
            
            Please review and approve/reject this request.
            
            Best regards,
            GageFX System
            """,
            reallocate.getId(),
            reallocate.getGageSerialNumber(),
            reallocate.getGageModelNumber(),
            reallocate.getGageTypeName(),
            reallocate.getRequestedBy(),
            reallocate.getRequestedByRole(),
            reallocate.getRequestedByFunction(),
            reallocate.getRequestedByOperation(),
            reallocate.getTimeLimit().getDisplayName(),
            reallocate.getReason(),
            reallocate.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            reallocate.getOriginalDepartment(),
            reallocate.getOriginalFunction(),
            reallocate.getOriginalOperation()
        );
    }

    private String buildApprovalMessage(ReallocateResponse reallocate, boolean approved) {
        String status = approved ? "APPROVED" : "REJECTED";
        String action = approved ? "approved" : "rejected";
        
        return String.format("""
            Gage Reallocation Request %s
            
            Request Details:
            - Request ID: %d
            - Gage Serial Number: %s
            - Status: %s
            - %s By: %s
            - %s At: %s
            
            %s
            
            %s
            """,
            status,
            reallocate.getId(),
            reallocate.getGageSerialNumber(),
            status,
            action,
            reallocate.getApprovedBy(),
            action,
            reallocate.getApprovedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            approved ? "Your gage reallocation request has been approved and is now active." : 
                      "Your gage reallocation request has been rejected.",
            approved ? 
                String.format("""
                    Current Allocation:
                    - Department: %s
                    - Function: %s
                    - Operation: %s
                    - Time Limit: %s
                    - Expires At: %s
                    """,
                    reallocate.getCurrentDepartment(),
                    reallocate.getCurrentFunction(),
                    reallocate.getCurrentOperation(),
                    reallocate.getTimeLimit().getDisplayName(),
                    reallocate.getExpiresAt() != null ? 
                        reallocate.getExpiresAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A"
                ) : 
                String.format("Reason: %s", reallocate.getNotes())
        );
    }

    private String buildExpirationMessage(ReallocateResponse reallocate) {
        return String.format("""
            Gage Reallocation Expiring Soon
            
            Request Details:
            - Request ID: %d
            - Gage Serial Number: %s
            - Current User: %s
            - Expires At: %s
            - Remaining Time: %d minutes
            
            Please return the gage before expiration to avoid automatic return.
            
            Best regards,
            GageFX System
            """,
            reallocate.getId(),
            reallocate.getGageSerialNumber(),
            reallocate.getRequestedBy(),
            reallocate.getExpiresAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            reallocate.getRemainingMinutes()
        );
    }

    private String buildAutoReturnMessage(ReallocateResponse reallocate) {
        return String.format("""
            Gage Automatically Returned
            
            Request Details:
            - Request ID: %d
            - Gage Serial Number: %s
            - Was Used By: %s
            - Expired At: %s
            - Returned At: %s
            
            The gage has been automatically returned to its original allocation due to expiration.
            
            Original Allocation:
            - Department: %s
            - Function: %s
            - Operation: %s
            
            Best regards,
            GageFX System
            """,
            reallocate.getId(),
            reallocate.getGageSerialNumber(),
            reallocate.getRequestedBy(),
            reallocate.getExpiresAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            reallocate.getOriginalDepartment(),
            reallocate.getOriginalFunction(),
            reallocate.getOriginalOperation()
        );
    }

    private String buildDailySummaryMessage(List<ReallocateResponse> pendingApprovals, List<ReallocateResponse> expiringSoon) {
        return String.format("""
            Daily Gage Reallocation Summary - %s
            
            Pending Approvals: %d
            %s
            
            Expiring Soon (within 1 hour): %d
            %s
            
            Please review pending approvals and expiring reallocations.
            
            Best regards,
            GageFX System
            """,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            pendingApprovals.size(),
            buildPendingApprovalsList(pendingApprovals),
            expiringSoon.size(),
            buildExpiringSoonList(expiringSoon)
        );
    }

    private String buildPendingApprovalsList(List<ReallocateResponse> pendingApprovals) {
        if (pendingApprovals.isEmpty()) {
            return "No pending approvals.";
        }
        
        StringBuilder sb = new StringBuilder();
        for (ReallocateResponse reallocate : pendingApprovals) {
            sb.append(String.format("- ID: %d, Gage: %s, Requested By: %s\n",
                    reallocate.getId(),
                    reallocate.getGageSerialNumber(),
                    reallocate.getRequestedBy()));
        }
        return sb.toString();
    }

    private String buildExpiringSoonList(List<ReallocateResponse> expiringSoon) {
        if (expiringSoon.isEmpty()) {
            return "No gages expiring soon.";
        }
        
        StringBuilder sb = new StringBuilder();
        for (ReallocateResponse reallocate : expiringSoon) {
            sb.append(String.format("- ID: %d, Gage: %s, User: %s, Expires: %s\n",
                    reallocate.getId(),
                    reallocate.getGageSerialNumber(),
                    reallocate.getRequestedBy(),
                    reallocate.getExpiresAt().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        }
        return sb.toString();
    }

    private String getOperatorEmail(String username) {
        // TODO: Implement logic to get operator email from user service
        // For now, return a default email format
        return username + "@gagefx.com";
    }

    // Simple fetch of users from the API gateway; returns raw list of user maps.
    private java.util.List<java.util.Map<String, Object>> fetchAllUsers() {
        try {
            org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
            String url = System.getProperty("user.service.url", "http://localhost:8080/api/users");
            Object resp = rt.getForObject(url, Object.class);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<java.util.Map<String, Object>> list = mapper.convertValue(resp, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>(){});
            return list == null ? java.util.Collections.emptyList() : list;
        } catch (Exception e) {
            log.warn("fetchAllUsers failed: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
}
