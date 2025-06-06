# Finance Admin Management System - Password Setup Guide

This document outlines the password management system implemented in the Finance Admin Management System, including how to set up initial admin users, password security features, and best practices.

## 🔐 Password Security Implementation

The system uses BCrypt password hashing with the following security features:

- **BCrypt Encoding**: All passwords are hashed using BCrypt with a strength factor of 10
- **Password Expiration**: Configurable password expiration periods
- **Account Locking**: Automatic account locking after failed login attempts
- **Force Password Change**: Ability to force users to change passwords on next login
- **Password History**: Prevention of password reuse (configurable depth)

## 📋 Setting Up Admin Users

### Option 1: Using the AdminUserSeeder (Recommended)

The system includes an `AdminUserSeeder` class that automatically creates a default admin user on application startup if one doesn't exist:

```java
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
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setRole(AdminRole.SUPER_ADMIN);
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
            
            System.out.println("Default admin user created with username: admin and password: Admin@123");
        }
    }
}
```

### Option 2: Manual Password Hash Generation

For creating admin users manually or for testing purposes, you can use the `PasswordHashGenerator` utility:

```java
public class PasswordHashGenerator {
    public static void main(String[] args) {
        String rawPassword = "Admin@123"; // <-- your chosen admin password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(rawPassword);
        System.out.println("Hashed password: " + hashedPassword);
    }
}
```

Run this utility to generate a BCrypt hash, then insert it directly into the database:

```sql
INSERT INTO admin_users (id, username, email, password_hash, role, status, mfa_enabled, created_at, updated_at)
VALUES (
    gen_random_uuid(), 'admin', 'admin@finance.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIo0jQDjT6VmRq',
    'SUPER_ADMIN', 'ACTIVE', false, NOW(), NOW()
);
```

### Option 3: Using the API (After Initial Setup)

Once you have at least one admin user set up, you can use the Admin API to create additional users:

```bash
curl -X POST \
  http://localhost:8090/api/admin/users \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "newadmin",
    "email": "newadmin@finance.com",
    "password": "SecurePass@123",
    "role": "SYSTEM_ADMIN"
  }'
```

## ⚠️ Troubleshooting Password Issues

### BCrypt Hash Format Issues

If you see the error `Encoded password does not look like BCrypt` in the logs, ensure:

1. The password hash in the database starts with `$2a$10$`
2. The hash is not truncated (should be ~60 characters)
3. The database column is wide enough (VARCHAR(255) recommended)

To fix a truncated hash, update it directly:

```sql
UPDATE admin_users 
SET password_hash = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIo0jQDjT6VmRq' 
WHERE username = 'admin';
```

### Database Schema

The admin_users table should have these password-related columns:

```sql
CREATE TABLE admin_users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    last_login TIMESTAMP,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    password_expires_at TIMESTAMP,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret VARCHAR(100),
    force_password_change BOOLEAN NOT NULL DEFAULT FALSE,
    last_password_change TIMESTAMP,
    session_timeout_minutes INTEGER DEFAULT 30,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);
```

## 🔒 Password Policy Configuration

Password policies are configured in `application.yml`:

```yaml
finance:
  security:
    password:
      min-length: 8
      require-uppercase: true
      require-lowercase: true
      require-digit: true
      require-special-char: true
      max-age-days: 90
      history-count: 5
      max-failed-attempts: 5
      lockout-duration-minutes: 30
```

## 🔄 Password Reset Flow

1. User requests password reset via `/api/admin/auth/forgot-password`
2. System generates a time-limited reset token
3. Reset link is sent to user's email
4. User sets new password via `/api/admin/auth/reset-password`
5. System validates token and updates password

## 🛡️ Best Practices

1. **Change Default Passwords**: Always change the default admin password after initial setup
2. **Regular Rotation**: Implement regular password rotation for admin accounts
3. **MFA**: Enable Multi-Factor Authentication for all admin users
4. **IP Restrictions**: Use the IP whitelisting feature for admin accounts
5. **Audit Logging**: Monitor failed login attempts and password changes
6. **Session Management**: Set appropriate session timeout values

## 📝 Testing Authentication

To test authentication with the default admin user:

```bash
curl -X POST \
  http://localhost:8090/api/admin/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "admin",
    "password": "Admin@123"
  }'
```

A successful response will include JWT access and refresh tokens.
