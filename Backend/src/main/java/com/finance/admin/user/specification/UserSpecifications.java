package com.finance.admin.user.specification;

import com.finance.admin.user.dto.UserSearchCriteria;
import com.finance.admin.user.entity.User;
import com.finance.admin.user.entity.UserStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {

    public static Specification<User> withCriteria(UserSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // General search term - searches across multiple fields
            if (StringUtils.hasText(criteria.getSearchTerm())) {
                String searchPattern = "%" + criteria.getSearchTerm().toLowerCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchPattern)
                );
                predicates.add(searchPredicate);
            }

            // Specific field searches
            if (StringUtils.hasText(criteria.getUsername())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("username")), 
                    "%" + criteria.getUsername().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(criteria.getEmail())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")), 
                    "%" + criteria.getEmail().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(criteria.getFirstName())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstName")), 
                    "%" + criteria.getFirstName().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(criteria.getLastName())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lastName")), 
                    "%" + criteria.getLastName().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(criteria.getPhoneNumber())) {
                predicates.add(criteriaBuilder.like(
                    root.get("phoneNumber"), 
                    "%" + criteria.getPhoneNumber() + "%"
                ));
            }

            // Status filters
            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            if (criteria.getStatuses() != null && !criteria.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(criteria.getStatuses()));
            }

            // Verification status filters
            if (criteria.getEmailVerified() != null) {
                predicates.add(criteriaBuilder.equal(root.get("emailVerified"), criteria.getEmailVerified()));
            }

            if (criteria.getPhoneVerified() != null) {
                predicates.add(criteriaBuilder.equal(root.get("phoneVerified"), criteria.getPhoneVerified()));
            }

            // Account locked filter
            if (criteria.getAccountLocked() != null) {
                if (criteria.getAccountLocked()) {
                    Predicate lockedPredicate = criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("status"), UserStatus.LOCKED),
                        criteriaBuilder.ge(root.get("failedLoginAttempts"), 5)
                    );
                    predicates.add(lockedPredicate);
                } else {
                    Predicate notLockedPredicate = criteriaBuilder.and(
                        criteriaBuilder.notEqual(root.get("status"), UserStatus.LOCKED),
                        criteriaBuilder.or(
                            criteriaBuilder.isNull(root.get("failedLoginAttempts")),
                            criteriaBuilder.lt(root.get("failedLoginAttempts"), 5)
                        )
                    );
                    predicates.add(notLockedPredicate);
                }
            }

            // Date range filters
            if (criteria.getCreatedDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), criteria.getCreatedDateFrom()));
            }

            if (criteria.getCreatedDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), criteria.getCreatedDateTo()));
            }

            if (criteria.getLastLoginFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("lastLoginAt"), criteria.getLastLoginFrom()));
            }

            if (criteria.getLastLoginTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("lastLoginAt"), criteria.getLastLoginTo()));
            }

            if (criteria.getStatusChangedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("statusChangedAt"), criteria.getStatusChangedFrom()));
            }

            if (criteria.getStatusChangedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("statusChangedAt"), criteria.getStatusChangedTo()));
            }

            // Analytics filters
            if (criteria.getMinLoginCount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("loginCount"), criteria.getMinLoginCount()));
            }

            if (criteria.getMaxLoginCount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("loginCount"), criteria.getMaxLoginCount()));
            }

            if (criteria.getMaxFailedAttempts() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("failedLoginAttempts"), criteria.getMaxFailedAttempts()));
            }

            // Geographic filters
            if (StringUtils.hasText(criteria.getTimezone())) {
                predicates.add(criteriaBuilder.equal(root.get("timezone"), criteria.getTimezone()));
            }

            if (StringUtils.hasText(criteria.getLocale())) {
                predicates.add(criteriaBuilder.equal(root.get("locale"), criteria.getLocale()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Convenience methods for common filters
    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<User> hasStatusIn(List<UserStatus> statuses) {
        return (root, query, criteriaBuilder) -> 
            root.get("status").in(statuses);
    }

    public static Specification<User> isEmailVerified(boolean verified) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("emailVerified"), verified);
    }

    public static Specification<User> isPhoneVerified(boolean verified) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("phoneVerified"), verified);
    }

    public static Specification<User> isAccountLocked() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.or(
                criteriaBuilder.equal(root.get("status"), UserStatus.LOCKED),
                criteriaBuilder.ge(root.get("failedLoginAttempts"), 5)
            );
    }

    public static Specification<User> isNotAccountLocked() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.notEqual(root.get("status"), UserStatus.LOCKED),
                criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("failedLoginAttempts")),
                    criteriaBuilder.lt(root.get("failedLoginAttempts"), 5)
                )
            );
    }

    public static Specification<User> createdAfter(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<User> createdBefore(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<User> lastLoginAfter(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.greaterThanOrEqualTo(root.get("lastLoginAt"), date);
    }

    public static Specification<User> lastLoginBefore(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.lessThanOrEqualTo(root.get("lastLoginAt"), date);
    }

    public static Specification<User> hasNeverLoggedIn() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.isNull(root.get("lastLoginAt"));
    }

    public static Specification<User> hasLoginCountGreaterThan(Long count) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.greaterThan(root.get("loginCount"), count);
    }

    public static Specification<User> hasFailedAttemptsGreaterThan(Integer attempts) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.greaterThan(root.get("failedLoginAttempts"), attempts);
    }

    public static Specification<User> hasTimezone(String timezone) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("timezone"), timezone);
    }

    public static Specification<User> hasLocale(String locale) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("locale"), locale);
    }

    public static Specification<User> searchByTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchTerm)) {
                return criteriaBuilder.conjunction();
            }
            
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchPattern)
            );
        };
    }
} 
