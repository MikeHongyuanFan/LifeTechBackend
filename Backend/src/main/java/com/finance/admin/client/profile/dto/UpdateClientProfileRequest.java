package com.finance.admin.client.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating client profile information (Sprint 3.4)
 * Contains only editable fields that clients can modify
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateClientProfileRequest {

    // Contact Information (Editable)
    @Email(message = "Primary email must be valid")
    @Size(max = 255, message = "Primary email must not exceed 255 characters")
    private String emailPrimary;
    
    @Email(message = "Secondary email must be valid")
    @Size(max = 255, message = "Secondary email must not exceed 255 characters")
    private String emailSecondary;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{8,20}$", message = "Primary phone number format is invalid")
    private String phonePrimary;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{8,20}$", message = "Secondary phone number format is invalid")
    private String phoneSecondary;
    
    // Address Information (Editable)
    @Size(max = 255, message = "Street address must not exceed 255 characters")
    private String addressStreet;
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String addressCity;
    
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String addressState;
    
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String addressPostalCode;
    
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String addressCountry;
    
    // Mailing Address (Editable)
    private Boolean mailingAddressSame;
    
    @Size(max = 255, message = "Mailing street must not exceed 255 characters")
    private String mailingStreet;
    
    @Size(max = 100, message = "Mailing city must not exceed 100 characters")
    private String mailingCity;
    
    @Size(max = 100, message = "Mailing state must not exceed 100 characters")
    private String mailingState;
    
    @Size(max = 20, message = "Mailing postal code must not exceed 20 characters")
    private String mailingPostalCode;
    
    @Size(max = 100, message = "Mailing country must not exceed 100 characters")
    private String mailingCountry;
    
    // Bank Information (Editable)
    @Pattern(regexp = "^[0-9]{6}$", message = "BSB must be 6 digits")
    private String bankBsb;
    
    @Size(max = 255, message = "Bank account name must not exceed 255 characters")
    private String bankAccountName;
    
    @Pattern(regexp = "^[0-9]{6,12}$", message = "Bank account number must be 6-12 digits")
    private String bankAccountNumber;
    
    // Investment Profile (Editable)
    private BigDecimal investmentTarget;
    
    @Pattern(regexp = "^(CONSERVATIVE|MODERATE|BALANCED|GROWTH|AGGRESSIVE)$", 
             message = "Risk profile must be one of: CONSERVATIVE, MODERATE, BALANCED, GROWTH, AGGRESSIVE")
    private String riskProfile;
    
    // Emergency Contact (Editable)
    @Size(max = 255, message = "Emergency contact name must not exceed 255 characters")
    private String emergencyContactName;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{8,20}$", message = "Emergency contact phone format is invalid")
    private String emergencyContactPhone;
    
    @Size(max = 100, message = "Emergency contact relationship must not exceed 100 characters")
    private String emergencyContactRelationship;
    
    // Preferences
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean marketingOptIn;
    
    /**
     * Validate that if mailing address is not same as residential,
     * then mailing address fields should be provided
     */
    public boolean isMailingAddressValid() {
        if (Boolean.FALSE.equals(mailingAddressSame)) {
            return mailingStreet != null && !mailingStreet.trim().isEmpty() &&
                   mailingCity != null && !mailingCity.trim().isEmpty() &&
                   mailingState != null && !mailingState.trim().isEmpty() &&
                   mailingPostalCode != null && !mailingPostalCode.trim().isEmpty();
        }
        return true;
    }
    
    /**
     * Validate that if bank details are provided, all required fields are present
     */
    public boolean isBankDetailsValid() {
        if (bankBsb != null || bankAccountNumber != null || bankAccountName != null) {
            return bankBsb != null && !bankBsb.trim().isEmpty() &&
                   bankAccountNumber != null && !bankAccountNumber.trim().isEmpty() &&
                   bankAccountName != null && !bankAccountName.trim().isEmpty();
        }
        return true;
    }
} 