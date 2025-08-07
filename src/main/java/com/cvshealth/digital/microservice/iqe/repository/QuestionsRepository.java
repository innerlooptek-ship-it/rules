package com.cvshealth.digital.microservice.iqe.repository;

import com.cvshealth.digital.microservice.iqe.entity.QuestionsEntity;
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

    Mono<Void> deleteByActionId(@Param("actionId") String actionId);

    @Query("SELECT * FROM questions WHERE action_id = :actionId AND question_id = :questionId")
    Mono<QuestionsEntity> findByActionIdAndQuestionId(@Param("actionId") String actionId, @Param("questionId") String questionId);


}