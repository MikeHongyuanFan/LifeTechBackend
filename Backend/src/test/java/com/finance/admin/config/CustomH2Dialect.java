package com.finance.admin.config;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.SqlTypes;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.type.descriptor.jdbc.JsonJdbcType;
import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.service.ServiceRegistry;

/**
 * Custom H2 dialect that adds support for PostgreSQL JSONB type
 * by mapping it to JSON in H2
 */
public class CustomH2Dialect extends H2Dialect {
    
    public CustomH2Dialect() {
        super();
    }

    public CustomH2Dialect(DialectResolutionInfo info) {
        super(info);
    }

    @Override
    public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        super.contributeTypes(typeContributions, serviceRegistry);
        
        // Register JSON JDBC type support
        typeContributions.getTypeConfiguration().getJdbcTypeRegistry()
            .addDescriptor(SqlTypes.JSON, JsonJdbcType.INSTANCE);
    }

    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);
        
        // Register PostgreSQL JSON functions
        functionContributions.getFunctionRegistry().register(
            "jsonb_extract_path_text",
            new StandardSQLFunction("json_extract", StandardBasicTypes.STRING)
        );
        
        functionContributions.getFunctionRegistry().register(
            "jsonb_extract_path",
            new StandardSQLFunction("json_extract", StandardBasicTypes.STRING)
        );
    }
} 