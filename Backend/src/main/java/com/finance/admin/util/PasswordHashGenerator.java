package com.finance.admin.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes for admin users.
 * This can be used to create initial admin passwords for database seeding.
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        String rawPassword = "admin123"; // <-- your chosen admin password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(rawPassword);
        System.out.println("Hashed password for 'admin123': " + hashedPassword);
        
        // Generate hash for 'password123' as well
        String otherPassword = "password123";
        String otherHashedPassword = encoder.encode(otherPassword);
        System.out.println("Hashed password for 'password123': " + otherHashedPassword);
    }
}
