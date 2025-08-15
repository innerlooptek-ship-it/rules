package com.cvshealth.digital.microservice.iqe.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "state-minor-age-limit")
@Data
public class StateMinorAgeConfig {
    private Map<String, Integer> config;
}