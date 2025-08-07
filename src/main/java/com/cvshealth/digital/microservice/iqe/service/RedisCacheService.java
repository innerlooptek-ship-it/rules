package com.cvshealth.digital.microservice.iqe.service;


import com.cvshealth.digital.microservice.iqe.config.RedisConfig;
import com.cvshealth.digital.microservice.iqe.http.DhsHttpConnector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.ENTRY_LOG;
import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.EXIT_LOG;


@Service
@RequiredArgsConstructor
@Slf4j
public final class RedisCacheService {

    private final RedisConfig.RedisConfigProperties redisConfigProperties;

    private final DhsHttpConnector httpConnector;
    private Disposable disposable;

    /**
     * The mapper obj.
     */
    private final ObjectMapper mapperObj;

    private Boolean redisFlag;

    @Value("${service.redis-cache.redisFlag}")
    public void setRetrievalPeriod(Boolean redisFlag) {
        this.redisFlag = redisFlag;
    }

    /**
     * get data from redis
     *
     * @param type
     * @param key
     * @param eventMap
     * @return
     * @throws Exception
     */
    public Mono<JsonNode> getDataFromRedis(String type, String key, Map<String, String> eventMap) {

        return Boolean.TRUE.equals(redisFlag) ? Mono.deferContextual(ctx -> {
                    String methodName = "getDataFromRedis";
                    log.debug("ENTRY_LOG {}", methodName);
                    String redisURL = redisConfigProperties.getBaseUrl() + "?key=" + key + "&cachetype=" + redisConfigProperties.getCacheType();
                    return httpConnector.invokeGETService(redisURL, eventMap)
                            .flatMap(redisResponse -> {
                                if (redisResponse != null) {
                                    return Mono.fromCallable(() -> {
                                        ObjectMapper mapper = new ObjectMapper();
                                        JsonNode jsonNode = mapper.readTree(redisResponse);
                                        return jsonNode.get("cacheObject");
                                    });
                                } else {
                                    return Mono.empty();
                                }
                            })
                            .doOnSuccess(cacheObjectNode -> log.debug("Successfully fetched data from Redis: {}", cacheObjectNode))
                            .doOnError(e -> log.error("Exception in getDataFromRedis {}", e.getMessage()))
                            .doFinally(signal -> log.debug("EXIT_LOG {}", methodName));
                })
                .onErrorResume(e -> Mono.empty()) : Mono.empty();

    }

    /**
     * Sets data to redis rest.
     *
     * @param key         the key
     * @param cacheObject the cacheObject
     * @param pEventMap   the p event map
     * @throws Exception the exception
     */
    public Mono<Void> setDataToRedisRest(String key, Object cacheObject, Map<String, String> pEventMap) {
        String methodName = "setDataToRedisRest";
        log.debug(ENTRY_LOG, methodName);
        if (Boolean.TRUE.equals(redisFlag)) {
            final String redisURL = redisConfigProperties.getBaseUrl();
            final String cacheType = redisConfigProperties.getCacheType();
            final RedisCacheObject redisCacheObject = new RedisCacheObject(cacheType, key, cacheObject);

            String redisSetDataRequest = null;
            try {
                redisSetDataRequest = mapperObj.writeValueAsString(redisCacheObject);
            } catch (JsonProcessingException e) {
                return Mono.empty();
            }
            disposable = httpConnector.invokePOSTService(redisSetDataRequest, redisURL, pEventMap)
                    .doOnSuccess(response -> log.info("Successfully set data to Redis"))
                    .doOnError(e -> log.error("Exception in setDataToRedisRest {}", e.getMessage()))
                    .doFinally(signal -> log.debug(EXIT_LOG, methodName))
                    .subscribe();
        }
        return Mono.empty();
    }


    @PreDestroy
    public void dispose() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    /**
     * Deletes data from redis.
     *
     * @param type     the type
     * @param key      the key
     * @param eventMap the event map
     * @return the mono
     */
    public Mono<Void> deleteDataFromRedis(String type, String key, Map<String, String> eventMap) {

        return Boolean.TRUE.equals(redisFlag) ? Mono.deferContextual(ctx -> {
                    String methodName = "deleteDataFromRedisRest";
                    log.debug(ENTRY_LOG, methodName);
                    String redisURL = redisConfigProperties.getBaseUrl() + "?key=" + key + "&cachetype=" + type;
                    return Mono.fromCallable(() -> {
                                log.debug("redisURL={}", redisURL);
                                return httpConnector.invokeDELETEService(redisURL, eventMap);
                            })
                            .flatMap(httpResponse -> httpResponse.map(response -> {
                                if (HttpStatus.valueOf(response.getStatusCode()).is5xxServerError()) {
                                    return Mono.error(new Exception("setDataToRedisRest HTTP statusCode=" + response.getStatusCode().toString() +
                                            "exceptionMsg=" + response.getMessage()));
                                }
                                return Mono.empty();
                            }))
                            .onErrorResume(Exception.class, e -> {
                                log.error("Exception in setDataToRedisRest {}", e.getMessage());
                                return Mono.error(e);
                            })
                            .doFinally(signal -> log.debug(EXIT_LOG, methodName))
                            .then();
                })
                .onErrorResume(e -> Mono.empty()) : Mono.empty();
    }

    @Data
    @AllArgsConstructor
    static class RedisCacheObject {

        private String cachetype;
        private String key;
        private Object cacheobject;
    }


}