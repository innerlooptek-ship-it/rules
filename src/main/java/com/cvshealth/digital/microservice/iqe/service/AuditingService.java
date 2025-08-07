package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.udt.AuditEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.DATE_TIME_MILLI_SECONDS_FORMATTER;
import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.DEFAULT_USER;

@Service
@Slf4j
public class AuditingService {
    
    public AuditEntity createAuditForNew(String userId, String remarks) {
        String timestamp = LocalDateTime.now(ZoneOffset.UTC).format(DATE_TIME_MILLI_SECONDS_FORMATTER);
        
        return AuditEntity.builder()
            .createdTs(timestamp)
            .createdBy(userId != null ? userId : DEFAULT_USER)
            .modifiedTs(timestamp)
            .modifiedBy(userId != null ? userId : DEFAULT_USER)
            .remarks(remarks)
            .build();
    }
    
    public AuditEntity updateAuditForModification(AuditEntity existingAudit, String userId, String changeReason) {
        String timestamp = LocalDateTime.now(ZoneOffset.UTC).format(DATE_TIME_MILLI_SECONDS_FORMATTER);
        
        return AuditEntity.builder()
            .createdTs(existingAudit.getCreatedTs())
            .createdBy(existingAudit.getCreatedBy())
            .modifiedTs(timestamp)
            .modifiedBy(userId != null ? userId : DEFAULT_USER)
            .remarks(changeReason)
            .build();
    }
    
    public String extractUserIdFromHeaders(java.util.Map<String, String> headers) {
        return headers != null ? headers.get("user_id") : null;
    }
}
