package com.finance.admin.config;

import com.finance.admin.auth.entity.AdminRole;
import com.finance.admin.auth.entity.AdminUser;
import com.finance.admin.auth.entity.AdminUserStatus;
import com.finance.admin.auth.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Seeds the database with an initial admin user on application startup.
 * Only creates the admin user if it doesn't already exist.
 */
@Component
public class AdminUserSeeder implements CommandLineRunner {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminUserRepository userRepository;

    @Override
    public void run(String... args) {
        // Check if admin user exists
        Optional<AdminUser> existingAdmin = userRepository.findByUsername("admin");
        
        if (existingAdmin.isEmpty()) {
            // Create a new admin user
            AdminUser admin = new AdminUser();
            admin.setUsername("admin");
            admin.setEmail("admin@finance.com");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setStatus(AdminUserStatus.ACTIVE);
            admin.setMfaEnabled(false);
            admin.setForcePasswordChange(false);
            admin.setFailedLoginAttempts(0);
            admin.setSessionTimeoutMinutes(30);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setVersion(0L);
            
            // Add SUPER_ADMIN role
            Set<AdminRole> roles = new HashSet<>();
            roles.add(AdminRole.SUPER_ADMIN);
            admin.setRoles(roles);
            
            userRepository.save(admin);
            
            System.out.println("Default admin user created with username: admin and password: admin123");
        } else {
            System.out.println("Admin user already exists, skipping seeding");
        }
    }
}
