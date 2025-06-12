package com.finance.admin.certificate.service;

import com.finance.admin.certificate.model.Certificate;
import com.finance.admin.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateEmailService {

    private final EmailService emailService;
    private final CloudStorageService cloudStorageService;

    @Value("${app.certificate.email.enabled:true}")
    private boolean emailNotificationsEnabled;

    @Value("${app.certificate.email.from:certificates@lifetech.com.au}")
    private String fromEmail;

    @Value("${app.certificate.email.admin:admin@lifetech.com.au}")
    private String adminEmail;

    /**
     * Send certificate generation notification to client
     */
    public void sendCertificateGeneratedNotification(Certificate certificate) {
        if (!emailNotificationsEnabled) {
            log.info("Email notifications disabled, skipping certificate generation notification for: {}", 
                certificate.getCertificateNumber());
            return;
        }

        try {
            String clientEmail = certificate.getClient().getEmailPrimary();
            String clientName = certificate.getClient().getFirstName() + " " + certificate.getClient().getLastName();
            
            String subject = "Your E-Share Certificate is Ready - " + certificate.getCertificateNumber();
            
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("clientName", clientName);
            templateVariables.put("certificateNumber", certificate.getCertificateNumber());
            templateVariables.put("certificateType", certificate.getCertificateType().getDisplayName());
            templateVariables.put("investmentName", certificate.getInvestment().getInvestmentName());
            templateVariables.put("investmentAmount", String.format("$%.2f", certificate.getInvestmentAmount()));
            templateVariables.put("numberOfShares", certificate.getNumberOfShares());
            templateVariables.put("issueDate", certificate.getIssueDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            templateVariables.put("expiryDate", certificate.getExpiryDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            templateVariables.put("downloadUrl", generateDownloadUrl(certificate));
            
            String emailContent = buildCertificateGeneratedEmail(templateVariables);
            
            emailService.sendSimpleEmail(clientEmail, subject, emailContent);
            
            log.info("Certificate generation notification sent to: {} for certificate: {}", 
                clientEmail, certificate.getCertificateNumber());
                
        } catch (Exception e) {
            log.error("Failed to send certificate generation notification for certificate: {}", 
                certificate.getCertificateNumber(), e);
        }
    }

    /**
     * Send certificate expiry warning notification
     */
    public void sendCertificateExpiryWarning(Certificate certificate, int daysUntilExpiry) {
        if (!emailNotificationsEnabled) {
            log.info("Email notifications disabled, skipping expiry warning for: {}", 
                certificate.getCertificateNumber());
            return;
        }

        try {
            String clientEmail = certificate.getClient().getEmailPrimary();
            String clientName = certificate.getClient().getFirstName() + " " + certificate.getClient().getLastName();
            
            String subject = String.format("Certificate Expiry Notice - %s (Expires in %d days)", 
                certificate.getCertificateNumber(), daysUntilExpiry);
            
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("clientName", clientName);
            templateVariables.put("certificateNumber", certificate.getCertificateNumber());
            templateVariables.put("certificateType", certificate.getCertificateType().getDisplayName());
            templateVariables.put("investmentName", certificate.getInvestment().getInvestmentName());
            templateVariables.put("expiryDate", certificate.getExpiryDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            templateVariables.put("daysUntilExpiry", daysUntilExpiry);
            templateVariables.put("renewalUrl", generateRenewalUrl(certificate));
            templateVariables.put("contactEmail", adminEmail);
            
            String emailContent = buildCertificateExpiryWarningEmail(templateVariables);
            
            emailService.sendSimpleEmail(clientEmail, subject, emailContent);
            
            log.info("Certificate expiry warning sent to: {} for certificate: {} (expires in {} days)", 
                clientEmail, certificate.getCertificateNumber(), daysUntilExpiry);
                
        } catch (Exception e) {
            log.error("Failed to send certificate expiry warning for certificate: {}", 
                certificate.getCertificateNumber(), e);
        }
    }

    /**
     * Send batch expiry notifications to admin
     */
    public void sendBatchExpiryNotificationToAdmin(List<Certificate> expiringCertificates) {
        if (!emailNotificationsEnabled || expiringCertificates.isEmpty()) {
            return;
        }

        try {
            String subject = String.format("Certificate Expiry Report - %d certificates expiring soon", 
                expiringCertificates.size());
            
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("certificateCount", expiringCertificates.size());
            templateVariables.put("certificates", expiringCertificates);
            templateVariables.put("reportDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            
            String emailContent = buildBatchExpiryReportEmail(templateVariables);
            
            emailService.sendSimpleEmail(adminEmail, subject, emailContent);
            
            log.info("Batch expiry notification sent to admin for {} certificates", expiringCertificates.size());
                
        } catch (Exception e) {
            log.error("Failed to send batch expiry notification to admin", e);
        }
    }

    /**
     * Send certificate revocation notification
     */
    public void sendCertificateRevokedNotification(Certificate certificate, String reason) {
        if (!emailNotificationsEnabled) {
            return;
        }

        try {
            String clientEmail = certificate.getClient().getEmailPrimary();
            String clientName = certificate.getClient().getFirstName() + " " + certificate.getClient().getLastName();
            
            String subject = "Certificate Revoked - " + certificate.getCertificateNumber();
            
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("clientName", clientName);
            templateVariables.put("certificateNumber", certificate.getCertificateNumber());
            templateVariables.put("certificateType", certificate.getCertificateType().getDisplayName());
            templateVariables.put("investmentName", certificate.getInvestment().getInvestmentName());
            templateVariables.put("revocationReason", reason);
            templateVariables.put("revocationDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            templateVariables.put("contactEmail", adminEmail);
            
            String emailContent = buildCertificateRevokedEmail(templateVariables);
            
            emailService.sendSimpleEmail(clientEmail, subject, emailContent);
            
            log.info("Certificate revocation notification sent to: {} for certificate: {}", 
                clientEmail, certificate.getCertificateNumber());
                
        } catch (Exception e) {
            log.error("Failed to send certificate revocation notification for certificate: {}", 
                certificate.getCertificateNumber(), e);
        }
    }

    /**
     * Send test email to verify Gmail SMTP integration
     */
    public void sendTestEmail(String toEmail, String subject) {
        try {
            String emailContent = buildTestEmail();
            emailService.sendSimpleEmail(toEmail, subject, emailContent);
            log.info("Test email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send test email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send test email: " + e.getMessage(), e);
        }
    }

    private String generateDownloadUrl(Certificate certificate) {
        return String.format("https://lifetech.com.au/certificates/download/%s", certificate.getCertificateNumber());
    }

    private String generateRenewalUrl(Certificate certificate) {
        return String.format("https://lifetech.com.au/certificates/renew/%s", certificate.getCertificateNumber());
    }

    private String buildCertificateGeneratedEmail(Map<String, Object> variables) {
        return String.format("""
            Dear %s,
            
            We are pleased to inform you that your E-Share Certificate has been successfully generated and is now available for download.
            
            Certificate Details:
            • Certificate Number: %s
            • Certificate Type: %s
            • Investment: %s
            • Investment Amount: %s
            • Number of Shares: %s
            • Issue Date: %s
            • Expiry Date: %s
            
            You can download your certificate using the following link:
            %s
            
            Please keep this certificate in a safe place as it serves as proof of your investment with LifeTech Financial Services.
            
            If you have any questions or concerns, please don't hesitate to contact us.
            
            Best regards,
            LifeTech Financial Services Team
            
            ---
            This is an automated message. Please do not reply to this email.
            For support, contact us at: certificates@lifetech.com.au
            """,
            variables.get("clientName"),
            variables.get("certificateNumber"),
            variables.get("certificateType"),
            variables.get("investmentName"),
            variables.get("investmentAmount"),
            variables.get("numberOfShares"),
            variables.get("issueDate"),
            variables.get("expiryDate"),
            variables.get("downloadUrl")
        );
    }

    private String buildCertificateExpiryWarningEmail(Map<String, Object> variables) {
        return String.format("""
            Dear %s,
            
            This is an important notice regarding your E-Share Certificate that is approaching its expiry date.
            
            Certificate Details:
            • Certificate Number: %s
            • Certificate Type: %s
            • Investment: %s
            • Expiry Date: %s
            • Days Until Expiry: %d
            
            To ensure continuous coverage of your investment, please take action before the expiry date.
            
            You can renew your certificate using the following link:
            %s
            
            If you need assistance or have any questions, please contact us at %s
            
            Best regards,
            LifeTech Financial Services Team
            
            ---
            This is an automated reminder. Please do not reply to this email.
            For support, contact us at: certificates@lifetech.com.au
            """,
            variables.get("clientName"),
            variables.get("certificateNumber"),
            variables.get("certificateType"),
            variables.get("investmentName"),
            variables.get("expiryDate"),
            variables.get("daysUntilExpiry"),
            variables.get("renewalUrl"),
            variables.get("contactEmail")
        );
    }

    private String buildBatchExpiryReportEmail(Map<String, Object> variables) {
        StringBuilder certificateList = new StringBuilder();
        @SuppressWarnings("unchecked")
        List<Certificate> certificates = (List<Certificate>) variables.get("certificates");
        
        for (Certificate cert : certificates) {
            certificateList.append(String.format(
                "• %s - %s %s (Expires: %s)\n",
                cert.getCertificateNumber(),
                cert.getClient().getFirstName(),
                cert.getClient().getLastName(),
                cert.getExpiryDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            ));
        }

        return String.format("""
            Certificate Expiry Report - %s
            
            The following %d certificates are expiring soon and require attention:
            
            %s
            
            Please review these certificates and take appropriate action to notify clients or process renewals.
            
            ---
            LifeTech Financial Services
            Certificate Management System
            """,
            variables.get("reportDate"),
            variables.get("certificateCount"),
            certificateList.toString()
        );
    }

    private String buildCertificateRevokedEmail(Map<String, Object> variables) {
        return String.format("""
            Dear %s,
            
            We regret to inform you that your E-Share Certificate has been revoked.
            
            Certificate Details:
            • Certificate Number: %s
            • Certificate Type: %s
            • Investment: %s
            • Revocation Date: %s
            • Reason: %s
            
            This certificate is no longer valid and should not be used for any transactions.
            
            If you believe this revocation was made in error or if you have any questions, please contact us immediately at %s
            
            We apologize for any inconvenience this may cause.
            
            Best regards,
            LifeTech Financial Services Team
            
            ---
            This is an automated notification. Please do not reply to this email.
            For support, contact us at: certificates@lifetech.com.au
            """,
            variables.get("clientName"),
            variables.get("certificateNumber"),
            variables.get("certificateType"),
            variables.get("investmentName"),
            variables.get("revocationDate"),
            variables.get("revocationReason"),
            variables.get("contactEmail")
        );
    }

    private String buildTestEmail() {
        return String.format("""
            LifeTech Certificate System - Email Integration Test
            
            This is a test email to verify that the Gmail SMTP integration is working correctly.
            
            Test Details:
            • Timestamp: %s
            • Email Service: Gmail SMTP
            • From: %s
            • System: LifeTech Certificate Management System
            
            If you received this email, the Gmail SMTP integration is working successfully!
            
            Features tested:
            ✓ SMTP Connection to Gmail
            ✓ Authentication with App Password
            ✓ TLS/STARTTLS Encryption
            ✓ Email Delivery
            
            Best regards,
            LifeTech Development Team
            
            ---
            This is a test email from the LifeTech Certificate Management System.
            Sprint 2.3 - Email Integration Implementation
            """,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' HH:mm:ss")),
            fromEmail
        );
    }
} 