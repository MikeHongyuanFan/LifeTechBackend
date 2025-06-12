package com.finance.admin.client.service;

import com.finance.admin.client.dto.*;
import com.finance.admin.client.mapper.ClientMapper;
import com.finance.admin.client.model.Client;
import com.finance.admin.client.model.ClientLoginHistory;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.client.repository.ClientLoginHistoryRepository;
import com.finance.admin.common.exception.ResourceNotFoundException;
import com.finance.admin.common.exception.DuplicateResourceException;
import com.finance.admin.common.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientLoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;
    private final BlockchainService blockchainService;
    private final MembershipNumberGenerator membershipNumberGenerator;
    private final ClientMapper clientMapper;

    /**
     * Create a new client profile
     */
    public ClientResponse createClient(CreateClientRequest request) {
        log.info("Creating new client with email: {}", request.getEmailPrimary());

        // Validate for duplicates
        validateUniqueEmail(request.getEmailPrimary());

        // Generate membership number
        String membershipNumber = membershipNumberGenerator.generate();
        while (clientRepository.existsByMembershipNumber(membershipNumber)) {
            membershipNumber = membershipNumberGenerator.generate();
        }

        // Build client entity
        Client client = Client.builder()
                .membershipNumber(membershipNumber)
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .emailPrimary(request.getEmailPrimary())
                .emailSecondary(request.getEmailSecondary())
                .phonePrimary(request.getPhonePrimary())
                .phoneSecondary(request.getPhoneSecondary())
                .addressStreet(request.getAddressStreet())
                .addressCity(request.getAddressCity())
                .addressState(request.getAddressState())
                .addressPostalCode(request.getAddressPostalCode())
                .addressCountry(request.getAddressCountry())
                .mailingAddressSame(request.getMailingAddressSame())
                .mailingStreet(request.getMailingStreet())
                .mailingCity(request.getMailingCity())
                .mailingState(request.getMailingState())
                .mailingPostalCode(request.getMailingPostalCode())
                .mailingCountry(request.getMailingCountry())
                .taxResidencyStatus(request.getTaxResidencyStatus())
                .bankBsb(request.getBankBsb())
                .bankAccountName(request.getBankAccountName())
                .investmentTarget(request.getInvestmentTarget())
                .riskProfile(request.getRiskProfile())
                .status(Client.ClientStatus.PENDING)
                .build();

        // Encrypt sensitive data
        if (StringUtils.hasText(request.getTfn())) {
            client.setTfnEncrypted(encryptionService.encrypt(request.getTfn()));
        }

        if (StringUtils.hasText(request.getBankAccountNumber())) {
            client.setBankAccountNumberEncrypted(encryptionService.encrypt(request.getBankAccountNumber()));
        }

        // Save client
        client = clientRepository.save(client);

        // Generate blockchain identity hash asynchronously
        generateBlockchainIdentityHashAsync(client);

        log.info("Created client with ID: {} and membership number: {}", client.getId(), client.getMembershipNumber());
        return clientMapper.toResponse(client);
    }

    /**
     * Get client by ID
     */
    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        Client client = findClientById(id);
        return clientMapper.toResponse(client);
    }

    /**
     * Get client by membership number
     */
    @Transactional(readOnly = true)
    public ClientResponse getClientByMembershipNumber(String membershipNumber) {
        Client client = clientRepository.findByMembershipNumber(membershipNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with membership number: " + membershipNumber));
        return clientMapper.toResponse(client);
    }

    /**
     * Get all clients with pagination
     */
    @Transactional(readOnly = true)
    public Page<ClientResponse> getAllClients(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Client> clients = clientRepository.findAll(pageable);
        return clients.map(clientMapper::toResponse);
    }

    /**
     * Update client information
     */
    public ClientResponse updateClient(Long id, UpdateClientRequest request) {
        log.info("Updating client with ID: {}", id);

        Client client = findClientById(id);

        // Check for email uniqueness if changed
        if (StringUtils.hasText(request.getEmailPrimary()) && 
            !request.getEmailPrimary().equals(client.getEmailPrimary())) {
            if (clientRepository.existsByEmailPrimaryAndIdNot(request.getEmailPrimary(), id)) {
                throw new DuplicateResourceException("Email already exists: " + request.getEmailPrimary());
            }
        }

        // Update non-null fields
        updateClientFields(client, request);

        // Re-generate blockchain identity hash if sensitive data changed
        boolean sensitiveDataChanged = false;
        if (StringUtils.hasText(request.getTfn()) || 
            StringUtils.hasText(request.getEmailPrimary()) ||
            request.getDateOfBirth() != null) {
            sensitiveDataChanged = true;
        }

        client = clientRepository.save(client);

        if (sensitiveDataChanged) {
            generateBlockchainIdentityHashAsync(client);
        }

        log.info("Updated client with ID: {}", id);
        return clientMapper.toResponse(client);
    }

    /**
     * Deactivate client (soft delete)
     */
    public void deactivateClient(Long id) {
        log.info("Deactivating client with ID: {}", id);

        Client client = findClientById(id);
        client.setStatus(Client.ClientStatus.INACTIVE);
        clientRepository.save(client);

        log.info("Deactivated client with ID: {}", id);
    }

    /**
     * Search clients with advanced filtering
     */
    @Transactional(readOnly = true)
    public Page<ClientResponse> searchClients(ClientSearchRequest searchRequest) {
        log.info("Searching clients with criteria: {}", searchRequest);

        // Build pageable
        Sort.Direction direction = "DESC".equalsIgnoreCase(searchRequest.getSortDirection()) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(), 
                searchRequest.getSize(), 
                Sort.by(direction, searchRequest.getSortBy())
        );

        Page<Client> clients;

        // Simple search term query
        if (StringUtils.hasText(searchRequest.getSearchTerm()) && isSimpleSearch(searchRequest)) {
            clients = clientRepository.findBySearchTerm(searchRequest.getSearchTerm(), pageable);
        } else {
            // Complex multi-filter search
            clients = clientRepository.findByMultipleFilters(
                    searchRequest.getFirstName(),
                    searchRequest.getLastName(),
                    searchRequest.getEmail(),
                    searchRequest.getStatus(),
                    searchRequest.getCity(),
                    searchRequest.getState(),
                    searchRequest.getCountry(),
                    searchRequest.getRiskProfile(),
                    searchRequest.getHasBlockchainIdentity(),
                    pageable
            );
        }

        return clients.map(clientMapper::toResponse);
    }

    /**
     * Anchor client identity on blockchain
     */
    public void anchorClientIdentity(Long clientId) {
        log.info("Anchoring client identity on blockchain for client ID: {}", clientId);

        Client client = findClientById(clientId);
        
        if (StringUtils.hasText(client.getBlockchainIdentityHash())) {
            log.warn("Client {} already has blockchain identity hash", clientId);
            return;
        }

        String identityHash = generateIdentityHash(client);
        
        try {
            String transactionHash = blockchainService.anchorIdentity(clientId, identityHash);
            client.setBlockchainIdentityHash(identityHash);
            clientRepository.save(client);
            
            log.info("Successfully anchored identity for client {} with transaction hash: {}", clientId, transactionHash);
        } catch (Exception e) {
            log.error("Failed to anchor identity for client {}: {}", clientId, e.getMessage(), e);
            throw new RuntimeException("Failed to anchor identity on blockchain", e);
        }
    }

    /**
     * Get client activity summary
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getClientActivitySummary() {
        Map<String, Object> summary = new HashMap<>();
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        summary.put("totalClients", clientRepository.count());
        summary.put("activeClients", clientRepository.countByStatus(Client.ClientStatus.ACTIVE));
        summary.put("pendingClients", clientRepository.countByStatus(Client.ClientStatus.PENDING));
        summary.put("inactiveClients", clientRepository.countByStatus(Client.ClientStatus.INACTIVE));
        summary.put("newClientsLast30Days", clientRepository.countByCreatedAtAfter(thirtyDaysAgo));
        summary.put("newClientsLast7Days", clientRepository.countByCreatedAtAfter(sevenDaysAgo));
        summary.put("clientsWithBlockchainIdentity", clientRepository.findByBlockchainIdentityHashIsNotNull().size());
        
        return summary;
    }

    // Private helper methods

    private Client findClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + id));
    }

    private void validateUniqueEmail(String email) {
        if (clientRepository.existsByEmailPrimary(email)) {
            throw new DuplicateResourceException("Email already exists: " + email);
        }
    }

    private void updateClientFields(Client client, UpdateClientRequest request) {
        if (StringUtils.hasText(request.getFirstName())) {
            client.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getMiddleName())) {
            client.setMiddleName(request.getMiddleName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            client.setLastName(request.getLastName());
        }
        if (request.getDateOfBirth() != null) {
            client.setDateOfBirth(request.getDateOfBirth());
        }
        if (StringUtils.hasText(request.getEmailPrimary())) {
            client.setEmailPrimary(request.getEmailPrimary());
        }
        if (StringUtils.hasText(request.getEmailSecondary())) {
            client.setEmailSecondary(request.getEmailSecondary());
        }
        if (StringUtils.hasText(request.getPhonePrimary())) {
            client.setPhonePrimary(request.getPhonePrimary());
        }
        if (StringUtils.hasText(request.getPhoneSecondary())) {
            client.setPhoneSecondary(request.getPhoneSecondary());
        }
        // ... continue for all other fields

        // Handle encrypted fields
        if (StringUtils.hasText(request.getTfn())) {
            client.setTfnEncrypted(encryptionService.encrypt(request.getTfn()));
        }
        if (StringUtils.hasText(request.getBankAccountNumber())) {
            client.setBankAccountNumberEncrypted(encryptionService.encrypt(request.getBankAccountNumber()));
        }
    }

    private boolean isSimpleSearch(ClientSearchRequest request) {
        return request.getFirstName() == null && 
               request.getLastName() == null && 
               request.getEmail() == null && 
               request.getStatus() == null &&
               request.getCity() == null &&
               request.getState() == null &&
               request.getCountry() == null &&
               request.getRiskProfile() == null &&
               request.getHasBlockchainIdentity() == null;
    }

    private void generateBlockchainIdentityHashAsync(Client client) {
        // This would typically be done asynchronously
        // For now, we'll generate and save the hash
        try {
            String identityHash = generateIdentityHash(client);
            client.setBlockchainIdentityHash(identityHash);
            clientRepository.save(client);
        } catch (Exception e) {
            log.error("Failed to generate blockchain identity hash for client {}: {}", client.getId(), e.getMessage());
        }
    }

    private String generateIdentityHash(Client client) {
        try {
            // Decrypt TFN for hashing (in real implementation, ensure this is secure)
            String tfn = StringUtils.hasText(client.getTfnEncrypted()) 
                    ? encryptionService.decrypt(client.getTfnEncrypted()) : "";
            
            String dataToHash = tfn + 
                               (client.getDateOfBirth() != null ? client.getDateOfBirth().toString() : "") + 
                               (client.getEmailPrimary() != null ? client.getEmailPrimary() : "");
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToHash.getBytes());
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
} 