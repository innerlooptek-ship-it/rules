package com.cvshealth.digital.microservice.iqe.repository;

import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * The Class RulesByFlowRepository
 *
 */

public interface RulesByFlowRepository  extends ReactiveCassandraRepository<RulesByFlowEntity, String> {

    @Query("SELECT * FROM iqe.rules_by_flow WHERE flow = :flow AND condition = :condition AND is_active in (true,false) ALLOW FILTERING")
    Mono<RulesByFlowEntity> findByFlowAndCondition(@Param("flow") String flow, @Param("condition") String condition);

    @Query("SELECT * FROM iqe.rules_by_flow WHERE is_active in (true,false) ALLOW FILTERING")
    Flux<RulesByFlowEntity> findAll();

    @Query("SELECT * FROM iqe.rules_by_flow WHERE action_id = :actionId  ALLOW FILTERING")
    Flux<RulesByFlowEntity> findByActionId(@Param("actionId") String actionId);

    @Query("DELETE FROM iqe.rules_by_flow WHERE flow= :flow AND rule_id = :ruleId")
    Mono<Void> deleteByFlowAndRuleId(@Param("flow") String flow, @Param("ruleId") String ruleId);

    @Query("SELECT * FROM iqe.rules_by_flow WHERE flow= :flow")
    Flux<RulesByFlowEntity> findByFlow(@Param("flow") String flow);
}