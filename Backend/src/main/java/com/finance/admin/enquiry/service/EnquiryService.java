package com.finance.admin.enquiry.service;

import com.finance.admin.client.model.Client;
import com.finance.admin.client.repository.ClientRepository;
import com.finance.admin.enquiry.dto.CreateEnquiryRequest;
import com.finance.admin.enquiry.dto.EnquiryResponse;
import com.finance.admin.enquiry.dto.UpdateEnquiryRequest;
import com.finance.admin.enquiry.model.Enquiry;
import com.finance.admin.enquiry.repository.EnquiryRepository;
import com.finance.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    /**
     * Create a new enquiry
     */
    public EnquiryResponse createEnquiry(CreateEnquiryRequest request) {
        log.info("Creating enquiry with subject: {}", request.getSubject());

        // Validate client if provided
        Client client = null;
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + request.getClientId()));
        }

        // Generate enquiry number
        String enquiryNumber = generateEnquiryNumber();

        // Create enquiry entity
        Enquiry enquiry = Enquiry.builder()
            .enquiryNumber(enquiryNumber)
            .subject(request.getSubject())
            .description(request.getDescription())
            .enquiryType(request.getEnquiryType())
            .priority(request.getPriority() != null ? request.getPriority() : Enquiry.Priority.MEDIUM)
            .contactName(request.getContactName())
            .contactEmail(request.getContactEmail())
            .contactPhone(request.getContactPhone())
            .client(client)
            .assignedTo(request.getAssignedTo())
            .source(request.getSource())
            .category(request.getCategory())
            .tags(request.getTags())
            .internalNotes(request.getInternalNotes())
            .dueDate(request.getDueDate())
            .status(Enquiry.EnquiryStatus.OPEN)
            .build();

        Enquiry savedEnquiry = enquiryRepository.save(enquiry);
        log.info("Enquiry created successfully with ID: {} and number: {}", savedEnquiry.getId(), enquiryNumber);

        return mapToResponse(savedEnquiry);
    }

    /**
     * Get enquiry by ID
     */
    @Transactional(readOnly = true)
    public EnquiryResponse getEnquiryById(Long id) {
        Enquiry enquiry = enquiryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Enquiry not found with ID: " + id));
        return mapToResponse(enquiry);
    }

    /**
     * Get enquiry by enquiry number
     */
    @Transactional(readOnly = true)
    public EnquiryResponse getEnquiryByNumber(String enquiryNumber) {
        Enquiry enquiry = enquiryRepository.findByEnquiryNumber(enquiryNumber)
            .orElseThrow(() -> new RuntimeException("Enquiry not found with number: " + enquiryNumber));
        return mapToResponse(enquiry);
    }

    /**
     * Update enquiry
     */
    public EnquiryResponse updateEnquiry(Long id, UpdateEnquiryRequest request) {
        log.info("Updating enquiry with ID: {}", id);

        Enquiry enquiry = enquiryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Enquiry not found with ID: " + id));

        // Update fields if provided
        if (request.getSubject() != null) {
            enquiry.setSubject(request.getSubject());
        }
        if (request.getDescription() != null) {
            enquiry.setDescription(request.getDescription());
        }
        if (request.getEnquiryType() != null) {
            enquiry.setEnquiryType(request.getEnquiryType());
        }
        if (request.getPriority() != null) {
            enquiry.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            enquiry.setStatus(request.getStatus());
            
            // Set response date if status changed to IN_PROGRESS and no response date set
            if (request.getStatus() == Enquiry.EnquiryStatus.IN_PROGRESS && enquiry.getResponseDate() == null) {
                enquiry.setResponseDate(LocalDateTime.now());
            }
            
            // Set resolved date if status changed to RESOLVED or CLOSED
            if ((request.getStatus() == Enquiry.EnquiryStatus.RESOLVED || 
                 request.getStatus() == Enquiry.EnquiryStatus.CLOSED) && 
                enquiry.getResolvedDate() == null) {
                enquiry.setResolvedDate(LocalDateTime.now());
            }
        }
        if (request.getContactName() != null) {
            enquiry.setContactName(request.getContactName());
        }
        if (request.getContactEmail() != null) {
            enquiry.setContactEmail(request.getContactEmail());
        }
        if (request.getContactPhone() != null) {
            enquiry.setContactPhone(request.getContactPhone());
        }
        if (request.getAssignedTo() != null) {
            enquiry.setAssignedTo(request.getAssignedTo());
        }
        if (request.getResponse() != null) {
            enquiry.setResponse(request.getResponse());
        }
        if (request.getResponseDate() != null) {
            enquiry.setResponseDate(request.getResponseDate());
        }
        if (request.getResolvedDate() != null) {
            enquiry.setResolvedDate(request.getResolvedDate());
        }
        if (request.getDueDate() != null) {
            enquiry.setDueDate(request.getDueDate());
        }
        if (request.getSource() != null) {
            enquiry.setSource(request.getSource());
        }
        if (request.getCategory() != null) {
            enquiry.setCategory(request.getCategory());
        }
        if (request.getTags() != null) {
            enquiry.setTags(request.getTags());
        }
        if (request.getInternalNotes() != null) {
            enquiry.setInternalNotes(request.getInternalNotes());
        }

        Enquiry updatedEnquiry = enquiryRepository.save(enquiry);
        log.info("Enquiry updated successfully with ID: {}", id);

        return mapToResponse(updatedEnquiry);
    }

    /**
     * Delete enquiry
     */
    public void deleteEnquiry(Long id) {
        log.info("Deleting enquiry with ID: {}", id);

        Enquiry enquiry = enquiryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Enquiry not found with ID: " + id));

        enquiryRepository.delete(enquiry);
        log.info("Enquiry deleted successfully with ID: {}", id);
    }

    /**
     * Get all enquiries with pagination
     */
    @Transactional(readOnly = true)
    public Page<EnquiryResponse> getAllEnquiries(Pageable pageable) {
        return enquiryRepository.findAll(pageable)
            .map(this::mapToResponse);
    }

    /**
     * Get recent enquiries
     */
    @Transactional(readOnly = true)
    public Page<EnquiryResponse> getRecentEnquiries(Pageable pageable, String status, String type) {
        if (status != null && type != null) {
            Enquiry.EnquiryStatus enquiryStatus = Enquiry.EnquiryStatus.valueOf(status.toUpperCase());
            Enquiry.EnquiryType enquiryType = Enquiry.EnquiryType.valueOf(type.toUpperCase());
            return enquiryRepository.findByMultipleFilters(
                null, enquiryType, enquiryStatus, null, null, null, null, null, null, null, pageable
            ).map(this::mapToResponse);
        } else if (status != null) {
            Enquiry.EnquiryStatus enquiryStatus = Enquiry.EnquiryStatus.valueOf(status.toUpperCase());
            return enquiryRepository.findByStatus(enquiryStatus, pageable)
                .map(this::mapToResponse);
        } else if (type != null) {
            Enquiry.EnquiryType enquiryType = Enquiry.EnquiryType.valueOf(type.toUpperCase());
            return enquiryRepository.findByEnquiryType(enquiryType, pageable)
                .map(this::mapToResponse);
        } else {
            return enquiryRepository.findRecentEnquiries(pageable)
                .map(this::mapToResponse);
        }
    }

    /**
     * Search enquiries with filters
     */
    @Transactional(readOnly = true)
    public Page<EnquiryResponse> searchEnquiries(
            Long clientId, Enquiry.EnquiryType enquiryType, Enquiry.EnquiryStatus status,
            Enquiry.Priority priority, java.util.UUID assignedTo, String source, String category,
            LocalDateTime startDate, LocalDateTime endDate, String searchTerm,
            Pageable pageable) {

        return enquiryRepository.findByMultipleFilters(
            clientId, enquiryType, status, priority, assignedTo, source, category,
            startDate, endDate, searchTerm, pageable
        ).map(this::mapToResponse);
    }

    /**
     * Get enquiry statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEnquiryStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic counts
        long totalEnquiries = enquiryRepository.count();
        long openEnquiries = enquiryRepository.countByStatus(Enquiry.EnquiryStatus.OPEN);
        long inProgressEnquiries = enquiryRepository.countByStatus(Enquiry.EnquiryStatus.IN_PROGRESS);
        long pendingEnquiries = enquiryRepository.countByStatus(Enquiry.EnquiryStatus.PENDING_CLIENT);
        long resolvedEnquiries = enquiryRepository.countByStatus(Enquiry.EnquiryStatus.RESOLVED);
        long closedEnquiries = enquiryRepository.countByStatus(Enquiry.EnquiryStatus.CLOSED);
        
        stats.put("totalEnquiries", totalEnquiries);
        stats.put("openEnquiries", openEnquiries);
        stats.put("inProgressEnquiries", inProgressEnquiries);
        stats.put("pendingEnquiries", pendingEnquiries);
        stats.put("resolvedEnquiries", resolvedEnquiries);
        stats.put("closedEnquiries", closedEnquiries);
        
        // Performance metrics
        Double averageResponseTime = enquiryRepository.getAverageResponseTimeHours();
        Double averageResolutionTime = enquiryRepository.getAverageResolutionTimeHours();
        
        stats.put("averageResponseTime", averageResponseTime != null ? averageResponseTime : 0.0);
        stats.put("averageResolutionTime", averageResolutionTime != null ? averageResolutionTime : 0.0);
        
        // Recent activity
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
        long newEnquiriesLast30Days = enquiryRepository.countByCreatedAtAfter(last30Days);
        
        stats.put("newEnquiriesLast30Days", newEnquiriesLast30Days);
        
        // Resolution rate
        Double resolutionRate = enquiryRepository.getResolutionRatePercentage(last30Days);
        stats.put("resolutionRate", resolutionRate != null ? resolutionRate : 0.0);
        
        // Overdue and unassigned
        long overdueEnquiries = enquiryRepository.countOverdueEnquiries();
        long unassignedEnquiries = enquiryRepository.countUnassignedEnquiries();
        
        stats.put("overdueEnquiries", overdueEnquiries);
        stats.put("unassignedEnquiries", unassignedEnquiries);
        
        return stats;
    }

    /**
     * Get enquiries by client ID
     */
    @Transactional(readOnly = true)
    public Page<EnquiryResponse> getEnquiriesByClientId(Long clientId, Pageable pageable) {
        // Validate client exists
        if (!clientRepository.existsById(clientId)) {
            throw new RuntimeException("Client not found with ID: " + clientId);
        }

        return enquiryRepository.findByClientId(clientId, pageable)
            .map(this::mapToResponse);
    }

    /**
     * Assign enquiry to user
     */
    public EnquiryResponse assignEnquiry(Long enquiryId, java.util.UUID userId) {
        log.info("Assigning enquiry {} to user {}", enquiryId, userId);

        Enquiry enquiry = enquiryRepository.findById(enquiryId)
            .orElseThrow(() -> new RuntimeException("Enquiry not found with ID: " + enquiryId));

        // TODO: Add user validation when UserService is available
        // In production, this would need proper user ID mapping or schema alignment

        enquiry.setAssignedTo(userId);
        
        // Update status to IN_PROGRESS if currently OPEN
        if (enquiry.getStatus() == Enquiry.EnquiryStatus.OPEN) {
            enquiry.setStatus(Enquiry.EnquiryStatus.IN_PROGRESS);
            enquiry.setResponseDate(LocalDateTime.now());
        }

        Enquiry updatedEnquiry = enquiryRepository.save(enquiry);
        log.info("Enquiry {} assigned successfully to user {}", enquiryId, userId);

        return mapToResponse(updatedEnquiry);
    }

    /**
     * Generate unique enquiry number
     */
    private String generateEnquiryNumber() {
        String prefix = "ENQ";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // Find the next sequence number for today
        String pattern = prefix + timestamp + "%";
        long count = enquiryRepository.count(); // Simple approach, could be improved
        
        String enquiryNumber;
        do {
            count++;
            enquiryNumber = String.format("%s%s%04d", prefix, timestamp, count);
        } while (enquiryRepository.existsByEnquiryNumber(enquiryNumber));
        
        return enquiryNumber;
    }

    /**
     * Map Enquiry entity to response DTO
     */
    private EnquiryResponse mapToResponse(Enquiry enquiry) {
        EnquiryResponse.EnquiryResponseBuilder builder = EnquiryResponse.builder()
            .id(enquiry.getId())
            .enquiryNumber(enquiry.getEnquiryNumber())
            .subject(enquiry.getSubject())
            .description(enquiry.getDescription())
            .enquiryType(enquiry.getEnquiryType())
            .enquiryTypeDisplayName(enquiry.getEnquiryType() != null ? enquiry.getEnquiryType().getDisplayName() : null)
            .priority(enquiry.getPriority())
            .priorityDisplayName(enquiry.getPriority() != null ? enquiry.getPriority().getDisplayName() : null)
            .status(enquiry.getStatus())
            .statusDisplayName(enquiry.getStatus() != null ? enquiry.getStatus().getDisplayName() : null)
            .contactName(enquiry.getContactName())
            .contactEmail(enquiry.getContactEmail())
            .contactPhone(enquiry.getContactPhone())
            .assignedTo(enquiry.getAssignedTo())
            .response(enquiry.getResponse())
            .responseDate(enquiry.getResponseDate())
            .resolvedDate(enquiry.getResolvedDate())
            .dueDate(enquiry.getDueDate())
            .source(enquiry.getSource())
            .category(enquiry.getCategory())
            .tags(enquiry.getTags())
            .internalNotes(enquiry.getInternalNotes())
            .createdAt(enquiry.getCreatedAt())
            .updatedAt(enquiry.getUpdatedAt())
            .createdBy(enquiry.getCreatedBy())
            .updatedBy(enquiry.getUpdatedBy())
            .isOpen(enquiry.isOpen())
            .isResolved(enquiry.isResolved())
            .isOverdue(enquiry.isOverdue())
            .responseTimeHours(enquiry.getResponseTimeHours());

        // Add client information if available
        if (enquiry.getClient() != null) {
            builder.clientId(enquiry.getClient().getId())
                   .clientName(enquiry.getClient().getFullName())
                   .clientEmail(enquiry.getClient().getEmailPrimary());
        }

        // Calculate computed fields
        if (enquiry.getCreatedAt() != null) {
            long daysSinceCreated = ChronoUnit.DAYS.between(enquiry.getCreatedAt(), LocalDateTime.now());
            builder.daysSinceCreated(daysSinceCreated);
        }

        if (enquiry.getDueDate() != null) {
            long daysUntilDue = ChronoUnit.DAYS.between(LocalDateTime.now(), enquiry.getDueDate());
            builder.daysUntilDue(daysUntilDue);
        }

        return builder.build();
    }
} 