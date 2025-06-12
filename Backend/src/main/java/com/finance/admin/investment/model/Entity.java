package com.finance.admin.investment.model;

import com.finance.admin.client.model.Client;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@jakarta.persistence.Entity
@Table(name = "entities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "entity_name", nullable = false)
    private String entityName;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "abn", length = 11)
    private String abn;

    @Column(name = "acn", length = 9)
    private String acn;

    @Column(name = "tfn_encrypted")
    private String tfnEncrypted;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    // Registered Address
    @Column(name = "registered_street")
    private String registeredStreet;

    @Column(name = "registered_city", length = 100)
    private String registeredCity;

    @Column(name = "registered_state", length = 100)
    private String registeredState;

    @Column(name = "registered_postal_code", length = 20)
    private String registeredPostalCode;

    @Column(name = "registered_country", length = 100)
    private String registeredCountry;

    // Contact Information
    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    // Tax Information
    @Column(name = "tax_residency_status", length = 50)
    private String taxResidencyStatus;

    @Column(name = "gst_registered")
    @Builder.Default
    private Boolean gstRegistered = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

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
    public String getFullRegisteredAddress() {
        StringBuilder address = new StringBuilder();
        if (registeredStreet != null) address.append(registeredStreet);
        if (registeredCity != null) {
            if (address.length() > 0) address.append(", ");
            address.append(registeredCity);
        }
        if (registeredState != null) {
            if (address.length() > 0) address.append(", ");
            address.append(registeredState);
        }
        if (registeredPostalCode != null) {
            if (address.length() > 0) address.append(" ");
            address.append(registeredPostalCode);
        }
        if (registeredCountry != null) {
            if (address.length() > 0) address.append(", ");
            address.append(registeredCountry);
        }
        return address.toString();
    }

    public boolean isActive() {
        return EntityStatus.ACTIVE.equals(this.status);
    }

    public boolean requiresAbn() {
        return entityType == EntityType.COMPANY || 
               entityType == EntityType.FAMILY_TRUST || 
               entityType == EntityType.UNIT_TRUST || 
               entityType == EntityType.DISCRETIONARY_TRUST || 
               entityType == EntityType.PARTNERSHIP ||
               entityType == EntityType.SMSF;
    }

    public boolean requiresAcn() {
        return entityType == EntityType.COMPANY;
    }

    // Entity Types Enum
    public enum EntityType {
        INDIVIDUAL("Individual Client"),
        JOINT_ACCOUNT("Joint Account"),
        COMPANY("Company"),
        FAMILY_TRUST("Family Trust"),
        UNIT_TRUST("Unit Trust"),
        DISCRETIONARY_TRUST("Discretionary Trust"),
        SMSF("Self-Managed Super Fund"),
        PARTNERSHIP("Partnership"),
        OTHER("Other");

        private final String displayName;

        EntityType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Entity Status Enum
    public enum EntityStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        PENDING("Pending Registration"),
        SUSPENDED("Suspended");

        private final String displayName;

        EntityStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 