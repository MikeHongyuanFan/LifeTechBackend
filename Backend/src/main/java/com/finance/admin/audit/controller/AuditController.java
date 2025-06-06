package com.finance.admin.audit.controller;

import com.finance.admin.audit.entity.AuditLog;
import com.finance.admin.audit.service.AuditService;
import com.finance.admin.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/audit")
@Tag(name = "Audit Management", description = "APIs for managing audit logs")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Get audit logs with filters")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        Page<AuditLog> auditLogs = auditService.findAuditLogs(
                userId, userType, actionType, entityType, entityId,
                startDate, endDate, status, pageable);

        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", auditLogs));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Export audit logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> exportAuditLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<AuditLog> auditLogs = auditService.exportAuditLogs(
                userId, userType, actionType, entityType, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success("Audit logs exported successfully", auditLogs));
    }

    @GetMapping("/user/{userId}/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Get user action history")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getUserActionHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "100") int limit) {

        List<AuditLog> history = auditService.findUserActionHistory(userId, limit);
        return ResponseEntity.ok(ApiResponse.success("User action history retrieved successfully", history));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Get entity history")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable UUID entityId) {

        List<AuditLog> history = auditService.findEntityHistory(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success("Entity history retrieved successfully", history));
    }

    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Manually trigger audit log cleanup")
    public ResponseEntity<ApiResponse<Void>> triggerCleanup() {
        auditService.cleanupOldAuditLogs();
        return ResponseEntity.ok(ApiResponse.success("Audit log cleanup triggered successfully"));
    }
} 
