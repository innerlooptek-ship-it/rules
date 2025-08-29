package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.dto.*;
import com.cvshealth.digital.microservice.iqe.entity.*;
import com.cvshealth.digital.microservice.iqe.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CassandraService {
    
    private final ActionsRepository actionsRepo;
    private final QuestionsRepository questionsRepo;
    private final AnswerOptionsRepository answerOptionsRepo;
    private final QuestionsDetailsRepository questionnaireDetailsRepo;
    private final IQERepoOrchestrator orchestrator;
    
    public Mono<QuestionareRequest> getQuestionnaire(String actionId) {
        return Mono.zip(
            actionsRepo.findByActionId(actionId).collectList(),
            questionsRepo.findByActionId(actionId).collectList(),
            answerOptionsRepo.findByActionId(actionId).collectList(),
            questionnaireDetailsRepo.findByActionId(actionId).collectList()
        ).flatMap(tuple -> {
            List<ActionsEntity> actionsEntities = tuple.getT1();
            List<QuestionsEntity> questionsEntities = tuple.getT2();
            List<AnswerOptionsEntity> answerOptionsEntities = tuple.getT3();
            List<QuestionsDetailsEntity> detailsEntities = tuple.getT4();
            
            if (actionsEntities.isEmpty()) {
                return Mono.just(new QuestionareRequest());
            }
            
            return buildQuestionnaireFromEntities(actionsEntities, questionsEntities, answerOptionsEntities, detailsEntities);
        });
    }
    
    private Mono<QuestionareRequest> buildQuestionnaireFromEntities(
            List<ActionsEntity> actionsEntities,
            List<QuestionsEntity> questionsEntities,
            List<AnswerOptionsEntity> answerOptionsEntities,
            List<QuestionsDetailsEntity> detailsEntities) {
        
        QuestionareRequest request = new QuestionareRequest();
        
        if (!actionsEntities.isEmpty()) {
            ActionsEntity actionEntity = actionsEntities.get(0);
            Actions action = Actions.builder()
                .actionId(actionEntity.getActionId())
                .actionText(actionEntity.getActionText())
                .questionIds(actionEntity.getQuestionId())
                .detailIds(actionEntity.getDetailId())
                .build();
            request.setActions(action);
        }
        
        if (!questionsEntities.isEmpty()) {
            return Flux.fromIterable(questionsEntities)
                .flatMap(questionEntity -> orchestrator.processQuestionnaire(questionEntity, answerOptionsEntities, questionsEntities))
                .collectList()
                .flatMap(questions -> {
                    request.setQuestions(questions);
                    List<Details> details = detailsEntities.stream()
                        .map(entity -> Details.builder()
                            .actionId(entity.getActionId())
                            .detailId(entity.getDetailId())
                            .title(entity.getTitle())
                            .instructions(entity.getInstructions())
                            .helper(entity.getHelper())
                            .subContext(entity.getSubContext())
                            .pageNumber(entity.getPageNumber())
                            .footer(entity.getFooter())
                            .sequenceId(entity.getSequenceId())
                            .build())
                        .toList();
                    request.setDetails(details);
                    return Mono.just(request);
                });
        }
        
        return Mono.just(request);
    }
}
