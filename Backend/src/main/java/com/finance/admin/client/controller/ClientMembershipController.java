package com.finance.admin.client.controller;

import com.finance.admin.client.dto.MembershipResponse;
import com.finance.admin.client.service.ClientMembershipService;
import com.finance.admin.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/client/membership")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Client Membership", description = "Client membership management endpoints")
public class ClientMembershipController {

    private final ClientMembershipService membershipService;

    @Operation(summary = "Get client membership", description = "Retrieve membership details for a client")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Membership details retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @GetMapping("/{clientId}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<MembershipResponse> getMembership(
            @Parameter(description = "Client ID") @PathVariable Long clientId) {
        log.info("Getting membership details for client: {}", clientId);
        MembershipResponse response = membershipService.getMembershipByClientId(clientId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create client membership", description = "Create a new membership for a client")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Membership created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Client already has a membership"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client not found")
    })
    @PostMapping("/{clientId}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<MembershipResponse> createMembership(
            @Parameter(description = "Client ID") @PathVariable Long clientId) {
        log.info("Creating membership for client: {}", clientId);
        MembershipResponse response = membershipService.createMembership(clientId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update membership tier", description = "Update the membership tier for a client")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Membership tier updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid tier"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @PutMapping("/{clientId}/tier")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<?> updateMembershipTier(
            @Parameter(description = "Client ID") @PathVariable Long clientId,
            @Parameter(description = "New tier") @RequestBody Map<String, String> request) {
        log.info("Updating membership tier for client: {}", clientId);
        
        // Validate request
        if (request == null || !request.containsKey("tier") || request.get("tier") == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing tier field"));
        }
        
        String tier = request.get("tier");
        MembershipResponse response = membershipService.updateMembershipTier(clientId, tier);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update points balance", description = "Update the points balance for a client's membership")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Points balance updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid points delta"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @PutMapping("/{clientId}/points")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<?> updatePointsBalance(
            @Parameter(description = "Client ID") @PathVariable Long clientId,
            @Parameter(description = "Points delta") @RequestBody Map<String, Integer> request) {
        log.info("Updating points balance for client: {}", clientId);
        
        // Validate request
        if (request == null || !request.containsKey("pointsDelta") || request.get("pointsDelta") == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing pointsDelta field"));
        }
        
        Integer pointsDelta = request.get("pointsDelta");
        MembershipResponse response = membershipService.updatePointsBalance(clientId, pointsDelta);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Generate digital card", description = "Generate or retrieve digital membership card")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Digital card generated/retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @PostMapping("/{clientId}/digital-card")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<MembershipResponse> generateDigitalCard(
            @Parameter(description = "Client ID") @PathVariable Long clientId) {
        log.info("Generating digital card for client: {}", clientId);
        MembershipResponse response = membershipService.generateDigitalCard(clientId);
        return ResponseEntity.ok(response);
    }
} 