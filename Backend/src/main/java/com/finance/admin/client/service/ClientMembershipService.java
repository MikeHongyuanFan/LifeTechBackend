package com.finance.admin.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.admin.client.dto.MembershipResponse;
import com.finance.admin.client.model.ClientMembership;
import com.finance.admin.client.repository.ClientMembershipRepository;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.common.exception.ResourceNotFoundException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientMembershipService {

    private final ClientMembershipRepository membershipRepository;
    private final ClientRepository clientRepository;
    private final ObjectMapper objectMapper;
    private final MembershipNumberGenerator membershipNumberGenerator;

    /**
     * Get membership details for a client
     */
    @Transactional(readOnly = true)
    public MembershipResponse getMembershipByClientId(Long clientId) {
        ClientMembership membership = findMembershipByClientId(clientId);
        return buildMembershipResponse(membership);
    }

    /**
     * Create new membership for a client
     */
    public MembershipResponse createMembership(Long clientId) {
        // Verify client exists
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found with ID: " + clientId);
        }

        // Check if membership already exists
        if (membershipRepository.findByClientId(clientId).isPresent()) {
            throw new IllegalStateException("Membership already exists for client: " + clientId);
        }

        // Generate unique membership number
        String membershipNumber = membershipNumberGenerator.generate();
        while (membershipRepository.existsByMembershipNumber(membershipNumber)) {
            membershipNumber = membershipNumberGenerator.generate();
        }

        // Initialize default benefits
        Map<String, Object> defaultBenefits = new HashMap<>();
        defaultBenefits.put("basicAccess", true);
        defaultBenefits.put("monthlyNewsletter", true);
        defaultBenefits.put("investmentAlerts", true);

        // Create membership
        ClientMembership membership = ClientMembership.builder()
                .clientId(clientId)
                .membershipNumber(membershipNumber)
                .membershipTier("BASIC")
                .membershipStatus("ACTIVE")
                .joinedDate(LocalDate.now())
                .pointsBalance(0)
                .benefitsUnlocked(serializeBenefits(defaultBenefits))
                .digitalCardIssued(false)
                .build();

        membership = membershipRepository.save(membership);
        return buildMembershipResponse(membership);
    }

    /**
     * Update membership tier
     */
    public MembershipResponse updateMembershipTier(Long clientId, String newTier) {
        ClientMembership membership = findMembershipByClientId(clientId);
        
        if (!isValidTier(newTier)) {
            throw new IllegalArgumentException("Invalid membership tier: " + newTier);
        }

        membership.setMembershipTier(newTier.toUpperCase());
        membership.setTierUpgradeDate(LocalDate.now());

        // Update benefits based on new tier
        Map<String, Object> benefits = deserializeBenefits(membership.getBenefitsUnlocked());
        benefits.putAll(getTierBenefits(newTier));
        membership.setBenefitsUnlocked(serializeBenefits(benefits));

        membership = membershipRepository.save(membership);
        return buildMembershipResponse(membership);
    }

    /**
     * Update points balance
     */
    public MembershipResponse updatePointsBalance(Long clientId, int pointsDelta) {
        ClientMembership membership = findMembershipByClientId(clientId);
        
        int newBalance = membership.getPointsBalance() + pointsDelta;
        if (newBalance < 0) {
            throw new IllegalArgumentException("Points balance cannot be negative");
        }

        membership.setPointsBalance(newBalance);
        membership = membershipRepository.save(membership);
        return buildMembershipResponse(membership);
    }

    /**
     * Generate digital membership card
     */
    public MembershipResponse generateDigitalCard(Long clientId) {
        ClientMembership membership = findMembershipByClientId(clientId);

        if (!membership.getDigitalCardIssued()) {
            membership.setDigitalCardIssued(true);
            membership = membershipRepository.save(membership);
        }

        return buildMembershipResponse(membership);
    }

    // Private helper methods

    private ClientMembership findMembershipByClientId(Long clientId) {
        return membershipRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found for client: " + clientId));
    }

    private MembershipResponse buildMembershipResponse(ClientMembership membership) {
        return MembershipResponse.builder()
                .id(membership.getId())
                .clientId(membership.getClientId())
                .membershipNumber(membership.getMembershipNumber())
                .membershipTier(membership.getMembershipTier())
                .membershipStatus(membership.getMembershipStatus())
                .joinedDate(membership.getJoinedDate())
                .tierUpgradeDate(membership.getTierUpgradeDate())
                .pointsBalance(membership.getPointsBalance())
                .benefitsUnlocked(deserializeBenefits(membership.getBenefitsUnlocked()))
                .digitalCardIssued(membership.getDigitalCardIssued())
                .qrCode(generateQRCode(membership))
                .build();
    }

    private boolean isValidTier(String tier) {
        String upperTier = tier.toUpperCase();
        return upperTier.equals("BASIC") || upperTier.equals("PREMIUM") || upperTier.equals("VIP");
    }

    private Map<String, Object> getTierBenefits(String tier) {
        Map<String, Object> benefits = new HashMap<>();
        
        switch (tier.toUpperCase()) {
            case "PREMIUM":
                benefits.put("prioritySupport", true);
                benefits.put("exclusiveEvents", true);
                benefits.put("quarterlyReview", true);
                break;
            case "VIP":
                benefits.put("prioritySupport", true);
                benefits.put("exclusiveEvents", true);
                benefits.put("quarterlyReview", true);
                benefits.put("personalizedService", true);
                benefits.put("vipEvents", true);
                benefits.put("monthlyReview", true);
                break;
        }
        
        return benefits;
    }

    private String generateQRCode(ClientMembership membership) {
        try {
            String qrContent = String.format("LIFETECH-MEMBER:%s", membership.getMembershipNumber());
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            var bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("Failed to generate QR code for membership {}: {}", membership.getId(), e.getMessage());
            return null;
        }
    }

    private String serializeBenefits(Map<String, Object> benefits) {
        try {
            return objectMapper.writeValueAsString(benefits);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize benefits: {}", e.getMessage());
            return "{}";
        }
    }

    private Map<String, Object> deserializeBenefits(String benefitsJson) {
        try {
            if (benefitsJson == null || benefitsJson.isBlank()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(benefitsJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize benefits: {}", e.getMessage());
            return new HashMap<>();
        }
    }
} 