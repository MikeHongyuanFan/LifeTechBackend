package com.finance.admin.certificate.controller;

import com.finance.admin.certificate.dto.CertificateResponse;
import com.finance.admin.certificate.dto.CreateCertificateRequest;
import com.finance.admin.certificate.model.Certificate;
import com.finance.admin.certificate.service.CertificateService;
import com.finance.admin.certificate.service.CertificateExpiryMonitoringService;
import com.finance.admin.certificate.service.CertificateEmailService;
import com.finance.admin.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/certificates")
@RequiredArgsConstructor
@Slf4j
public class CertificateController {

    private final CertificateService certificateService;
    private final CertificateExpiryMonitoringService expiryMonitoringService;
    private final CertificateEmailService certificateEmailService;

    /**
     * Create a new certificate
     */
    @PostMapping
    public ResponseEntity<CertificateResponse> createCertificate(@Valid @RequestBody CreateCertificateRequest request) {
        log.info("Creating certificate for investment ID: {}", request.getInvestmentId());
        CertificateResponse response = certificateService.createCertificate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all certificates with pagination
     */
    @GetMapping
    public ResponseEntity<Page<CertificateResponse>> getAllCertificates(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CertificateResponse> certificates = certificateService.getAllCertificates(pageable);
        return ResponseEntity.ok(certificates);
    }

    /**
     * Get certificate by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CertificateResponse> getCertificateById(@PathVariable Long id) {
        CertificateResponse certificate = certificateService.getCertificateById(id);
        return ResponseEntity.ok(certificate);
    }

    /**
     * Get certificate by certificate number
     */
    @GetMapping("/number/{certificateNumber}")
    public ResponseEntity<CertificateResponse> getCertificateByCertificateNumber(
            @PathVariable String certificateNumber) {
        CertificateResponse certificate = certificateService.getCertificateByCertificateNumber(certificateNumber);
        return ResponseEntity.ok(certificate);
    }

    /**
     * Search certificates with filters
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CertificateResponse>> searchCertificates(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long investmentId,
            @RequestParam(required = false) Certificate.CertificateType certificateType,
            @RequestParam(required = false) Certificate.CertificateStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<CertificateResponse> certificates = certificateService.searchCertificates(
            clientId, investmentId, certificateType, status, startDate, endDate, searchTerm, pageable);
        
        return ResponseEntity.ok(certificates);
    }

    /**
     * Get certificates by client ID
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<Page<CertificateResponse>> getCertificatesByClientId(
            @PathVariable Long clientId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CertificateResponse> certificates = certificateService.getCertificatesByClientId(clientId, pageable);
        return ResponseEntity.ok(certificates);
    }

    /**
     * Generate PDF for existing certificate
     */
    @PostMapping("/{id}/generate")
    public ResponseEntity<CertificateResponse> generateCertificatePdf(@PathVariable Long id) {
        log.info("Generating PDF for certificate ID: {}", id);
        CertificateResponse response = certificateService.generateCertificatePdf(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Generate batch certificates
     */
    @PostMapping("/batch-generate")
    public ResponseEntity<Map<String, CertificateResponse>> generateBatchCertificates(
            @RequestBody List<Long> certificateIds) {
        log.info("Generating batch certificates for {} certificates", certificateIds.size());
        Map<String, CertificateResponse> results = certificateService.generateBatchCertificates(certificateIds);
        return ResponseEntity.ok(results);
    }

    /**
     * Update certificate status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<CertificateResponse> updateCertificateStatus(
            @PathVariable Long id,
            @RequestParam Certificate.CertificateStatus status) {
        log.info("Updating certificate {} status to {}", id, status);
        CertificateResponse response = certificateService.updateCertificateStatus(id, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete (revoke) certificate
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertificate(@PathVariable Long id) {
        log.info("Deleting certificate ID: {}", id);
        certificateService.deleteCertificate(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get certificate statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCertificateStats() {
        Map<String, Object> stats = certificateService.getCertificateStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Download certificate PDF
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<String> downloadCertificate(@PathVariable Long id) {
        // This is a mock implementation
        // In production, this would return the actual PDF file
        log.info("Download request for certificate ID: {}", id);
        
        CertificateResponse certificate = certificateService.getCertificateById(id);
        
        if (!certificate.isHasFile()) {
            return ResponseEntity.notFound().build();
        }
        
        // Mock download response
        String downloadInfo = String.format(
            "Certificate download for: %s\nFile: %s\nSize: %d bytes", 
            certificate.getCertificateNumber(),
            certificate.getFilePath(),
            certificate.getFileSize()
        );
        
        return ResponseEntity.ok()
            .header("Content-Type", "application/pdf")
            .header("Content-Disposition", "attachment; filename=\"" + certificate.getCertificateNumber() + ".pdf\"")
            .body(downloadInfo);
    }

    /**
     * Verify certificate authenticity
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<Map<String, Object>> verifyCertificate(@PathVariable Long id) {
        log.info("Verifying certificate ID: {}", id);
        
        CertificateResponse certificate = certificateService.getCertificateById(id);
        
        Map<String, Object> verificationResult = Map.of(
            "certificateNumber", certificate.getCertificateNumber(),
            "isValid", certificate.isActive() && !certificate.isExpired(),
            "status", certificate.getStatus(),
            "issueDate", certificate.getIssueDate(),
            "expiryDate", certificate.getExpiryDate(),
            "clientName", certificate.getClientName(),
            "investmentAmount", certificate.getInvestmentAmount(),
            "verifiedAt", java.time.LocalDateTime.now()
        );
        
        return ResponseEntity.ok(verificationResult);
    }

    /**
     * Revoke certificate with reason
     */
    @PostMapping("/{id}/revoke")
    public ResponseEntity<CertificateResponse> revokeCertificate(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Revoked by administrator") String reason) {
        log.info("Revoking certificate ID: {} with reason: {}", id, reason);
        CertificateResponse response = certificateService.revokeCertificate(id, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Get expiry statistics
     */
    @GetMapping("/expiry/stats")
    public ResponseEntity<CertificateExpiryMonitoringService.ExpiryStatistics> getExpiryStatistics() {
        CertificateExpiryMonitoringService.ExpiryStatistics stats = expiryMonitoringService.getExpiryStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get detailed expiry report
     */
    @GetMapping("/expiry/report")
    public ResponseEntity<CertificateExpiryMonitoringService.ExpiryReport> getExpiryReport() {
        CertificateExpiryMonitoringService.ExpiryReport report = expiryMonitoringService.getDetailedExpiryReport();
        return ResponseEntity.ok(report);
    }

    /**
     * Get certificates expiring within specified days
     */
    @GetMapping("/expiry/within/{days}")
    public ResponseEntity<List<CertificateResponse>> getCertificatesExpiringWithinDays(@PathVariable int days) {
        List<Certificate> certificates = expiryMonitoringService.getCertificatesExpiringWithinDays(days);
        List<CertificateResponse> responses = certificates.stream()
            .map(cert -> certificateService.getCertificateById(cert.getId()))
            .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get overdue certificates
     */
    @GetMapping("/expiry/overdue")
    public ResponseEntity<List<CertificateResponse>> getOverdueCertificates() {
        List<Certificate> certificates = expiryMonitoringService.getOverdueCertificates();
        List<CertificateResponse> responses = certificates.stream()
            .map(cert -> certificateService.getCertificateById(cert.getId()))
            .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Manually trigger expiry check
     */
    @PostMapping("/expiry/check")
    public ResponseEntity<Map<String, String>> triggerExpiryCheck() {
        log.info("Manual expiry check triggered");
        expiryMonitoringService.performManualExpiryCheck();
        return ResponseEntity.ok(Map.of("message", "Expiry check completed successfully"));
    }

    /**
     * Renew certificate
     */
    @PostMapping("/{id}/renew")
    public ResponseEntity<CertificateResponse> renewCertificate(
            @PathVariable Long id,
            @RequestParam(defaultValue = "12") int extensionMonths) {
        log.info("Renewing certificate ID: {} for {} months", id, extensionMonths);
        Certificate renewed = expiryMonitoringService.renewCertificate(id, extensionMonths);
        CertificateResponse response = certificateService.getCertificateById(renewed.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-email")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Test email functionality", description = "Send a test email to verify Gmail SMTP integration")
    public ResponseEntity<ApiResponse<String>> testEmail(
            @RequestParam String toEmail,
            @RequestParam(required = false, defaultValue = "Test Email from LifeTech Certificate System") String subject) {
        try {
            certificateEmailService.sendTestEmail(toEmail, subject);
            return ResponseEntity.ok(ApiResponse.success("Test email sent successfully to " + toEmail));
        } catch (Exception e) {
            log.error("Failed to send test email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to send test email: " + e.getMessage()));
        }
    }
} 