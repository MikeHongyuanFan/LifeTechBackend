package com.finance.admin.certificate.service;

import com.finance.admin.certificate.dto.CertificateResponse;
import com.finance.admin.certificate.dto.CreateCertificateRequest;
import com.finance.admin.certificate.model.Certificate;
import com.finance.admin.certificate.model.CertificateTemplate;
import com.finance.admin.certificate.repository.CertificateRepository;
import com.finance.admin.certificate.repository.CertificateTemplateRepository;
import com.finance.admin.client.model.Client;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.investment.model.Investment;
import com.finance.admin.investment.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateTemplateRepository templateRepository;
    private final ClientRepository clientRepository;
    private final InvestmentRepository investmentRepository;
    private final PdfGenerationService pdfGenerationService;
    private final CertificateEmailService certificateEmailService;

    /**
     * Create a new certificate
     */
    public CertificateResponse createCertificate(CreateCertificateRequest request) {
        log.info("Creating certificate for investment ID: {} and client ID: {}", 
            request.getInvestmentId(), request.getClientId());

        // Validate investment and client
        Investment investment = investmentRepository.findById(request.getInvestmentId())
            .orElseThrow(() -> new RuntimeException("Investment not found with ID: " + request.getInvestmentId()));

        Client client = clientRepository.findById(request.getClientId())
            .orElseThrow(() -> new RuntimeException("Client not found with ID: " + request.getClientId()));

        // Check if certificate already exists for this investment
        Optional<Certificate> existingCertificate = certificateRepository
            .findByInvestmentIdAndStatus(request.getInvestmentId(), Certificate.CertificateStatus.ACTIVE);
        
        if (existingCertificate.isPresent()) {
            throw new RuntimeException("Active certificate already exists for investment ID: " + request.getInvestmentId());
        }

        // Get or determine template
        CertificateTemplate template = getTemplateForCertificate(request);

        // Generate certificate number
        String certificateNumber = generateCertificateNumber(request.getCertificateType());

        // Create certificate entity
        Certificate certificate = Certificate.builder()
            .certificateNumber(certificateNumber)
            .investment(investment)
            .client(client)
            .template(template)
            .certificateType(request.getCertificateType())
            .issueDate(request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now())
            .expiryDate(request.getExpiryDate())
            .status(Certificate.CertificateStatus.PENDING)
            .investmentAmount(request.getInvestmentAmount() != null ? request.getInvestmentAmount() : investment.getInitialAmount())
            .numberOfShares(request.getNumberOfShares())
            .sharePrice(request.getSharePrice())
            .version(1)
            .build();

        // Save certificate
        Certificate savedCertificate = certificateRepository.save(certificate);

        // Generate PDF if requested
        if (request.isGenerateImmediately()) {
            generateCertificatePdf(savedCertificate);
        }

        log.info("Certificate created successfully with ID: {} and number: {}", 
            savedCertificate.getId(), certificateNumber);

        return mapToResponse(savedCertificate);
    }

    /**
     * Generate certificate automatically when investment is created
     */
    public CertificateResponse generateCertificateForInvestment(Investment investment) {
        log.info("Auto-generating certificate for investment: {}", investment.getId());

        // Determine certificate type based on investment type
        Certificate.CertificateType certificateType = determineCertificateType(investment);

        CreateCertificateRequest request = CreateCertificateRequest.builder()
            .investmentId(investment.getId())
            .clientId(investment.getClient().getId())
            .certificateType(certificateType)
            .investmentAmount(investment.getInitialAmount())
            .generateImmediately(true)
            .sendNotification(true)
            .build();

        return createCertificate(request);
    }

    /**
     * Get certificate by ID
     */
    @Transactional(readOnly = true)
    public CertificateResponse getCertificateById(Long id) {
        Certificate certificate = certificateRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Certificate not found with ID: " + id));
        
        return mapToResponse(certificate);
    }

    /**
     * Get certificate by certificate number
     */
    @Transactional(readOnly = true)
    public CertificateResponse getCertificateByCertificateNumber(String certificateNumber) {
        Certificate certificate = certificateRepository.findByCertificateNumber(certificateNumber)
            .orElseThrow(() -> new RuntimeException("Certificate not found with number: " + certificateNumber));
        
        return mapToResponse(certificate);
    }

    /**
     * Get all certificates with pagination
     */
    @Transactional(readOnly = true)
    public Page<CertificateResponse> getAllCertificates(Pageable pageable) {
        return certificateRepository.findAll(pageable)
            .map(this::mapToResponse);
    }

    /**
     * Get certificates by client ID
     */
    @Transactional(readOnly = true)
    public Page<CertificateResponse> getCertificatesByClientId(Long clientId, Pageable pageable) {
        // Validate client exists
        if (!clientRepository.existsById(clientId)) {
            throw new RuntimeException("Client not found with ID: " + clientId);
        }

        return certificateRepository.findByClientId(clientId, pageable)
            .map(this::mapToResponse);
    }

    /**
     * Search certificates with filters
     */
    @Transactional(readOnly = true)
    public Page<CertificateResponse> searchCertificates(
            Long clientId, Long investmentId, Certificate.CertificateType certificateType,
            Certificate.CertificateStatus status, LocalDate startDate, LocalDate endDate,
            String searchTerm, Pageable pageable) {

        return certificateRepository.findByMultipleFilters(
            clientId, investmentId, certificateType, status, startDate, endDate, searchTerm, pageable
        ).map(this::mapToResponse);
    }

    /**
     * Generate PDF for existing certificate
     */
    public CertificateResponse generateCertificatePdf(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new RuntimeException("Certificate not found with ID: " + certificateId));

        return generateCertificatePdf(certificate);
    }

    /**
     * Generate PDF for certificate
     */
    private CertificateResponse generateCertificatePdf(Certificate certificate) {
        log.info("Generating PDF for certificate: {}", certificate.getCertificateNumber());

        try {
            CertificateTemplate template = certificate.getTemplate();
            if (template == null) {
                template = getDefaultTemplate(certificate.getCertificateType());
            }

            PdfGenerationService.CertificateGenerationResult result = 
                pdfGenerationService.generateCertificatePdf(certificate, template);

            if (result.isSuccess()) {
                // Update certificate with file information
                certificate.setFilePath(result.getFilePath());
                certificate.setFileSize(result.getFileSize());
                certificate.setFileHash(result.getFileHash());
                certificate.setDigitalSignature(result.getDigitalSignature());
                certificate.setStatus(Certificate.CertificateStatus.ACTIVE);

                Certificate updatedCertificate = certificateRepository.save(certificate);
                log.info("Certificate PDF generated successfully: {}", result.getFileName());

                // Send email notification to client
                try {
                    certificateEmailService.sendCertificateGeneratedNotification(updatedCertificate);
                } catch (Exception e) {
                    log.warn("Failed to send certificate generation notification for certificate: {}", 
                        certificate.getCertificateNumber(), e);
                }

                return mapToResponse(updatedCertificate);
            } else {
                log.error("Failed to generate PDF for certificate: {}", result.getErrorMessage());
                throw new RuntimeException("Failed to generate certificate PDF: " + result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("Error generating PDF for certificate: {}", certificate.getCertificateNumber(), e);
            throw new RuntimeException("Failed to generate certificate PDF", e);
        }
    }

    /**
     * Generate batch certificates
     */
    public Map<String, CertificateResponse> generateBatchCertificates(List<Long> certificateIds) {
        log.info("Generating batch certificates for {} certificates", certificateIds.size());

        Map<String, CertificateResponse> results = new HashMap<>();
        
        for (Long certificateId : certificateIds) {
            try {
                CertificateResponse response = generateCertificatePdf(certificateId);
                results.put(response.getCertificateNumber(), response);
            } catch (Exception e) {
                log.error("Failed to generate certificate for ID: {}", certificateId, e);
                // Continue with other certificates
            }
        }

        return results;
    }

    /**
     * Update certificate status
     */
    public CertificateResponse updateCertificateStatus(Long certificateId, Certificate.CertificateStatus status) {
        Certificate certificate = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new RuntimeException("Certificate not found with ID: " + certificateId));

        certificate.setStatus(status);
        Certificate updatedCertificate = certificateRepository.save(certificate);

        log.info("Certificate status updated to {} for certificate: {}", status, certificate.getCertificateNumber());

        return mapToResponse(updatedCertificate);
    }

    /**
     * Delete certificate
     */
    public void deleteCertificate(Long certificateId) {
        revokeCertificate(certificateId, "Certificate deleted by administrator");
    }

    /**
     * Revoke certificate with reason
     */
    public CertificateResponse revokeCertificate(Long certificateId, String reason) {
        Certificate certificate = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new RuntimeException("Certificate not found with ID: " + certificateId));

        // Update status to REVOKED
        certificate.setStatus(Certificate.CertificateStatus.REVOKED);
        certificate.setUpdatedAt(LocalDateTime.now());
        Certificate revokedCertificate = certificateRepository.save(certificate);

        log.info("Certificate revoked: {} - Reason: {}", certificate.getCertificateNumber(), reason);

        // Send revocation notification
        try {
            certificateEmailService.sendCertificateRevokedNotification(revokedCertificate, reason);
        } catch (Exception e) {
            log.warn("Failed to send certificate revocation notification for certificate: {}", 
                certificate.getCertificateNumber(), e);
        }

        return mapToResponse(revokedCertificate);
    }

    /**
     * Get certificate statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCertificateStats() {
        Map<String, Object> stats = new HashMap<>();

        // Basic counts
        long totalCertificates = certificateRepository.count();
        long activeCertificates = certificateRepository.countByStatus(Certificate.CertificateStatus.ACTIVE);
        long pendingCertificates = certificateRepository.countByStatus(Certificate.CertificateStatus.PENDING);
        long expiredCertificates = certificateRepository.countByStatus(Certificate.CertificateStatus.EXPIRED);

        stats.put("totalCertificates", totalCertificates);
        stats.put("activeCertificates", activeCertificates);
        stats.put("pendingCertificates", pendingCertificates);
        stats.put("expiredCertificates", expiredCertificates);

        // Recent activity
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
        long newCertificatesLast30Days = certificateRepository.countCertificatesCreatedSince(last30Days);
        stats.put("newCertificatesLast30Days", newCertificatesLast30Days);

        // Certificates by type
        List<Object[]> typeStats = certificateRepository.getCertificateCountByType();
        Map<String, Long> certificatesByType = typeStats.stream()
            .collect(Collectors.toMap(
                row -> ((Certificate.CertificateType) row[0]).getDisplayName(),
                row -> (Long) row[1]
            ));
        stats.put("certificatesByType", certificatesByType);

        // Expiring certificates (next 30 days)
        LocalDate next30Days = LocalDate.now().plusDays(30);
        List<Certificate> expiringCertificates = certificateRepository.findCertificatesExpiringBetween(
            LocalDate.now(), next30Days);
        stats.put("certificatesExpiringNext30Days", expiringCertificates.size());

        return stats;
    }

    /**
     * Get template for certificate creation
     */
    private CertificateTemplate getTemplateForCertificate(CreateCertificateRequest request) {
        if (request.getTemplateId() != null) {
            return templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + request.getTemplateId()));
        }

        return getDefaultTemplate(request.getCertificateType());
    }

    /**
     * Get default template for certificate type
     */
    private CertificateTemplate getDefaultTemplate(Certificate.CertificateType certificateType) {
        CertificateTemplate.TemplateType templateType = mapCertificateTypeToTemplateType(certificateType);
        
        return templateRepository.findByTemplateTypeAndIsDefaultTrue(templateType)
            .orElse(null); // Will use basic template if no default found
    }

    /**
     * Map certificate type to template type
     */
    private CertificateTemplate.TemplateType mapCertificateTypeToTemplateType(Certificate.CertificateType certificateType) {
        return switch (certificateType) {
            case SHARE_CERTIFICATE -> CertificateTemplate.TemplateType.SHARE_CERTIFICATE;
            case INVESTMENT_CERTIFICATE -> CertificateTemplate.TemplateType.INVESTMENT_CERTIFICATE;
            case UNIT_CERTIFICATE -> CertificateTemplate.TemplateType.UNIT_CERTIFICATE;
            case BOND_CERTIFICATE -> CertificateTemplate.TemplateType.BOND_CERTIFICATE;
            case EQUITY_CERTIFICATE -> CertificateTemplate.TemplateType.EQUITY_CERTIFICATE;
        };
    }

    /**
     * Determine certificate type based on investment
     */
    private Certificate.CertificateType determineCertificateType(Investment investment) {
        Investment.InvestmentType investmentType = investment.getInvestmentType();
        
        return switch (investmentType) {
            case EQUITY_LISTED_SHARES, EQUITY_PRIVATE -> Certificate.CertificateType.SHARE_CERTIFICATE;
            case FIXED_INCOME_BONDS, FIXED_INCOME_GOVERNMENT, FIXED_INCOME_TERM_DEPOSITS -> Certificate.CertificateType.BOND_CERTIFICATE;
            case MANAGED_FUNDS_MUTUAL, MANAGED_FUNDS_ETF, MANAGED_FUNDS_HEDGE -> Certificate.CertificateType.UNIT_CERTIFICATE;
            default -> Certificate.CertificateType.INVESTMENT_CERTIFICATE;
        };
    }

    /**
     * Generate unique certificate number
     */
    private String generateCertificateNumber(Certificate.CertificateType certificateType) {
        String prefix = switch (certificateType) {
            case SHARE_CERTIFICATE -> "SHR";
            case INVESTMENT_CERTIFICATE -> "INV";
            case UNIT_CERTIFICATE -> "UNT";
            case BOND_CERTIFICATE -> "BND";
            case EQUITY_CERTIFICATE -> "EQT";
        };

        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        
        // Find next sequence number for this month
        String pattern = prefix + "-" + datePart + "-%";
        long count = certificateRepository.count(); // Simplified - in production, use proper sequence
        String sequence = String.format("%04d", (count % 10000) + 1);

        String certificateNumber = String.format("%s-%s-%s", prefix, datePart, sequence);

        // Ensure uniqueness
        while (certificateRepository.existsByCertificateNumber(certificateNumber)) {
            count++;
            sequence = String.format("%04d", (count % 10000) + 1);
            certificateNumber = String.format("%s-%s-%s", prefix, datePart, sequence);
        }

        return certificateNumber;
    }

    /**
     * Map Certificate entity to CertificateResponse DTO
     */
    private CertificateResponse mapToResponse(Certificate certificate) {
        CertificateResponse response = CertificateResponse.builder()
            .id(certificate.getId())
            .certificateNumber(certificate.getCertificateNumber())
            .certificateType(certificate.getCertificateType())
            .status(certificate.getStatus())
            .issueDate(certificate.getIssueDate())
            .expiryDate(certificate.getExpiryDate())
            .version(certificate.getVersion())
            .investmentAmount(certificate.getInvestmentAmount())
            .numberOfShares(certificate.getNumberOfShares())
            .sharePrice(certificate.getSharePrice())
            .filePath(certificate.getFilePath())
            .fileSize(certificate.getFileSize())
            .fileHash(certificate.getFileHash())
            .createdAt(certificate.getCreatedAt())
            .updatedAt(certificate.getUpdatedAt())
            .createdBy(certificate.getCreatedBy())
            .updatedBy(certificate.getUpdatedBy())
            .build();

        // Set investment information
        if (certificate.getInvestment() != null) {
            response.setInvestmentId(certificate.getInvestment().getId());
            response.setInvestmentName(certificate.getInvestment().getInvestmentName());
            response.setInvestmentType(certificate.getInvestment().getInvestmentType().getDisplayName());
        }

        // Set client information
        if (certificate.getClient() != null) {
            response.setClientId(certificate.getClient().getId());
            response.setClientName(certificate.getClient().getFirstName() + " " + certificate.getClient().getLastName());
            response.setClientEmail(certificate.getClient().getEmailPrimary());
            response.setMembershipNumber(certificate.getClient().getMembershipNumber());
        }

        // Set template information
        if (certificate.getTemplate() != null) {
            response.setTemplateId(certificate.getTemplate().getId());
            response.setTemplateName(certificate.getTemplate().getTemplateName());
        }

        // Compute derived fields
        response.computeFields();

        return response;
    }
} 