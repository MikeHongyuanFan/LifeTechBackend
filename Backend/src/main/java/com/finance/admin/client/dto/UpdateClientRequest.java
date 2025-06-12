package com.finance.admin.client.dto;

import jakarta.validation.constraints.*;
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
public class UpdateClientRequest {

    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Middle name must not exceed 100 characters")
    private String middleName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Email(message = "Primary email must be a valid email address")
    private String emailPrimary;

    @Email(message = "Secondary email must be a valid email address")
    private String emailSecondary;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Primary phone must be a valid phone number")
    private String phonePrimary;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Secondary phone must be a valid phone number")
    private String phoneSecondary;

    private String addressStreet;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String addressCity;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String addressState;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String addressPostalCode;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String addressCountry;

    private Boolean mailingAddressSame;

    private String mailingStreet;

    @Size(max = 100, message = "Mailing city must not exceed 100 characters")
    private String mailingCity;

    @Size(max = 100, message = "Mailing state must not exceed 100 characters")
    private String mailingState;

    @Size(max = 20, message = "Mailing postal code must not exceed 20 characters")
    private String mailingPostalCode;

    @Size(max = 100, message = "Mailing country must not exceed 100 characters")
    private String mailingCountry;

    @Pattern(regexp = "^\\d{9}$", message = "TFN must be exactly 9 digits")
    private String tfn;

    @Size(max = 50, message = "Tax residency status must not exceed 50 characters")
    private String taxResidencyStatus;

    @Pattern(regexp = "^\\d{6}$", message = "BSB must be exactly 6 digits")
    private String bankBsb;

    @Pattern(regexp = "^\\d{6,10}$", message = "Bank account number must be 6-10 digits")
    private String bankAccountNumber;

    private String bankAccountName;

    @DecimalMin(value = "0.0", inclusive = false, message = "Investment target must be positive")
    @Digits(integer = 13, fraction = 2, message = "Investment target must have at most 13 integer digits and 2 decimal places")
    private BigDecimal investmentTarget;

    @Size(max = 50, message = "Risk profile must not exceed 50 characters")
    private String riskProfile;
} 