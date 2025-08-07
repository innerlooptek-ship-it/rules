package com.cvshealth.digital.microservice.iqe.repository;


import com.cvshealth.digital.microservice.iqe.entity.QuestionsDetailsEntity;
import com.cvshealth.digital.microservice.iqe.udt.AuditEntity;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface QuestionnaireDetailsRepository extends ReactiveCassandraRepository<QuestionsDetailsEntity, String> {
    @Query("SELECT * FROM questions_details WHERE action_id = :actionId ")
    Flux<QuestionsDetailsEntity> findByActionId(@Param("actionId") String actionId);
    
    @Query("SELECT * FROM questions_details WHERE action_id = :actionId AND is_active = true ALLOW FILTERING")
    Flux<QuestionsDetailsEntity> findActiveByActionId(@Param("actionId") String actionId);
    
    @Query("SELECT * FROM questions_details WHERE is_active = true ALLOW FILTERING")
    Flux<QuestionsDetailsEntity> findAllActive();
    
    @Query("UPDATE questions_details SET is_active = false, audit = :audit WHERE action_id = :actionId AND detail_id = :detailId")
    Mono<Void> softDeleteByActionIdAndDetailId(@Param("actionId") String actionId, @Param("detailId") String detailId, @Param("audit") AuditEntity audit);
    
    @Query("UPDATE questions_details SET is_active = true, audit = :audit WHERE action_id = :actionId AND detail_id = :detailId")
    Mono<Void> restoreByActionIdAndDetailId(@Param("actionId") String actionId, @Param("detailId") String detailId, @Param("audit") AuditEntity audit);

    Mono<Void> deleteByActionId(@Param("actionId") String actionId);

}
