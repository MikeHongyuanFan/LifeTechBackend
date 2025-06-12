package com.finance.admin.certificate.model;

import com.finance.admin.client.model.Client;
import com.finance.admin.investment.model.Investment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "certificate_number", unique = true, nullable = false, length = 50)
    private String certificateNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investment_id", nullable = false)
    private Investment investment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private CertificateTemplate template;

    // Certificate Details
    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_type", nullable = false, length = 50)
    private CertificateType certificateType;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private CertificateStatus status = CertificateStatus.ACTIVE;

    // Investment Information
    @Column(name = "investment_amount", precision = 15, scale = 2)
    private BigDecimal investmentAmount;

    @Column(name = "number_of_shares", precision = 15, scale = 4)
    private BigDecimal numberOfShares;

    @Column(name = "share_price", precision = 10, scale = 4)
    private BigDecimal sharePrice;

    // File Information
    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_hash", length = 128)
    private String fileHash;

    @Column(name = "digital_signature", columnDefinition = "TEXT")
    private String digitalSignature;

    // Metadata
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false)
    private java.util.UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private java.util.UUID updatedBy;

    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    // Utility methods
    public boolean isActive() {
        return CertificateStatus.ACTIVE.equals(this.status);
    }

    public boolean isExpired() {
        return expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }

    public boolean hasFile() {
        return filePath != null && !filePath.trim().isEmpty();
    }

    // Certificate Type Enum
    public enum CertificateType {
        SHARE_CERTIFICATE("Share Certificate"),
        INVESTMENT_CERTIFICATE("Investment Certificate"),
        UNIT_CERTIFICATE("Unit Certificate"),
        BOND_CERTIFICATE("Bond Certificate"),
        EQUITY_CERTIFICATE("Equity Certificate");

        private final String displayName;

        CertificateType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Certificate Status Enum
    public enum CertificateStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        EXPIRED("Expired"),
        REVOKED("Revoked"),
        PENDING("Pending");

        private final String displayName;

        CertificateStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 