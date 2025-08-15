package com.cvshealth.digital.microservice.iqe.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@ConfigurationProperties(prefix = "service.gcs-fallback")
@Data
public class GcsConfig {
    
    private boolean enabled = false;
    private String bucketName;
    private String keyPrefix = "iqe-fallback/";
    private String credentialsPath;
    private int timeoutSeconds = 30;
    private String projectId;
    
    @Bean
    @ConditionalOnProperty(name = "service.gcs-fallback.enabled", havingValue = "true")
    public Storage googleCloudStorage() throws IOException {
        GoogleCredentials credentials;
        
        if (credentialsPath != null && !credentialsPath.isEmpty()) {
            credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath));
        } else {
            credentials = GoogleCredentials.getApplicationDefault();
        }
        
        StorageOptions.Builder builder = StorageOptions.newBuilder()
            .setCredentials(credentials);
            
        if (projectId != null && !projectId.isEmpty()) {
            builder.setProjectId(projectId);
        }
        
        return builder.build().getService();
    }
}
