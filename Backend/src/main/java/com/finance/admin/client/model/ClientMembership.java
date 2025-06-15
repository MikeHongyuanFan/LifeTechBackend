package com.finance.admin.client.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "client_membership")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "membership_number", unique = true, nullable = false, length = 50)
    private String membershipNumber;

    @Column(name = "membership_tier", length = 20)
    @Builder.Default
    private String membershipTier = "BASIC";

    @Column(name = "membership_status", length = 20)
    @Builder.Default
    private String membershipStatus = "ACTIVE";

    @Column(name = "joined_date", nullable = false)
    private LocalDate joinedDate;

    @Column(name = "tier_upgrade_date")
    private LocalDate tierUpgradeDate;

    @Column(name = "points_balance")
    @Builder.Default
    private Integer pointsBalance = 0;

    @Column(name = "benefits_unlocked", columnDefinition = "jsonb")
    private String benefitsUnlocked;

    @Column(name = "digital_card_issued")
    @Builder.Default
    private Boolean digitalCardIssued = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 