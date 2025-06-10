package com.finance.admin.config;

import com.finance.admin.audit.entity.AuditLog;
import com.finance.admin.auth.entity.AdminUser;
import com.finance.admin.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify JPA entity scanning and metamodel configuration
 * Using the successful minimal configuration pattern
 */
@SpringBootTest
@ContextConfiguration(classes = JpaEntityScanTest.TestConfig.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
public class JpaEntityScanTest {

    @EnableAutoConfiguration
    @EntityScan(basePackages = {
        "com.finance.admin.user.entity",
        "com.finance.admin.auth.entity", 
        "com.finance.admin.audit.entity",
        "com.finance.admin.common.entity"
    })
    static class TestConfig {
        // Minimal configuration for JPA testing with entity scanning
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void testJpaMetamodelNotEmpty() {
        Metamodel metamodel = entityManager.getMetamodel();
        
        assertNotNull(metamodel, "Metamodel should not be null");
        
        // Check that we have entities in the metamodel
        assertFalse(metamodel.getEntities().isEmpty(), 
            "JPA metamodel should not be empty - entities should be scanned");
        
        System.out.println("Found " + metamodel.getEntities().size() + " entities in metamodel");
        
        // Print all found entities for debugging
        for (EntityType<?> entityType : metamodel.getEntities()) {
            System.out.println("Entity found: " + entityType.getName() + " (" + entityType.getJavaType().getSimpleName() + ")");
        }
    }

    @Test
    void testSpecificEntitiesAreScanned() {
        Metamodel metamodel = entityManager.getMetamodel();
        
        // Check for specific entities
        boolean hasUser = metamodel.getEntities().stream()
            .anyMatch(e -> e.getJavaType() == User.class);
        
        boolean hasAdminUser = metamodel.getEntities().stream()
            .anyMatch(e -> e.getJavaType() == AdminUser.class);
        
        boolean hasAuditLog = metamodel.getEntities().stream()
            .anyMatch(e -> e.getJavaType() == AuditLog.class);
        
        assertTrue(hasUser, "User entity should be in metamodel");
        assertTrue(hasAdminUser, "AdminUser entity should be in metamodel");
        assertTrue(hasAuditLog, "AuditLog entity should be in metamodel");
    }
} 