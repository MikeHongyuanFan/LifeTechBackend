package com.finance.admin.client.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientLoginRequest {

    @NotBlank(message = "Email or phone is required")
    private String emailOrPhone;

    @NotBlank(message = "Password is required")
    private String password;

    // Optional MFA token
    private String mfaToken;

    // Device information for security tracking
    private String deviceFingerprint;
    private String userAgent;
    private String ipAddress;
} 