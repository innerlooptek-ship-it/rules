package com.cvshealth.digital.microservice.iqe.repository;

import com.cvshealth.digital.microservice.iqe.entity.AnswerOptionsEntity;
import com.cvshealth.digital.microservice.iqe.udt.AuditEntity;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The Class AnswerOptionsRepository
 *
 */

public interface AnswerOptionsRepository  extends ReactiveCassandraRepository<AnswerOptionsEntity, String> {

    @Query("SELECT * FROM answer_options WHERE action_id = :actionId ")
    Flux<AnswerOptionsEntity> findByActionId(@Param("actionId") String actionId);
    
    @Query("SELECT * FROM answer_options WHERE action_id = :actionId AND is_active = true ALLOW FILTERING")
    Flux<AnswerOptionsEntity> findActiveByActionId(@Param("actionId") String actionId);
    
    @Query("SELECT * FROM answer_options WHERE is_active = true ALLOW FILTERING")
    Flux<AnswerOptionsEntity> findAllActive();
    
    @Query("UPDATE answer_options SET is_active = false, audit = :audit WHERE action_id = :actionId AND question_id = :questionId AND answer_option_id = :answerOptionId")
    Mono<Void> softDeleteByActionIdAndQuestionIdAndAnswerOptionId(@Param("actionId") String actionId, @Param("questionId") String questionId, @Param("answerOptionId") String answerOptionId, @Param("audit") AuditEntity audit);
    
    @Query("UPDATE answer_options SET is_active = true, audit = :audit WHERE action_id = :actionId AND question_id = :questionId AND answer_option_id = :answerOptionId")
    Mono<Void> restoreByActionIdAndQuestionIdAndAnswerOptionId(@Param("actionId") String actionId, @Param("questionId") String questionId, @Param("answerOptionId") String answerOptionId, @Param("audit") AuditEntity audit);

    Mono<Void> deleteByActionId(@Param("actionId") String actionId);

    @Query("SELECT * FROM iqe.answer_options WHERE action_id = :actionId AND question_id = :questionId")
    Flux<AnswerOptionsEntity> findByActionIdAndQuestionId(@Param("actionId") String actionId, @Param("questionId") String questionId);
    
    @Query("SELECT * FROM iqe.answer_options WHERE action_id = :actionId AND question_id = :questionId AND is_active = true ALLOW FILTERING")
    Flux<AnswerOptionsEntity> findActiveByActionIdAndQuestionId(@Param("actionId") String actionId, @Param("questionId") String questionId);
}
