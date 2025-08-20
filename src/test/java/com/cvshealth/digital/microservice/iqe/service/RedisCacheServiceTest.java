package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.RedisConfig;
import com.cvshealth.digital.microservice.iqe.http.DhsHttpConnector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.*;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RedisCacheServiceTest {

    @Mock
    private RedisConfig.RedisConfigProperties redisConfigProperties;
    @Mock
    private DhsHttpConnector httpConnector;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RedisCacheService redisCacheService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        redisCacheService.setRetrievalPeriod(true);
    }

    @Test
    void getDataFromRedis_returnsEmpty_whenRedisFlagFalse() {
        redisCacheService.setRetrievalPeriod(false);
        StepVerifier.create(redisCacheService.getDataFromRedis("type", "key", Map.of()))
                .expectComplete()
                .verify();
    }

    @Test
    void getDataFromRedis_returnsEmpty_onError() {
        when(redisConfigProperties.getBaseUrl()).thenReturn("http://redis");
        when(redisConfigProperties.getCacheType()).thenReturn("testType");
        when(httpConnector.invokeGETService(anyString(), anyMap())).thenReturn(Mono.error(new RuntimeException("fail")));

        StepVerifier.create(redisCacheService.getDataFromRedis("type", "key", Map.of()))
                .expectComplete()
                .verify();
    }



    @Test
    void setDataToRedisRest_returnsEmpty_onJsonProcessingException() throws Exception {
        redisCacheService.setRetrievalPeriod(true);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("fail") {});

        Mono<Void> result = redisCacheService.setDataToRedisRest("key", Map.of(), Map.of());
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void setDataToRedisRest_returnsEmpty_whenRedisFlagFalse() {
        redisCacheService.setRetrievalPeriod(false);
        Mono<Void> result = redisCacheService.setDataToRedisRest("key", Map.of(), Map.of());
        StepVerifier.create(result).verifyComplete();
    }


    @Test
    void deleteDataFromRedis_returnsEmpty_whenRedisFlagFalse() {
        redisCacheService.setRetrievalPeriod(false);
        StepVerifier.create(redisCacheService.deleteDataFromRedis("type", "key", Map.of()))
                .verifyComplete();
    }

    @Test
    void deleteDataFromRedis_returnsEmpty_onError() {
        redisCacheService.setRetrievalPeriod(true);
        when(redisConfigProperties.getBaseUrl()).thenReturn("http://redis");
        when(httpConnector.invokeDELETEService(anyString(), anyMap())).thenThrow(new RuntimeException("fail"));

        StepVerifier.create(redisCacheService.deleteDataFromRedis("type", "key", Map.of()))
                .verifyComplete();
    }

    @Test
    void dispose_disposesDisposable() {
        Disposable disposable = mock(Disposable.class);
        when(disposable.isDisposed()).thenReturn(false);
        // Use reflection to set the private field
        try {
            var field = RedisCacheService.class.getDeclaredField("disposable");
            field.setAccessible(true);
            field.set(redisCacheService, disposable);
        } catch (Exception e) {
            fail("Reflection failed");
        }
        redisCacheService.dispose();
        verify(disposable).dispose();
    }
}