package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.FallbackConfig;
import com.cvshealth.digital.microservice.iqe.entity.ActionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.AnswerOptionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionsDetailsEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import com.cvshealth.digital.microservice.iqe.repository.ActionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.AnswerOptionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionnaireDetailsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.RulesByFlowRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class FallbackCacheService {

    @Autowired
    private FallbackConfig fallbackConfig;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private RulesByFlowRepository rulesByFlowRepository;

    @Autowired
    private ActionsRepository actionsRepository;

    @Autowired
    private QuestionsRepository questionsRepository;

    @Autowired
    private AnswerOptionsRepository answerOptionsRepository;

    @Autowired
    private QuestionnaireDetailsRepository questionnaireDetailsRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicBoolean cacheWarmingInProgress = new AtomicBoolean(false);
    private final AtomicBoolean cassandraHealthy = new AtomicBoolean(true);

    @EventListener(ApplicationReadyEvent.class)
    public void warmCacheOnStartup() {
        if (fallbackConfig.isEnabled() && fallbackConfig.isCacheWarmingEnabled()) {
            log.info("Starting fallback cache warming on application startup");
            warmCache()
                .doOnSuccess(v -> log.info("Fallback cache warming completed successfully"))
                .doOnError(e -> log.error("Fallback cache warming failed: {}", e.getMessage()))
                .subscribe();
        }
    }

    @Scheduled(fixedRateString = "#{${service.fallback-cache.cache-refresh-interval-minutes:60} * 60 * 1000}")
    public void scheduledCacheRefresh() {
        if (fallbackConfig.isEnabled() && cassandraHealthy.get()) {
            log.debug("Starting scheduled fallback cache refresh");
            warmCache()
                .doOnSuccess(v -> log.debug("Scheduled fallback cache refresh completed"))
                .doOnError(e -> log.warn("Scheduled fallback cache refresh failed: {}", e.getMessage()))
                .subscribe();
        }
    }

    public Mono<Void> warmCache() {
        if (!fallbackConfig.isEnabled()) {
            return Mono.empty();
        }

        if (!cacheWarmingInProgress.compareAndSet(false, true)) {
            log.debug("Cache warming already in progress, skipping");
            return Mono.empty();
        }

        return Mono.defer(() -> {
            log.info("Starting fallback cache warming process");

            return Mono.when(
                warmRulesByFlowCache(),
                warmActionsCache(),
                warmQuestionsCache(),
                warmAnswerOptionsCache(),
                warmQuestionsDetailsCache()
            )
            .doFinally(signal -> {
                cacheWarmingInProgress.set(false);
                log.info("Fallback cache warming process completed with signal: {}", signal);
            });
        })
        .timeout(Duration.ofMinutes(5))
        .onErrorResume(e -> {
            log.error("Cache warming failed: {}", e.getMessage(), e);
            cacheWarmingInProgress.set(false);
            cassandraHealthy.set(false);
            return Mono.empty();
        });
    }

    private Mono<Void> warmRulesByFlowCache() {
        return rulesByFlowRepository.findAll()
            .collectMultimap(RulesByFlowEntity::getFlow)
            .flatMap(flowRulesMap -> {
                return Flux.fromIterable(flowRulesMap.entrySet())
                    .flatMap(entry -> {
                        String flow = entry.getKey();
                        List<RulesByFlowEntity> rules = (List<RulesByFlowEntity>) entry.getValue();
                        return cacheRulesByFlow(flow, rules);
                    })
                    .then();
            })
            .doOnSuccess(v -> log.debug("Rules by flow cache warming completed"))
            .doOnError(e -> log.error("Rules by flow cache warming failed: {}", e.getMessage()));
    }

    private Mono<Void> warmActionsCache() {
        return actionsRepository.findAll()
            .flatMap(action -> cacheAction(action.getActionId(), action))
            .then()
            .doOnSuccess(v -> log.debug("Actions cache warming completed"))
            .doOnError(e -> log.error("Actions cache warming failed: {}", e.getMessage()));
    }

    private Mono<Void> warmQuestionsCache() {
        return questionsRepository.findAll()
            .collectMultimap(QuestionsEntity::getActionId)
            .flatMap(actionQuestionsMap -> {
                return Flux.fromIterable(actionQuestionsMap.entrySet())
                    .flatMap(entry -> {
                        String actionId = entry.getKey();
                        List<QuestionsEntity> questions = (List<QuestionsEntity>) entry.getValue();
                        return cacheQuestionsByActionId(actionId, questions);
                    })
                    .then();
            })
            .doOnSuccess(v -> log.debug("Questions cache warming completed"))
            .doOnError(e -> log.error("Questions cache warming failed: {}", e.getMessage()));
    }

    private Mono<Void> warmAnswerOptionsCache() {
        return answerOptionsRepository.findAll()
            .collectMultimap(AnswerOptionsEntity::getActionId)
            .flatMap(actionAnswersMap -> {
                return Flux.fromIterable(actionAnswersMap.entrySet())
                    .flatMap(entry -> {
                        String actionId = entry.getKey();
                        List<AnswerOptionsEntity> answers = (List<AnswerOptionsEntity>) entry.getValue();
                        return cacheAnswerOptionsByActionId(actionId, answers);
                    })
                    .then();
            })
            .doOnSuccess(v -> log.debug("Answer options cache warming completed"))
            .doOnError(e -> log.error("Answer options cache warming failed: {}", e.getMessage()));
    }

    private Mono<Void> warmQuestionsDetailsCache() {
        return questionnaireDetailsRepository.findAll()
            .collectMultimap(QuestionsDetailsEntity::getActionId)
            .flatMap(actionDetailsMap -> {
                return Flux.fromIterable(actionDetailsMap.entrySet())
                    .flatMap(entry -> {
                        String actionId = entry.getKey();
                        List<QuestionsDetailsEntity> details = (List<QuestionsDetailsEntity>) entry.getValue();
                        return cacheQuestionDetailsByActionId(actionId, details);
                    })
                    .then();
            })
            .doOnSuccess(v -> log.debug("Questions details cache warming completed"))
            .doOnError(e -> log.error("Questions details cache warming failed: {}", e.getMessage()));
    }

    public Mono<List<RulesByFlowEntity>> getRulesByFlow(String flow) {
        if (!fallbackConfig.isEnabled()) {
            return Mono.empty();
        }

        String cacheKey = fallbackConfig.getCacheKeyPrefix() + "rules_by_flow:" + flow;
        return redisCacheService.getDataFromRedis("fallback", cacheKey, Map.of())
            .map(jsonNode -> jsonNode.asText())
            .flatMap(cachedData -> {
                try {
                    List<RulesByFlowEntity> rules = objectMapper.readValue(cachedData,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, RulesByFlowEntity.class));
                    log.debug("Retrieved {} rules from fallback cache for flow: {}", rules.size(), flow);
                    return Mono.just(rules);
                } catch (JsonProcessingException e) {
                    log.error("Failed to deserialize cached rules for flow {}: {}", flow, e.getMessage());
                    return Mono.empty();
                }
            })
            .doOnError(e -> log.error("Failed to retrieve rules from fallback cache for flow {}: {}", flow, e.getMessage()));
    }

    public Mono<List<ActionsEntity>> getActionsByActionId(String actionId) {
        if (!fallbackConfig.isEnabled()) {
            return Mono.empty();
        }

        String cacheKey = fallbackConfig.getCacheKeyPrefix() + "actions:" + actionId;
        return redisCacheService.getDataFromRedis("fallback", cacheKey, Map.of())
            .map(jsonNode -> jsonNode.asText())
            .flatMap(cachedData -> {
                try {
                    List<ActionsEntity> actions = objectMapper.readValue(cachedData,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ActionsEntity.class));
                    log.debug("Retrieved {} actions from fallback cache for actionId: {}", actions.size(), actionId);
                    return Mono.just(actions);
                } catch (JsonProcessingException e) {
                    log.error("Failed to deserialize cached actions for actionId {}: {}", actionId, e.getMessage());
                    return Mono.empty();
                }
            })
            .doOnError(e -> log.error("Failed to retrieve actions from fallback cache for actionId {}: {}", actionId, e.getMessage()));
    }

    public Mono<List<QuestionsEntity>> getQuestionsByActionId(String actionId) {
        if (!fallbackConfig.isEnabled()) {
            return Mono.empty();
        }

        String cacheKey = fallbackConfig.getCacheKeyPrefix() + "questions:" + actionId;
        return redisCacheService.getDataFromRedis("fallback", cacheKey, Map.of())
            .map(jsonNode -> jsonNode.asText())
            .flatMap(cachedData -> {
                try {
                    List<QuestionsEntity> questions = objectMapper.readValue(cachedData,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionsEntity.class));
                    log.debug("Retrieved {} questions from fallback cache for actionId: {}", questions.size(), actionId);
                    return Mono.just(questions);
                } catch (JsonProcessingException e) {
                    log.error("Failed to deserialize cached questions for actionId {}: {}", actionId, e.getMessage());
                    return Mono.empty();
                }
            })
            .doOnError(e -> log.error("Failed to retrieve questions from fallback cache for actionId {}: {}", actionId, e.getMessage()));
    }

    public Mono<List<AnswerOptionsEntity>> getAnswerOptionsByActionId(String actionId) {
        if (!fallbackConfig.isEnabled()) {
            return Mono.empty();
        }

        String cacheKey = fallbackConfig.getCacheKeyPrefix() + "answer_options:" + actionId;
        return redisCacheService.getDataFromRedis("fallback", cacheKey, Map.of())
            .map(jsonNode -> jsonNode.asText())
            .flatMap(cachedData -> {
                try {
                    List<AnswerOptionsEntity> answerOptions = objectMapper.readValue(cachedData,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, AnswerOptionsEntity.class));
                    log.debug("Retrieved {} answer options from fallback cache for actionId: {}", answerOptions.size(), actionId);
                    return Mono.just(answerOptions);
                } catch (JsonProcessingException e) {
                    log.error("Failed to deserialize cached answer options for actionId {}: {}", actionId, e.getMessage());
                    return Mono.empty();
                }
            })
            .doOnError(e -> log.error("Failed to retrieve answer options from fallback cache for actionId {}: {}", actionId, e.getMessage()));
    }

    public Mono<List<QuestionsDetailsEntity>> getQuestionDetailsByActionId(String actionId) {
        if (!fallbackConfig.isEnabled()) {
            return Mono.empty();
        }

        String cacheKey = fallbackConfig.getCacheKeyPrefix() + "questions_details:" + actionId;
        return redisCacheService.getDataFromRedis("fallback", cacheKey, Map.of())
            .map(jsonNode -> jsonNode.asText())
            .flatMap(cachedData -> {
                try {
                    List<QuestionsDetailsEntity> questionDetails = objectMapper.readValue(cachedData,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionsDetailsEntity.class));
                    log.debug("Retrieved {} question details from fallback cache for actionId: {}", questionDetails.size(), actionId);
                    return Mono.just(questionDetails);
                } catch (JsonProcessingException e) {
                    log.error("Failed to deserialize cached question details for actionId {}: {}", actionId, e.getMessage());
                    return Mono.empty();
                }
            })
            .doOnError(e -> log.error("Failed to retrieve question details from fallback cache for actionId {}: {}", actionId, e.getMessage()));
    }

    private Mono<Void> cacheRulesByFlow(String flow, List<RulesByFlowEntity> rules) {
        try {
            String cacheKey = fallbackConfig.getCacheKeyPrefix() + "rules_by_flow:" + flow;
            String serializedData = objectMapper.writeValueAsString(rules);
            return redisCacheService.setDataToRedisRest(cacheKey, serializedData, Map.of())
                .doOnSuccess(v -> log.debug("Cached {} rules for flow: {}", rules.size(), flow))
                .then();
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize rules for flow {}: {}", flow, e.getMessage());
            return Mono.empty();
        }
    }

    private Mono<Void> cacheAction(String actionId, ActionsEntity action) {
        try {
            String cacheKey = fallbackConfig.getCacheKeyPrefix() + "actions:" + actionId;
            String serializedData = objectMapper.writeValueAsString(List.of(action));
            return redisCacheService.setDataToRedisRest(cacheKey, serializedData, Map.of())
                .doOnSuccess(v -> log.debug("Cached action for actionId: {}", actionId))
                .then();
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize action for actionId {}: {}", actionId, e.getMessage());
            return Mono.empty();
        }
    }

    private Mono<Void> cacheQuestionsByActionId(String actionId, List<QuestionsEntity> questions) {
        try {
            String cacheKey = fallbackConfig.getCacheKeyPrefix() + "questions:" + actionId;
            String serializedData = objectMapper.writeValueAsString(questions);
            return redisCacheService.setDataToRedisRest(cacheKey, serializedData, Map.of())
                .doOnSuccess(v -> log.debug("Cached {} questions for actionId: {}", questions.size(), actionId))
                .then();
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize questions for actionId {}: {}", actionId, e.getMessage());
            return Mono.empty();
        }
    }

    private Mono<Void> cacheAnswerOptionsByActionId(String actionId, List<AnswerOptionsEntity> answerOptions) {
        try {
            String cacheKey = fallbackConfig.getCacheKeyPrefix() + "answer_options:" + actionId;
            String serializedData = objectMapper.writeValueAsString(answerOptions);
            return redisCacheService.setDataToRedisRest(cacheKey, serializedData, Map.of())
                .doOnSuccess(v -> log.debug("Cached {} answer options for actionId: {}", answerOptions.size(), actionId))
                .then();
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize answer options for actionId {}: {}", actionId, e.getMessage());
            return Mono.empty();
        }
    }

    private Mono<Void> cacheQuestionDetailsByActionId(String actionId, List<QuestionsDetailsEntity> questionDetails) {
        try {
            String cacheKey = fallbackConfig.getCacheKeyPrefix() + "questions_details:" + actionId;
            String serializedData = objectMapper.writeValueAsString(questionDetails);
            return redisCacheService.setDataToRedisRest(cacheKey, serializedData, Map.of())
                .doOnSuccess(v -> log.debug("Cached {} question details for actionId: {}", questionDetails.size(), actionId))
                .then();
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize question details for actionId {}: {}", actionId, e.getMessage());
            return Mono.empty();
        }
    }

    public void markCassandraHealthy() {
        if (!cassandraHealthy.get()) {
            log.info("Cassandra is healthy again, enabling scheduled cache refresh");
            cassandraHealthy.set(true);
        }
    }

    public void markCassandraUnhealthy() {
        if (cassandraHealthy.get()) {
            log.warn("Cassandra is unhealthy, disabling scheduled cache refresh");
            cassandraHealthy.set(false);
        }
    }

    public boolean isCassandraHealthy() {
        return cassandraHealthy.get();
    }
}
