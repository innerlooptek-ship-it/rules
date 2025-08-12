package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.GcsConfig;
import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.health.CassandraHealthIndicator;
import com.cvshealth.digital.microservice.iqe.model.WriteOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GcsFallbackServiceTest {

    @Mock
    private Storage storage;

    @Mock
    private GcsConfig gcsConfig;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CassandraHealthIndicator cassandraHealthIndicator;

    @Mock
    private Blob blob;

    private GcsFallbackService gcsFallbackService;

    @BeforeEach
    void setUp() {
        gcsFallbackService = new GcsFallbackService(storage, gcsConfig, objectMapper, cassandraHealthIndicator);
    }

    @Test
    void testGetQuestionnaireWithFallback_CassandraHealthy_ReturnsCassandraResult() {
        String actionId = "test-action-id";
        QuestionareRequest iqeOutput = new QuestionareRequest();
        QuestionareRequest cassandraResult = new QuestionareRequest();
        cassandraResult.setStatusCode("0000");

        when(cassandraHealthIndicator.isHealthy()).thenReturn(Mono.just(true));
        Supplier<Mono<QuestionareRequest>> cassandraFallback = () -> Mono.just(cassandraResult);

        StepVerifier.create(gcsFallbackService.getQuestionnaireWithFallback(actionId, iqeOutput, cassandraFallback))
                .expectNext(cassandraResult)
                .verifyComplete();

        verify(cassandraHealthIndicator).isHealthy();
    }

    @Test
    void testGetQuestionnaireWithFallback_CassandraUnhealthy_ReturnsGcsResult() throws Exception {
        String actionId = "test-action-id";
        QuestionareRequest iqeOutput = new QuestionareRequest();
        QuestionareRequest gcsResult = new QuestionareRequest();
        gcsResult.setStatusCode("0000");
        String jsonContent = "{\"statusCode\":\"0000\"}";

        when(cassandraHealthIndicator.isHealthy()).thenReturn(Mono.just(false));
        when(gcsConfig.getKeyPrefix()).thenReturn("iqe-fallback/");
        when(gcsConfig.getBucketName()).thenReturn("test-bucket");
        when(storage.get("test-bucket", "iqe-fallback/test-action-id.json")).thenReturn(blob);
        when(blob.exists()).thenReturn(true);
        when(blob.getContent()).thenReturn(jsonContent.getBytes());
        when(objectMapper.readValue(jsonContent, QuestionareRequest.class)).thenReturn(gcsResult);

        Supplier<Mono<QuestionareRequest>> cassandraFallback = () -> Mono.just(new QuestionareRequest());

        StepVerifier.create(gcsFallbackService.getQuestionnaireWithFallback(actionId, iqeOutput, cassandraFallback))
                .expectNext(gcsResult)
                .verifyComplete();

        verify(cassandraHealthIndicator).isHealthy();
        verify(storage).get("test-bucket", "iqe-fallback/test-action-id.json");
    }

    @Test
    void testGetQuestionnaireWithFallback_CassandraUnhealthy_GcsNotFound_ReturnsUnavailableResponse() {
        String actionId = "test-action-id";
        QuestionareRequest iqeOutput = new QuestionareRequest();

        when(cassandraHealthIndicator.isHealthy()).thenReturn(Mono.just(false));
        when(gcsConfig.getKeyPrefix()).thenReturn("iqe-fallback/");
        when(gcsConfig.getBucketName()).thenReturn("test-bucket");
        when(storage.get("test-bucket", "iqe-fallback/test-action-id.json")).thenReturn(null);

        Supplier<Mono<QuestionareRequest>> cassandraFallback = () -> Mono.just(new QuestionareRequest());

        StepVerifier.create(gcsFallbackService.getQuestionnaireWithFallback(actionId, iqeOutput, cassandraFallback))
                .expectNextMatches(result -> "5000".equals(result.getStatusCode()))
                .verifyComplete();

        verify(cassandraHealthIndicator).isHealthy();
        verify(storage).get("test-bucket", "iqe-fallback/test-action-id.json");
    }

    @Test
    void testWriteWithFallback_CassandraHealthy_ExecutesWrite() {
        WriteOperation operation = new WriteOperation();
        operation.setActionId("test-action-id");
        operation.setType("CREATE");

        when(cassandraHealthIndicator.isHealthy()).thenReturn(Mono.just(true));

        StepVerifier.create(gcsFallbackService.writeWithFallback(operation))
                .verifyComplete();

        verify(cassandraHealthIndicator).isHealthy();
    }

    @Test
    void testWriteWithFallback_CassandraUnhealthy_QueuesWrite() {
        WriteOperation operation = new WriteOperation();
        operation.setActionId("test-action-id");
        operation.setType("CREATE");

        when(cassandraHealthIndicator.isHealthy()).thenReturn(Mono.just(false));

        StepVerifier.create(gcsFallbackService.writeWithFallback(operation))
                .verifyComplete();

        verify(cassandraHealthIndicator).isHealthy();
    }
}
