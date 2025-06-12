package com.finance.admin.enquiry.dto;

import com.finance.admin.enquiry.model.Enquiry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryResponse {

    private Long id;
    private String enquiryNumber;
    private String subject;
    private String description;
    private Enquiry.EnquiryType enquiryType;
    private String enquiryTypeDisplayName;
    private Enquiry.Priority priority;
    private String priorityDisplayName;
    private Enquiry.EnquiryStatus status;
    private String statusDisplayName;
    
    // Contact information
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    
    // Client information
    private Long clientId;
    private String clientName;
    private String clientEmail;
    
    // Assignment and response
    private java.util.UUID assignedTo;
    private String assignedToName;
    private String response;
    private LocalDateTime responseDate;
    private LocalDateTime resolvedDate;
    private LocalDateTime dueDate;
    
    // Additional details
    private String source;
    private String category;
    private String tags;
    private String internalNotes;
    
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private java.util.UUID createdBy;
    private String createdByName;
    private java.util.UUID updatedBy;
    private String updatedByName;
    
    // Computed fields
    private boolean isOpen;
    private boolean isResolved;
    private boolean isOverdue;
    private long responseTimeHours;
    private long daysSinceCreated;
    private long daysUntilDue;
} 