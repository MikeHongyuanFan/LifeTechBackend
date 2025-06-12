package com.finance.admin.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;

@Configuration
public class DigitalSignatureConfig {

    @Value("${digital-signature.keystore.path:classpath:certificates/keystore.p12}")
    private String keystorePath;

    @Value("${digital-signature.keystore.password:changeit}")
    private String keystorePassword;

    @Value("${digital-signature.key.alias:certificate-signing}")
    private String keyAlias;

    @PostConstruct
    public void init() {
        // Add BouncyCastle as a security provider
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    public KeyStore keyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        
        try {
            // Try to load from file system first
            if (!keystorePath.startsWith("classpath:")) {
                try (FileInputStream fis = new FileInputStream(keystorePath)) {
                    keyStore.load(fis, keystorePassword.toCharArray());
                    return keyStore;
                }
            }
        } catch (Exception e) {
            // Fall back to creating empty keystore for development
        }
        
        // Initialize empty keystore for development/testing
        keyStore.load(null, keystorePassword.toCharArray());
        return keyStore;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }
} 