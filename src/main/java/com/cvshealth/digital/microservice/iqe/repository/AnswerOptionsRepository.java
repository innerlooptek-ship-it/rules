package com.cvshealth.digital.microservice.iqe.repository;

import com.cvshealth.digital.microservice.iqe.entity.AnswerOptionsEntity;
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

    Mono<Void> deleteByActionId(@Param("actionId") String actionId);

    @Query("SELECT * FROM iqe.answer_options WHERE action_id = :actionId AND question_id = :questionId")
    Flux<AnswerOptionsEntity> findByActionIdAndQuestionId(@Param("actionId") String actionId, @Param("questionId") String questionId);
}