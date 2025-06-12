package com.finance.admin.certificate.dto;

import com.finance.admin.certificate.model.Certificate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCertificateRequest {

    @NotNull(message = "Investment ID is required")
    private Long investmentId;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Certificate type is required")
    private Certificate.CertificateType certificateType;

    private Long templateId;

    private LocalDate issueDate;
    private LocalDate expiryDate;

    // Investment Information (optional - can be derived from investment)
    @Positive(message = "Investment amount must be positive")
    private BigDecimal investmentAmount;

    @Positive(message = "Number of shares must be positive")
    private BigDecimal numberOfShares;

    @Positive(message = "Share price must be positive")
    private BigDecimal sharePrice;

    // Generation options
    @Builder.Default
    private boolean generateImmediately = true;

    @Builder.Default
    private boolean sendNotification = true;

    private String notes;
} 