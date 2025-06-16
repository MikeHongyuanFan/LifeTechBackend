package com.finance.admin.certificate;

import com.finance.admin.certificate.service.CertificateEmailService;
import com.finance.admin.config.TestConfig;
import com.finance.admin.audit.service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.mail.enabled=false",
        "app.email.enabled=false",
        "app.certificate.email.enabled=false"
    }
)
@ActiveProfiles("test")
@Sql(scripts = {"/test-schema.sql", "/test-data.sql"})
@Import(TestConfig.class)
@Slf4j
public class EmailIntegrationTest {

    @Autowired
    private JavaMailSender javaMailSender;

    @MockBean
    private CertificateEmailService certificateEmailService;

    @MockBean
    private AuditService auditService;

    @Test
    void sendEmail_WithValidData_SendsSuccessfully() {
        // Setup
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        // Test
        assertDoesNotThrow(() -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("test@example.com");
            message.setSubject("Test Subject");
            message.setText("Test Content");
            javaMailSender.send(message);
        });

        // Verify
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    // @Test
    // void sendCertificateEmail_WithValidData_SendsSuccessfully() {
    //     // Setup
    //     doNothing().when(certificateEmailService).sendCertificateEmail(anyLong(), anyString(), anyString());

    //     // Test
    //     assertDoesNotThrow(() -> 
    //         certificateEmailService.sendCertificateEmail(1L, "test.pdf", "test@example.com")
    //     );

    //     // Verify
    //     verify(certificateEmailService, times(1))
    //         .sendCertificateEmail(eq(1L), eq("test.pdf"), eq("test@example.com"));
    // }

    // Add more test methods here...
} 