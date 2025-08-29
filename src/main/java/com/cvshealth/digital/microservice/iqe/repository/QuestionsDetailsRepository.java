package com.cvshealth.digital.microservice.iqe.repository;

import com.cvshealth.digital.microservice.iqe.entity.QuestionsDetailsEntity;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface QuestionsDetailsRepository extends ReactiveCassandraRepository<QuestionsDetailsEntity, String> {

    @Query("SELECT * FROM questions_details WHERE action_id = :actionId")
    Flux<QuestionsDetailsEntity> findByActionId(@Param("actionId") String actionId);

    Mono<Void> deleteByActionId(@Param("actionId") String actionId);
}
