package com.finance.admin.enquiry.model;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "enquiries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Enquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "enquiry_number", unique = true, nullable = false, length = 20)
    private String enquiryNumber;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "enquiry_type", nullable = false)
    private EnquiryType enquiryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private EnquiryStatus status = EnquiryStatus.OPEN;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "assigned_to")
    private java.util.UUID assignedTo; // User ID

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "response_date")
    private LocalDateTime responseDate;

    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "source", length = 50)
    private String source; // EMAIL, PHONE, WEBSITE, IN_PERSON

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "tags")
    private String tags; // Comma-separated tags

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    private java.util.UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private java.util.UUID updatedBy;

    // Utility methods
    public boolean isOpen() {
        return EnquiryStatus.OPEN.equals(this.status);
    }

    public boolean isResolved() {
        return EnquiryStatus.RESOLVED.equals(this.status) || EnquiryStatus.CLOSED.equals(this.status);
    }

    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) && !isResolved();
    }

    public long getResponseTimeHours() {
        if (responseDate != null && createdAt != null) {
            return java.time.Duration.between(createdAt, responseDate).toHours();
        }
        return 0;
    }

    // Enquiry Types Enum
    public enum EnquiryType {
        GENERAL_INQUIRY("General Inquiry"),
        INVESTMENT_INQUIRY("Investment Inquiry"),
        ACCOUNT_SUPPORT("Account Support"),
        TECHNICAL_SUPPORT("Technical Support"),
        COMPLAINT("Complaint"),
        FEEDBACK("Feedback"),
        PARTNERSHIP("Partnership Inquiry"),
        MEDIA_INQUIRY("Media Inquiry"),
        COMPLIANCE("Compliance Inquiry"),
        OTHER("Other");

        private final String displayName;

        EnquiryType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Priority Enum
    public enum Priority {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        URGENT("Urgent");

        private final String displayName;

        Priority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Enquiry Status Enum
    public enum EnquiryStatus {
        OPEN("Open"),
        IN_PROGRESS("In Progress"),
        PENDING_CLIENT("Pending Client Response"),
        RESOLVED("Resolved"),
        CLOSED("Closed"),
        ESCALATED("Escalated");

        private final String displayName;

        EnquiryStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 