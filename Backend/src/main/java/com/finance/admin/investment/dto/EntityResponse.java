package com.finance.admin.investment.dto;

import com.finance.admin.investment.model.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityResponse {

    private Long id;
    private Long clientId;
    private String clientName;
    private String entityName;
    private Entity.EntityType entityType;
    private String entityTypeDisplayName;
    private String registrationNumber;
    private String abn;
    private String acn;
    private LocalDate registrationDate;

    // Registered Address
    private String registeredStreet;
    private String registeredCity;
    private String registeredState;
    private String registeredPostalCode;
    private String registeredCountry;
    private String fullRegisteredAddress;

    // Contact Information
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;

    // Tax Information
    private String taxResidencyStatus;
    private Boolean gstRegistered;

    private Entity.EntityStatus status;
    private String statusDisplayName;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private String createdByName;
    private String updatedByName;

    // Utility fields
    private Boolean requiresAbn;
    private Boolean requiresAcn;
    private Boolean isActive;
} 