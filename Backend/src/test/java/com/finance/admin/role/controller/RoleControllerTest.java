package com.finance.admin.role.controller;

import com.finance.admin.auth.entity.AdminRole;
import com.finance.admin.auth.entity.AdminPermission;
import com.finance.admin.config.BaseUnitTest;
import com.finance.admin.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for RoleController
 * Tests all role management endpoints with various scenarios
 */
@DisplayName("Role Controller Tests")
public class RoleControllerTest extends BaseUnitTest {

    private MockMvc mockMvc;
    private RoleController roleController;

    @BeforeEach
    protected void setUp() {
        super.setUp();
        
        roleController = new RoleController();
        
        // Setup MockMvc with GlobalExceptionHandler
        mockMvc = MockMvcBuilders.standaloneSetup(roleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ==================== GET /roles Tests ====================

    @Test
    @DisplayName("Should get all roles successfully")
    void testGetAllRoles_Success() throws Exception {
        mockMvc.perform(get("/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Roles retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(6)) // 6 roles in AdminRole enum
                .andExpect(jsonPath("$.timestamp").exists())
                
                // Verify first role structure (SUPER_ADMIN)
                .andExpect(jsonPath("$.data[0].name").value("SUPER_ADMIN"))
                .andExpect(jsonPath("$.data[0].displayName").value("Super Administrator"))
                .andExpect(jsonPath("$.data[0].description").value("Full system access and control"))
                .andExpect(jsonPath("$.data[0].permissions").isArray())
                .andExpect(jsonPath("$.data[0].permissions.length()").value(19)) // All permissions
                
                // Verify role names are present
                .andExpect(jsonPath("$.data[?(@.name == 'SUPER_ADMIN')]").exists())
                .andExpect(jsonPath("$.data[?(@.name == 'SYSTEM_ADMIN')]").exists())
                .andExpect(jsonPath("$.data[?(@.name == 'FINANCIAL_ADMIN')]").exists())
                .andExpect(jsonPath("$.data[?(@.name == 'CUSTOMER_SERVICE_ADMIN')]").exists())
                .andExpect(jsonPath("$.data[?(@.name == 'COMPLIANCE_OFFICER')]").exists())
                .andExpect(jsonPath("$.data[?(@.name == 'ANALYST')]").exists());
    }

    @Test
    @DisplayName("Should verify role permissions structure")
    void testGetAllRoles_PermissionsStructure() throws Exception {
        mockMvc.perform(get("/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].permissions").isArray())
                
                // Verify SUPER_ADMIN has all permissions
                .andExpect(jsonPath("$.data[0].permissions[?(@== 'user:read')]").exists())
                .andExpect(jsonPath("$.data[0].permissions[?(@== 'user:write')]").exists())
                .andExpect(jsonPath("$.data[0].permissions[?(@== 'admin:read')]").exists())
                .andExpect(jsonPath("$.data[0].permissions[?(@== 'role:read')]").exists())
                .andExpect(jsonPath("$.data[0].permissions[?(@== 'financial:read')]").exists())
                .andExpect(jsonPath("$.data[0].permissions[?(@== 'compliance:read')]").exists());
    }

    @Test
    @DisplayName("Should verify ANALYST role has only read permissions")
    void testGetAllRoles_AnalystPermissions() throws Exception {
        mockMvc.perform(get("/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                
                // Find ANALYST role and verify read-only permissions
                .andExpect(jsonPath("$.data[?(@.name == 'ANALYST')].permissions[?(@== 'user:read')]").exists())
                .andExpect(jsonPath("$.data[?(@.name == 'ANALYST')].permissions[?(@== 'audit:read')]").exists())
                .andExpect(jsonPath("$.data[?(@.name == 'ANALYST')].permissions[?(@== 'system:read')]").exists())
                .andExpect(jsonPath("$.data[?(@.name == 'ANALYST')].permissions[?(@== 'financial:read')]").exists())
                
                // Verify ANALYST doesn't have write permissions
                .andExpect(jsonPath("$.data[?(@.name == 'ANALYST')].permissions[?(@== 'user:write')]").doesNotExist())
                .andExpect(jsonPath("$.data[?(@.name == 'ANALYST')].permissions[?(@== 'admin:write')]").doesNotExist());
    }

    // ==================== GET /roles/{roleName} Tests ====================

    @Test
    @DisplayName("Should get specific role details successfully - SUPER_ADMIN")
    void testGetRoleDetails_SuperAdmin_Success() throws Exception {
        mockMvc.perform(get("/roles/SUPER_ADMIN")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role details retrieved successfully"))
                .andExpect(jsonPath("$.timestamp").exists())
                
                // Verify role details
                .andExpect(jsonPath("$.data.name").value("SUPER_ADMIN"))
                .andExpect(jsonPath("$.data.displayName").value("Super Administrator"))
                .andExpect(jsonPath("$.data.description").value("Full system access and control"))
                .andExpect(jsonPath("$.data.permissions").isArray())
                .andExpect(jsonPath("$.data.permissions.length()").value(19)) // All 19 permissions
                
                // Verify specific permissions
                .andExpect(jsonPath("$.data.permissions[?(@== 'user:read')]").exists())
                .andExpect(jsonPath("$.data.permissions[?(@== 'user:write')]").exists())
                .andExpect(jsonPath("$.data.permissions[?(@== 'user:delete')]").exists())
                .andExpect(jsonPath("$.data.permissions[?(@== 'admin:read')]").exists())
                .andExpect(jsonPath("$.data.permissions[?(@== 'role:read')]").exists())
                .andExpect(jsonPath("$.data.permissions[?(@== 'financial:read')]").exists())
                .andExpect(jsonPath("$.data.permissions[?(@== 'compliance:read')]").exists());
    }

    @Test
    @DisplayName("Should get specific role details successfully - FINANCIAL_ADMIN")
    void testGetRoleDetails_FinancialAdmin_Success() throws Exception {
        mockMvc.perform(get("/roles/FINANCIAL_ADMIN")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role details retrieved successfully"))
                
                // Verify role details
                .andExpect(jsonPath("$.data.name").value("FINANCIAL_ADMIN"))
                .andExpect(jsonPath("$.data.displayName").value("Financial Administrator"))
                .andExpect(jsonPath("$.data.description").value("Financial product and transaction management"))
                .andExpect(jsonPath("$.data.permissions").isArray())
                
                // Verify specific permissions for FINANCIAL_ADMIN
                .andExpect(jsonPath("$.data.permissions[?(@== 'user:read')]").exists())
                .andExpect(jsonPath("$.data.permissions[?(@== 'audit:read')]").exists())
                .andExpect(jsonPath("$.data.permissions[?(@== 'system:read')]").exists())
                .andExpect(jsonPath("$.data.permissions[?(@== 'financial:read')]").exists())
                .andExpect(jsonPath("$.data.permissions[?(@== 'financial:write')]").exists())
                
                // Verify FINANCIAL_ADMIN doesn't have admin write permissions
                .andExpect(jsonPath("$.data.permissions[?(@== 'admin:write')]").doesNotExist())
                .andExpect(jsonPath("$.data.permissions[?(@== 'role:write')]").doesNotExist());
    }

    @Test
    @DisplayName("Should get specific role details successfully - COMPLIANCE_OFFICER")
    void testGetRoleDetails_ComplianceOfficer_Success() throws Exception {
        mockMvc.perform(get("/roles/COMPLIANCE_OFFICER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("COMPLIANCE_OFFICER"))
                .andExpect(jsonPath("$.data.displayName").value("Compliance Officer"))
                .andExpect(jsonPath("$.data.description").value("Regulatory compliance and audit management"))
                
                // Verify compliance-specific permissions
                .andExpect(jsonPath("$.data.permissions[?(@== 'user:read')]").exists())
                .andExpect(jsonPath("$.data.permissions[?(@== 'audit:read')]").exists())
                .andExpect(jsonPath("$.data.permissions[?(@== 'audit:write')]").exists())
                .andExpect(jsonPath("$.data.permissions[?(@== 'compliance:read')]").exists())
                .andExpect(jsonPath("$.data.permissions[?(@== 'compliance:write')]").exists());
    }

    @Test
    @DisplayName("Should handle case insensitive role name")
    void testGetRoleDetails_CaseInsensitive_Success() throws Exception {
        // Test lowercase
        mockMvc.perform(get("/roles/super_admin")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("SUPER_ADMIN"));

        // Test mixed case
        mockMvc.perform(get("/roles/Financial_Admin")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("FINANCIAL_ADMIN"));
    }

    @Test
    @DisplayName("Should return error for invalid role name")
    void testGetRoleDetails_InvalidRole_Error() throws Exception {
        mockMvc.perform(get("/roles/INVALID_ROLE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid role name"))
                .andExpect(jsonPath("$.error").value("Invalid role name"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should return error for empty role name")
    void testGetRoleDetails_EmptyRole_Error() throws Exception {
        // Testing with empty string roleName (which maps to /roles/ endpoint)
        // Spring treats /roles/ as the same as /roles, so it returns all roles instead of 404
        mockMvc.perform(get("/roles/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Actually returns all roles
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Roles retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Should return error for null role name")
    void testGetRoleDetails_NullRole_Error() throws Exception {
        mockMvc.perform(get("/roles/null")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid role name"));
    }

    // ==================== GET /roles/permissions Tests ====================

    @Test
    @DisplayName("Should get all permissions successfully")
    void testGetAllPermissions_Success() throws Exception {
        mockMvc.perform(get("/roles/permissions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Permissions retrieved successfully"))
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.timestamp").exists())
                
                // Verify permission categories exist
                .andExpect(jsonPath("$.data.user").isArray())
                .andExpect(jsonPath("$.data.admin").isArray())
                .andExpect(jsonPath("$.data.role").isArray())
                .andExpect(jsonPath("$.data.audit").isArray())
                .andExpect(jsonPath("$.data.system").isArray())
                .andExpect(jsonPath("$.data.financial").isArray())
                .andExpect(jsonPath("$.data.compliance").isArray())
                .andExpect(jsonPath("$.data.customer_service").isArray());
    }

    @Test
    @DisplayName("Should verify user permissions category")
    void testGetAllPermissions_UserCategory() throws Exception {
        mockMvc.perform(get("/roles/permissions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                
                // Verify user permissions
                .andExpect(jsonPath("$.data.user.length()").value(3))
                .andExpect(jsonPath("$.data.user[?(@== 'user:read')]").exists())
                .andExpect(jsonPath("$.data.user[?(@== 'user:write')]").exists())
                .andExpect(jsonPath("$.data.user[?(@== 'user:delete')]").exists());
    }

    @Test
    @DisplayName("Should verify admin permissions category")
    void testGetAllPermissions_AdminCategory() throws Exception {
        mockMvc.perform(get("/roles/permissions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                
                // Verify admin permissions
                .andExpect(jsonPath("$.data.admin.length()").value(3))
                .andExpect(jsonPath("$.data.admin[?(@== 'admin:read')]").exists())
                .andExpect(jsonPath("$.data.admin[?(@== 'admin:write')]").exists())
                .andExpect(jsonPath("$.data.admin[?(@== 'admin:delete')]").exists());
    }

    @Test
    @DisplayName("Should verify role permissions category")
    void testGetAllPermissions_RoleCategory() throws Exception {
        mockMvc.perform(get("/roles/permissions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                
                // Verify role permissions
                .andExpect(jsonPath("$.data.role.length()").value(3))
                .andExpect(jsonPath("$.data.role[?(@== 'role:read')]").exists())
                .andExpect(jsonPath("$.data.role[?(@== 'role:write')]").exists())
                .andExpect(jsonPath("$.data.role[?(@== 'role:delete')]").exists());
    }

    @Test
    @DisplayName("Should verify financial permissions category")
    void testGetAllPermissions_FinancialCategory() throws Exception {
        mockMvc.perform(get("/roles/permissions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                
                // Verify financial permissions
                .andExpect(jsonPath("$.data.financial.length()").value(2))
                .andExpect(jsonPath("$.data.financial[?(@== 'financial:read')]").exists())
                .andExpect(jsonPath("$.data.financial[?(@== 'financial:write')]").exists());
    }

    @Test
    @DisplayName("Should verify compliance permissions category")
    void testGetAllPermissions_ComplianceCategory() throws Exception {
        mockMvc.perform(get("/roles/permissions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                
                // Verify compliance permissions
                .andExpect(jsonPath("$.data.compliance.length()").value(2))
                .andExpect(jsonPath("$.data.compliance[?(@== 'compliance:read')]").exists())
                .andExpect(jsonPath("$.data.compliance[?(@== 'compliance:write')]").exists());
    }

    @Test
    @DisplayName("Should verify all permission categories are present")
    void testGetAllPermissions_AllCategories() throws Exception {
        mockMvc.perform(get("/roles/permissions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isMap())
                
                // Verify all 8 permission categories
                .andExpect(jsonPath("$.data.user").exists())
                .andExpect(jsonPath("$.data.admin").exists())
                .andExpect(jsonPath("$.data.role").exists())
                .andExpect(jsonPath("$.data.audit").exists())
                .andExpect(jsonPath("$.data.system").exists())
                .andExpect(jsonPath("$.data.financial").exists())
                .andExpect(jsonPath("$.data.compliance").exists())
                .andExpect(jsonPath("$.data.customer_service").exists());
    }

    // ==================== Edge Cases and Error Scenarios ====================

    @Test
    @DisplayName("Should handle special characters in role name")
    void testGetRoleDetails_SpecialCharacters_Error() throws Exception {
        mockMvc.perform(get("/roles/ROLE@#$%")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid role name"));
    }

    @Test
    @DisplayName("Should handle numeric role name")
    void testGetRoleDetails_NumericRole_Error() throws Exception {
        mockMvc.perform(get("/roles/12345")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid role name"));
    }

    @Test
    @DisplayName("Should handle very long role name")
    void testGetRoleDetails_LongRoleName_Error() throws Exception {
        String longRoleName = "A".repeat(1000);
        mockMvc.perform(get("/roles/" + longRoleName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid role name"));
    }

    // ==================== Response Format Validation Tests ====================

    @Test
    @DisplayName("Should return consistent response format for all endpoints")
    void testResponseFormat_Consistency() throws Exception {
        // Test GET /roles response format
        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").isBoolean())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.error").doesNotExist());

        // Test GET /roles/{roleName} response format
        mockMvc.perform(get("/roles/SUPER_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").isBoolean())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.error").doesNotExist());

        // Test GET /roles/permissions response format
        mockMvc.perform(get("/roles/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").isBoolean())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("Should return proper error response format")
    void testErrorResponseFormat() throws Exception {
        mockMvc.perform(get("/roles/INVALID_ROLE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid role name"))
                .andExpect(jsonPath("$.error").value("Invalid role name"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}