package com.finance.admin.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    @Value("${app.encryption.key:defaultkey1234567890123456}")
    private String encryptionKey;

    /**
     * Encrypt sensitive data using AES-GCM
     * 
     * @param plainText The text to encrypt
     * @return Base64 encoded encrypted data with IV prepended
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            // Generate a random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // Create cipher instance
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKey secretKey = getSecretKey();
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            
            // Initialize cipher for encryption
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);
            
            // Encrypt the data
            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            byte[] encryptedWithIv = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encryptedData, 0, encryptedWithIv, iv.length, encryptedData.length);
            
            // Return Base64 encoded result
            return Base64.getEncoder().encodeToString(encryptedWithIv);
            
        } catch (Exception e) {
            log.error("Failed to encrypt data: {}", e.getMessage(), e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt sensitive data using AES-GCM
     * 
     * @param encryptedText Base64 encoded encrypted data with IV prepended
     * @return Decrypted plain text
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            // Decode from Base64
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            
            System.arraycopy(encryptedWithIv, 0, iv, 0, iv.length);
            System.arraycopy(encryptedWithIv, iv.length, encryptedData, 0, encryptedData.length);
            
            // Create cipher instance
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKey secretKey = getSecretKey();
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            
            // Initialize cipher for decryption
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
            
            // Decrypt the data
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Failed to decrypt data: {}", e.getMessage(), e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Generate a hash of sensitive data for comparison without decryption
     * 
     * @param plainText The text to hash
     * @return SHA-256 hash of the input
     */
    public String hash(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            log.error("Failed to hash data: {}", e.getMessage(), e);
            throw new RuntimeException("Hashing failed", e);
        }
    }

    /**
     * Verify if plain text matches encrypted text
     * 
     * @param plainText The plain text to verify
     * @param encryptedText The encrypted text to compare against
     * @return true if they match
     */
    public boolean verify(String plainText, String encryptedText) {
        try {
            String decrypted = decrypt(encryptedText);
            return plainText.equals(decrypted);
        } catch (Exception e) {
            log.error("Failed to verify encrypted data: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate a secure random key for encryption
     * 
     * @return Base64 encoded encryption key
     */
    public String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256); // AES-256
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            log.error("Failed to generate encryption key: {}", e.getMessage(), e);
            throw new RuntimeException("Key generation failed", e);
        }
    }

    private SecretKey getSecretKey() {
        try {
            // In production, this should be loaded from a secure key management service
            // For now, we'll use the configured key
            byte[] keyBytes;
            
            if (encryptionKey.length() == 32) {
                // If key is 32 characters, use it directly
                keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
            } else {
                // If key is Base64 encoded, decode it
                keyBytes = Base64.getDecoder().decode(encryptionKey);
            }
            
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            log.error("Failed to get secret key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get encryption key", e);
        }
    }
} 