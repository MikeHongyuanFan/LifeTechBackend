package com.finance.admin.client.wallet.model;

import com.finance.admin.client.model.Client;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_digital_certificates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ClientDigitalCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "certificate_number", unique = true, nullable = false, length = 100)
    private String certificateNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_type", nullable = false, length = 50)
    private CertificateType certificateType;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "number_of_shares", precision = 15, scale = 4)
    private BigDecimal numberOfShares;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "digital_signature", columnDefinition = "TEXT")
    private String digitalSignature;

    @Column(name = "blockchain_hash", length = 255)
    private String blockchainHash;

    @Column(name = "certificate_data", columnDefinition = "JSON")
    private String certificateData;

    @Column(name = "is_valid")
    @Builder.Default
    private Boolean isValid = true;

    // Additional fields for enhanced functionality
    @Column(name = "share_class", length = 50)
    private String shareClass;

    @Column(name = "nominal_value", precision = 10, scale = 4)
    private BigDecimal nominalValue;

    @Column(name = "current_market_value", precision = 15, scale = 2)
    private BigDecimal currentMarketValue;

    @Column(name = "transfer_restrictions", columnDefinition = "TEXT")
    private String transferRestrictions;

    @Column(name = "voting_rights")
    private Boolean votingRights;

    @Column(name = "dividend_entitlement")
    private Boolean dividendEntitlement;

    @Column(name = "last_transfer_date")
    private LocalDate lastTransferDate;

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    public enum CertificateType {
        ORDINARY_SHARES,
        PREFERENCE_SHARES,
        CONVERTIBLE_SHARES,
        REDEEMABLE_SHARES,
        BONUS_SHARES,
        RIGHTS_SHARES,
        TREASURY_SHARES,
        OTHER
    }
} 