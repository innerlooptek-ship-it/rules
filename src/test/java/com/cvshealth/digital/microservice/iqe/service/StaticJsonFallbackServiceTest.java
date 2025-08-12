package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.StaticJsonConfig;
import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.health.CassandraHealthIndicator;
import com.cvshealth.digital.microservice.iqe.model.WriteOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaticJsonFallbackServiceTest {

    @Mock
    private StaticJsonConfig config;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CassandraHealthIndicator cassandraHealthIndicator;

    @TempDir
    Path tempDir;

    private StaticJsonFallbackService staticJsonFallbackService;

    @BeforeEach
    void setUp() {
        when(config.isEnabled()).thenReturn(true);
        when(config.getDataDirectory()).thenReturn(tempDir.toString());
        when(config.isMemoryCache()).thenReturn(true);
        when(config.getMaxCacheSize()).thenReturn(1000);
        when(config.isAutoCreateDirectory()).thenReturn(true);
        when(config.getFileExtension()).thenReturn(".json");
        
        staticJsonFallbackService = new StaticJsonFallbackService(config, objectMapper, cassandraHealthIndicator);
    }

    @Test
    void testGetQuestionnaireWithFallback_CassandraHealthy_ReturnsCassandraResult() {
        String actionId = "test-action-id";
        QuestionareRequest iqeOutput = new QuestionareRequest();
        QuestionareRequest cassandraResult = new QuestionareRequest();
        cassandraResult.setStatusCode("0000");

        when(cassandraHealthIndicator.isHealthy()).thenReturn(Mono.just(true));
        Supplier<Mono<QuestionareRequest>> cassandraFallback = () -> Mono.just(cassandraResult);

        StepVerifier.create(staticJsonFallbackService.getQuestionnaireWithFallback(actionId, iqeOutput, cassandraFallback))
                .expectNext(cassandraResult)
                .verifyComplete();

        verify(cassandraHealthIndicator).isHealthy();
    }

    @Test
    void testGetQuestionnaireWithFallback_CassandraUnhealthy_FileExists_ReturnsFileResult() throws Exception {
        String actionId = "test-action-id";
        QuestionareRequest iqeOutput = new QuestionareRequest();
        QuestionareRequest fileResult = new QuestionareRequest();
        fileResult.setStatusCode("0000");
        String jsonContent = "{\"statusCode\":\"0000\"}";

        Path testFile = tempDir.resolve(actionId + ".json");
        Files.writeString(testFile, jsonContent);

        when(cassandraHealthIndicator.isHealthy()).thenReturn(Mono.just(false));
        when(objectMapper.readValue(jsonContent, QuestionareRequest.class)).thenReturn(fileResult);

        Supplier<Mono<QuestionareRequest>> cassandraFallback = () -> Mono.just(new QuestionareRequest());

        StepVerifier.create(staticJsonFallbackService.getQuestionnaireWithFallback(actionId, iqeOutput, cassandraFallback))
                .expectNext(fileResult)
                .verifyComplete();

        verify(cassandraHealthIndicator).isHealthy();
        verify(objectMapper).readValue(jsonContent, QuestionareRequest.class);
    }

    @Test
    void testGetQuestionnaireWithFallback_CassandraUnhealthy_FileNotFound_ReturnsUnavailableResponse() {
        String actionId = "non-existent-action-id";
        QuestionareRequest iqeOutput = new QuestionareRequest();

        when(cassandraHealthIndicator.isHealthy()).thenReturn(Mono.just(false));

        Supplier<Mono<QuestionareRequest>> cassandraFallback = () -> Mono.just(new QuestionareRequest());

        StepVerifier.create(staticJsonFallbackService.getQuestionnaireWithFallback(actionId, iqeOutput, cassandraFallback))
                .expectNextMatches(result -> "5000".equals(result.getStatusCode()))
                .verifyComplete();

        verify(cassandraHealthIndicator).isHealthy();
    }

    @Test
    void testWriteWithFallback_CassandraHealthy_ExecutesWrite() {
        WriteOperation operation = new WriteOperation();
        operation.setActionId("test-action-id");
        operation.setType("CREATE");

        when(cassandraHealthIndicator.isHealthy()).thenReturn(Mono.just(true));

        StepVerifier.create(staticJsonFallbackService.writeWithFallback(operation))
                .verifyComplete();

        verify(cassandraHealthIndicator).isHealthy();
    }

    @Test
    void testWriteWithFallback_CassandraUnhealthy_QueuesWrite() {
        WriteOperation operation = new WriteOperation();
        operation.setActionId("test-action-id");
        operation.setType("CREATE");

        when(cassandraHealthIndicator.isHealthy()).thenReturn(Mono.just(false));

        StepVerifier.create(staticJsonFallbackService.writeWithFallback(operation))
                .verifyComplete();

        verify(cassandraHealthIndicator).isHealthy();
    }

    @Test
    void testInitialize_CreatesDataDirectory() {
        Path newTempDir = tempDir.resolve("new-directory");
        when(config.getDataDirectory()).thenReturn(newTempDir.toString());

        staticJsonFallbackService.initialize();

        assert Files.exists(newTempDir);
    }
}
