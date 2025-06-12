package com.finance.admin.investment.dto;

import com.finance.admin.investment.model.Entity;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEntityRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotBlank(message = "Entity name is required")
    @Size(max = 255, message = "Entity name must not exceed 255 characters")
    private String entityName;

    @NotNull(message = "Entity type is required")
    private Entity.EntityType entityType;

    @Size(max = 50, message = "Registration number must not exceed 50 characters")
    private String registrationNumber;

    @Pattern(regexp = "^\\d{11}$", message = "ABN must be exactly 11 digits")
    private String abn;

    @Pattern(regexp = "^\\d{9}$", message = "ACN must be exactly 9 digits")
    private String acn;

    @Pattern(regexp = "^\\d{9}$", message = "TFN must be exactly 9 digits")
    private String tfn;

    @PastOrPresent(message = "Registration date cannot be in the future")
    private LocalDate registrationDate;

    // Registered Address
    @Size(max = 255, message = "Registered street must not exceed 255 characters")
    private String registeredStreet;

    @Size(max = 100, message = "Registered city must not exceed 100 characters")
    private String registeredCity;

    @Size(max = 100, message = "Registered state must not exceed 100 characters")
    private String registeredState;

    @Size(max = 20, message = "Registered postal code must not exceed 20 characters")
    private String registeredPostalCode;

    @Size(max = 100, message = "Registered country must not exceed 100 characters")
    private String registeredCountry;

    // Contact Information
    @Size(max = 255, message = "Contact person must not exceed 255 characters")
    private String contactPerson;

    @Pattern(regexp = "^[\\+]?[0-9\\s\\-\\(\\)]+$", message = "Contact phone must be a valid phone number")
    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    @Email(message = "Contact email must be a valid email address")
    @Size(max = 255, message = "Contact email must not exceed 255 characters")
    private String contactEmail;

    // Tax Information
    @Size(max = 50, message = "Tax residency status must not exceed 50 characters")
    private String taxResidencyStatus;

    private Boolean gstRegistered;

    private Entity.EntityStatus status;
} 