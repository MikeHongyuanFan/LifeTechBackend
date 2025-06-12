package com.finance.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;

/**
 * Mock implementation of JavaMailSender for when email service is disabled
 */
@Slf4j
public class MockJavaMailSender implements JavaMailSender {

    @Override
    public MimeMessage createMimeMessage() {
        log.debug("Mock: Creating MimeMessage (email service disabled)");
        return new MimeMessage(Session.getDefaultInstance(new Properties()));
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        log.debug("Mock: Creating MimeMessage from InputStream (email service disabled)");
        return createMimeMessage();
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        log.info("Mock: Would send MimeMessage (email service disabled)");
    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        log.info("Mock: Would send {} MimeMessages (email service disabled)", mimeMessages.length);
    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
        log.info("Mock: Would send MimeMessage via preparator (email service disabled)");
    }

    @Override
    public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
        log.info("Mock: Would send {} MimeMessages via preparators (email service disabled)", mimeMessagePreparators.length);
    }

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        log.info("Mock: Would send SimpleMailMessage to: {} with subject: {} (email service disabled)", 
                simpleMessage.getTo(), simpleMessage.getSubject());
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        log.info("Mock: Would send {} SimpleMailMessages (email service disabled)", simpleMessages.length);
    }
} 