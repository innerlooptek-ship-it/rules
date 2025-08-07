package com.cvshealth.digital.microservice.iqe.repository;


import com.cvshealth.digital.microservice.iqe.constants.DBConstants;
import com.cvshealth.digital.microservice.iqe.entity.QuestionnaireRules;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;


public interface QuestionnaireRulesRepository extends ReactiveCassandraRepository<QuestionnaireRules, String> {
    @Query("SELECT rule_id,rule_name,action,condition,flow,lob,salience  FROM " + DBConstants.QUESTIONNAIRE_RULES + " WHERE " + "flow"
            + " =  :flow ")
    Flux<QuestionnaireRules> findByFlow(@Param("flow") String flow);
}