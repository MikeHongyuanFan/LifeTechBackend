package com.finance.admin.certificate.service;

import com.finance.admin.certificate.model.Certificate;
import com.finance.admin.certificate.model.Certificate.CertificateStatus;
import com.finance.admin.certificate.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateExpiryMonitoringService {

    private final CertificateRepository certificateRepository;
    private final CertificateEmailService certificateEmailService;

    @Value("${app.certificate.expiry.warning-days:30,7,1}")
    private int[] warningDays;

    @Value("${app.certificate.expiry.monitoring.enabled:true}")
    private boolean monitoringEnabled;

    @Value("${app.certificate.expiry.auto-expire:true}")
    private boolean autoExpireEnabled;

    /**
     * Scheduled task to check for expiring certificates
     * Runs daily at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void checkExpiringCertificates() {
        if (!monitoringEnabled) {
            log.debug("Certificate expiry monitoring is disabled");
            return;
        }

        log.info("Starting certificate expiry monitoring check");

        try {
            // Check for certificates expiring at different intervals
            for (int days : warningDays) {
                checkAndNotifyExpiringCertificates(days);
            }

            // Auto-expire certificates that have passed their expiry date
            if (autoExpireEnabled) {
                autoExpireOverdueCertificates();
            }

            // Send daily summary to admin
            sendDailyExpiryReport();

            log.info("Certificate expiry monitoring check completed successfully");

        } catch (Exception e) {
            log.error("Error during certificate expiry monitoring", e);
        }
    }

    /**
     * Manual trigger for expiry check (for testing or admin use)
     */
    public void performManualExpiryCheck() {
        log.info("Manual certificate expiry check triggered");
        checkExpiringCertificates();
    }

    /**
     * Get certificates expiring within specified days
     */
    public List<Certificate> getCertificatesExpiringWithinDays(int days) {
        LocalDate cutoffDate = LocalDate.now().plusDays(days);
        return certificateRepository.findByExpiryDateBeforeAndStatus(cutoffDate, CertificateStatus.ACTIVE);
    }

    /**
     * Get expired certificates that haven't been marked as expired
     */
    public List<Certificate> getOverdueCertificates() {
        LocalDate today = LocalDate.now();
        return certificateRepository.findByExpiryDateBeforeAndStatus(today, CertificateStatus.ACTIVE);
    }

    /**
     * Get expiry statistics
     */
    public ExpiryStatistics getExpiryStatistics() {
        LocalDate today = LocalDate.now();
        
        long expiredCount = certificateRepository.countByExpiryDateBeforeAndStatus(today, CertificateStatus.ACTIVE);
        long expiring7Days = certificateRepository.countByExpiryDateBetweenAndStatus(
            today, today.plusDays(7), CertificateStatus.ACTIVE);
        long expiring30Days = certificateRepository.countByExpiryDateBetweenAndStatus(
            today, today.plusDays(30), CertificateStatus.ACTIVE);
        long expiring90Days = certificateRepository.countByExpiryDateBetweenAndStatus(
            today, today.plusDays(90), CertificateStatus.ACTIVE);

        return ExpiryStatistics.builder()
            .overdueCertificates(expiredCount)
            .expiringIn7Days(expiring7Days)
            .expiringIn30Days(expiring30Days)
            .expiringIn90Days(expiring90Days)
            .lastChecked(LocalDate.now())
            .build();
    }

    private void checkAndNotifyExpiringCertificates(int daysUntilExpiry) {
        LocalDate targetDate = LocalDate.now().plusDays(daysUntilExpiry);
        
        List<Certificate> expiringCertificates = certificateRepository
            .findByExpiryDateAndStatus(targetDate, CertificateStatus.ACTIVE);

        if (expiringCertificates.isEmpty()) {
            log.debug("No certificates expiring in {} days", daysUntilExpiry);
            return;
        }

        log.info("Found {} certificates expiring in {} days", expiringCertificates.size(), daysUntilExpiry);

        // Send individual notifications to clients
        for (Certificate certificate : expiringCertificates) {
            try {
                certificateEmailService.sendCertificateExpiryWarning(certificate, daysUntilExpiry);
                
                // Update last notification date (you might want to add this field to Certificate entity)
                log.debug("Expiry warning sent for certificate: {}", certificate.getCertificateNumber());
                
            } catch (Exception e) {
                log.error("Failed to send expiry warning for certificate: {}", 
                    certificate.getCertificateNumber(), e);
            }
        }
    }

    @Transactional
    private void autoExpireOverdueCertificates() {
        List<Certificate> overdueCertificates = getOverdueCertificates();
        
        if (overdueCertificates.isEmpty()) {
            log.debug("No overdue certificates found");
            return;
        }

        log.info("Auto-expiring {} overdue certificates", overdueCertificates.size());

        for (Certificate certificate : overdueCertificates) {
            try {
                certificate.setStatus(CertificateStatus.EXPIRED);
                certificate.setUpdatedAt(LocalDate.now().atStartOfDay());
                certificateRepository.save(certificate);
                
                log.info("Auto-expired certificate: {} (expired on: {})", 
                    certificate.getCertificateNumber(), certificate.getExpiryDate());
                
            } catch (Exception e) {
                log.error("Failed to auto-expire certificate: {}", 
                    certificate.getCertificateNumber(), e);
            }
        }
    }

    private void sendDailyExpiryReport() {
        try {
            ExpiryStatistics stats = getExpiryStatistics();
            
            // Only send report if there are certificates requiring attention
            if (stats.getOverdueCertificates() > 0 || stats.getExpiringIn7Days() > 0) {
                List<Certificate> urgentCertificates = getCertificatesExpiringWithinDays(7);
                urgentCertificates.addAll(getOverdueCertificates());
                
                if (!urgentCertificates.isEmpty()) {
                    certificateEmailService.sendBatchExpiryNotificationToAdmin(urgentCertificates);
                }
            }
            
            log.info("Daily expiry report sent. Stats: Overdue={}, Expiring in 7 days={}, Expiring in 30 days={}", 
                stats.getOverdueCertificates(), stats.getExpiringIn7Days(), stats.getExpiringIn30Days());
                
        } catch (Exception e) {
            log.error("Failed to send daily expiry report", e);
        }
    }

    /**
     * Get detailed expiry report for admin dashboard
     */
    public ExpiryReport getDetailedExpiryReport() {
        LocalDate today = LocalDate.now();
        
        List<Certificate> overdue = getOverdueCertificates();
        List<Certificate> expiring7Days = getCertificatesExpiringWithinDays(7);
        List<Certificate> expiring30Days = getCertificatesExpiringWithinDays(30);
        
        // Group by client for better reporting
        Map<String, List<Certificate>> overdueByClient = overdue.stream()
            .collect(Collectors.groupingBy(cert -> 
                cert.getClient().getFirstName() + " " + cert.getClient().getLastName()));
        
        Map<String, List<Certificate>> expiringByClient = expiring7Days.stream()
            .collect(Collectors.groupingBy(cert -> 
                cert.getClient().getFirstName() + " " + cert.getClient().getLastName()));

        return ExpiryReport.builder()
            .reportDate(today)
            .overdueCertificates(overdue)
            .certificatesExpiring7Days(expiring7Days)
            .certificatesExpiring30Days(expiring30Days)
            .overdueByClient(overdueByClient)
            .expiringByClient(expiringByClient)
            .statistics(getExpiryStatistics())
            .build();
    }

    /**
     * Renew a certificate (extend expiry date)
     */
    @Transactional
    public Certificate renewCertificate(Long certificateId, int extensionMonths) {
        Certificate certificate = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new RuntimeException("Certificate not found"));

        LocalDate newExpiryDate = certificate.getExpiryDate().plusMonths(extensionMonths);
        certificate.setExpiryDate(newExpiryDate);
        certificate.setStatus(CertificateStatus.ACTIVE);
        certificate.setUpdatedAt(LocalDate.now().atStartOfDay());

        Certificate renewed = certificateRepository.save(certificate);
        
        log.info("Certificate renewed: {} - new expiry date: {}", 
            certificate.getCertificateNumber(), newExpiryDate);

        return renewed;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExpiryStatistics {
        private long overdueCertificates;
        private long expiringIn7Days;
        private long expiringIn30Days;
        private long expiringIn90Days;
        private LocalDate lastChecked;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExpiryReport {
        private LocalDate reportDate;
        private List<Certificate> overdueCertificates;
        private List<Certificate> certificatesExpiring7Days;
        private List<Certificate> certificatesExpiring30Days;
        private Map<String, List<Certificate>> overdueByClient;
        private Map<String, List<Certificate>> expiringByClient;
        private ExpiryStatistics statistics;
    }
} 