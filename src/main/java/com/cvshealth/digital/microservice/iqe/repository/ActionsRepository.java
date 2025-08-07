package com.cvshealth.digital.microservice.iqe.repository;

import com.cvshealth.digital.microservice.iqe.entity.ActionsEntity;
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

    Mono<Void> deleteByActionId(@Param("actionId") String actionId);
}