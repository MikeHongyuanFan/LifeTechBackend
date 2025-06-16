package com.finance.admin.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.admin.client.dto.MembershipResponse;
import com.finance.admin.client.model.ClientMembership;
import com.finance.admin.client.repository.ClientMembershipRepository;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientMembershipService
 * Tests all business logic for membership management
 */
@ExtendWith(MockitoExtension.class)
class ClientMembershipServiceTest {

    @Mock
    private ClientMembershipRepository membershipRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MembershipNumberGenerator membershipNumberGenerator;

    @InjectMocks
    private ClientMembershipService membershipService;

    private ClientMembership testMembership;
    private Map<String, Object> testBenefits;
    private String testBenefitsJson;

    @BeforeEach
    void setUp() {
        // Create test benefits
        testBenefits = new HashMap<>();
        testBenefits.put("basicAccess", true);
        testBenefits.put("monthlyNewsletter", true);
        testBenefits.put("investmentAlerts", true);
        
        testBenefitsJson = "{\"basicAccess\":true,\"monthlyNewsletter\":true,\"investmentAlerts\":true}";

        // Create test membership
        testMembership = ClientMembership.builder()
                .id(1L)
                .clientId(1L)
                .membershipNumber("FM-202406-ABC12345")
                .membershipTier("BASIC")
                .membershipStatus("ACTIVE")
                .joinedDate(LocalDate.of(2024, 6, 1))
                .tierUpgradeDate(null)
                .pointsBalance(100)
                .benefitsUnlocked(testBenefitsJson)
                .digitalCardIssued(false)
                .build();
    }

    // ================ Get Membership Tests ================

    @Test
    void getMembershipByClientId_WithExistingMembership_ReturnsResponse() throws Exception {
        // Arrange
        Long clientId = 1L;
        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.of(testMembership));
        when(objectMapper.readValue(eq(testBenefitsJson), any(TypeReference.class))).thenReturn(testBenefits);

        // Act
        MembershipResponse result = membershipService.getMembershipByClientId(clientId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getClientId()).isEqualTo(1L);
        assertThat(result.getMembershipNumber()).isEqualTo("FM-202406-ABC12345");
        assertThat(result.getMembershipTier()).isEqualTo("BASIC");
        assertThat(result.getMembershipStatus()).isEqualTo("ACTIVE");
        assertThat(result.getPointsBalance()).isEqualTo(100);
        assertThat(result.getDigitalCardIssued()).isFalse();
        assertThat(result.getBenefitsUnlocked()).isEqualTo(testBenefits);
        assertThat(result.getQrCode()).isNotNull();

