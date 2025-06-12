package com.finance.admin.enquiry.dto;

import com.finance.admin.enquiry.model.Enquiry;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEnquiryRequest {

    @Size(max = 255, message = "Subject must not exceed 255 characters")
    private String subject;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Enquiry.EnquiryType enquiryType;
    private Enquiry.Priority priority;
    private Enquiry.EnquiryStatus status;

    // Contact information
    @Size(max = 100, message = "Contact name must not exceed 100 characters")
    private String contactName;

    @Email(message = "Contact email must be valid")
    private String contactEmail;

    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    // Assignment
    private java.util.UUID assignedTo;

    // Response and resolution
    @Size(max = 5000, message = "Response must not exceed 5000 characters")
    private String response;

    private LocalDateTime responseDate;
    private LocalDateTime resolvedDate;
    private LocalDateTime dueDate;

    // Additional details
    @Size(max = 50, message = "Source must not exceed 50 characters")
    private String source;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    @Size(max = 2000, message = "Internal notes must not exceed 2000 characters")
    private String internalNotes;
} 