package com.tycoon.admin.auth.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class MfaService {

    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final int TIME_STEP = 30; // 30 seconds
    private static final int CODE_DIGITS = 6;
    private static final int SECRET_LENGTH = 20; // 160 bits

    public String generateSecret() {
        byte[] secret = new byte[SECRET_LENGTH];
        new SecureRandom().nextBytes(secret);
        return Base64.getEncoder().encodeToString(secret);
    }

    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }

        try {
            long currentTime = Instant.now().getEpochSecond();
            long timeWindow = currentTime / TIME_STEP;

            // Check current window and adjacent windows (to account for clock skew)
            for (int i = -1; i <= 1; i++) {
                String expectedCode = generateCode(secret, timeWindow + i);
                if (code.equals(expectedCode)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public String generateCode(String secret, long timeWindow) {
        try {
            byte[] secretBytes = Base64.getDecoder().decode(secret);
            byte[] timeBytes = ByteBuffer.allocate(8).putLong(timeWindow).array();

            Mac mac = Mac.getInstance(HMAC_SHA1);
            SecretKeySpec keySpec = new SecretKeySpec(secretBytes, HMAC_SHA1);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(timeBytes);

            int offset = hash[hash.length - 1] & 0x0F;
            int truncatedHash = ((hash[offset] & 0x7F) << 24) |
                               ((hash[offset + 1] & 0xFF) << 16) |
                               ((hash[offset + 2] & 0xFF) << 8) |
                               (hash[offset + 3] & 0xFF);

            int code = truncatedHash % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%0" + CODE_DIGITS + "d", code);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating TOTP code", e);
        }
    }

    public String generateQrCodeUrl(String username, String secret, String issuer) {
        String encodedSecret = secret.replace("=", "");
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=%d&period=%d",
            issuer,
            username,
            encodedSecret,
            issuer,
            CODE_DIGITS,
            TIME_STEP
        );
    }
} 