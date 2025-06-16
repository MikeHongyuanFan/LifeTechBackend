package com.finance.admin.client.wallet.dto;

import com.finance.admin.client.wallet.model.ClientDigitalCertificate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalCertificateResponse {

    private Long id;
    private String certificateNumber;
    private ClientDigitalCertificate.CertificateType certificateType;
    private String companyName;
    private BigDecimal numberOfShares;
    private LocalDate issueDate;
    private String digitalSignature;
    private String blockchainHash;
    private Boolean isValid;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional display information
    private String shareClass;
    private BigDecimal nominalValue;
    private BigDecimal currentMarketValue;
    private String transferRestrictions;
    private Boolean votingRights;
    private Boolean dividendEntitlement;
    private LocalDate lastTransferDate;
    private String certificateUrl;

    // Verification status
    private Boolean isBlockchainVerified;
    private Boolean isDigitalSignatureValid;
    private String verificationStatus;

    // Display helpers
    private String certificateTypeDisplay;
    private String formattedMarketValue;
    private String shareClassDisplay;
    private Boolean canTransfer;
} 