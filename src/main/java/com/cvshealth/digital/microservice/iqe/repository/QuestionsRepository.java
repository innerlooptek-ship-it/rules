package com.cvshealth.digital.microservice.iqe.repository;

import com.cvshealth.digital.microservice.iqe.entity.QuestionsEntity;
import com.cvshealth.digital.microservice.iqe.udt.AuditEntity;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The Class QuestionsRepository
 *
 */

public interface QuestionsRepository  extends ReactiveCassandraRepository<QuestionsEntity, String> {
    @Query("SELECT * FROM questions WHERE action_id = :actionId ")
    Flux<QuestionsEntity> findByActionId(@Param("actionId") String actionId);
    
    @Query("SELECT * FROM questions WHERE action_id = :actionId AND is_active = true ALLOW FILTERING")
    Flux<QuestionsEntity> findActiveByActionId(@Param("actionId") String actionId);
    
    @Query("SELECT * FROM questions WHERE is_active = true ALLOW FILTERING")
    Flux<QuestionsEntity> findAllActive();
    
    @Query("UPDATE questions SET is_active = false, audit = :audit WHERE action_id = :actionId AND question_id = :questionId")
    Mono<Void> softDeleteByActionIdAndQuestionId(@Param("actionId") String actionId, @Param("questionId") String questionId, @Param("audit") AuditEntity audit);
    
    @Query("UPDATE questions SET is_active = true, audit = :audit WHERE action_id = :actionId AND question_id = :questionId")
    Mono<Void> restoreByActionIdAndQuestionId(@Param("actionId") String actionId, @Param("questionId") String questionId, @Param("audit") AuditEntity audit);

    Mono<Void> deleteByActionId(@Param("actionId") String actionId);

    @Query("SELECT * FROM questions WHERE action_id = :actionId AND question_id = :questionId")
    Mono<QuestionsEntity> findByActionIdAndQuestionId(@Param("actionId") String actionId, @Param("questionId") String questionId);
    
    @Query("SELECT * FROM questions WHERE action_id = :actionId AND question_id = :questionId AND is_active = true")
    Mono<QuestionsEntity> findActiveByActionIdAndQuestionId(@Param("actionId") String actionId, @Param("questionId") String questionId);


}
