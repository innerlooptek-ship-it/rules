package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.repository.RulesByFlowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IQEServiceFallbackTest {

    @Mock
    private RulesByFlowRepository rulesByFlowRepo;

    @Mock
    private FallbackCacheService fallbackCacheService;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private IQEService iqeService;

    private RulesDetails testRulesDetails;
    private QuestionareRequest testQuestionareRequest;
    private RulesByFlowEntity testRule;

    @BeforeEach
    void setUp() {
        testRulesDetails = new RulesDetails();
        testRulesDetails.setFlow("test-flow");
        testRulesDetails.setRequiredQuestionnaireContext("testContext");

        testQuestionareRequest = new QuestionareRequest();

        testRule = new RulesByFlowEntity();
        testRule.setFlow("test-flow");
        testRule.setRuleId("rule-1");
        testRule.setActionId("action-1");
        testRule.setCondition("requiredQuestionnaireContext==\"testContext\"");
        testRule.setSalience(100);
    }

    @Test
    void testQuestionnaireByFlowAndCondition_CassandraSuccess() {
        when(rulesByFlowRepo.findByFlow("test-flow"))
            .thenReturn(Flux.just(testRule));
        doNothing().when(fallbackCacheService).markCassandraHealthy();

        StepVerifier.create(iqeService.questionnaireByFlowAndCondition(testRulesDetails, testQuestionareRequest, Map.of()))
            .expectNextMatches(result -> result != null)
            .verifyComplete();

        verify(rulesByFlowRepo).findByFlow("test-flow");
        verify(fallbackCacheService).markCassandraHealthy();
        verifyNoMoreInteractions(fallbackCacheService);
    }

    @Test
    void testQuestionnaireByFlowAndCondition_CassandraFailure_FallbackSuccess() {
        when(rulesByFlowRepo.findByFlow("test-flow"))
            .thenReturn(Flux.error(new RuntimeException("Cassandra connection failed")));
        doNothing().when(fallbackCacheService).markCassandraUnhealthy();
        when(fallbackCacheService.getRulesByFlow("test-flow"))
            .thenReturn(Mono.just(List.of(testRule)));

        StepVerifier.create(iqeService.questionnaireByFlowAndCondition(testRulesDetails, testQuestionareRequest, Map.of()))
            .expectNextMatches(result -> result != null)
            .verifyComplete();

        verify(rulesByFlowRepo).findByFlow("test-flow");
        verify(fallbackCacheService).markCassandraUnhealthy();
        verify(fallbackCacheService).getRulesByFlow("test-flow");
    }

    @Test
    void testQuestionnaireByFlowAndCondition_BothCassandraAndFallbackFail() {
        when(rulesByFlowRepo.findByFlow("test-flow"))
            .thenReturn(Flux.error(new RuntimeException("Cassandra connection failed")));
        doNothing().when(fallbackCacheService).markCassandraUnhealthy();
        when(fallbackCacheService.getRulesByFlow("test-flow"))
            .thenReturn(Mono.error(new RuntimeException("Cache also failed")));

        StepVerifier.create(iqeService.questionnaireByFlowAndCondition(testRulesDetails, testQuestionareRequest, Map.of()))
            .expectErrorMatches(throwable -> 
                throwable instanceof RuntimeException && 
                throwable.getMessage().contains("Service temporarily unavailable"))
            .verify();

        verify(rulesByFlowRepo).findByFlow("test-flow");
        verify(fallbackCacheService).markCassandraUnhealthy();
        verify(fallbackCacheService).getRulesByFlow("test-flow");
    }

    @Test
    void testQuestionnaireByFlowAndCondition_EmptyRulesFromCassandra() {
        when(rulesByFlowRepo.findByFlow("test-flow"))
            .thenReturn(Flux.empty());
        doNothing().when(fallbackCacheService).markCassandraHealthy();

        StepVerifier.create(iqeService.questionnaireByFlowAndCondition(testRulesDetails, testQuestionareRequest, Map.of()))
            .expectNext(testQuestionareRequest)
            .verifyComplete();

        verify(rulesByFlowRepo).findByFlow("test-flow");
        verify(fallbackCacheService).markCassandraHealthy();
    }

    @Test
    void testQuestionnaireByFlowAndCondition_EmptyRulesFromFallback() {
        when(rulesByFlowRepo.findByFlow("test-flow"))
            .thenReturn(Flux.error(new RuntimeException("Cassandra down")));
        doNothing().when(fallbackCacheService).markCassandraUnhealthy();
        when(fallbackCacheService.getRulesByFlow("test-flow"))
            .thenReturn(Mono.just(List.of()));

        StepVerifier.create(iqeService.questionnaireByFlowAndCondition(testRulesDetails, testQuestionareRequest, Map.of()))
            .expectNext(testQuestionareRequest)
            .verifyComplete();

        verify(fallbackCacheService).getRulesByFlow("test-flow");
    }

    @Test
    void testQuestionnaireByFlowAndCondition_RuleExecutionWithActionId() {
        testRule.setCondition("requiredQuestionnaireContext==\"testContext\"");
        
        when(rulesByFlowRepo.findByFlow("test-flow"))
            .thenReturn(Flux.just(testRule));
        doNothing().when(fallbackCacheService).markCassandraHealthy();

        StepVerifier.create(iqeService.questionnaireByFlowAndCondition(testRulesDetails, testQuestionareRequest, Map.of()))
            .expectNextMatches(result -> {
                return testRulesDetails.getActionId() != null;
            })
            .verifyComplete();

        verify(rulesByFlowRepo).findByFlow("test-flow");
    }
}
