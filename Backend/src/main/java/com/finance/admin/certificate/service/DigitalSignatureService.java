package com.finance.admin.certificate.service;

import com.finance.admin.config.DigitalSignatureConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalSignatureService {

    private final DigitalSignatureConfig signatureConfig;
    private final KeyStore keyStore;

    @Value("${digital-signature.enabled:false}")
    private boolean digitalSignatureEnabled;

    /**
     * Sign a document with digital signature
     */
    public String signDocument(byte[] documentContent, String documentId) {
        if (!digitalSignatureEnabled) {
            log.info("Digital signature is disabled, generating mock signature for document: {}", documentId);
            return generateMockSignature(documentContent, documentId);
        }

        try {
            // Get private key from keystore
            PrivateKey privateKey = getPrivateKey();
            if (privateKey == null) {
                log.warn("No private key available, generating mock signature for document: {}", documentId);
                return generateMockSignature(documentContent, documentId);
            }

            // Create signature
            Signature signature = Signature.getInstance("SHA256withRSA", BouncyCastleProvider.PROVIDER_NAME);
            signature.initSign(privateKey);
            
            // Add document content and metadata to signature
            String signatureData = documentId + ":" + LocalDateTime.now().toString();
            signature.update(documentContent);
            signature.update(signatureData.getBytes());
            
            byte[] digitalSignature = signature.sign();
            String encodedSignature = Base64.getEncoder().encodeToString(digitalSignature);
            
            log.info("Successfully generated digital signature for document: {}", documentId);
            return encodedSignature;
            
        } catch (Exception e) {
            log.error("Failed to generate digital signature for document: {}", documentId, e);
            return generateMockSignature(documentContent, documentId);
        }
    }

    /**
     * Verify a digital signature
     */
    public boolean verifySignature(byte[] documentContent, String documentId, String signatureToVerify) {
        if (!digitalSignatureEnabled) {
            log.info("Digital signature is disabled, verifying mock signature for document: {}", documentId);
            return verifyMockSignature(documentContent, documentId, signatureToVerify);
        }

        try {
            // Get public key from certificate
            PublicKey publicKey = getPublicKey();
            if (publicKey == null) {
                log.warn("No public key available, falling back to mock verification for document: {}", documentId);
                return verifyMockSignature(documentContent, documentId, signatureToVerify);
            }

            // Verify signature
            Signature signature = Signature.getInstance("SHA256withRSA", BouncyCastleProvider.PROVIDER_NAME);
            signature.initVerify(publicKey);
            
            String signatureData = documentId + ":" + LocalDateTime.now().toString();
            signature.update(documentContent);
            signature.update(signatureData.getBytes());
            
            byte[] decodedSignature = Base64.getDecoder().decode(signatureToVerify);
            boolean isValid = signature.verify(decodedSignature);
            
            log.info("Digital signature verification result for document {}: {}", documentId, isValid);
            return isValid;
            
        } catch (Exception e) {
            log.error("Failed to verify digital signature for document: {}", documentId, e);
            return false;
        }
    }

    /**
     * Get certificate information
     */
    public String getCertificateInfo() {
        try {
            Certificate certificate = getCertificate();
            if (certificate != null) {
                return certificate.toString();
            }
            return "No certificate available";
        } catch (Exception e) {
            log.error("Failed to get certificate info", e);
            return "Certificate info unavailable: " + e.getMessage();
        }
    }

    /**
     * Check if digital signature is properly configured
     */
    public boolean isConfigured() {
        if (!digitalSignatureEnabled) {
            return false;
        }
        
        try {
            PrivateKey privateKey = getPrivateKey();
            PublicKey publicKey = getPublicKey();
            return privateKey != null && publicKey != null;
        } catch (Exception e) {
            log.error("Digital signature configuration check failed", e);
            return false;
        }
    }

    private PrivateKey getPrivateKey() {
        try {
            return (PrivateKey) keyStore.getKey(
                signatureConfig.getKeyAlias(), 
                signatureConfig.getKeystorePassword().toCharArray()
            );
        } catch (Exception e) {
            log.debug("Failed to get private key from keystore", e);
            return null;
        }
    }

    private PublicKey getPublicKey() {
        try {
            Certificate certificate = getCertificate();
            return certificate != null ? certificate.getPublicKey() : null;
        } catch (Exception e) {
            log.debug("Failed to get public key from certificate", e);
            return null;
        }
    }

    private Certificate getCertificate() {
        try {
            return keyStore.getCertificate(signatureConfig.getKeyAlias());
        } catch (Exception e) {
            log.debug("Failed to get certificate from keystore", e);
            return null;
        }
    }

    private String generateMockSignature(byte[] documentContent, String documentId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String signatureData = documentId + ":" + LocalDateTime.now().toString() + ":" + documentContent.length;
            byte[] hash = digest.digest(signatureData.getBytes());
            
            StringBuilder signature = new StringBuilder("MOCK_SIG_");
            for (int i = 0; i < Math.min(hash.length, 16); i++) {
                signature.append(String.format("%02x", hash[i]));
            }
            
            return signature.toString();
        } catch (Exception e) {
            log.error("Failed to generate mock signature", e);
            return "MOCK_SIG_" + System.currentTimeMillis();
        }
    }

    private boolean verifyMockSignature(byte[] documentContent, String documentId, String signatureToVerify) {
        if (!signatureToVerify.startsWith("MOCK_SIG_")) {
            return false;
        }
        
        // For mock signatures, we just check if it's properly formatted
        return signatureToVerify.length() > 10 && signatureToVerify.matches("MOCK_SIG_[a-f0-9]+");
    }
} 