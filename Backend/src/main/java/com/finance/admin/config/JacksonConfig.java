package com.finance.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(JacksonConfig.class);

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        logger.info("Configuring primary Jackson ObjectMapper");
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Register JavaTimeModule for LocalDateTime serialization
        objectMapper.registerModule(new JavaTimeModule());
        
        // Disable writing dates as timestamps
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Configure JSON output formatting
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        return objectMapper;
    }
}
