package com.finance.admin.certificate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "certificate_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class CertificateTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 50)
    private TemplateType templateType;

    @Column(name = "template_content", nullable = false, columnDefinition = "TEXT")
    private String templateContent;

    @Column(name = "template_variables", columnDefinition = "TEXT")
    private String templateVariables; // JSON string of available variables

    // Template Settings
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    // File template information
    @Column(name = "template_file_path", length = 500)
    private String templateFilePath;

    @Column(name = "template_format", length = 20)
    @Builder.Default
    private String templateFormat = "PDF";

    // Styling and branding
    @Column(name = "company_logo_path", length = 500)
    private String companyLogoPath;

    @Column(name = "background_image_path", length = 500)
    private String backgroundImagePath;

    @Column(name = "primary_color", length = 7)
    @Builder.Default
    private String primaryColor = "#2196F3";

    @Column(name = "secondary_color", length = 7)
    @Builder.Default
    private String secondaryColor = "#4CAF50";

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

    // Utility methods
    public boolean isActiveTemplate() {
        return Boolean.TRUE.equals(this.isActive);
    }

    public boolean isDefaultTemplate() {
        return Boolean.TRUE.equals(this.isDefault);
    }

    // Template Type Enum
    public enum TemplateType {
        SHARE_CERTIFICATE("Share Certificate Template"),
        INVESTMENT_CERTIFICATE("Investment Certificate Template"),
        UNIT_CERTIFICATE("Unit Certificate Template"),
        BOND_CERTIFICATE("Bond Certificate Template"),
        EQUITY_CERTIFICATE("Equity Certificate Template"),
        CUSTOM("Custom Template");

        private final String displayName;

        TemplateType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 