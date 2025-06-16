package com.finance.admin.client.wallet.service;

import com.finance.admin.client.model.Client;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.client.service.BlockchainService;
import com.finance.admin.client.wallet.dto.DigitalCertificateResponse;
import com.finance.admin.client.wallet.model.ClientDigitalCertificate;
import com.finance.admin.client.wallet.repository.ClientDigitalCertificateRepository;
import com.finance.admin.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientDigitalCertificateService {

    private final ClientDigitalCertificateRepository certificateRepository;
    private final ClientRepository clientRepository;
    private final BlockchainService blockchainService;

    /**
     * Get all digital certificates for a client
     */
    @Transactional(readOnly = true)
    public Page<DigitalCertificateResponse> getClientCertificates(Long clientId, Pageable pageable) {
        log.info("Getting digital certificates for client: {}", clientId);

        // Validate client exists
        clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + clientId));

        Page<ClientDigitalCertificate> certificates = certificateRepository
                .findByClientIdAndIsValidTrueOrderByIssueDateDesc(clientId, pageable);

        return certificates.map(this::mapToResponse);
    }

    /**
     * Get a specific digital certificate
     */
    @Transactional(readOnly = true)
    public DigitalCertificateResponse getCertificate(Long clientId, Long certificateId) {
        log.info("Getting digital certificate {} for client: {}", certificateId, clientId);

        ClientDigitalCertificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with ID: " + certificateId));

        // Verify certificate belongs to client
        if (!certificate.getClient().getId().equals(clientId)) {
            throw new RuntimeException("Certificate does not belong to client");
        }

        return mapToResponse(certificate);
    }

    /**
     * Get certificate by certificate number
     */
    @Transactional(readOnly = true)
    public DigitalCertificateResponse getCertificateByNumber(String certificateNumber) {
        log.info("Getting digital certificate with number: {}", certificateNumber);

        ClientDigitalCertificate certificate = certificateRepository
                .findByCertificateNumberAndIsValidTrue(certificateNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with number: " + certificateNumber));

        return mapToResponse(certificate);
    }

    /**
     * Search certificates for a client
     */
    @Transactional(readOnly = true)
    public Page<DigitalCertificateResponse> searchCertificates(
            Long clientId, String companyName, ClientDigitalCertificate.CertificateType certificateType,
            Boolean isValid, Pageable pageable) {
        log.info("Searching certificates for client: {} with filters", clientId);

        // Validate client exists
        clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + clientId));

        Page<ClientDigitalCertificate> certificates = certificateRepository
                .searchCertificates(clientId, companyName, certificateType, isValid, pageable);

        return certificates.map(this::mapToResponse);
    }

    /**
     * Calculate total market value of certificates for client
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalCertificateValue(Long clientId) {
        log.info("Calculating total certificate value for client: {}", clientId);

        return certificateRepository.calculateTotalMarketValueByClientId(clientId);
    }

    /**
     * Verify certificate authenticity
     */
    @Transactional(readOnly = true)
    public boolean verifyCertificateAuthenticity(String certificateNumber) {
        log.info("Verifying authenticity of certificate: {}", certificateNumber);

        ClientDigitalCertificate certificate = certificateRepository
                .findByCertificateNumberAndIsValidTrue(certificateNumber)
                .orElse(null);

        if (certificate == null) {
            return false;
        }

        // Verify digital signature
        boolean isSignatureValid = verifyDigitalSignature(certificate);
        
        // Verify blockchain hash if available
        boolean isBlockchainValid = verifyBlockchainHash(certificate);

        log.info("Certificate {} verification result - Signature: {}, Blockchain: {}", 
                certificateNumber, isSignatureValid, isBlockchainValid);

        return isSignatureValid && isBlockchainValid;
    }

    /**
     * Create mock certificates for testing (since we don't have real certificate issuance yet)
     */
    public List<DigitalCertificateResponse> createMockCertificatesForClient(Long clientId) {
        log.info("Creating mock certificates for client: {}", clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + clientId));

        List<ClientDigitalCertificate> certificates = createMockCertificates(client);
        
        return certificates.stream()
                .map(certificateRepository::save)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private List<ClientDigitalCertificate> createMockCertificates(Client client) {
        Random random = new Random();
        String[] companies = {"TechCorp Ltd", "GreenEnergy Inc", "FinanceFirst Pty Ltd", "HealthMax Ltd", "EduTech Solutions"};
        ClientDigitalCertificate.CertificateType[] types = ClientDigitalCertificate.CertificateType.values();

        return List.of(
                createMockCertificate(client, companies[0], types[0], random),
                createMockCertificate(client, companies[1], types[1], random),
                createMockCertificate(client, companies[2], types[0], random)
        );
    }

    private ClientDigitalCertificate createMockCertificate(Client client, String companyName, 
                                                         ClientDigitalCertificate.CertificateType type, Random random) {
        String certificateNumber = "CERT-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
        BigDecimal shares = BigDecimal.valueOf(100 + random.nextInt(1000));
        BigDecimal marketValue = shares.multiply(BigDecimal.valueOf(10 + random.nextInt(50)));

        // Generate mock blockchain hash
        String blockchainHash = generateMockBlockchainHash();
        
        // Generate mock digital signature
        String digitalSignature = generateMockDigitalSignature(certificateNumber, companyName);

        return ClientDigitalCertificate.builder()
                .client(client)
                .certificateNumber(certificateNumber)
                .certificateType(type)
                .companyName(companyName)
                .numberOfShares(shares)
                .issueDate(LocalDate.now().minusDays(random.nextInt(365)))
                .digitalSignature(digitalSignature)
                .blockchainHash(blockchainHash)
                .isValid(true)
                .shareClass("Ordinary")
                .nominalValue(BigDecimal.valueOf(1.00))
                .currentMarketValue(marketValue)
                .votingRights(true)
                .dividendEntitlement(true)
                .certificateUrl("https://certificates.lifetech.com/" + certificateNumber + ".pdf")
                .build();
    }

    private DigitalCertificateResponse mapToResponse(ClientDigitalCertificate certificate) {
        // Verify certificate authenticity
        boolean isBlockchainVerified = certificate.getBlockchainHash() != null && 
                                     verifyBlockchainHash(certificate);
        boolean isDigitalSignatureValid = certificate.getDigitalSignature() != null && 
                                        verifyDigitalSignature(certificate);

        String verificationStatus = "VERIFIED";
        if (!isBlockchainVerified || !isDigitalSignatureValid) {
            verificationStatus = "VERIFICATION_PENDING";
        }

        return DigitalCertificateResponse.builder()
                .id(certificate.getId())
                .certificateNumber(certificate.getCertificateNumber())
                .certificateType(certificate.getCertificateType())
                .companyName(certificate.getCompanyName())
                .numberOfShares(certificate.getNumberOfShares())
                .issueDate(certificate.getIssueDate())
                .digitalSignature(certificate.getDigitalSignature())
                .blockchainHash(certificate.getBlockchainHash())
                .isValid(certificate.getIsValid())
                .createdAt(certificate.getCreatedAt())
                .updatedAt(certificate.getUpdatedAt())
                .shareClass(certificate.getShareClass())
                .nominalValue(certificate.getNominalValue())
                .currentMarketValue(certificate.getCurrentMarketValue())
                .transferRestrictions(certificate.getTransferRestrictions())
                .votingRights(certificate.getVotingRights())
                .dividendEntitlement(certificate.getDividendEntitlement())
                .lastTransferDate(certificate.getLastTransferDate())
                .certificateUrl(certificate.getCertificateUrl())
                .isBlockchainVerified(isBlockchainVerified)
                .isDigitalSignatureValid(isDigitalSignatureValid)
                .verificationStatus(verificationStatus)
                .certificateTypeDisplay(formatCertificateType(certificate.getCertificateType()))
                .formattedMarketValue(formatCurrency(certificate.getCurrentMarketValue()))
                .shareClassDisplay(certificate.getShareClass() != null ? certificate.getShareClass() : "Standard")
                .canTransfer(certificate.getTransferRestrictions() == null || certificate.getTransferRestrictions().isEmpty())
                .build();
    }

    private boolean verifyDigitalSignature(ClientDigitalCertificate certificate) {
        // In a real implementation, this would verify the digital signature
        // For now, we'll simulate verification based on signature format
        String signature = certificate.getDigitalSignature();
        return signature != null && signature.startsWith("MOCK_SIG_") && signature.length() > 20;
    }

    private boolean verifyBlockchainHash(ClientDigitalCertificate certificate) {
        // In a real implementation, this would verify the blockchain hash
        // For now, we'll simulate verification
        String hash = certificate.getBlockchainHash();
        return hash != null && hash.startsWith("0x") && hash.length() == 66; // 64 hex chars + 0x prefix
    }

    private String generateMockBlockchainHash() {
        return "0x" + UUID.randomUUID().toString().replace("-", "") + 
               UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    private String generateMockDigitalSignature(String certificateNumber, String companyName) {
        String data = certificateNumber + companyName + LocalDateTime.now().toString();
        return "MOCK_SIG_" + Math.abs(data.hashCode());
    }

    private String formatCertificateType(ClientDigitalCertificate.CertificateType type) {
        switch (type) {
            case ORDINARY_SHARES:
                return "Ordinary Shares";
            case PREFERENCE_SHARES:
                return "Preference Shares";
            case CONVERTIBLE_SHARES:
                return "Convertible Shares";
            case REDEEMABLE_SHARES:
                return "Redeemable Shares";
            case BONUS_SHARES:
                return "Bonus Shares";
            case RIGHTS_SHARES:
                return "Rights Shares";
            case TREASURY_SHARES:
                return "Treasury Shares";
            default:
                return "Other";
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "N/A";
        }
        return String.format("$%,.2f AUD", amount);
    }
} 