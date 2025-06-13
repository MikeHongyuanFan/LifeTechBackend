package com.finance.admin.client.profile.service;

import com.finance.admin.client.model.Client;
import com.finance.admin.client.model.ClientLoginHistory;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.client.repository.ClientLoginHistoryRepository;
import com.finance.admin.client.profile.dto.ClientProfileResponse;
import com.finance.admin.client.profile.dto.UpdateClientProfileRequest;
import com.finance.admin.client.document.repository.ClientDocumentRepository;
import com.finance.admin.client.document.model.ClientDocument;
import com.finance.admin.common.exception.ResourceNotFoundException;
import com.finance.admin.common.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for client profile management (Sprint 3.4)
 * Handles profile viewing, editing, and KYC status tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientProfileService {

    private final ClientRepository clientRepository;
    private final ClientLoginHistoryRepository loginHistoryRepository;
    private final ClientDocumentRepository documentRepository;

    /**
     * Get client profile information (Sprint 3.4.1 - View Info)
     */
    @Transactional(readOnly = true)
    public ClientProfileResponse getClientProfile(Long clientId) {
        log.info("Getting profile for client ID: {}", clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + clientId));

        // Get last login time
        List<ClientLoginHistory> loginHistory = loginHistoryRepository
                .findByClientIdOrderByLoginTimestampDesc(clientId);
        LocalDateTime lastLoginAt = loginHistory.isEmpty() ? null : 
                loginHistory.get(0).getLoginTimestamp();

        // Build profile response
        ClientProfileResponse response = ClientProfileResponse.builder()
                .id(client.getId())
                .membershipNumber(client.getMembershipNumber())
                .firstName(client.getFirstName())
                .middleName(client.getMiddleName())
                .lastName(client.getLastName())
                .fullName(client.getFullName())
                .dateOfBirth(client.getDateOfBirth())
                .age(calculateAge(client.getDateOfBirth()))
                .emailPrimary(client.getEmailPrimary())
                .emailSecondary(client.getEmailSecondary())
                .phonePrimary(client.getPhonePrimary())
                .phoneSecondary(client.getPhoneSecondary())
                .addressStreet(client.getAddressStreet())
                .addressCity(client.getAddressCity())
                .addressState(client.getAddressState())
                .addressPostalCode(client.getAddressPostalCode())
                .addressCountry(client.getAddressCountry())
                .mailingAddressSame(client.getMailingAddressSame())
                .mailingStreet(client.getMailingStreet())
                .mailingCity(client.getMailingCity())
                .mailingState(client.getMailingState())
                .mailingPostalCode(client.getMailingPostalCode())
                .mailingCountry(client.getMailingCountry())
                .taxResidencyStatus(client.getTaxResidencyStatus())
                .hasTfn(StringUtils.hasText(client.getTfnEncrypted()))
                .bankBsb(client.getBankBsb())
                .bankAccountName(client.getBankAccountName())
                .hasBankAccount(StringUtils.hasText(client.getBankAccountNumberEncrypted()))
                .investmentTarget(client.getInvestmentTarget())
                .riskProfile(client.getRiskProfile())
                .status(client.getStatus())
                .createdAt(client.getCreatedAt())
                .lastLoginAt(lastLoginAt)
                .kycStatus(getKycStatusInfo(clientId))
                .profileCompletion(getProfileCompletionInfo(client))
                .build();

        // Set formatted addresses
        response.setFullAddress(response.getFormattedAddress());
        response.setFullMailingAddress(response.getFormattedMailingAddress());

        log.info("Retrieved profile for client: {}", client.getFullName());
        return response;
    }

    /**
     * Update client profile information (Sprint 3.4.2 - Edit Profile)
     */
    public ClientProfileResponse updateClientProfile(Long clientId, UpdateClientProfileRequest request) {
        log.info("Updating profile for client ID: {}", clientId);

        // Validate request
        validateUpdateRequest(request);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + clientId));

        // Check for email uniqueness if changed
        if (StringUtils.hasText(request.getEmailPrimary()) && 
            !request.getEmailPrimary().equals(client.getEmailPrimary())) {
            if (clientRepository.existsByEmailPrimaryAndIdNot(request.getEmailPrimary(), clientId)) {
                throw new ValidationException("Email already exists: " + request.getEmailPrimary());
            }
        }

        // Update editable fields
        updateClientFields(client, request);

        // Save changes
        client = clientRepository.save(client);

        log.info("Updated profile for client: {}", client.getFullName());
        return getClientProfile(clientId);
    }

    /**
     * Get KYC status information (Sprint 3.4.3 - KYC Status Tracking)
     * Mock implementation until KYC API is integrated
     */
    @Transactional(readOnly = true)
    public ClientProfileResponse.KycStatusInfo getKycStatusInfo(Long clientId) {
        log.debug("Getting KYC status for client ID: {}", clientId);

        // Get client documents to determine KYC status
        List<ClientDocument> kycDocuments = documentRepository
                .findByClientIdAndDocumentCategoryAndIsActiveTrue(clientId, ClientDocument.DocumentCategory.KYC);

        // Mock KYC status based on available documents
        ClientProfileResponse.KycStatusInfo.KycStatus overallStatus;
        List<String> missingDocuments = new ArrayList<>();
        List<ClientProfileResponse.KycStatusInfo.KycDocumentStatus> documentStatuses = new ArrayList<>();
        String nextSteps;
        Integer completionPercentage;

        // Required KYC documents
        Map<String, String> requiredDocs = Map.of(
                "IDENTITY_VERIFICATION", "Identity Verification",
                "ADDRESS_PROOF", "Proof of Address",
                "TAX_FILE_NUMBER", "Tax File Number",
                "BANK_STATEMENT", "Bank Statement"
        );

        // Check which documents are available
        Set<String> availableDocTypes = kycDocuments.stream()
                .map(doc -> doc.getDocumentType().name())
                .collect(Collectors.toSet());

        // Build document statuses
        for (Map.Entry<String, String> entry : requiredDocs.entrySet()) {
            String docType = entry.getKey();
            String docName = entry.getValue();
            
            if (availableDocTypes.contains(docType)) {
                // Find the document
                ClientDocument doc = kycDocuments.stream()
                        .filter(d -> d.getDocumentType().name().equals(docType))
                        .findFirst()
                        .orElse(null);
                
                if (doc != null) {
                    documentStatuses.add(ClientProfileResponse.KycStatusInfo.KycDocumentStatus.builder()
                            .documentType(docType)
                            .documentName(docName)
                            .status(doc.getDocumentStatus().getDisplayName())
                            .uploadDate(doc.getUploadDate())
                            .reviewDate(doc.getUpdatedAt())
                            .notes("Document uploaded successfully")
                            .isRequired(true)
                            .build());
                }
            } else {
                missingDocuments.add(docName);
                documentStatuses.add(ClientProfileResponse.KycStatusInfo.KycDocumentStatus.builder()
                        .documentType(docType)
                        .documentName(docName)
                        .status("MISSING")
                        .uploadDate(null)
                        .reviewDate(null)
                        .notes("Document not yet uploaded")
                        .isRequired(true)
                        .build());
            }
        }

        // Determine overall status and completion percentage
        if (missingDocuments.isEmpty()) {
            overallStatus = ClientProfileResponse.KycStatusInfo.KycStatus.PASSED;
            completionPercentage = 100;
            nextSteps = "KYC verification completed. No further action required.";
        } else if (kycDocuments.isEmpty()) {
            overallStatus = ClientProfileResponse.KycStatusInfo.KycStatus.MISSING;
            completionPercentage = 0;
            nextSteps = "Please upload all required KYC documents to complete verification.";
        } else {
            overallStatus = ClientProfileResponse.KycStatusInfo.KycStatus.PENDING;
            completionPercentage = (int) ((double) (requiredDocs.size() - missingDocuments.size()) / requiredDocs.size() * 100);
            nextSteps = "Upload missing documents: " + String.join(", ", missingDocuments);
        }

        return ClientProfileResponse.KycStatusInfo.builder()
                .overallStatus(overallStatus)
                .completionPercentage(completionPercentage)
                .documentStatuses(documentStatuses)
                .missingDocuments(missingDocuments)
                .nextSteps(nextSteps)
                .lastUpdated(LocalDateTime.now())
                .approvalDate(overallStatus == ClientProfileResponse.KycStatusInfo.KycStatus.PASSED ? LocalDateTime.now().minusDays(7) : null)
                .expiryDate(overallStatus == ClientProfileResponse.KycStatusInfo.KycStatus.PASSED ? LocalDateTime.now().plusYears(2) : null)
                .requiresRenewal(false)
                .build();
    }

    /**
     * Get profile completion information
     */
    private ClientProfileResponse.ProfileCompletionInfo getProfileCompletionInfo(Client client) {
        List<String> missingFields = new ArrayList<>();
        List<String> recommendedActions = new ArrayList<>();

        // Check required fields
        if (!StringUtils.hasText(client.getPhonePrimary())) {
            missingFields.add("Primary Phone Number");
        }
        if (!StringUtils.hasText(client.getAddressStreet())) {
            missingFields.add("Street Address");
        }
        if (!StringUtils.hasText(client.getTaxResidencyStatus())) {
            missingFields.add("Tax Residency Status");
        }
        if (!StringUtils.hasText(client.getBankBsb()) || !StringUtils.hasText(client.getBankAccountNumberEncrypted())) {
            missingFields.add("Bank Account Details");
            recommendedActions.add("Add bank account information for investment distributions");
        }
        if (client.getInvestmentTarget() == null) {
            missingFields.add("Investment Target");
            recommendedActions.add("Set your investment goals and target amount");
        }
        if (!StringUtils.hasText(client.getRiskProfile())) {
            missingFields.add("Risk Profile");
            recommendedActions.add("Complete risk assessment to match suitable investments");
        }

        int totalFields = 10; // Total important fields
        int completedFields = totalFields - missingFields.size();
        int completionPercentage = (int) ((double) completedFields / totalFields * 100);

        return ClientProfileResponse.ProfileCompletionInfo.builder()
                .completionPercentage(completionPercentage)
                .missingFields(missingFields)
                .recommendedActions(recommendedActions)
                .isProfileComplete(missingFields.isEmpty())
                .build();
    }

    /**
     * Update client fields from request
     */
    private void updateClientFields(Client client, UpdateClientProfileRequest request) {
        // Contact Information
        if (StringUtils.hasText(request.getEmailPrimary())) {
            client.setEmailPrimary(request.getEmailPrimary());
        }
        if (request.getEmailSecondary() != null) {
            client.setEmailSecondary(request.getEmailSecondary());
        }
        if (request.getPhonePrimary() != null) {
            client.setPhonePrimary(request.getPhonePrimary());
        }
        if (request.getPhoneSecondary() != null) {
            client.setPhoneSecondary(request.getPhoneSecondary());
        }

        // Address Information
        if (request.getAddressStreet() != null) {
            client.setAddressStreet(request.getAddressStreet());
        }
        if (request.getAddressCity() != null) {
            client.setAddressCity(request.getAddressCity());
        }
        if (request.getAddressState() != null) {
            client.setAddressState(request.getAddressState());
        }
        if (request.getAddressPostalCode() != null) {
            client.setAddressPostalCode(request.getAddressPostalCode());
        }
        if (request.getAddressCountry() != null) {
            client.setAddressCountry(request.getAddressCountry());
        }

        // Mailing Address
        if (request.getMailingAddressSame() != null) {
            client.setMailingAddressSame(request.getMailingAddressSame());
        }
        if (request.getMailingStreet() != null) {
            client.setMailingStreet(request.getMailingStreet());
        }
        if (request.getMailingCity() != null) {
            client.setMailingCity(request.getMailingCity());
        }
        if (request.getMailingState() != null) {
            client.setMailingState(request.getMailingState());
        }
        if (request.getMailingPostalCode() != null) {
            client.setMailingPostalCode(request.getMailingPostalCode());
        }
        if (request.getMailingCountry() != null) {
            client.setMailingCountry(request.getMailingCountry());
        }

        // Bank Information
        if (request.getBankBsb() != null) {
            client.setBankBsb(request.getBankBsb());
        }
        if (request.getBankAccountName() != null) {
            client.setBankAccountName(request.getBankAccountName());
        }
        // Note: Bank account number would need encryption service
        // if (StringUtils.hasText(request.getBankAccountNumber())) {
        //     client.setBankAccountNumberEncrypted(encryptionService.encrypt(request.getBankAccountNumber()));
        // }

        // Investment Profile
        if (request.getInvestmentTarget() != null) {
            client.setInvestmentTarget(request.getInvestmentTarget());
        }
        if (request.getRiskProfile() != null) {
            client.setRiskProfile(request.getRiskProfile());
        }
    }

    /**
     * Validate update request
     */
    private void validateUpdateRequest(UpdateClientProfileRequest request) {
        if (!request.isMailingAddressValid()) {
            throw new ValidationException("If mailing address is different from residential address, all mailing address fields are required");
        }
        
        if (!request.isBankDetailsValid()) {
            throw new ValidationException("If providing bank details, BSB, account number, and account name are all required");
        }
    }

    /**
     * Calculate age from date of birth
     */
    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
} 