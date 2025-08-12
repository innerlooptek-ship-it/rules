package com.cvshealth.digital.microservice.iqe.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WriteOperation {
    private String actionId;
    private String type;
    private Object data;
    private Map<String, Object> metadata;
    private Instant timestamp;
    private int retryCount;
    
    public static WriteOperation create(String actionId, String type, Object data) {
        return WriteOperation.builder()
            .actionId(actionId)
            .type(type)
            .data(data)
            .timestamp(Instant.now())
            .retryCount(0)
            .build();
    }
}
