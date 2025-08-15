package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.health.CassandraHealthIndicator;
import com.cvshealth.digital.microservice.iqe.model.WriteOperation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheFallbackServiceTest {

    @Mock
    private RedisCacheService redisCacheService;
    
    @Mock
    private CassandraHealthIndicator cassandraHealthIndicator;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private JsonNode jsonNode;
    
    private RedisCacheFallbackService fallbackService;
    
    @BeforeEach
    void setUp() {
        fallbackService = new RedisCacheFallbackService(redisCacheService, cassandraHealthIndicator, objectMapper);
    }
    
    @Test
    void getQuestionnaireWithFallback_WhenCassandraHealthy_ShouldUseCassandra() {
        String actionId = "test-action-id";
        QuestionareRequest iqeOutput = new QuestionareRequest();
        QuestionareRequest cassandraResult = new QuestionareRequest();
        cassandraResult.setStatusCode("200");
        
        Supplier<Mono<QuestionareRequest>> cassandraFallback = () -> Mono.just(cassandraResult);
        
        when(cassandraHealthIndicator.isHealthy()).thenReturn(Mono.just(true));
        when(redisCacheService.setDataToRedisRest(eq(actionId), eq(cassandraResult), any()))
            .thenReturn(Mono.empty());
        
        StepVerifier.create(fallbackService.getQuestionnaireWithFallback(actionId, iqeOutput, cassandraFallback))
            .expectNext(cassandraResult)
            .verifyComplete();
        
        verify(cassandraHealthIndicator).isHealthy();
        verify(redisCacheService).setDataToRedisRest(eq(actionId), eq(cassandraResult), any());
    }
    
    @Test
    void getQuestionnaireWithFallback_WhenCassandraUnhealthy_ShouldUseRedisCache() {
        String actionId = "test-action-id";
        QuestionareRequest iqeOutput = new QuestionareRequest();
        QuestionareRequest cachedResult = new QuestionareRequest();
        cachedResult.setStatusCode("200");
        
        Supplier<Mono<QuestionareRequest>> cassandraFallback = () -> Mono.just(new QuestionareRequest());
        
        when(cassandraHealthIndicator.isHealthy()).thenReturn(Mono.just(false));
        when(redisCacheService.getDataFromRedis(anyString(), eq(actionId), any()))
            .thenReturn(Mono.just(jsonNode));
        when(objectMapper.convertValue(jsonNode, QuestionareRequest.class))
            .thenReturn(cachedResult);
        
        StepVerifier.create(fallbackService.getQuestionnaireWithFallback(actionId, iqeOutput, cassandraFallback))
            .expectNext(cachedResult)
            .verifyComplete();
        
        verify(cassandraHealthIndicator).isHealthy();
        verify(redisCacheService).getDataFromRedis(anyString(), eq(actionId), any());
        verify(objectMapper).convertValue(jsonNode, QuestionareRequest.class);
    }
    
    @Test
    void getQuestionnaireWithFallback_WhenBothUnavailable_ShouldReturnUnavailableResponse() {
        String actionId = "test-action-id";
        QuestionareRequest iqeOutput = new QuestionareRequest();
        
        Supplier<Mono<QuestionareRequest>> cassandraFallback = () -> Mono.just(new QuestionareRequest());
        
        when(cassandraHealthIndicator.isHealthy()).thenReturn(Mono.just(false));
        when(redisCacheService.getDataFromRedis(anyString(), eq(actionId), any()))
            .thenReturn(Mono.empty());
        
        StepVerifier.create(fallbackService.getQuestionnaireWithFallback(actionId, iqeOutput, cassandraFallback))
            .expectNextMatches(result -> "503".equals(result.getStatusCode()))
            .verifyComplete();
        
        verify(cassandraHealthIndicator).isHealthy();
        verify(redisCacheService).getDataFromRedis(anyString(), eq(actionId), any());
    }
    
    @Test
    void writeWithFallback_WhenCassandraHealthy_ShouldExecuteWrite() {
        WriteOperation operation = WriteOperation.create("test-action", "CREATE", new Object());
        
        when(cassandraHealthIndicator.isHealthy()).thenReturn(Mono.just(true));
        
        StepVerifier.create(fallbackService.writeWithFallback(operation))
            .verifyComplete();
        
        verify(cassandraHealthIndicator).isHealthy();
    }
    
    @Test
    void writeWithFallback_WhenCassandraUnhealthy_ShouldQueueWrite() {
        WriteOperation operation = WriteOperation.create("test-action", "CREATE", new Object());
        
        when(cassandraHealthIndicator.isHealthy()).thenReturn(Mono.just(false));
        
        StepVerifier.create(fallbackService.writeWithFallback(operation))
            .verifyComplete();
        
        verify(cassandraHealthIndicator).isHealthy();
    }
}
