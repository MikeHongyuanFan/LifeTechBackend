package com.finance.admin.client.profile.dto;

import com.finance.admin.client.model.Client;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for client profile information (Sprint 3.4)
 * Contains personal information and KYC status for client app
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientProfileResponse {

    // Basic Profile Information
    private Long id;
    private String membershipNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private LocalDate dateOfBirth;
    private Integer age;
    
    // Contact Information (Editable)
    private String emailPrimary;
    private String emailSecondary;
    private String phonePrimary;
    private String phoneSecondary;
    
    // Address Information (Editable)
    private String addressStreet;
    private String addressCity;
    private String addressState;
    private String addressPostalCode;
    private String addressCountry;
    private String fullAddress;
    
    // Mailing Address (Editable)
    private Boolean mailingAddressSame;
    private String mailingStreet;
    private String mailingCity;
    private String mailingState;
    private String mailingPostalCode;
    private String mailingCountry;
    private String fullMailingAddress;
    
    // Tax Information (Partially Editable)
    private String taxResidencyStatus;
    private Boolean hasTfn; // Don't expose actual TFN for security
    
    // Bank Information (Editable)
    private String bankBsb;
    private String bankAccountName;
    private Boolean hasBankAccount; // Don't expose actual account number
    
    // Investment Profile
    private BigDecimal investmentTarget;
    private String riskProfile;
    
    // Account Status
    private Client.ClientStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    
    // KYC Status Information
    private KycStatusInfo kycStatus;
    
    // Profile Completion
    private ProfileCompletionInfo profileCompletion;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KycStatusInfo {
        private KycStatus overallStatus;
        private Integer completionPercentage;
        private List<KycDocumentStatus> documentStatuses;
        private List<String> missingDocuments;
        private String nextSteps;
        private LocalDateTime lastUpdated;
        private LocalDateTime approvalDate;
        private LocalDateTime expiryDate;
        private Boolean requiresRenewal;
        
        public enum KycStatus {
            PASSED("KYC Completed Successfully"),
            PENDING("Under Review"),
            MISSING("Missing Required Documents"),
            REJECTED("Requires Resubmission"),
            EXPIRED("KYC Expired - Renewal Required");
            
            private final String displayName;
            
            KycStatus(String displayName) {
                this.displayName = displayName;
            }
            
            public String getDisplayName() {
                return displayName;
            }
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class KycDocumentStatus {
            private String documentType;
            private String documentName;
            private String status;
            private LocalDateTime uploadDate;
            private LocalDateTime reviewDate;
            private String notes;
            private Boolean isRequired;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileCompletionInfo {
        private Integer completionPercentage;
        private List<String> missingFields;
        private List<String> recommendedActions;
        private Boolean isProfileComplete;
    }
    
    // Helper methods for UI display
    public String getFormattedAddress() {
        if (addressStreet == null) return null;
        StringBuilder address = new StringBuilder();
        address.append(addressStreet);
        if (addressCity != null) address.append(", ").append(addressCity);
        if (addressState != null) address.append(", ").append(addressState);
        if (addressPostalCode != null) address.append(" ").append(addressPostalCode);
        if (addressCountry != null) address.append(", ").append(addressCountry);
        return address.toString();
    }
    
    public String getFormattedMailingAddress() {
        if (Boolean.TRUE.equals(mailingAddressSame)) {
            return getFormattedAddress();
        }
        if (mailingStreet == null) return null;
        StringBuilder address = new StringBuilder();
        address.append(mailingStreet);
        if (mailingCity != null) address.append(", ").append(mailingCity);
        if (mailingState != null) address.append(", ").append(mailingState);
        if (mailingPostalCode != null) address.append(" ").append(mailingPostalCode);
        if (mailingCountry != null) address.append(", ").append(mailingCountry);
        return address.toString();
    }
} 