package com.finance.admin.client.repository;

import com.finance.admin.client.model.ClientMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientMembershipRepository extends JpaRepository<ClientMembership, Long> {

    Optional<ClientMembership> findByClientId(Long clientId);
    
    Optional<ClientMembership> findByMembershipNumber(String membershipNumber);
    
    boolean existsByMembershipNumber(String membershipNumber);
    
    @Query("SELECT m FROM ClientMembership m WHERE m.clientId = :clientId AND m.membershipStatus = 'ACTIVE'")
    Optional<ClientMembership> findActiveByClientId(@Param("clientId") Long clientId);
} 