package com.tycoon.admin.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MfaVerificationRequest {
    
    @NotBlank(message = "MFA token is required")
    private String mfaToken;
    
    @NotBlank(message = "MFA code is required")
    @Pattern(regexp = "\\d{6}", message = "MFA code must be 6 digits")
    private String mfaCode;

    public MfaVerificationRequest() {}

    public MfaVerificationRequest(String mfaToken, String mfaCode) {
        this.mfaToken = mfaToken;
        this.mfaCode = mfaCode;
    }

    public String getMfaToken() {
        return mfaToken;
    }

    public void setMfaToken(String mfaToken) {
        this.mfaToken = mfaToken;
    }

    public String getMfaCode() {
        return mfaCode;
    }

    public void setMfaCode(String mfaCode) {
        this.mfaCode = mfaCode;
    }
} 