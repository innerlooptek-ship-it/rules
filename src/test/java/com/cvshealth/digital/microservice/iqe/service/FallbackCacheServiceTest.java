package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.FallbackConfig;
import com.cvshealth.digital.microservice.iqe.entity.*;
import com.cvshealth.digital.microservice.iqe.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FallbackCacheServiceTest {

    @Mock
    private FallbackConfig fallbackConfig;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private RulesByFlowRepository rulesByFlowRepo;

    @Mock
    private ActionsRepository actionsRepo;

    @Mock
    private QuestionsRepository questionsRepo;

    @Mock
    private AnswerOptionsRepository answerOptionsRepo;

    @Mock
    private QuestionnaireDetailsRepository questionnaireDetailsRepo;

    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private FallbackCacheService fallbackCacheService;

    private RulesByFlowEntity testRule;
    private ActionsEntity testAction;
    private QuestionsEntity testQuestion;
    private AnswerOptionsEntity testAnswerOption;
    private QuestionsDetailsEntity testQuestionDetail;

    @BeforeEach
    void setUp() {
        lenient().when(fallbackConfig.isEnabled()).thenReturn(true);
        lenient().when(fallbackConfig.getCacheKeyPrefix()).thenReturn("fallback:");
        lenient().when(fallbackConfig.getCacheTtlHours()).thenReturn(24);

        testRule = new RulesByFlowEntity();
        testRule.setFlow("test-flow");
        testRule.setRuleId("rule-1");
        testRule.setActionId("action-1");

        testAction = new ActionsEntity();
        testAction.setActionId("action-1");
        testAction.setActionText("Test Action");

        testQuestion = new QuestionsEntity();
        testQuestion.setQuestionId("question-1");
        testQuestion.setActionId("action-1");
        testQuestion.setQuestionText("Test Question");

        testAnswerOption = new AnswerOptionsEntity();
        testAnswerOption.setAnswerOptionId("option-1");
        testAnswerOption.setQuestionId("question-1");
        testAnswerOption.setAnswerText("Test Option");

        testQuestionDetail = new QuestionsDetailsEntity();
        testQuestionDetail.setDetailId("question-1");
        testQuestionDetail.setActionId("action-1");
    }

    @Test
    void testGetRulesByFlow_CacheHit() throws Exception {
        String flow = "test-flow";
        String cacheKey = "fallback:rules_by_flow:" + flow;
        String cachedData = "[{\"flow\":\"test-flow\",\"ruleId\":\"rule-1\",\"actionId\":\"action-1\"}]";
        
        when(redisCacheService.getDataFromRedis("fallback", cacheKey, Map.of()))
            .thenReturn(Mono.just(new com.fasterxml.jackson.databind.node.TextNode(cachedData)));

        StepVerifier.create(fallbackCacheService.getRulesByFlow(flow))
            .expectNext(List.of(testRule))
            .verifyComplete();

        verify(redisCacheService).getDataFromRedis("fallback", cacheKey, Map.of());
    }

    @Test
    void testGetRulesByFlow_CacheMiss() {
        String flow = "test-flow";
        String cacheKey = "fallback:rules_by_flow:" + flow;
        
        when(redisCacheService.getDataFromRedis("fallback", cacheKey, Map.of()))
            .thenReturn(Mono.empty());

        StepVerifier.create(fallbackCacheService.getRulesByFlow(flow))
            .verifyComplete();

        verify(redisCacheService).getDataFromRedis("fallback", cacheKey, Map.of());
    }

    @Test
    void testGetRulesByFlow_FallbackDisabled() {
        when(fallbackConfig.isEnabled()).thenReturn(false);

        StepVerifier.create(fallbackCacheService.getRulesByFlow("test-flow"))
            .verifyComplete();

        verifyNoInteractions(redisCacheService);
    }

    @Test
    void testGetActionsByActionId_CacheHit() throws Exception {
        String actionId = "action-1";
        String cacheKey = "fallback:actions:" + actionId;
        String cachedData = "[{\"actionId\":\"action-1\",\"actionText\":\"Test Action\"}]";
        
        when(redisCacheService.getDataFromRedis("fallback", cacheKey, Map.of()))
            .thenReturn(Mono.just(new com.fasterxml.jackson.databind.node.TextNode(cachedData)));

        StepVerifier.create(fallbackCacheService.getActionsByActionId(actionId))
            .expectNext(List.of(testAction))
            .verifyComplete();

        verify(redisCacheService).getDataFromRedis("fallback", cacheKey, Map.of());
    }

    @Test
    void testWarmCache_Success() {
        when(rulesByFlowRepo.findAll()).thenReturn(Flux.just(testRule));
        when(actionsRepo.findAll()).thenReturn(Flux.just(testAction));
        when(questionsRepo.findAll()).thenReturn(Flux.just(testQuestion));
        when(answerOptionsRepo.findAll()).thenReturn(Flux.just(testAnswerOption));
        when(questionnaireDetailsRepo.findAll()).thenReturn(Flux.just(testQuestionDetail));

        when(redisCacheService.setDataToRedisRest(anyString(), anyString(), any(Map.class)))
            .thenReturn(Mono.empty());

        StepVerifier.create(fallbackCacheService.warmCache())
            .verifyComplete();

        verify(rulesByFlowRepo).findAll();
        verify(actionsRepo).findAll();
        verify(questionsRepo).findAll();
        verify(answerOptionsRepo).findAll();
        verify(questionnaireDetailsRepo).findAll();
    }

    @Test
    void testWarmCache_CassandraFailure() {
        when(rulesByFlowRepo.findAll()).thenReturn(Flux.error(new RuntimeException("Cassandra down")));
        when(actionsRepo.findAll()).thenReturn(Flux.error(new RuntimeException("Cassandra down")));
        when(questionsRepo.findAll()).thenReturn(Flux.error(new RuntimeException("Cassandra down")));
        when(answerOptionsRepo.findAll()).thenReturn(Flux.error(new RuntimeException("Cassandra down")));
        when(questionnaireDetailsRepo.findAll()).thenReturn(Flux.error(new RuntimeException("Cassandra down")));

        StepVerifier.create(fallbackCacheService.warmCache())
            .verifyComplete();

        assertFalse(fallbackCacheService.isCassandraHealthy());
    }

    @Test
    void testCassandraHealthTracking() {
        assertTrue(fallbackCacheService.isCassandraHealthy());

        fallbackCacheService.markCassandraUnhealthy();
        assertFalse(fallbackCacheService.isCassandraHealthy());

        fallbackCacheService.markCassandraHealthy();
        assertTrue(fallbackCacheService.isCassandraHealthy());
    }

    @Test
    void testGetQuestionsByActionId_CacheHit() throws Exception {
        String actionId = "action-1";
        String cacheKey = "fallback:questions:" + actionId;
        String cachedData = "[{\"questionId\":\"question-1\",\"actionId\":\"action-1\",\"questionText\":\"Test Question\"}]";
        
        when(redisCacheService.getDataFromRedis("fallback", cacheKey, Map.of()))
            .thenReturn(Mono.just(new com.fasterxml.jackson.databind.node.TextNode(cachedData)));

        StepVerifier.create(fallbackCacheService.getQuestionsByActionId(actionId))
            .expectNext(List.of(testQuestion))
            .verifyComplete();

        verify(redisCacheService).getDataFromRedis("fallback", cacheKey, Map.of());
    }

    @Test
    void testGetAnswerOptionsByActionId_CacheHit() throws Exception {
        String actionId = "action-1";
        String cacheKey = "fallback:answer_options:" + actionId;
        String cachedData = "[{\"answerOptionId\":\"option-1\",\"questionId\":\"question-1\",\"answerText\":\"Test Option\"}]";
        
        when(redisCacheService.getDataFromRedis("fallback", cacheKey, Map.of()))
            .thenReturn(Mono.just(new com.fasterxml.jackson.databind.node.TextNode(cachedData)));

        StepVerifier.create(fallbackCacheService.getAnswerOptionsByActionId(actionId))
            .expectNext(List.of(testAnswerOption))
            .verifyComplete();

        verify(redisCacheService).getDataFromRedis("fallback", cacheKey, Map.of());
    }

    @Test
    void testGetQuestionDetailsByActionId_CacheHit() throws Exception {
        String actionId = "action-1";
        String cacheKey = "fallback:questions_details:" + actionId;
        String cachedData = "[{\"detailId\":\"question-1\",\"actionId\":\"action-1\"}]";
        
        when(redisCacheService.getDataFromRedis("fallback", cacheKey, Map.of()))
            .thenReturn(Mono.just(new com.fasterxml.jackson.databind.node.TextNode(cachedData)));

        StepVerifier.create(fallbackCacheService.getQuestionDetailsByActionId(actionId))
            .expectNext(List.of(testQuestionDetail))
            .verifyComplete();

        verify(redisCacheService).getDataFromRedis("fallback", cacheKey, Map.of());
    }

    @Test
    void testCacheOperations_JsonProcessingException() throws Exception {
        when(rulesByFlowRepo.findAll()).thenReturn(Flux.just(testRule));
        when(actionsRepo.findAll()).thenReturn(Flux.empty());
        when(questionsRepo.findAll()).thenReturn(Flux.empty());
        when(answerOptionsRepo.findAll()).thenReturn(Flux.empty());
        when(questionnaireDetailsRepo.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(fallbackCacheService.warmCache())
            .verifyComplete();

    }
}
