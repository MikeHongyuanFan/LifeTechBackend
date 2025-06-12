package com.finance.admin.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class MembershipNumberGenerator {

    private static final String PREFIX = "FM"; // Finance Member
    private static final int RANDOM_PART_LENGTH = 8;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generate a unique membership number
     * Format: FM-YYYYMM-XXXXXXXX
     * Where YYYYMM is year and month, XXXXXXXX is random alphanumeric
     * 
     * @return Generated membership number
     */
    public String generate() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String randomPart = generateRandomAlphanumeric(RANDOM_PART_LENGTH);
        return String.format("%s-%s-%s", PREFIX, datePart, randomPart);
    }

    /**
     * Generate membership number with custom prefix
     * 
     * @param prefix Custom prefix to use
     * @return Generated membership number
     */
    public String generateWithPrefix(String prefix) {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String randomPart = generateRandomAlphanumeric(RANDOM_PART_LENGTH);
        return String.format("%s-%s-%s", prefix, datePart, randomPart);
    }

    /**
     * Validate membership number format
     * 
     * @param membershipNumber The membership number to validate
     * @return true if valid format
     */
    public boolean isValidFormat(String membershipNumber) {
        if (membershipNumber == null || membershipNumber.trim().isEmpty()) {
            return false;
        }

        // Expected format: XX-YYYYMM-XXXXXXXX
        String[] parts = membershipNumber.split("-");
        if (parts.length != 3) {
            return false;
        }

        // Check prefix (2 letters)
        if (parts[0].length() != 2 || !parts[0].matches("[A-Z]{2}")) {
            return false;
        }

        // Check date part (YYYYMM)
        if (parts[1].length() != 6 || !parts[1].matches("\\d{6}")) {
            return false;
        }

        // Check random part (8 alphanumeric)
        if (parts[2].length() != RANDOM_PART_LENGTH || !parts[2].matches("[A-Z0-9]{8}")) {
            return false;
        }

        return true;
    }

    private String generateRandomAlphanumeric(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            result.append(characters.charAt(index));
        }
        
        return result.toString();
    }
} 