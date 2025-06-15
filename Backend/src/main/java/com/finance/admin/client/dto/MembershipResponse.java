package com.finance.admin.client.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class MembershipResponse {
    private Long id;
    private Long clientId;
    private String membershipNumber;
    private String membershipTier;
    private String membershipStatus;
    private LocalDate joinedDate;
    private LocalDate tierUpgradeDate;
    private Integer pointsBalance;
    private Map<String, Object> benefitsUnlocked;
    private Boolean digitalCardIssued;
    private String qrCode;
} 