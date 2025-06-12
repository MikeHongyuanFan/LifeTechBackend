package com.finance.admin.client.dto;

import com.finance.admin.client.model.Client;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {

    private Long id;
    private String membershipNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private LocalDate dateOfBirth;
    private String emailPrimary;
    private String emailSecondary;
    private String phonePrimary;
    private String phoneSecondary;
    private String addressStreet;
    private String addressCity;
    private String addressState;
    private String addressPostalCode;
    private String addressCountry;
    private Boolean mailingAddressSame;
    private String mailingStreet;
    private String mailingCity;
    private String mailingState;
    private String mailingPostalCode;
    private String mailingCountry;
    private String taxResidencyStatus;
    private String bankBsb;
    private String bankAccountName;
    private BigDecimal investmentTarget;
    private String riskProfile;
    private String blockchainIdentityHash;
    private Client.ClientStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;

    // Sensitive fields are excluded (tfnEncrypted, bankAccountNumberEncrypted)
    // These should only be accessed through specific secure endpoints with proper authorization
} 