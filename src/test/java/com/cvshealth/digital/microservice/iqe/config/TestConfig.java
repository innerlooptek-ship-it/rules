package com.cvshealth.digital.microservice.iqe.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public Map<String, String> errorMessages() {
        Map<String, String> messages = new HashMap<>();
        messages.put("test.error", "Test error message");
        return messages;
    }
}
