package com.secureauth.productservice.controller;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    @Autowired
    private JavaMailSender mailSender;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    // Replace your generateCleanHtmlEmailWithEmbeddedLogo method with this:

    private String generateCleanHtmlEmailWithEmbeddedLogo(String subject, String content) {
        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>%s</title>
    <style>
        /* Mobile-first responsive design */
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            background-color: #f8fafc;
            margin: 0;
            padding: 0;
            color: #1e293b;
            line-height: 1.6;
        }
        .email-container {
            max-width: 700px;
            margin: 20px auto;
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
        }
        .header {
            background: #f1f5f9;
            padding: 20px 24px;
            display: flex;
            align-items: center;
            gap: 16px; /* Reduced gap for mobile */
            border-bottom: 1px solid #e2e8f0;
        }
        .logo {
            width: 60px; /* Smaller logo for mobile */
            height: 60px;
            border-radius: 50%%;
            object-fit: cover;
            border: 2px solid #cbd5e1;
        }
        .header h1 {
            margin: 0;
            font-size: 18px; /* Smaller font for mobile */
            font-weight: 600;
            color: #0f172a;
        }
        .content {
            padding: 24px 20px; /* Reduced padding for mobile */
            color: #334155;
        }
        .content h2 {
            margin-top: 0;
            color: #0f172a;
            font-size: 16px; /* Smaller font for mobile */
        }
        .content p {
            margin: 12px 0; /* Reduced margin for mobile */
            white-space: pre-wrap;
            font-size: 14px; /* Better readability on mobile */
        }
        table {
            width: 100%%;
            border-collapse: collapse;
            margin: 12px 0;
            font-size: 14px; /* Smaller table text */
        }
        th, td {
            border: 1px solid #e2e8f0;
            padding: 8px; /* Smaller padding for mobile */
            text-align: left;
        }
        th {
            background-color: #f8fafc;
            font-weight: 600;
            color: #334155;
        }
        .footer {
            background: #f8fafc;
            padding: 16px 20px; /* Reduced padding for mobile */
            text-align: left;
            color: #64748b;
            font-size: 12px; /* Smaller footer text */
            border-top: 1px solid #e2e8f0;
        }
        .footer p {
            margin: 3px 0;
        }

        /* Mobile-specific styles */
        @media screen and (max-width: 600px) {
            .email-container {
                margin: 10px auto;
                border-radius: 6px;
            }
            .header {
                padding: 16px 20px;
                flex-direction: column; /* Stack logo and text vertically */
                text-align: center;
            }
            .logo {
                width: 50px;
                height: 50px;
                margin-bottom: 8px;
            }
            .header h1 {
                font-size: 16px;
                margin: 0;
            }
            .content {
                padding: 20px 16px;
            }
            .content h2 {
                font-size: 15px;
            }
            .content p {
                font-size: 13px;
                margin: 10px 0;
            }
            table {
                font-size: 13px;
            }
            th, td {
                padding: 6px 4px; /* Even smaller on very small screens */
            }
            .footer {
                padding: 12px 16px;
                font-size: 11px;
            }
        }

        /* For very small screens */
        @media screen and (max-width: 480px) {
            .logo {
                width: 45px;
                height: 45px;
            }
            .header h1 {
                font-size: 15px;
            }
            .content {
                padding: 16px 12px;
            }
            .content h2 {
                font-size: 14px;
            }
            .content p {
                font-size: 12px;
            }
            table {
                font-size: 12px;
            }
            th, td {
                padding: 5px 3px;
            }
        }
    </style>
</head>
<body>
    <div class="email-container">
        <div class="header">
            <img src="cid:logo" alt="Logo" class="logo">
            <h1>GageFX Support</h1>
        </div>
        <div class="content">
            <h2>%s</h2>
            <p>%s</p>
        </div>
        <div class="footer">
            <p>Best regards,<br><strong>GageFX Team</strong></p>
            <p>This is an automated message. Please do not reply.</p>
        </div>
    </div>
