package com.finance.admin.certificate.dto;

import com.finance.admin.certificate.model.Certificate;
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
public class CertificateResponse {

    private Long id;
    private String certificateNumber;
    
    // Certificate Details
    private Certificate.CertificateType certificateType;
    private String certificateTypeDisplay;
    private Certificate.CertificateStatus status;
    private String statusDisplay;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Integer version;

    // Investment Information
    private Long investmentId;
    private String investmentName;
    private String investmentType;
    private BigDecimal investmentAmount;
    private BigDecimal numberOfShares;
    private BigDecimal sharePrice;

    // Client Information
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private String membershipNumber;

    // Template Information
    private Long templateId;
    private String templateName;

    // File Information
    private String filePath;
    private Long fileSize;
    private String fileHash;
    private boolean hasFile;
    private String downloadUrl;

    // Computed Fields
    private boolean isActive;
    private boolean isExpired;
    private long daysUntilExpiry;
    private String certificateAge;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private java.util.UUID createdBy;
    private java.util.UUID updatedBy;
    private String createdByName;
    private String updatedByName;

    // Utility methods for computed fields
    public void computeFields() {
        this.certificateTypeDisplay = certificateType != null ? certificateType.getDisplayName() : null;
        this.statusDisplay = status != null ? status.getDisplayName() : null;
        this.isActive = Certificate.CertificateStatus.ACTIVE.equals(status);
        this.isExpired = expiryDate != null && LocalDate.now().isAfter(expiryDate);
        this.hasFile = filePath != null && !filePath.trim().isEmpty();
        
        if (expiryDate != null) {
            this.daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        }
        
        if (createdAt != null) {
            long daysSinceCreation = java.time.temporal.ChronoUnit.DAYS.between(createdAt.toLocalDate(), LocalDate.now());
            if (daysSinceCreation == 0) {
                this.certificateAge = "Today";
            } else if (daysSinceCreation == 1) {
                this.certificateAge = "1 day ago";
            } else if (daysSinceCreation < 30) {
                this.certificateAge = daysSinceCreation + " days ago";
            } else if (daysSinceCreation < 365) {
                long months = daysSinceCreation / 30;
                this.certificateAge = months + (months == 1 ? " month ago" : " months ago");
            } else {
                long years = daysSinceCreation / 365;
                this.certificateAge = years + (years == 1 ? " year ago" : " years ago");
            }
        }
        
        if (hasFile && certificateNumber != null) {
            this.downloadUrl = "/api/admin/certificates/" + id + "/download";
        }
    }
} 