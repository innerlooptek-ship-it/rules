package com.cvshealth.digital.microservice.iqe.service;


import com.cvshealth.digital.microservice.iqe.config.DroolConfig;
import com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants;
import com.cvshealth.digital.microservice.iqe.dto.*;
import com.cvshealth.digital.microservice.iqe.entity.*;
import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import com.cvshealth.digital.microservice.iqe.error.RedisServerException;
import com.cvshealth.digital.microservice.iqe.error.ServerErrorException;
import com.cvshealth.digital.microservice.iqe.model.IQEResponse;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.repository.*;
import com.cvshealth.digital.microservice.iqe.utils.LoggingUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class IQEService implements SchedulingConstants {

    private final QuestionnaireRulesRepository rulesRepo;
    private final LoggingUtils loggingUtils;
    private final IQERepoOrchestrator helper;
    private final RulesServiceRepoOrchestrator rulesServiceRepoOrchestrator;
    private final ActionsRepository actionsRepo;
    private final QuestionsRepository questionsRepo;
    private final RulesByFlowRepository rulesByFlowRepo;
    private final AnswerOptionsRepository answerOptionsRepo;
    private final RedisCacheService redisCacheService;
    private final FallbackCacheService fallbackCacheService;
    private final QuestionnaireDetailsRepository questionnaireDetailsRepo;

    private static final Logger logger = LoggerFactory.getLogger(IQEService.class);

    /**
     * Legacy method used to get the rule details by flow
     *
     * @param rulesDetails
     * @param headers
     * @param eventMap
     * @return
     */
    public Mono<com.cvshealth.digital.microservice.iqe.model.Questions> getRuleDetails(RulesDetails rulesDetails, Map<String, String> headers, Map<String, Object> eventMap) {

        eventMap.putAll(headers);
        eventMap.put("flow", rulesDetails.getFlow());

        Flux<QuestionnaireRules> ruleAttributesFlux = rulesRepo.findByFlow(rulesDetails.getFlow());

        ObjectDataCompiler compiler = new ObjectDataCompiler();
        ObjectMapper objectMapper = new ObjectMapper();

        return ruleAttributesFlux.collectList()
                .map(ruleAttributesList -> {
                    if (ruleAttributesList != null && !ruleAttributesList.isEmpty()) {
                        String generatedDRL = compiler.compile(ruleAttributesList, getClass().getClassLoader()
                                .getResourceAsStream(DroolConfig.QUESTIONNAIRE_TEMPLATE_FILE));
                        KieServices kieServices = KieServices.Factory.get();
                        KieHelper kieHelper = new KieHelper();
                        byte[] b1 = generatedDRL.getBytes();
                        Resource resource1 = kieServices.getResources().newByteArrayResource(b1);
                        kieHelper.addResource(resource1, ResourceType.DRL);
                        KieBase kieBase = kieHelper.build();
                        KieSession kieSession = kieBase.newKieSession();
                        kieSession.insert(rulesDetails);
                        int numberOfRulesFired = kieSession.fireAllRules(1);
                        logRuleExecutionStatus(numberOfRulesFired, eventMap);
                        kieSession.dispose();
                        return numberOfRulesFired;
                    } else {
                        return 0;
                    }
                })
                .handle((numberOfRulesFired, sink) -> {
                    if (rulesDetails.getQuestions() != null && !rulesDetails.getQuestions().isEmpty()) {
                        eventMap.put(STATUSCODE_KEY, SUCCESS_MSG);
                        eventMap.put(STATUS, SUCCESS_MSG);
                        try {
                            sink.next(objectMapper.readValue(rulesDetails.getQuestions(), com.cvshealth.digital.microservice.iqe.model.Questions.class));
                        } catch (JsonProcessingException e) {
                            sink.error(new RuntimeException(e));
                        }
                    } else {
                        eventMap.put(STATUSCODE_KEY, "NO_RULE_CONFIGURED");
                        eventMap.put(STATUS, "NO_RULE_CONFIGURED");
                        sink.next(new com.cvshealth.digital.microservice.iqe.model.Questions());
                    }
                });

    }

    private void logRuleExecutionStatus(int numberOfRulesFired, Map<String, Object> tags) {
        if (numberOfRulesFired == 0) {
            tags.put("statusCode", "NO_RULES_MATCH");
            tags.put("status", "NO_RULES_MATCH");
            loggingUtils.errorEventLogging(logger, tags);
        } else {
            tags.put("statusCode", "SUCCESS");
            tags.put("status", "SUCCESS");
        }
    }

    /**
     * Process the input data and store into database.
     *
     * @param questionareRequest : The input request
     * @param reqHdrMap          : The request headers
     * @param eventMap           : Map to store the event data
     * @return The Mono object which contains the IQEResponse
     */
    public Mono<IQEResponse> processQuestionnaire(QuestionareRequest questionareRequest, IQEResponse iqeResponse,
                                                  Map<String, String> reqHdrMap,
                                                  Map<String, Object> eventMap) {

        return Mono.deferContextual(ctx -> rulesServiceRepoOrchestrator.validateRequest(questionareRequest, iqeResponse)
                .flatMap(helper::assignSequenceIds)
                .flatMap(assignedRequest ->
                        helper.processInputData(assignedRequest, reqHdrMap, iqeResponse, eventMap)
                                .flatMap(processedRequest ->
                                        helper.insertQuestionsIntoDB(processedRequest, eventMap, iqeResponse)
                                                .thenReturn(iqeResponse)
                                )
                ).onErrorResume(e -> {
                    if (e instanceof RedisServerException) {
                        return Mono.just(iqeResponse);
                    }
                    log.info("Exception occurred in the method {} and error {}", PROCESS_AND_STORE_INPUT_DATA, e.getMessage());
                    return Mono.error(e);
                }));
    }


    /**
     * Retrieve all the rules for all the flows
     *
     * @return The Mono object which contains all the rules
     */
    public Mono<QuestionareRequest> rules() {
        return Mono.deferContextual(ctx -> rulesByFlowRepo.findAll()
                .collectList()
                .map(rulesByFlows -> {
                    QuestionareRequest request = new QuestionareRequest();
                    List<RulesByFlowEntity> activeRules = rulesByFlows.stream()
                            .filter(RulesByFlowEntity::isActive)
                            .toList();
                    List<RulesByFlowEntity> inactiveRules = rulesByFlows.stream()
                            .filter(rule -> !rule.isActive())
                            .toList();
                    request.setActiveRules(activeRules);
                    request.setInactiveRules(inactiveRules);
                    return request;
                }).onErrorResume(e -> Mono.error(new ServerErrorException(FAILURE_CD, e.getMessage()))));
    }


    /**
     * Retrieve the questionnaire by actionId
     *
     * @param actionId : The action id
     * @return The Mono object which contains the questionnaire
     */
    public Mono<QuestionareRequest> questionnaireByActionId(String actionId, QuestionareRequest iqeOutPut) {
        String methodName = GET_QUESTIONARE_BY_ACTION_ID;
        Map<String, String> eventMap = new HashMap<>();
        log.debug(ENTRY_LOG, methodName);

        return Mono.deferContextual(ctx -> redisCacheService.getDataFromRedis(IQE_QUESTIONNAIRE, actionId, eventMap)
                        .flatMap(object -> {
                            if (object != null) {
                                return Mono.justOrEmpty(object)
                                        .map(jsonNode -> {
                                            ObjectMapper mapper = new ObjectMapper();
                                            return mapper.convertValue(jsonNode, QuestionareRequest.class);
                                        });
                            } else {
                                log.info("No data found in redis for actionId: {}", actionId);
                                return Mono.empty();
                            }
                        })
                        .switchIfEmpty(Mono.deferContextual(ctx1 -> {
                            Mono<RulesByFlowEntity> rulesByFlowMono = rulesByFlowRepo.findByActionId(actionId)
                                    .next()
                                    .onErrorResume(NoSuchElementException.class, e -> {
                                        log.info("No RulesByFlow found for actionId: {}", actionId);
                                        return Mono.empty();
                                    })
                                    .defaultIfEmpty(new RulesByFlowEntity());

                            Mono<ActionsEntity> actionsMono = actionsRepo.findByActionId(actionId)
                                    .next()
                                    .onErrorResume(NoSuchElementException.class, e -> {
                                        log.info("No Actions found for actionId: {}", actionId);
                                        return Mono.empty();
                                    })
                                    .defaultIfEmpty(new ActionsEntity());

                            return Mono.zip(rulesByFlowMono, actionsMono)
                                    .flatMap(tuple -> {
                                        RulesByFlowEntity rulesByFlow = tuple.getT1();
                                        ActionsEntity actions = tuple.getT2();
                                        if (rulesByFlow.getActionId() == null || actions.getActionId() == null) {
                                            iqeOutPut.setStatusCode(DATA_NOT_FOUND_CODE);
                                            iqeOutPut.setErrorDescription(DATA_NOT_FOUND_MESSAGE);
                                            return Mono.just(iqeOutPut);
                                        }

                                        RulesByFlow rulesByFlowData = RulesByFlow.builder()
                                                .flow(rulesByFlow.getFlow())
                                                .ruleId(rulesByFlow.getRuleId())
                                                .ruleName(rulesByFlow.getRuleName())
                                                .actionId(rulesByFlow.getActionId())
                                                .condition(rulesByFlow.getCondition())
                                                .lob(rulesByFlow.getLob())
                                                .salience(rulesByFlow.getSalience())
                                                .isActive(rulesByFlow.isActive())
                                                .build();

                                        Audit audit = new Audit();
                                        audit.setCreatedBy(rulesByFlow.getAudit().getCreatedBy());
                                        audit.setCreatedTs(rulesByFlow.getAudit().getCreatedTs());
                                        audit.setModifiedBy(rulesByFlow.getAudit().getModifiedBy());
                                        audit.setModifiedTs(rulesByFlow.getAudit().getModifiedTs());
                                        rulesByFlowData.setAudit(audit);

                                        Actions actionsData = Actions.builder()
                                                .actionId(actions.getActionId())
                                                .actionText(actions.getActionText())
                                                .questionIds(actions.getQuestionId())
                                                .detailIds(actions.getDetailId())
                                                .build();

                                        iqeOutPut.setRulesByFlow(rulesByFlowData);
                                        iqeOutPut.setActions(actionsData);

                                        return actionsRepo.findByActionId(actionId)
                                                .next()
                                                .flatMap(actions1 ->
                                                        questionsRepo.findByActionId(actionId)
                                                                .collectList()
                                                                .flatMap(questions ->
                                                                        answerOptionsRepo.findByActionId(actionId)
                                                                                .collectList()
                                                                                .flatMap(answerOptions ->
                                                                                        questionnaireDetailsRepo.findByActionId(actionId)
                                                                                                .collectList()
                                                                                                .flatMap(details -> {

                                                                                                    // Filter questions matching action IDs
                                                                                                    List<QuestionsEntity> filteredQuestions = questions.stream()
                                                                                                            .filter(question -> actions1.getQuestionId()
                                                                                                                    .contains(question.getQuestionId()))
                                                                                                            .toList();

                                                                                                    // Filter details matching action IDs
                                                                                                    List<QuestionsDetailsEntity> filteredDetails = details.stream()
                                                                                                            .filter(detail -> actions1.getDetailId()
                                                                                                                    .contains(detail.getDetailId()))
                                                                                                            .toList();
                                                                                                    return Flux.fromIterable(filteredQuestions)
                                                                                                            .flatMap(questionEntity ->
                                                                                                                    helper.processQuestionnaire(questionEntity,
                                                                                                                            answerOptions, questions))
                                                                                                            .collectList()
                                                                                                            .flatMap(questionsDataList -> {
                                                                                                                // Sort the questionsDataList by sequenceId
                                                                                                                List<Questions> sortedQuestionsList = questionsDataList.stream()
                                                                                                                        .sorted(Comparator.comparingInt(Questions::getSequenceId))
                                                                                                                        .toList();
                                                                                                                iqeOutPut.setQuestions(sortedQuestionsList);
                                                                                                                List<Details> detailsList = filteredDetails.stream()
                                                                                                                        .map(detailEntity -> Details.builder()
                                                                                                                                .title(detailEntity.getTitle())
                                                                                                                                .instructions(detailEntity.getInstructions())
                                                                                                                                .helper(detailEntity.getHelper())
                                                                                                                                .subContext(detailEntity.getSubContext())
                                                                                                                                .pageNumber(detailEntity.getPageNumber())
                                                                                                                                .sequenceId(detailEntity.getSequenceId())
                                                                                                                                .footer(detailEntity.getFooter())
                                                                                                                                .build())
                                                                                                                        .sorted(Comparator.comparingInt(Details::getSequenceId))
                                                                                                                        .toList();

                                                                                                                iqeOutPut.setDetails(detailsList);

                                                                                                                return Mono.just(iqeOutPut);
                                                                                                            });
                                                                                                })
                                                                                )
                                                                )
                                                )
                                                .onErrorResume(e -> {
                                                    log.info("Exception occurred in the method {} and error {}", methodName, e.getMessage());
                                                    return Mono.just(iqeOutPut);
                                                });
                                    })
                                    .doOnSuccess(result -> Mono.fromCallable(() -> redisCacheService.setDataToRedisRest(actionId, result, eventMap))
                                            .onErrorResume(e -> {
                                                log.error("Exception in questionnaireByActionId when setting data to redis {}", e.getMessage());
                                                return Mono.empty();
                                            })
                                            .subscribe());
                        }))
                        .doOnSuccess(result -> log.debug(EXIT_LOG, methodName)))
                .onErrorResume(e -> Mono.just(new QuestionareRequest()));
    }

    /**
     * Delete questionnaire by actionId.
     * <p>
     * This method takes the actionId as the parameter and deletes the questionnaire associated with it.
     * The method returns a Mono containing the IQEResponse object which consists of the actionId and the status code.
     * If the actionId is not found then the method returns a Mono containing IQEResponse with 404 status code and a error message.
     *
     * @param actionId the actionId of the questionnaire to delete
     * @return Mono containing IQEResponse object
     */
    public Mono<IQEResponse> deleteQuestionnaireByActionId(String actionId) {
        return Mono.deferContextual(ctx -> rulesByFlowRepo.findByActionId(actionId)
                .collectList()
                .flatMap(rulesByFlowList -> {
                    if (rulesByFlowList.isEmpty()) {
                        return Mono.just(new IQEResponse("5009", null, "ActionId not found"));
                    } else {
                        Map<String, String> eventMap = new HashMap<>();
                        RulesByFlowEntity rulesByFlow = rulesByFlowList.get(0);
                        return rulesByFlowRepo.deleteByFlowAndRuleId(rulesByFlow.getFlow(), rulesByFlow.getRuleId())
                                .then(actionsRepo.deleteByActionId(actionId))
                                .then(questionsRepo.deleteByActionId(actionId))
                                .then(answerOptionsRepo.deleteByActionId(actionId))
                                .then(questionnaireDetailsRepo.deleteByActionId(actionId))
                                .then(redisCacheService.deleteDataFromRedis(IQE_QUESTIONNAIRE, actionId, eventMap))
                                .then(Mono.just(new IQEResponse("0000", "ActionId deleted successfully", null)));
                    }
                })).onErrorResume(e -> Mono.error(new ServerErrorException(FAILURE_CD, e.getMessage())));
    }


    /**
     * This method is used to retrieve the questionnaire by actionId and conditionId
     *
     * @param rulesDetails the RulesDetails object which contains the flow, ruleId and conditionId
     * @param iqeOutPut    the QuestionnaireRequest object which contains the actionId and the status code
     * @param headers      the headers of the request
     * @return Mono containing QuestionnaireRequest object
     */
    public Mono<QuestionareRequest> questionnaireByFlowAndCondition(RulesDetails rulesDetails, QuestionareRequest iqeOutPut,
                                                                    Map<String, String> headers) {
        return Mono.deferContextual(ctx -> {
                    return rulesByFlowRepo.findByFlow(rulesDetails.getFlow())
                            .collectList()
                            .doOnSuccess(rules -> fallbackCacheService.markCassandraHealthy())
                            .flatMap(ruleAttributesList -> processRulesAndGetQuestionnaire(ruleAttributesList, rulesDetails, iqeOutPut))
                            .onErrorResume(e -> {
                                log.warn("Cassandra unavailable for flow {}, falling back to cache: {}", rulesDetails.getFlow(), e.getMessage());
                                fallbackCacheService.markCassandraUnhealthy();
                                return fallbackCacheService.getRulesByFlow(rulesDetails.getFlow())
                                        .flatMap(cachedRules -> processRulesAndGetQuestionnaire(cachedRules, rulesDetails, iqeOutPut))
                                        .onErrorResume(fallbackError -> {
                                            log.error("Both Cassandra and fallback cache failed for flow {}: {}", rulesDetails.getFlow(), fallbackError.getMessage());
                                            return Mono.error(new RuntimeException("Service temporarily unavailable"));
                                        });
                            });
                });
    }

    private Mono<QuestionareRequest> processRulesAndGetQuestionnaire(List<RulesByFlowEntity> ruleAttributesList, 
                                                                    RulesDetails rulesDetails, 
                                                                    QuestionareRequest iqeOutPut) {
        if (ruleAttributesList != null && !ruleAttributesList.isEmpty()) {
            ObjectDataCompiler compiler = new ObjectDataCompiler();
            String generatedDRL = compiler.compile(ruleAttributesList, getClass().getClassLoader()
                    .getResourceAsStream(DroolConfig.QUESTIONNAIRE_TEMPLATE_FILE_IQE));
            KieServices kieServices = KieServices.Factory.get();
            KieHelper kieHelper = new KieHelper();
            byte[] b1 = generatedDRL.getBytes();
            Resource resource1 = kieServices.getResources().newByteArrayResource(b1);
            kieHelper.addResource(resource1, ResourceType.DRL);
            KieBase kieBase = kieHelper.build();
            KieSession kieSession = kieBase.newKieSession();
            kieSession.insert(rulesDetails);
            kieSession.fireAllRules(1);
            kieSession.dispose();
            if (rulesDetails.getActionId() != null && !rulesDetails.getActionId().isEmpty()) {
                log.info("Rules Details: {}", rulesDetails);
                return questionnaireByActionId(rulesDetails.getActionId(), iqeOutPut);
            } else {
                return Mono.just(iqeOutPut);
            }
        } else {
            return Mono.just(iqeOutPut);
        }
    }


    /**
     * This method is used to retrieve the questionnaire by actionId and questionId
     *
     * @param actionId   the actionId of the questionnaire
     * @param questionId the questionId of the questionnaire
     * @param headers    the headers of the request
     * @return Mono containing QuestionareRequest object
     */
    public Mono<QuestionareRequest> questionnaireByActionAndQuestionId(String actionId, String questionId,

                                                                       Map<String, String> headers) {
        QuestionareRequest iqeOutPut = new QuestionareRequest();
        return Mono.deferContextual(ctx -> {
                    Mono<Questions> questionData = questionsRepo.findByActionIdAndQuestionId(actionId, questionId)
                            .map(questions -> Questions.builder()
                                    .actionId(questions.getActionId())
                                    .questionId(questions.getQuestionId())
                                    .answerOptionIds(questions.getAnswerOptionId())
                                    .answerType(questions.getAnswerType())
                                    .characterLimit(questions.getCharacterLimit())
                                    .errorMessage(questions.getErrorMessage())
                                    .helpText(questions.getHelpText())
                                    .stacked(questions.isStacked())
                                    .required(questions.isRequired())
                                    .text(questions.getQuestionText())
                                    .sequenceId(questions.getSequence_id()).build());

                    Flux<AnswerOptions> answerOptionData = answerOptionsRepo.findByActionIdAndQuestionId(actionId, questionId)
                            .map(answerOptions -> AnswerOptions.builder()
                                    .actionId(answerOptions.getActionId())
                                    .questionId(answerOptions.getQuestionId())
                                    .answerOptionId(answerOptions.getAnswerOptionId())
                                    .text(answerOptions.getAnswerText())
                                    .value(answerOptions.getAnswerValue())
                                    .relatedQuestionIds(answerOptions.getRelatedQuestions())
                                    .sequenceId(answerOptions.getSequence_id())
                                    .build())
                            .flatMap(answerOptions -> {
                                if (answerOptions.getRelatedQuestionIds() != null && !answerOptions.getRelatedQuestionIds().isEmpty()) {
                                    List<Questions> relatedQuestions = answerOptions.getRelatedQuestionIds().stream()
                                            .map(relatedQuestionId -> Questions.builder()
                                                    .actionId(answerOptions.getActionId())
                                                    .questionId(relatedQuestionId)
                                                    .build())
                                            .toList();
                                    answerOptions.setRelatedQuestions(relatedQuestions);
                                }
                                return Mono.just(answerOptions);
                            });


                    return questionData.flatMap(qd -> answerOptionData.collectList()
                            .map(aos -> aos.stream()
                                    .sorted(Comparator.comparingInt(AnswerOptions::getSequenceId))
                                    .toList())
                            .map(aos -> {
                                iqeOutPut.setQuestion(qd);
                                iqeOutPut.getQuestion().setAnswerOptions(aos);
                                return iqeOutPut;
                            }));
                })
                .onErrorResume(e -> Mono.error(new ServerErrorException(FAILURE_CD, e.getMessage())));
    }

    /**
     * This method is used to retrieve the questionnaire by flow.
     * It takes the flow details, questionnaire output object, and request headers as parameters.
     * It compiles the rules associated with the flow, executes them, and retrieves the questionnaire based on the actionId.
     * If no rules are found, it returns the empty questionnaire output object.
     *
     * @param rulesDetails the RulesDetails object containing the flow information
     * @param iqeOutPut    the QuestionareRequest object to store the retrieved questionnaire
     * @param headers      the headers of the request
     * @return Mono containing the QuestionareRequest object with the retrieved questionnaire
     */
    public Mono<QuestionareRequest> questionnaireByFlow(RulesDetails rulesDetails, QuestionareRequest iqeOutPut,
                                                        Map<String, String> headers) {
        return Mono.deferContextual(ctx -> {
                    Flux<RulesByFlowEntity> ruleAttributesFlux = rulesByFlowRepo.findByFlow(rulesDetails.getFlow());

                    ObjectDataCompiler compiler = new ObjectDataCompiler();

                    return ruleAttributesFlux.collectList()
                            .flatMap(ruleAttributesList -> {
                                if (ruleAttributesList != null && !ruleAttributesList.isEmpty()) {
                                    String generatedDRL = compiler.compile(ruleAttributesList, getClass().getClassLoader()
                                            .getResourceAsStream(DroolConfig.QUESTIONNAIRE_TEMPLATE_FILE_IQE));
                                    KieServices kieServices = KieServices.Factory.get();
                                    KieHelper kieHelper = new KieHelper();
                                    byte[] b1 = generatedDRL.getBytes();
                                    Resource resource1 = kieServices.getResources().newByteArrayResource(b1);
                                    kieHelper.addResource(resource1, ResourceType.DRL);
                                    KieBase kieBase = kieHelper.build();
                                    KieSession kieSession = kieBase.newKieSession();
                                    kieSession.insert(rulesDetails);
                                    kieSession.fireAllRules(1);
                                    kieSession.dispose();
                                    if (rulesDetails.getActionId() != null && !rulesDetails.getActionId().isEmpty()) {
                                        log.info("Rules Details: {}", rulesDetails);
                                        return actionsRepo.findByActionId(rulesDetails.getActionId())
                                                .collectList()
                                                .flatMap(actionsEntities -> {
                                                    ActionsEntity firstEntity = actionsEntities.stream().findFirst().orElse(null);
                                                    if (firstEntity != null) {
                                                        List<String> questionIds = firstEntity.getQuestionId().stream()
                                                                .filter(id -> !id.isEmpty())
                                                                .toList();
                                                        return Flux.fromIterable(questionIds)
                                                                .flatMap(questionId -> questionnaireByActionAndQuestionId(rulesDetails.getActionId(),
                                                                        questionId, headers))
                                                                .collectList()
                                                                .doOnSuccess(questions -> {
                                                                    if (iqeOutPut.getQuestions() == null) {
                                                                        iqeOutPut.setQuestions(new ArrayList<>());
                                                                    }
                                                                    questions.forEach(question ->
                                                                            iqeOutPut.getQuestions().add(question.getQuestion()));
                                                                })

                                                                .doOnSuccess(questions -> Collections.sort(iqeOutPut.getQuestions(), Comparator.comparingInt(Questions::getSequenceId)))
                                                                .thenReturn(iqeOutPut);
                                                    } else {
                                                        return Mono.just(iqeOutPut);
                                                    }
                                                });
                                    } else {
                                        return Mono.just(iqeOutPut);
                                    }
                                } else {
                                    return Mono.just(iqeOutPut);
                                }
                            });
                })
                .onErrorResume(e -> Mono.error(new ServerErrorException(FAILURE_CD, e.getMessage())));
    }


    /**
     * This method is used to retrieve the questions by related questions list.
     * It takes the related questions object, questionnaire output object, and request headers as parameters.
     * It iterates through the related questions list, retrieves the questions by actionId and questionId,
     * and adds them to the questionnaire output object.
     * The related questions list is sorted by sequence id.
     * If the related questions list is empty, the questionnaire output object is returned as is.
     *
     * @param answerOptions the related questions object
     * @param iqeOutPut     the QuestionareRequest object to store the retrieved questions
     * @param headers       the headers of the request
     * @return Mono containing the QuestionareRequest object with the retrieved questions
     */
    public Mono<QuestionareRequest> getQuestionsByRelatedQuestionsList(RelatedQuestionsRequest answerOptions, QuestionareRequest iqeOutPut, Map<String, String> headers) {

        return Flux.deferContextual(ctx -> Flux.fromIterable(answerOptions.getRelatedQuestions().isEmpty() ?
                                Collections.emptyList() : answerOptions.getRelatedQuestions().stream()
                                .map(Questions::getQuestionId).collect(Collectors.toList()))
                        .flatMap(questionId -> questionnaireByActionAndQuestionId(answerOptions
                                .getRelatedQuestions().get(0).getActionId(), questionId, headers))
                        .collectList())
                .doOnNext(questions -> {
                    if (iqeOutPut.getQuestions() == null) iqeOutPut.setQuestions(new ArrayList<>());
                    questions.forEach(question -> iqeOutPut.getQuestions().add(question.getQuestion()));
                })
                .doOnNext(questions -> Collections.sort(iqeOutPut.getQuestions(),
                        Comparator.comparingInt(Questions::getSequenceId)))
                .last()
                .map(questions -> iqeOutPut);

    }

}
