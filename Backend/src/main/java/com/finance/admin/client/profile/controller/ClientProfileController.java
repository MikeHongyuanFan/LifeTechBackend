package com.finance.admin.client.profile.controller;

import com.finance.admin.client.profile.dto.ClientProfileResponse;
import com.finance.admin.client.profile.dto.UpdateClientProfileRequest;
import com.finance.admin.client.profile.service.ClientProfileService;
import com.finance.admin.client.document.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Personal Profile functionality (Sprint 3.4)
 * Provides endpoints for viewing and editing client profile information
 */
@RestController
@RequestMapping("/api/client/profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Client Profile", description = "Personal Profile management for client app (Sprint 3.4)")
@SecurityRequirement(name = "bearerAuth")
public class ClientProfileController {

    private final ClientProfileService profileService;
    private final JwtUtils jwtUtils;

    /**
     * Sprint 3.4.1 - View Info
     * Display personal information (name, DOB, TFN, bank, tax info)
     */
    @GetMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get client profile", 
               description = "Retrieve complete client profile information including personal details and KYC status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<ClientProfileResponse> getProfile(HttpServletRequest request) {
        log.info("Client profile request received");

        Long clientId = jwtUtils.getClientIdFromRequest(request);
        ClientProfileResponse profile = profileService.getClientProfile(clientId);

        log.info("Profile retrieved for client ID: {}", clientId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Sprint 3.4.2 - Edit Profile
     * Modify editable fields (bank account, contact info, address)
     */
    @PutMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Update client profile", 
               description = "Update editable client profile fields such as contact information, address, and bank details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or validation errors"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Client not found"),
        @ApiResponse(responseCode = "409", description = "Conflict - Email already exists")
    })
    public ResponseEntity<ClientProfileResponse> updateProfile(
            HttpServletRequest request,
            @Parameter(description = "Profile update request with editable fields")
            @Valid @RequestBody UpdateClientProfileRequest updateRequest) {
        
        log.info("Client profile update request received");

        Long clientId = jwtUtils.getClientIdFromRequest(request);
        ClientProfileResponse updatedProfile = profileService.updateClientProfile(clientId, updateRequest);

        log.info("Profile updated for client ID: {}", clientId);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Sprint 3.4.3 - KYC Status Tracking
     * View current KYC progress (passed/pending/missing)
     */
    @GetMapping("/kyc-status")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get KYC status", 
               description = "Retrieve detailed KYC verification status and progress information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "KYC status retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<ClientProfileResponse.KycStatusInfo> getKycStatus(HttpServletRequest request) {
        log.info("KYC status request received");

        Long clientId = jwtUtils.getClientIdFromRequest(request);
        ClientProfileResponse.KycStatusInfo kycStatus = profileService.getKycStatusInfo(clientId);

        log.info("KYC status retrieved for client ID: {}", clientId);
        return ResponseEntity.ok(kycStatus);
    }

    /**
     * Get profile completion information
     */
    @GetMapping("/completion")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get profile completion status", 
               description = "Retrieve profile completion percentage and missing fields information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile completion status retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<Map<String, Object>> getProfileCompletion(HttpServletRequest request) {
        log.info("Profile completion status request received");

        Long clientId = jwtUtils.getClientIdFromRequest(request);
        ClientProfileResponse profile = profileService.getClientProfile(clientId);

        Map<String, Object> response = Map.of(
            "profileCompletion", profile.getProfileCompletion(),
            "kycCompletion", Map.of(
                "status", profile.getKycStatus().getOverallStatus(),
                "percentage", profile.getKycStatus().getCompletionPercentage(),
                "missingDocuments", profile.getKycStatus().getMissingDocuments()
            )
        );

        log.info("Profile completion status retrieved for client ID: {}", clientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get editable fields information
     */
    @GetMapping("/editable-fields")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get editable fields information", 
               description = "Retrieve information about which profile fields can be edited by the client")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Editable fields information retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<Map<String, Object>> getEditableFields() {
        log.info("Editable fields information request received");

        Map<String, Object> editableFields = Map.of(
            "contactInformation", Map.of(
                "emailPrimary", Map.of("editable", true, "requiresVerification", true),
                "emailSecondary", Map.of("editable", true, "requiresVerification", false),
                "phonePrimary", Map.of("editable", true, "requiresVerification", false),
                "phoneSecondary", Map.of("editable", true, "requiresVerification", false)
            ),
            "addressInformation", Map.of(
                "addressStreet", Map.of("editable", true, "requiresVerification", false),
                "addressCity", Map.of("editable", true, "requiresVerification", false),
                "addressState", Map.of("editable", true, "requiresVerification", false),
                "addressPostalCode", Map.of("editable", true, "requiresVerification", false),
                "addressCountry", Map.of("editable", true, "requiresVerification", false),
                "mailingAddress", Map.of("editable", true, "requiresVerification", false)
            ),
            "bankInformation", Map.of(
                "bankBsb", Map.of("editable", true, "requiresVerification", true),
                "bankAccountNumber", Map.of("editable", true, "requiresVerification", true),
                "bankAccountName", Map.of("editable", true, "requiresVerification", true)
            ),
            "investmentProfile", Map.of(
                "investmentTarget", Map.of("editable", true, "requiresVerification", false),
                "riskProfile", Map.of("editable", true, "requiresVerification", false)
            ),
            "nonEditableFields", Map.of(
                "firstName", "Requires admin approval",
                "lastName", "Requires admin approval", 
                "dateOfBirth", "Requires admin approval",
                "taxFileNumber", "Requires verification process",
                "membershipNumber", "System generated - cannot be changed"
            )
        );

        return ResponseEntity.ok(editableFields);
    }
} 