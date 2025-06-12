package com.finance.admin.client.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "membership_number", unique = true, nullable = false, length = 20)
    private String membershipNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "email_primary", unique = true, nullable = false)
    private String emailPrimary;

    @Column(name = "email_secondary")
    private String emailSecondary;

    @Column(name = "phone_primary", length = 20)
    private String phonePrimary;

    @Column(name = "phone_secondary", length = 20)
    private String phoneSecondary;

    @Column(name = "address_street")
    private String addressStreet;

    @Column(name = "address_city", length = 100)
    private String addressCity;

    @Column(name = "address_state", length = 100)
    private String addressState;

    @Column(name = "address_postal_code", length = 20)
    private String addressPostalCode;

    @Column(name = "address_country", length = 100)
    private String addressCountry;

    @Column(name = "mailing_address_same")
    @Builder.Default
    private Boolean mailingAddressSame = true;

    @Column(name = "mailing_street")
    private String mailingStreet;

    @Column(name = "mailing_city", length = 100)
    private String mailingCity;

    @Column(name = "mailing_state", length = 100)
    private String mailingState;

    @Column(name = "mailing_postal_code", length = 20)
    private String mailingPostalCode;

    @Column(name = "mailing_country", length = 100)
    private String mailingCountry;

    @Column(name = "tfn_encrypted")
    private String tfnEncrypted;

    @Column(name = "tax_residency_status", length = 50)
    private String taxResidencyStatus;

    @Column(name = "bank_bsb", length = 10)
    private String bankBsb;

    @Column(name = "bank_account_number_encrypted")
    private String bankAccountNumberEncrypted;

    @Column(name = "bank_account_name")
    private String bankAccountName;

    @Column(name = "investment_target", precision = 15, scale = 2)
    private BigDecimal investmentTarget;

    @Column(name = "risk_profile", length = 50)
    private String riskProfile;

    @Column(name = "blockchain_identity_hash", length = 128)
    private String blockchainIdentityHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ClientStatus status = ClientStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    // Utility methods
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null) {
            fullName.append(firstName);
        }
        if (middleName != null && !middleName.trim().isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(middleName);
        }
        if (lastName != null) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(lastName);
        }
        return fullName.toString();
    }

    public boolean isActive() {
        return ClientStatus.ACTIVE.equals(this.status);
    }

    public enum ClientStatus {
        ACTIVE,
        INACTIVE,
        PENDING,
        SUSPENDED
    }
} 