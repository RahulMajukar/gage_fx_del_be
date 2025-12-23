package com.secureauth.productapi.service.impl;

import com.secureauth.productapi.service.interfaces.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from:${spring.mail.username}}")
    private String fromAddress;

    @Override
    public void sendCredentials(String toEmail, String username, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Welcome to GageFX - Your Account Credentials");

            String emailContent = buildCredentialsEmailContent(username, password);
            message.setText(emailContent);

            mailSender.send(message);
            log.info("Successfully sent credentials email to {} using from address {}", toEmail, fromAddress);
        } catch (Exception ex) {
            // Do not fail user creation on email issues; log and continue
            log.warn("Failed to send credentials email to {}: {}", toEmail, ex.getMessage());
        }
    }

    private String buildCredentialsEmailContent(String username, String password) {
        StringBuilder content = new StringBuilder();

        content.append("Dear User,\n\n");

        content.append("Welcome to GageFX! We are pleased to inform you that your account has been successfully created.\n\n");

        content.append("Your login credentials are as follows:\n\n");
        content.append("━━━━━━━━━━━━━━━━━\n");
        content.append("━━━━━━━━━━━━━━━━━\n");
        content.append("Username: ").append(username).append("\n");
        content.append("Password: ").append(password).append("\n");
        content.append("━━━━━━━━━━━━━━━━━\n");
        content.append("━━━━━━━━━━━━━━━━━━\n\n");


        content.append("IMPORTANT SECURITY NOTICE:\n");
        content.append("• Please change your password immediately after your first login\n");
        content.append("• Keep your credentials confidential and do not share them with anyone\n");
        content.append("• If you suspect any unauthorized access, contact our support team immediately\n\n");

        content.append("Getting Started:\n");
        content.append("1. Visit our platform and log in using the credentials above\n");
        content.append("2. Complete your profile setup\n");
        content.append("3. Change your temporary password to a secure one of your choice\n\n");

        content.append("If you have any questions or need assistance, please don't hesitate to contact our support team.\n\n");

        content.append("Thank you for choosing GageFX. We look forward to serving you!\n\n");

        content.append("Best regards,\n");
        content.append("The GageFX Team\n\n");

        content.append("---\n");
        content.append("This is an automated message. Please do not reply to this email.\n");
        content.append("For support inquiries, please contact our customer service team.\n");
        content.append("© 2025 GageFX. All rights reserved.");

        return content.toString();
    }
}