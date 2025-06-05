package com.tycoon.admin.role.controller;

import com.tycoon.admin.auth.entity.AdminRole;
import com.tycoon.admin.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/roles")
@Tag(name = "Role Management", description = "APIs for managing admin roles and permissions")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get all roles", description = "Retrieve all available admin roles")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllRoles() {
        List<Map<String, Object>> roles = Arrays.stream(AdminRole.values())
                .map(role -> Map.of(
                        "name", role.name(),
                        "displayName", role.getDisplayName(),
                        "description", role.getDescription(),
                        "permissions", role.getPermissions().stream()
                                .map(permission -> permission.getPermission())
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", roles));
    }

    @GetMapping("/{roleName}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get role details", description = "Retrieve details for a specific role")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoleDetails(@PathVariable String roleName) {
        try {
            AdminRole role = AdminRole.valueOf(roleName.toUpperCase());
            
            Map<String, Object> roleDetails = Map.of(
                    "name", role.name(),
                    "displayName", role.getDisplayName(),
                    "description", role.getDescription(),
                    "permissions", role.getPermissions().stream()
                            .map(permission -> permission.getPermission())
                            .collect(Collectors.toList())
            );
            
            return ResponseEntity.ok(ApiResponse.success("Role details retrieved successfully", roleDetails));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid role name"));
        }
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get all permissions", description = "Retrieve all available permissions")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> getAllPermissions() {
        Map<String, List<String>> permissionsByCategory = Arrays.stream(AdminRole.values())
                .flatMap(role -> role.getPermissions().stream())
                .distinct()
                .collect(Collectors.groupingBy(
                        permission -> permission.getPermission().split(":")[0],
                        Collectors.mapping(permission -> permission.getPermission(), Collectors.toList())
                ));
        
        return ResponseEntity.ok(ApiResponse.success("Permissions retrieved successfully", permissionsByCategory));
    }
}
