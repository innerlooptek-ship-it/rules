package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.EnhancedRedisConfig;
import com.cvshealth.digital.microservice.iqe.entity.*;
import com.cvshealth.digital.microservice.iqe.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetSnapshotService {
    
    private final RulesByFlowRepository rulesByFlowRepo;
    private final ActionsRepository actionsRepo;
    private final QuestionsRepository questionsRepo;
    private final AnswerOptionsRepository answerOptionsRepo;
    private final QuestionnaireDetailsRepository detailsRepo;
    private final EnhancedRedisCacheService redisCacheService;
    private final EnhancedRedisConfig config;
    
    public Mono<Void> createCompleteDatasetSnapshot() {
        log.info("Starting complete dataset snapshot creation");
        long startTime = System.currentTimeMillis();
        
        return Mono.zip(
            rulesByFlowRepo.findAll().collectList(),
            actionsRepo.findAll().collectList(),
            questionsRepo.findAll().collectList(),
            answerOptionsRepo.findAll().collectList(),
            detailsRepo.findAll().collectList()
        ).flatMap(tuple -> {
            List<RulesByFlowEntity> rules = tuple.getT1();
            List<ActionsEntity> actions = tuple.getT2();
            List<QuestionsEntity> questions = tuple.getT3();
            List<AnswerOptionsEntity> answerOptions = tuple.getT4();
            List<QuestionsDetailsEntity> details = tuple.getT5();
            
            log.info("Extracted {} rules, {} actions, {} questions, {} answer options, {} details", 
                rules.size(), actions.size(), questions.size(), answerOptions.size(), details.size());
            
            return Mono.when(
                loadRulesDataset(rules),
                loadActionsDataset(actions),
                loadQuestionsDataset(questions),
                loadAnswerOptionsDataset(answerOptions),
                loadDetailsDataset(details),
                updateDatasetMetadata(rules.size(), actions.size(), questions.size(), answerOptions.size(), details.size())
            );
        }).doOnSuccess(v -> {
            long duration = System.currentTimeMillis() - startTime;
            log.info("Complete dataset snapshot created successfully in {}ms", duration);
        }).doOnError(error -> {
            log.error("Failed to create complete dataset snapshot", error);
        });
    }
    
    private Mono<Void> loadRulesDataset(List<RulesByFlowEntity> rules) {
        Map<String, List<RulesByFlowEntity>> rulesByFlow = rules.stream()
            .collect(Collectors.groupingBy(RulesByFlowEntity::getFlow));
            
        return Mono.when(
            rulesByFlow.entrySet().stream()
                .map(entry -> {
                    String key = config.getDatasetKeys().getRulesPrefix() + entry.getKey();
                    Map<String, RulesByFlowEntity> rulesMap = entry.getValue().stream()
                        .collect(Collectors.toMap(RulesByFlowEntity::getRuleId, rule -> rule));
                    return cacheDatasetObject(key, rulesMap);
                })
                .toArray(Mono[]::new)
        );
    }
    
    private Mono<Void> loadActionsDataset(List<ActionsEntity> actions) {
        Map<String, ActionsEntity> actionsMap = actions.stream()
            .collect(Collectors.toMap(ActionsEntity::getActionId, action -> action));
        return cacheDatasetObject(config.getDatasetKeys().getActionsKey(), actionsMap);
    }
    
    private Mono<Void> loadQuestionsDataset(List<QuestionsEntity> questions) {
        Map<String, List<QuestionsEntity>> questionsByAction = questions.stream()
            .collect(Collectors.groupingBy(QuestionsEntity::getActionId));
            
        return Mono.when(
            questionsByAction.entrySet().stream()
                .map(entry -> {
                    String key = config.getDatasetKeys().getQuestionsPrefix() + entry.getKey();
                    Map<String, QuestionsEntity> questionsMap = entry.getValue().stream()
                        .collect(Collectors.toMap(QuestionsEntity::getQuestionId, question -> question));
                    return cacheDatasetObject(key, questionsMap);
                })
                .toArray(Mono[]::new)
        );
    }
    
    private Mono<Void> loadAnswerOptionsDataset(List<AnswerOptionsEntity> answerOptions) {
        Map<String, Map<String, List<AnswerOptionsEntity>>> answerOptionsByActionAndQuestion = answerOptions.stream()
            .collect(Collectors.groupingBy(
                AnswerOptionsEntity::getActionId,
                Collectors.groupingBy(AnswerOptionsEntity::getQuestionId)
            ));
            
        return Mono.when(
            answerOptionsByActionAndQuestion.entrySet().stream()
                .flatMap(actionEntry -> 
                    actionEntry.getValue().entrySet().stream()
                        .map(questionEntry -> {
                            String key = config.getDatasetKeys().getAnswerOptionsPrefix() + 
                                actionEntry.getKey() + ":" + questionEntry.getKey();
                            Map<String, AnswerOptionsEntity> optionsMap = questionEntry.getValue().stream()
                                .collect(Collectors.toMap(AnswerOptionsEntity::getAnswerOptionId, option -> option));
                            return cacheDatasetObject(key, optionsMap);
                        })
                )
                .toArray(Mono[]::new)
        );
    }
    
    private Mono<Void> loadDetailsDataset(List<QuestionsDetailsEntity> details) {
        Map<String, List<QuestionsDetailsEntity>> detailsByAction = details.stream()
            .collect(Collectors.groupingBy(QuestionsDetailsEntity::getActionId));
            
        return Mono.when(
            detailsByAction.entrySet().stream()
                .map(entry -> {
                    String key = config.getDatasetKeys().getDetailsPrefix() + entry.getKey();
                    Map<String, QuestionsDetailsEntity> detailsMap = entry.getValue().stream()
                        .collect(Collectors.toMap(QuestionsDetailsEntity::getDetailId, detail -> detail));
                    return cacheDatasetObject(key, detailsMap);
                })
                .toArray(Mono[]::new)
        );
    }
    
    private Mono<Void> updateDatasetMetadata(int rulesCount, int actionsCount, int questionsCount, int answerOptionsCount, int detailsCount) {
        DatasetMetadata metadata = DatasetMetadata.builder()
            .lastRefresh(Instant.now())
            .version("1.0.0")
            .totalRules(rulesCount)
            .totalActions(actionsCount)
            .totalQuestions(questionsCount)
            .totalAnswerOptions(answerOptionsCount)
            .totalDetails(detailsCount)
            .build();
            
        return cacheDatasetObject(config.getDatasetKeys().getMetadataKey(), metadata);
    }
    
    private Mono<Void> cacheDatasetObject(String key, Object object) {
        return redisCacheService.cacheObject(key, object);
    }
    
    @lombok.Data
    @lombok.Builder
    public static class DatasetMetadata {
        private Instant lastRefresh;
        private String version;
        private int totalRules;
        private int totalActions;
        private int totalQuestions;
        private int totalAnswerOptions;
        private int totalDetails;
    }
}
