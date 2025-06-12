package com.finance.admin.client.dto;

import com.finance.admin.client.model.Client;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientSearchRequest {

    // Search term for full-text search
    private String searchTerm;

    // Filter by Name
    private String firstName;
    private String lastName;
    private String fullName;

    // Filter by identifiers
    private String membershipNumber;
    private Long clientId;

    // Filter by status
    private Client.ClientStatus status;

    // Filter by registration date range
    private LocalDate registrationDateFrom;
    private LocalDate registrationDateTo;

    // Filter by location
    private String city;
    private String state;
    private String country;

    // Filter by investment amount range
    private BigDecimal investmentAmountFrom;
    private BigDecimal investmentAmountTo;

    // Filter by email
    private String email;

    // Filter by phone
    private String phone;

    // Filter by risk profile
    private String riskProfile;

    // Pagination parameters
    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    // Sorting parameters
    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDirection = "DESC";

    // Additional filters
    private Boolean hasBlockchainIdentity;
    private LocalDate dateOfBirthFrom;
    private LocalDate dateOfBirthTo;
} 