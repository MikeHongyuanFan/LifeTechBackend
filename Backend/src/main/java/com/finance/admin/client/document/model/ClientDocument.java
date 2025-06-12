package com.finance.admin.client.document.model;

import com.finance.admin.client.model.Client;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing client documents in the Legal Document Center
 * Supports document viewing, uploading, and management functionality
 */
@Entity
@Table(name = "client_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "Client is required")
    private Client client;

    @Column(name = "document_name", nullable = false)
    @NotBlank(message = "Document name is required")
    @Size(max = 255, message = "Document name must not exceed 255 characters")
    private String documentName;

    @Column(name = "document_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @Column(name = "document_category", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Document category is required")
    private DocumentCategory documentCategory;

    @Column(name = "file_path", nullable = false)
    @NotBlank(message = "File path is required")
    @Size(max = 500, message = "File path must not exceed 500 characters")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type")
    @Size(max = 100, message = "MIME type must not exceed 100 characters")
    private String mimeType;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @Column(name = "uploaded_by_client")
    @Builder.Default
    private Boolean uploadedByClient = true;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "document_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DocumentStatus documentStatus = DocumentStatus.UPLOADED;

    @Column(name = "version_number")
    @Builder.Default
    private Integer versionNumber = 1;

    @Column(name = "description")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "access_count")
    @Builder.Default
    private Integer accessCount = 0;

    @Column(name = "last_accessed_date")
    private LocalDateTime lastAccessedDate;

    @Column(name = "tags")
    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Document types for classification
     */
    public enum DocumentType {
        // System-provided documents
        INVESTMENT_AGREEMENT("Investment Agreement"),
        TERMS_CONDITIONS("Terms and Conditions"),
        REGULATORY_DISCLOSURE("Regulatory Disclosure"),
        TAX_DOCUMENT("Tax Document"),
        COMPANY_POLICY("Company Policy"),
        PRODUCT_DISCLOSURE("Product Disclosure Statement"),
        
        // Client-uploaded documents
        KYC_DOCUMENT("KYC Document"),
        BANK_STATEMENT("Bank Statement"),
        IDENTITY_VERIFICATION("Identity Verification"),
        ADDRESS_PROOF("Address Proof"),
        TAX_FILE_NUMBER("Tax File Number"),
        BANK_ACCOUNT_UPDATE("Bank Account Update"),
        OTHER("Other");

        private final String displayName;

        DocumentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Document categories for organization
     */
    public enum DocumentCategory {
        LEGAL("Legal Documents"),
        COMPLIANCE("Compliance Documents"),
        KYC("Know Your Customer"),
        FINANCIAL("Financial Documents"),
        IDENTITY("Identity Documents"),
        TAX("Tax Documents"),
        INVESTMENT("Investment Documents"),
        REGULATORY("Regulatory Documents"),
        POLICY("Policy Documents"),
        PERSONAL("Personal Documents");

        private final String displayName;

        DocumentCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Document processing status
     */
    public enum DocumentStatus {
        UPLOADED("Uploaded"),
        UNDER_REVIEW("Under Review"),
        APPROVED("Approved"),
        REJECTED("Rejected"),
        EXPIRED("Expired"),
        REPLACED("Replaced"),
        ARCHIVED("Archived");

        private final String displayName;

        DocumentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Increment access count and update last accessed date
     */
    public void incrementAccessCount() {
        this.accessCount = (this.accessCount == null) ? 1 : this.accessCount + 1;
        this.lastAccessedDate = LocalDateTime.now();
    }

    /**
     * Check if document is expired
     */
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Check if document is active and not expired
     */
    public boolean isActiveAndValid() {
        return Boolean.TRUE.equals(isActive) && !isExpired();
    }
} 