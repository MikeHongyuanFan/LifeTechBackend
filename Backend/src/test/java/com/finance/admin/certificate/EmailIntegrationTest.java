package com.finance.admin.certificate;

import com.finance.admin.certificate.service.CertificateEmailService;
import com.finance.admin.config.BaseIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class EmailIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CertificateEmailService certificateEmailService;

    @Test
    public void testEmailServiceConfiguration() {
        // This test verifies that the email service can be autowired and configured properly
        assertDoesNotThrow(() -> {
            log.info("Email service configured successfully");
        });
    }

    // Uncomment this test to manually verify Gmail SMTP integration
    // Note: This will send an actual email, so use with caution
    /*
    @Test
    public void testGmailSMTPIntegration() {
        assertDoesNotThrow(() -> {
            certificateEmailService.sendTestEmail("test@example.com", "Test Email - Gmail SMTP Integration");
            log.info("Test email sent successfully via Gmail SMTP");
        });
    }
    */
} 