        verify(membershipRepository).findByClientId(clientId);
    }

    @Test
    void getMembershipByClientId_WithNonExistentMembership_ThrowsException() {
        // Arrange
        Long clientId = 999L;
        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> membershipService.getMembershipByClientId(clientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Membership not found for client: " + clientId);

        verify(membershipRepository).findByClientId(clientId);
    }

    // ================ Create Membership Tests ================

    @Test
    void createMembership_WithValidClient_ReturnsNewMembership() throws Exception {
        // Arrange
        Long clientId = 1L;
        String membershipNumber = "FM-202406-NEW12345";
        
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.empty());
        when(membershipNumberGenerator.generate()).thenReturn(membershipNumber);
        when(membershipRepository.existsByMembershipNumber(membershipNumber)).thenReturn(false);
        when(objectMapper.writeValueAsString(any())).thenReturn(testBenefitsJson);
        when(objectMapper.readValue(eq(testBenefitsJson), any(TypeReference.class))).thenReturn(testBenefits);
        
        ClientMembership savedMembership = ClientMembership.builder()
                .id(2L)
                .clientId(clientId)
                .membershipNumber(membershipNumber)
                .membershipTier("BASIC")
                .membershipStatus("ACTIVE")
                .joinedDate(LocalDate.now())
                .pointsBalance(0)
                .benefitsUnlocked(testBenefitsJson)
                .digitalCardIssued(false)
                .build();
                
        when(membershipRepository.save(any(ClientMembership.class))).thenReturn(savedMembership);

        // Act
        MembershipResponse result = membershipService.createMembership(clientId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getClientId()).isEqualTo(clientId);
        assertThat(result.getMembershipNumber()).isEqualTo(membershipNumber);
        assertThat(result.getMembershipTier()).isEqualTo("BASIC");
        assertThat(result.getMembershipStatus()).isEqualTo("ACTIVE");
        assertThat(result.getPointsBalance()).isEqualTo(0);
        assertThat(result.getDigitalCardIssued()).isFalse();

        verify(clientRepository).existsById(clientId);
        verify(membershipRepository).findByClientId(clientId);
        verify(membershipNumberGenerator).generate();
        verify(membershipRepository).existsByMembershipNumber(membershipNumber);
        verify(membershipRepository).save(any(ClientMembership.class));
    }

    @Test
    void createMembership_WithNonExistentClient_ThrowsException() {
        // Arrange
        Long clientId = 999L;
        when(clientRepository.existsById(clientId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> membershipService.createMembership(clientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Client not found with ID: " + clientId);

        verify(clientRepository).existsById(clientId);
        verify(membershipRepository, never()).findByClientId(any());
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void createMembership_WithExistingMembership_ThrowsException() {
        // Arrange
        Long clientId = 1L;
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.of(testMembership));

        // Act & Assert
        assertThatThrownBy(() -> membershipService.createMembership(clientId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Membership already exists for client: " + clientId);

        verify(clientRepository).existsById(clientId);
        verify(membershipRepository).findByClientId(clientId);
        verify(membershipRepository, never()).save(any());
    }

    // ================ Update Membership Tier Tests ================

    @Test
    void updateMembershipTier_WithValidTier_ReturnsUpdatedMembership() throws Exception {
        // Arrange
        Long clientId = 1L;
        String newTier = "PREMIUM";
        
        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.of(testMembership));
        when(objectMapper.readValue(eq(testBenefitsJson), any(TypeReference.class))).thenReturn(testBenefits);
        when(objectMapper.writeValueAsString(any())).thenReturn(testBenefitsJson);
        when(membershipRepository.save(testMembership)).thenReturn(testMembership);

        // Act
        MembershipResponse result = membershipService.updateMembershipTier(clientId, newTier);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMembershipTier()).isEqualTo(newTier);

        verify(membershipRepository).findByClientId(clientId);
        verify(membershipRepository).save(testMembership);
    }

    @Test
    void updateMembershipTier_WithInvalidTier_ThrowsException() {
        // Arrange
        Long clientId = 1L;
        String invalidTier = "INVALID_TIER";
        
        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.of(testMembership));

        // Act & Assert
        assertThatThrownBy(() -> membershipService.updateMembershipTier(clientId, invalidTier))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid membership tier: " + invalidTier);

        verify(membershipRepository).findByClientId(clientId);
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void updateMembershipTier_WithNonExistentMembership_ThrowsException() {
        // Arrange
        Long clientId = 999L;
        String tier = "PREMIUM";
        
        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> membershipService.updateMembershipTier(clientId, tier))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Membership not found for client: " + clientId);

        verify(membershipRepository).findByClientId(clientId);
        verify(membershipRepository, never()).save(any());
    }

    // ================ Update Points Balance Tests ================

    @Test
    void updatePointsBalance_WithPositiveDelta_ReturnsUpdatedMembership() throws Exception {
        // Arrange
        Long clientId = 1L;
        int pointsDelta = 50;
        
        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.of(testMembership));
        when(objectMapper.readValue(eq(testBenefitsJson), any(TypeReference.class))).thenReturn(testBenefits);
        when(membershipRepository.save(testMembership)).thenReturn(testMembership);

        // Act
        MembershipResponse result = membershipService.updatePointsBalance(clientId, pointsDelta);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPointsBalance()).isEqualTo(150); // 100 + 50

        verify(membershipRepository).findByClientId(clientId);
        verify(membershipRepository).save(testMembership);
    }

    @Test
    void updatePointsBalance_WithNegativeDelta_ReturnsUpdatedMembership() throws Exception {
        // Arrange
        Long clientId = 1L;
        int pointsDelta = -30;
        
        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.of(testMembership));
        when(objectMapper.readValue(eq(testBenefitsJson), any(TypeReference.class))).thenReturn(testBenefits);
        when(membershipRepository.save(testMembership)).thenReturn(testMembership);

        // Act
        MembershipResponse result = membershipService.updatePointsBalance(clientId, pointsDelta);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPointsBalance()).isEqualTo(70); // 100 - 30

        verify(membershipRepository).findByClientId(clientId);
        verify(membershipRepository).save(testMembership);
    }

    @Test
    void updatePointsBalance_WithNegativeResult_ThrowsException() {
        // Arrange
        Long clientId = 1L;
        int pointsDelta = -200; // 100 - 200 = -100 (negative)
        
        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.of(testMembership));

        // Act & Assert
        assertThatThrownBy(() -> membershipService.updatePointsBalance(clientId, pointsDelta))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Points balance cannot be negative");

        verify(membershipRepository).findByClientId(clientId);
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void updatePointsBalance_WithNonExistentMembership_ThrowsException() {
        // Arrange
        Long clientId = 999L;
        int pointsDelta = 50;
        
        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> membershipService.updatePointsBalance(clientId, pointsDelta))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Membership not found for client: " + clientId);

        verify(membershipRepository).findByClientId(clientId);
        verify(membershipRepository, never()).save(any());
    }

    // ================ Generate Digital Card Tests ================

    @Test
    void generateDigitalCard_WithExistingMembership_ReturnsUpdatedMembership() throws Exception {
        // Arrange
        Long clientId = 1L;
        
        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.of(testMembership));
        when(objectMapper.readValue(eq(testBenefitsJson), any(TypeReference.class))).thenReturn(testBenefits);
        when(membershipRepository.save(testMembership)).thenReturn(testMembership);

        // Act
        MembershipResponse result = membershipService.generateDigitalCard(clientId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDigitalCardIssued()).isTrue();
        assertThat(result.getQrCode()).isNotNull();

        verify(membershipRepository).findByClientId(clientId);
        verify(membershipRepository).save(testMembership);
    }

    @Test
    void generateDigitalCard_WithNonExistentMembership_ThrowsException() {
        // Arrange
        Long clientId = 999L;
        
        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> membershipService.generateDigitalCard(clientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Membership not found for client: " + clientId);

        verify(membershipRepository).findByClientId(clientId);
        verify(membershipRepository, never()).save(any());
    }
} 