</body>
</html>
""".formatted(
                escapeHtml(subject != null ? subject : "Notification"),
                escapeHtml(subject != null ? subject : "Notification"),
                content != null ? escapeHtml(content).replace("\n", "<br>") : ""
        );
    }
    // Simple HTML escaping to prevent XSS (basic)
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "<")
                .replace(">", ">")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendMailWithAttachments(
            @RequestParam("to") List<String> to,
            @RequestParam(value = "cc", required = false) List<String> cc,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "body", required = false) String body,
            @RequestParam(value = "html", required = false) String html,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "attachments", required = false) List<MultipartFile> attachments) {

        try {
            if (to == null || to.isEmpty()) {
                return ResponseEntity.badRequest().body("No 'to' recipients provided");
            }

            // Validate and clean 'to' emails
            List<String> validTo = to.stream()
                    .map(String::trim)
                    .filter(email -> !email.isEmpty() && isValidEmail(email))
                    .toList();

            if (validTo.isEmpty()) {
                return ResponseEntity.badRequest().body("No valid 'to' email addresses provided");
            }

            // Validate and clean 'cc' emails
            List<String> validCc = new ArrayList<>();
            if (cc != null && !cc.isEmpty()) {
                validCc = cc.stream()
                        .map(String::trim)
                        .filter(email -> !email.isEmpty() && isValidEmail(email))
                        .toList();
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("rms2@qsutra.com");
            helper.setTo(validTo.toArray(new String[0]));
            if (!validCc.isEmpty()) {
                helper.setCc(validCc.toArray(new String[0]));
            }

            subject = subject != null ? subject : "GageFX Notification";
            helper.setSubject(subject);

            // ✅ Determine final HTML content
            String emailContent;
            if (html != null && !html.trim().isEmpty()) {
                emailContent = html;
            } else {
                emailContent = generateCleanHtmlEmailWithEmbeddedLogo(subject, body);
            }
            helper.setText(emailContent, true); // Always send as HTML

            // ✅ Embed logo if HTML references "cid:logo" — works for ALL 4 email types!
            if (emailContent.contains("cid:logo")) {
                try {
                    org.springframework.core.io.Resource logoResource =
                            new org.springframework.core.io.ClassPathResource("static/stratum-aerospace.png");
                    if (logoResource.exists()) {
                        helper.addInline("logo", logoResource, "image/png"); // Explicit MIME type
                    } else {
                        System.err.println("⚠️ Logo file NOT found at: static/stratum-aerospace.png");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Failed to embed logo: " + e.getMessage());
                }
            }

            // Optional: set reply-to
            if (from != null && !from.isBlank()) {
                helper.setReplyTo(from);
            }

            // Handle attachments
            if (attachments != null && !attachments.isEmpty()) {
                for (MultipartFile file : attachments) {
                    if (file.isEmpty()) continue;

                    if (file.getSize() > 10 * 1024 * 1024) {
                        return ResponseEntity.badRequest()
                                .body("File too large: " + file.getOriginalFilename() + " (max 10 MB)");
                    }

                    String contentType = file.getContentType();
                    if (contentType == null ||
                            (!contentType.startsWith("image/") &&
                                    !contentType.equals("application/pdf") &&
                                    !contentType.startsWith("video/"))) {  // ✅ Allow videos
                        return ResponseEntity.badRequest()
                                .body("Unsupported file type: " + file.getOriginalFilename() +
                                        ". Only images, PDFs, and videos are allowed.");
                    }

                    helper.addAttachment(file.getOriginalFilename(), file);
                }
            }

            mailSender.send(mimeMessage);
            return ResponseEntity.ok("Email sent successfully to " + String.join(", ", validTo));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
        }
    }


    // Add this inside MailController or as a separate class
    @Data
    public static class MailRequest {
        private List<String> to;
        private String subject;
        private String body;
        private String html;
        private String from;
        private List<MultipartFile> attachments; // ← New field
    }

    // Method for sending HTML emails directly (used by services)
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom("rms2@qsutra.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true indicates HTML content

        mailSender.send(mimeMessage);
    }

    @GetMapping("/test")
    public String test() {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("rms2@qsutra.com");
            helper.setTo("nootanchougule5511@gmail.com");
            helper.setSubject("TEST: Embedded Logo");

            String emailContent = generateCleanHtmlEmailWithEmbeddedLogo("Test", "Logo should appear!");
            helper.setText(emailContent, true);

            // Embed logo
            org.springframework.core.io.Resource logo =
                    new org.springframework.core.io.ClassPathResource("static/stratum-aerospace.png");
            if (logo.exists()) {
                helper.addInline("logo", logo);
            }

            mailSender.send(mimeMessage);
            return "Check your inbox!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}