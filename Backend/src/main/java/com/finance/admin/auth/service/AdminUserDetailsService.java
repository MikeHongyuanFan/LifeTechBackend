package com.finance.admin.auth.service;

import com.finance.admin.auth.entity.AdminUser;
import com.finance.admin.auth.repository.AdminUserRepository;
import com.finance.admin.auth.security.AdminUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserDetailsService.class);

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        logger.debug("Loading user details for: {}", usernameOrEmail);
        
        AdminUser user = adminUserRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> {
                    logger.warn("Admin user not found with username or email: {}", usernameOrEmail);
                    return new UsernameNotFoundException("Admin user not found with username or email: " + usernameOrEmail);
                });

        logger.debug("Successfully loaded user details for: {}, Status: {}", 
                    user.getUsername(), user.getStatus());
        
        return AdminUserPrincipal.create(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) {
        logger.debug("Loading user details for ID: {}", id);
        
        AdminUser user = adminUserRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Admin user not found with ID: {}", id);
                    return new UsernameNotFoundException("Admin user not found with ID: " + id);
                });

        logger.debug("Successfully loaded user details for ID: {}, Username: {}", 
                    id, user.getUsername());
        
        return AdminUserPrincipal.create(user);
    }
} 
