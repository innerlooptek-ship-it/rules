package com.cvshealth.digital.microservice.iqe.repository;

import com.cvshealth.digital.microservice.iqe.entity.ActionsEntity;
import com.cvshealth.digital.microservice.iqe.udt.AuditEntity;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The Class ActionsRepository
 *
 */
public interface ActionsRepository extends ReactiveCassandraRepository<ActionsEntity, String> {

    @Query("SELECT * FROM actions WHERE action_id = :actionId ")
    Flux<ActionsEntity> findByActionId(@Param("actionId") String actionId);
    
    @Query("SELECT * FROM actions WHERE action_id = :actionId AND is_active = true ALLOW FILTERING")
    Flux<ActionsEntity> findActiveByActionId(@Param("actionId") String actionId);
    
    @Query("SELECT * FROM actions WHERE is_active = true ALLOW FILTERING")
    Flux<ActionsEntity> findAllActive();
    
    @Query("UPDATE actions SET is_active = false, audit = :audit WHERE action_id = :actionId")
    Mono<Void> softDeleteByActionId(@Param("actionId") String actionId, @Param("audit") AuditEntity audit);
    
    @Query("UPDATE actions SET is_active = true, audit = :audit WHERE action_id = :actionId")
    Mono<Void> restoreByActionId(@Param("actionId") String actionId, @Param("audit") AuditEntity audit);

    Mono<Void> deleteByActionId(@Param("actionId") String actionId);
}
