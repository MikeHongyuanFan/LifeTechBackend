package com.finance.admin.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@lifetech.com}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    /**
     * Send birthday greeting email (mock implementation)
     */
    public Map<String, Object> sendBirthdayGreeting(String toEmail, String clientName, String subject) {
        log.info("Sending birthday greeting to: {} for client: {}", toEmail, clientName);

        Map<String, Object> result = new HashMap<>();
        
        if (!emailEnabled) {
            log.warn("Email service is disabled. Birthday greeting not sent to: {}", toEmail);
            result.put("success", false);
            result.put("message", "Email service is disabled");
            result.put("timestamp", LocalDateTime.now());
            return result;
        }

        // Mock implementation - in production, this would send actual emails
        try {
            String emailSubject = subject != null ? subject : "Happy Birthday from LifeTech!";
            
            // Simulate email sending
            Thread.sleep(100); // Simulate network delay
            
            result.put("success", true);
            result.put("message", "Birthday greeting sent successfully");
            result.put("recipient", toEmail);
            result.put("clientName", clientName);
            result.put("subject", emailSubject);
            result.put("timestamp", LocalDateTime.now());

            log.info("Birthday greeting sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send birthday greeting to: {}", toEmail, e);
            result.put("success", false);
            result.put("message", "Failed to send birthday greeting: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }

        return result;
    }

    /**
     * Send bulk birthday greetings
     */
    public Map<String, Object> sendBulkBirthdayGreetings(Map<String, String> recipients) {
        log.info("Sending bulk birthday greetings to {} recipients", recipients.size());

        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;

        for (Map.Entry<String, String> entry : recipients.entrySet()) {
            String email = entry.getKey();
            String name = entry.getValue();
            
            Map<String, Object> individualResult = sendBirthdayGreeting(email, name, null);
            if ((Boolean) individualResult.get("success")) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        result.put("success", failureCount == 0);
        result.put("totalRecipients", recipients.size());
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("timestamp", LocalDateTime.now());

        log.info("Bulk birthday greetings completed. Success: {}, Failures: {}", successCount, failureCount);

        return result;
    }

    /**
     * Send enquiry notification email (mock implementation)
     */
    public Map<String, Object> sendEnquiryNotification(String toEmail, String enquiryNumber, String subject, String message) {
        log.info("Sending enquiry notification to: {} for enquiry: {}", toEmail, enquiryNumber);

        Map<String, Object> result = new HashMap<>();
        
        if (!emailEnabled) {
            log.warn("Email service is disabled. Enquiry notification not sent to: {}", toEmail);
            result.put("success", false);
            result.put("message", "Email service is disabled");
            result.put("timestamp", LocalDateTime.now());
            return result;
        }

        try {
            String emailSubject = "Enquiry Update - " + enquiryNumber;
            
            // Simulate email sending
            Thread.sleep(100); // Simulate network delay

            result.put("success", true);
            result.put("message", "Enquiry notification sent successfully");
            result.put("recipient", toEmail);
            result.put("enquiryNumber", enquiryNumber);
            result.put("subject", emailSubject);
            result.put("timestamp", LocalDateTime.now());

            log.info("Enquiry notification sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send enquiry notification to: {}", toEmail, e);
            result.put("success", false);
            result.put("message", "Failed to send enquiry notification: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }

        return result;
    }

    /**
     * Send simple text email
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        if (!emailEnabled) {
            log.warn("Email service is disabled. Email not sent to: {}", to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send HTML email
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        if (!emailEnabled) {
            log.warn("Email service is disabled. HTML email not sent to: {}", to);
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(message);
        log.info("HTML email sent successfully to: {}", to);
    }

    /**
     * Build birthday email HTML body
     */
    private String buildBirthdayEmailBody(String clientName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Happy Birthday!</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    .birthday-icon { font-size: 48px; text-align: center; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Happy Birthday! 🎉</h1>
                    </div>
                    <div class="content">
                        <div class="birthday-icon">🎂</div>
                        <h2>Dear %s,</h2>
                        <p>On behalf of everyone at LifeTech, we want to wish you a very happy birthday!</p>
                        <p>We hope your special day is filled with joy, laughter, and wonderful memories.</p>
                        <p>Thank you for being a valued client. We appreciate your trust in us and look forward to continuing to serve you.</p>
                        <p>Have a fantastic birthday and a wonderful year ahead!</p>
                        <p>Warm regards,<br>The LifeTech Team</p>
                    </div>
                    <div class="footer">
                        <p>This email was sent by LifeTech Financial Services</p>
                        <p>If you have any questions, please contact us at support@lifetech.com</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(clientName);
    }

    /**
     * Build enquiry notification email HTML body
     */
    private String buildEnquiryNotificationBody(String enquiryNumber, String subject, String message) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Enquiry Update</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    .enquiry-details { background-color: white; padding: 15px; border-left: 4px solid #2196F3; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Enquiry Update</h1>
                    </div>
                    <div class="content">
                        <h2>Your enquiry has been updated</h2>
                        <div class="enquiry-details">
                            <p><strong>Enquiry Number:</strong> %s</p>
                            <p><strong>Subject:</strong> %s</p>
                            <p><strong>Update:</strong></p>
                            <p>%s</p>
                        </div>
                        <p>If you have any questions about this update, please don't hesitate to contact us.</p>
                        <p>Best regards,<br>The LifeTech Support Team</p>
                    </div>
                    <div class="footer">
                        <p>This email was sent by LifeTech Financial Services</p>
                        <p>If you have any questions, please contact us at support@lifetech.com</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(enquiryNumber, subject, message);
    }

    /**
     * Test email configuration
     */
    public Map<String, Object> testEmailConfiguration() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (!emailEnabled) {
                result.put("success", false);
                result.put("message", "Email service is disabled");
                return result;
            }

            // Mock test - in production, this would test actual email configuration
            result.put("success", true);
            result.put("message", "Email configuration test successful (mock)");
            result.put("fromEmail", fromEmail);
            result.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            log.error("Email configuration test failed", e);
            result.put("success", false);
            result.put("message", "Email configuration test failed: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }

        return result;
    }
} 