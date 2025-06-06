package com.finance.admin.role.service;

import com.finance.admin.auth.entity.AdminPermission;
import com.finance.admin.auth.entity.AdminRole;
import com.finance.admin.auth.entity.AdminUser;
import com.finance.admin.auth.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private AdminUserRepository adminUserRepository;

    /**
     * Get all available roles
     */
    public List<AdminRole> getAllRoles() {
        return Arrays.asList(AdminRole.values());
    }

    /**
     * Get role by name
     */
    public Optional<AdminRole> getRoleByName(String roleName) {
        try {
            AdminRole role = AdminRole.valueOf(roleName.toUpperCase());
            return Optional.of(role);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Get all permissions
     */
    public Set<AdminPermission> getAllPermissions() {
        return Arrays.stream(AdminRole.values())
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Get permissions by category
     */
    public Map<String, List<AdminPermission>> getPermissionsByCategory() {
        return getAllPermissions().stream()
                .collect(Collectors.groupingBy(
                        permission -> permission.getPermission().split(":")[0]
                ));
    }

    /**
     * Assign role to user
     */
    @Transactional
    public void assignRoleToUser(UUID userId, AdminRole role) {
        AdminUser user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Set<AdminRole> roles = user.getRoles();
        roles.add(role);
        user.setRoles(roles);
        
        adminUserRepository.save(user);
    }

    /**
     * Remove role from user
     */
    @Transactional
    public void removeRoleFromUser(UUID userId, AdminRole role) {
        AdminUser user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Set<AdminRole> roles = user.getRoles();
        roles.remove(role);
        user.setRoles(roles);
        
        adminUserRepository.save(user);
    }

    /**
     * Get users by role
     */
    public List<AdminUser> getUsersByRole(AdminRole role) {
        return adminUserRepository.findByRole(role);
    }
}
