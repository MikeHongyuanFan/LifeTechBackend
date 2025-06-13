package com.finance.admin.client.profile.service;

import com.finance.admin.client.model.Client;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.client.repository.ClientLoginHistoryRepository;
import com.finance.admin.client.profile.dto.ClientProfileResponse;
import com.finance.admin.client.profile.dto.UpdateClientProfileRequest;
import com.finance.admin.client.document.repository.ClientDocumentRepository;
import com.finance.admin.client.document.model.ClientDocument;
import com.finance.admin.common.exception.ResourceNotFoundException;
import com.finance.admin.common.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientProfileService (Sprint 3.4)
 */
@ExtendWith(MockitoExtension.class)
class ClientProfileServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientLoginHistoryRepository loginHistoryRepository;

    @Mock
    private ClientDocumentRepository documentRepository;

    @InjectMocks
    private ClientProfileService profileService;

    private Client testClient;
    private ClientDocument testKycDocument;

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                .id(1L)
                .membershipNumber("LT2024001")
                .firstName("John")
                .middleName("Michael")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .emailPrimary("john.doe@example.com")
                .emailSecondary("john.personal@example.com")
                .phonePrimary("+61412345678")
                .phoneSecondary("+61487654321")
                .addressStreet("123 Main Street")
                .addressCity("Sydney")
                .addressState("NSW")
                .addressPostalCode("2000")
                .addressCountry("Australia")
                .mailingAddressSame(true)
                .taxResidencyStatus("Australian Resident")
                .tfnEncrypted("encrypted_tfn_123")
                .bankBsb("123456")
                .bankAccountNumberEncrypted("encrypted_account_789")
                .bankAccountName("John Michael Doe")
                .investmentTarget(new BigDecimal("100000.00"))
                .riskProfile("MODERATE")
                .status(Client.ClientStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusMonths(6))
                .build();

        testKycDocument = ClientDocument.builder()
                .id(1L)
                .client(testClient)
                .documentName("Driver License.pdf")
                .documentType(ClientDocument.DocumentType.IDENTITY_VERIFICATION)
                .documentCategory(ClientDocument.DocumentCategory.KYC)
                .filePath("/documents/kyc/driver_license.pdf")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .uploadDate(LocalDateTime.now().minusDays(10))
                .uploadedByClient(true)
                .isActive(true)
                .documentStatus(ClientDocument.DocumentStatus.APPROVED)
                .versionNumber(1)
                .build();
    }

    // ================ Sprint 3.4.1 - View Info Tests ================

    @Test
    void getClientProfile_WithValidClientId_ReturnsCompleteProfile() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(loginHistoryRepository.findByClientIdOrderByLoginTimestampDesc(1L))
                .thenReturn(Arrays.asList());
        when(documentRepository.findByClientIdAndDocumentCategoryAndIsActiveTrue(1L, ClientDocument.DocumentCategory.KYC))
                .thenReturn(Arrays.asList(testKycDocument));

        // Act
        ClientProfileResponse response = profileService.getClientProfile(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getMembershipNumber()).isEqualTo("LT2024001");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getMiddleName()).isEqualTo("Michael");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getFullName()).isEqualTo("John Michael Doe");
        assertThat(response.getAge()).isEqualTo(35); // Calculated from DOB (1990 to 2025)
        assertThat(response.getEmailPrimary()).isEqualTo("john.doe@example.com");
        assertThat(response.getHasTfn()).isTrue();
        assertThat(response.getHasBankAccount()).isTrue();
        assertThat(response.getInvestmentTarget()).isEqualTo(new BigDecimal("100000.00"));
        assertThat(response.getRiskProfile()).isEqualTo("MODERATE");
        assertThat(response.getStatus()).isEqualTo(Client.ClientStatus.ACTIVE);

        // Check KYC status
        assertThat(response.getKycStatus()).isNotNull();
        assertThat(response.getKycStatus().getOverallStatus()).isEqualTo(ClientProfileResponse.KycStatusInfo.KycStatus.PENDING);
        assertThat(response.getKycStatus().getCompletionPercentage()).isEqualTo(25); // 1 out of 4 required docs

        // Check profile completion
        assertThat(response.getProfileCompletion()).isNotNull();
        assertThat(response.getProfileCompletion().getCompletionPercentage()).isGreaterThan(0);

        verify(clientRepository).findById(1L);
        verify(documentRepository).findByClientIdAndDocumentCategoryAndIsActiveTrue(1L, ClientDocument.DocumentCategory.KYC);
    }

    @Test
    void getClientProfile_WithInvalidClientId_ThrowsResourceNotFoundException() {
        // Arrange
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> profileService.getClientProfile(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client not found with ID: 999");

        verify(clientRepository).findById(999L);
        verifyNoInteractions(documentRepository);
    }

    @Test
    void getClientProfile_WithFormattedAddresses_ReturnsCorrectAddresses() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(loginHistoryRepository.findByClientIdOrderByLoginTimestampDesc(1L))
                .thenReturn(Arrays.asList());
        when(documentRepository.findByClientIdAndDocumentCategoryAndIsActiveTrue(1L, ClientDocument.DocumentCategory.KYC))
                .thenReturn(Arrays.asList());

        // Act
        ClientProfileResponse response = profileService.getClientProfile(1L);

        // Assert
        assertThat(response.getFullAddress()).isEqualTo("123 Main Street, Sydney, NSW 2000, Australia");
        assertThat(response.getFullMailingAddress()).isEqualTo("123 Main Street, Sydney, NSW 2000, Australia");
    }

    // ================ Sprint 3.4.2 - Edit Profile Tests ================

    @Test
    void updateClientProfile_WithValidRequest_UpdatesProfileSuccessfully() {
        // Arrange
        UpdateClientProfileRequest request = UpdateClientProfileRequest.builder()
                .emailPrimary("john.updated@example.com")
                .phonePrimary("+61400000000")
                .addressStreet("456 New Street")
                .addressCity("Melbourne")
                .addressState("VIC")
                .addressPostalCode("3000")
                .bankBsb("654321")
                .bankAccountNumber("123456789")
                .bankAccountName("John M Doe")
                .investmentTarget(new BigDecimal("150000.00"))
                .riskProfile("GROWTH")
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(clientRepository.existsByEmailPrimaryAndIdNot("john.updated@example.com", 1L)).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(loginHistoryRepository.findByClientIdOrderByLoginTimestampDesc(1L))
                .thenReturn(Arrays.asList());
        when(documentRepository.findByClientIdAndDocumentCategoryAndIsActiveTrue(1L, ClientDocument.DocumentCategory.KYC))
                .thenReturn(Arrays.asList());

        // Act
        ClientProfileResponse response = profileService.updateClientProfile(1L, request);

        // Assert
        assertThat(response).isNotNull();
        verify(clientRepository).save(any(Client.class));
        verify(clientRepository).existsByEmailPrimaryAndIdNot("john.updated@example.com", 1L);
    }

    @Test
    void updateClientProfile_WithDuplicateEmail_ThrowsValidationException() {
        // Arrange
        UpdateClientProfileRequest request = UpdateClientProfileRequest.builder()
                .emailPrimary("existing@example.com")
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(clientRepository.existsByEmailPrimaryAndIdNot("existing@example.com", 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> profileService.updateClientProfile(1L, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email already exists: existing@example.com");

        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void updateClientProfile_WithInvalidMailingAddress_ThrowsValidationException() {
        // Arrange
        UpdateClientProfileRequest request = UpdateClientProfileRequest.builder()
                .mailingAddressSame(false)
                .mailingStreet("123 Mailing St")
                // Missing other mailing address fields
                .build();

        // Act & Assert - validation happens before repository access
        assertThatThrownBy(() -> profileService.updateClientProfile(1L, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("mailing address fields are required");
    }

    // ================ Sprint 3.4.3 - KYC Status Tracking Tests ================

    @Test
    void getKycStatusInfo_WithNoDocuments_ReturnsMissingStatus() {
        // Arrange
        when(documentRepository.findByClientIdAndDocumentCategoryAndIsActiveTrue(1L, ClientDocument.DocumentCategory.KYC))
                .thenReturn(Arrays.asList());

        // Act
        ClientProfileResponse.KycStatusInfo kycStatus = profileService.getKycStatusInfo(1L);

        // Assert
        assertThat(kycStatus).isNotNull();
        assertThat(kycStatus.getOverallStatus()).isEqualTo(ClientProfileResponse.KycStatusInfo.KycStatus.MISSING);
        assertThat(kycStatus.getCompletionPercentage()).isEqualTo(0);
        assertThat(kycStatus.getMissingDocuments()).hasSize(4);
        assertThat(kycStatus.getMissingDocuments()).contains(
                "Identity Verification", "Proof of Address", "Tax File Number", "Bank Statement"
        );
        assertThat(kycStatus.getNextSteps()).contains("Please upload all required KYC documents");
    }

    @Test
    void getKycStatusInfo_WithAllDocuments_ReturnsPassedStatus() {
        // Arrange
        List<ClientDocument> allKycDocuments = Arrays.asList(
                createKycDocument(ClientDocument.DocumentType.IDENTITY_VERIFICATION),
                createKycDocument(ClientDocument.DocumentType.ADDRESS_PROOF),
                createKycDocument(ClientDocument.DocumentType.TAX_FILE_NUMBER),
                createKycDocument(ClientDocument.DocumentType.BANK_STATEMENT)
        );

        when(documentRepository.findByClientIdAndDocumentCategoryAndIsActiveTrue(1L, ClientDocument.DocumentCategory.KYC))
                .thenReturn(allKycDocuments);

        // Act
        ClientProfileResponse.KycStatusInfo kycStatus = profileService.getKycStatusInfo(1L);

        // Assert
        assertThat(kycStatus).isNotNull();
        assertThat(kycStatus.getOverallStatus()).isEqualTo(ClientProfileResponse.KycStatusInfo.KycStatus.PASSED);
        assertThat(kycStatus.getCompletionPercentage()).isEqualTo(100);
        assertThat(kycStatus.getMissingDocuments()).isEmpty();
        assertThat(kycStatus.getNextSteps()).contains("KYC verification completed");
        assertThat(kycStatus.getApprovalDate()).isNotNull();
        assertThat(kycStatus.getExpiryDate()).isNotNull();
    }

    @Test
    void getKycStatusInfo_WithPartialDocuments_ReturnsPendingStatus() {
        // Arrange
        List<ClientDocument> partialKycDocuments = Arrays.asList(
                createKycDocument(ClientDocument.DocumentType.IDENTITY_VERIFICATION),
                createKycDocument(ClientDocument.DocumentType.ADDRESS_PROOF)
        );

        when(documentRepository.findByClientIdAndDocumentCategoryAndIsActiveTrue(1L, ClientDocument.DocumentCategory.KYC))
                .thenReturn(partialKycDocuments);

        // Act
        ClientProfileResponse.KycStatusInfo kycStatus = profileService.getKycStatusInfo(1L);

        // Assert
        assertThat(kycStatus).isNotNull();
        assertThat(kycStatus.getOverallStatus()).isEqualTo(ClientProfileResponse.KycStatusInfo.KycStatus.PENDING);
        assertThat(kycStatus.getCompletionPercentage()).isEqualTo(50); // 2 out of 4 docs
        assertThat(kycStatus.getMissingDocuments()).hasSize(2);
        assertThat(kycStatus.getMissingDocuments()).contains("Tax File Number", "Bank Statement");
        assertThat(kycStatus.getNextSteps()).contains("Upload missing documents");
    }

    // ================ Helper Methods ================

    private ClientDocument createKycDocument(ClientDocument.DocumentType documentType) {
        return ClientDocument.builder()
                .id(System.currentTimeMillis()) // Simple unique ID
                .client(testClient)
                .documentName(documentType.getDisplayName() + ".pdf")
                .documentType(documentType)
                .documentCategory(ClientDocument.DocumentCategory.KYC)
                .filePath("/documents/kyc/" + documentType.name().toLowerCase() + ".pdf")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .uploadDate(LocalDateTime.now().minusDays(5))
                .uploadedByClient(true)
                .isActive(true)
                .documentStatus(ClientDocument.DocumentStatus.APPROVED)
                .versionNumber(1)
                .build();
    }
} 