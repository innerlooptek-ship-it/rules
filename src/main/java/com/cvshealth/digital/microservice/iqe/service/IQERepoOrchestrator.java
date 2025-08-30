package com.cvshealth.digital.microservice.iqe.service;


import com.cvshealth.digital.microservice.iqe.dto.*;
import com.cvshealth.digital.microservice.iqe.entity.*;
import com.cvshealth.digital.microservice.iqe.error.RedisServerException;
import com.cvshealth.digital.microservice.iqe.error.ServerErrorException;
import com.cvshealth.digital.microservice.iqe.model.IQEResponse;

import com.cvshealth.digital.microservice.iqe.repository.*;
import com.cvshealth.digital.microservice.iqe.udt.AuditEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.*;


@Component

/** The Constant log. */
@Slf4j

/**
 * Instantiates a new dhs slot management app helper.
 */
@RequiredArgsConstructor
public class IQERepoOrchestrator {

    private final ActionsRepository actionsRepo;
    private final QuestionsRepository questionsRepo;
    private final RulesByFlowRepository rulesByFlowRepo;
    private final QuestionnaireDetailsRepository questionnaireDetailsRepo;
    private final AnswerOptionsRepository answerOptionsRepo;
    private final RedisCacheService redisCacheService;

    /**
     * Generate a random UUID.
     *
     * @return the string
     */
    public String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Processes the input data for a questionnaire request.
     * <p>
     * This method processes the input data by generating a ruleId and actionId, and updating the
     * questionnaire request, response, and event map accordingly.
     * <p>
     *
     * @param questionareRequest the request containing the questionnaire data
     * @param reqHdrMap          a map of request headers
     * @param iqeResponse        the response object to update with status codes and descriptions
     * @param eventMap           a map to store event-related data
     * @return a Mono containing the processed QuestionareRequest
     */
    public Mono<QuestionareRequest> processInputData(QuestionareRequest questionareRequest, Map<String, String> reqHdrMap, IQEResponse iqeResponse, Map<String, Object> eventMap) {
        String methodName = PROCESS_INPUT_DATA;
        return Mono.deferContextual(ctx -> Mono.just(questionareRequest)
                .flatMap(request -> {
                    // Generate ruleId and actionId
                    String ruleId = request.getRulesByFlow().getRuleId() == null ? generateUUID() : request.getRulesByFlow().getRuleId();
                    String actionId = request.getRulesByFlow().getActionId() == null ? generateUUID() : request.getRulesByFlow().getActionId();
                    // Set ruleId and actionId in RulesByFlow
                    request.getRulesByFlow().setRuleId(ruleId);
                    request.getRulesByFlow().setActionId(actionId);
                    request.getRulesByFlow().setActive(true);
                    // Set actionId in Actions
                    request.getActions().setActionId(actionId);
                    setAuditData(request.getRulesByFlow(), reqHdrMap);

                    // Initialize questions and details lists if null
                    if (request.getQuestions() == null) {
                        request.setQuestions(new ArrayList<>());
                    }
                    if (request.getDetails() == null) {
                        request.setDetails(new ArrayList<>());
                    }

                    // Process questions
                    Mono<List<Questions>> processedQuestions = Flux.fromIterable(request.getQuestions())
                            .flatMap(question -> processQuestion(question, false, actionId))
                            .collectList();

                    // Process details with sequence numbers
                    Mono<List<Details>> processedDetails = Flux.fromIterable(request.getDetails())
                            .index()
                            .flatMap(indexedDetail -> {
                                Long index = indexedDetail.getT1();
                                int sequenceNumber = (index != null ? index.intValue() : 0) + 1;
                                return processDetail(indexedDetail.getT2(), actionId, sequenceNumber);
                            })
                            .collectList();

                    return Mono.zip(processedQuestions, processedDetails)
                            .map(tuple -> {
                                List<Questions> questionList = tuple.getT1();
                                List<Details> detailList = tuple.getT2();
                                // Set questionIds into actions after processing
                                List<String> questionIdList = questionList.stream()
                                        .map(Questions::getQuestionId)
                                        .toList();
                                request.getActions().setQuestionIds(questionIdList);
                                // Set detailIds into actions after processing
                                List<String> detailIdList = detailList.stream()
                                        .map(Details::getDetailId)
                                        .toList();
                                request.getActions().setDetailIds(detailIdList);
                                request.setQuestions(questionList);
                                request.setDetails(detailList);
                                return request;
                            }).onErrorResume(e -> {
                                log.info("Exception occurred in the method {}", methodName);
                                return Mono.error(new ServerErrorException(FAILURE_CD, e.getMessage()));
                            });
                }));
    }
    public Mono<Details> processDetail(Details detail, String actionId, int sequenceNumber) {
        return Mono.deferContextual(ctx -> Mono.just(detail).flatMap(detailsDTO -> {
            if (detailsDTO.getDetailId() == null) {
                detailsDTO.setDetailId(generateUUID());
            }
            detailsDTO.setActionId(actionId);
            detailsDTO.setSequenceId(sequenceNumber);
            return Mono.just(detailsDTO);
        }));
    }

    /**
     * Process a single question.
     * <p>
     * This method takes a single question and processes it. This includes setting the IDs for the question and its answer options
     * as well as processing any nested questions.
     * <p>
     *
     * @param question the question to process
     * @param isNested whether the question is nested
     * @param actionId the ID of the action that contains the question
     * @return a Mono containing the processed question
     */
    public Mono<Questions> processQuestion(Questions question, boolean isNested, String actionId) {
        return Mono.deferContextual(ctx -> Mono.just(question).flatMap(questionsDTO -> {
            if (questionsDTO.getQuestionId() == null) {
                questionsDTO.setQuestionId(generateUUID());
            }
            questionsDTO.setActionId(actionId);
            // Check if question has answer options
            if (questionsDTO.getAnswerOptions() != null && !questionsDTO.getAnswerOptions().isEmpty()) {
                // Process Answer Options reactively
                return Flux.fromIterable(questionsDTO.getAnswerOptions())
                        .flatMap(answerOption -> {
                            // Set answerOptionId and actionId if null
                            if (answerOption.getAnswerOptionId() == null) {
                                answerOption.setAnswerOptionId(generateUUID());
                            }
                            answerOption.setActionId(actionId);
                            // Set questionId for answerOption
                            answerOption.setQuestionId(questionsDTO.getQuestionId());
                            // Process nested questions in AnswerOptions
                            if (answerOption.getRelatedQuestions() != null) {
                                return Flux.fromIterable(answerOption.getRelatedQuestions())
                                        .flatMap(nestedQuestion -> processQuestion(nestedQuestion, true, actionId))
                                        .collectList()
                                        .doOnNext(nestedQuestions -> {
                                            List<String> nestedIds = nestedQuestions.stream()
                                                    .map(Questions::getQuestionId)
                                                    .toList();
                                            answerOption.setRelatedQuestionIds(nestedIds);
                                        })
                                        .then(Mono.just(answerOption));
                            }
                            return Mono.just(answerOption);
                        })
                        .collectList().doOnNext(answerOptions -> {
                            questionsDTO.setAnswerOptions(answerOptions);
                            // Set answerOptionIdList for question
                            List<String> answerOptionIds = answerOptions.stream()
                                    .map(AnswerOptions::getAnswerOptionId)
                                    .toList();
                            questionsDTO.setAnswerOptionIds(answerOptionIds);
                        })
                        .then(Mono.just(questionsDTO));
            } else {
                // If question does not have answer options, return the question as is
                return Mono.just(questionsDTO);
            }

        }));
    }


    /**
     * This method will take QuestionareRequest and insert the questions into cassandra DB.
     *
     * @param questionareRequest - This is the object containing all the questions for a flow
     * @param eventMap           - This is the map containing the event details
     * @param iqeResponse        - This is the response object that will be populated with the result of the operation
     * @return - Mono of Void
     */
    public Mono<Void> insertQuestionsIntoDB(QuestionareRequest questionareRequest, Map<String, Object> eventMap, IQEResponse iqeResponse) {
        return Mono.deferContextual(ctx -> {
            RulesByFlow rulesByFlowData = questionareRequest.getRulesByFlow();
            AuditEntity audit = AuditEntity.builder()
                    .createdBy(rulesByFlowData.getAudit().getCreatedBy())
                    .createdTs(rulesByFlowData.getAudit().getCreatedTs())
                    .modifiedBy(rulesByFlowData.getAudit().getModifiedBy())
                    .modifiedTs(rulesByFlowData.getAudit().getModifiedTs())
                    .build();

            RulesByFlowEntity rulesByFlow = RulesByFlowEntity.builder()
                    .flow(rulesByFlowData.getFlow())
                    .ruleId(rulesByFlowData.getRuleId())
                    .ruleName(rulesByFlowData.getRuleName())
                    .actionId(rulesByFlowData.getActionId())
                    .condition(rulesByFlowData.getCondition())
                    .lob(rulesByFlowData.getLob())
                    .salience(rulesByFlowData.getSalience())
                    .isActive(rulesByFlowData.isActive())
                    .audit(audit)
                    .build();

            Actions actionsData = questionareRequest.getActions();

            ActionsEntity actions = ActionsEntity.builder()
                    .actionId(actionsData.getActionId())
                    .actionText(actionsData.getActionText())
                    .questionId(actionsData.getQuestionIds())
                    .detailId(actionsData.getDetailIds())
                    .build();

            List<Questions> questionsDTOList = questionareRequest.getQuestions();
            List<Details> detailsList = questionareRequest.getDetails();


            Flux<QuestionsEntity> questionsFlux = Flux.fromIterable(questionsDTOList)
                    .flatMap(this::extractQuestionsRecursive);

            Flux<AnswerOptionsEntity> answerOptionsFlux = Flux.fromIterable(questionsDTOList)
                    .flatMap(this::extractAnswerOptionsRecursive);
            Flux<QuestionsDetailsEntity> questionsDetailsEntityFlux = Flux.fromIterable(detailsList)
                    .flatMap(this::extractQuestionsDetails);

            return Mono.zip(
                    rulesByFlowRepo.save(rulesByFlow),
                    actionsRepo.save(actions),
                    questionsFlux.flatMap(questionsRepo::save).collectList(),
                    answerOptionsFlux.flatMap(answerOptionsRepo::save).collectList(),
                    questionsDetailsEntityFlux.flatMap(questionnaireDetailsRepo::save).collectList()
            ).flatMap(t -> {
                log.info("Data inserted successfully");
                eventMap.put("DBInsertStatus", "SUCCESS");
                iqeResponse.setStatusCode("0000");
                iqeResponse.setStatusDescription(("Data inserted successfully"));
                iqeResponse.setActionId(questionareRequest.getRulesByFlow().getActionId());
                return Mono.just(iqeResponse);
            }).onErrorResume(e -> {
                log.error("Error inserting data", e);
                eventMap.put("DBInsertStatus", "FAILED");
                return Mono.error(new ServerErrorException(FAILURE_CD, e.getMessage()));
            }).flatMap(iqeOutPuts -> {
                Map<String, String> reqHdrMap = new HashMap<>();
                questionareRequest.getRulesByFlow().setUpdate(false);
                return Mono.defer(() -> redisCacheService.setDataToRedisRest(questionareRequest.getRulesByFlow().getActionId(),
                         questionareRequest, reqHdrMap)).onErrorResume(e -> {
                    log.error("Error setting data to redis in insertQuestionsIntoDB", e.getMessage());
                    return Mono.error(new RedisServerException(FAILURE_CD, e.getMessage()));
                });
            }).then();
        });
    }

    private Flux<QuestionsDetailsEntity> extractQuestionsDetails(Details details) {
        QuestionsDetailsEntity questionDetails = QuestionsDetailsEntity.builder()
                .detailId(details.getDetailId())
                .actionId(details.getActionId())
                .footer(details.getFooter())
                .helper(details.getHelper())
                .instructions(details.getInstructions())
                .pageNumber(details.getPageNumber())
                .subContext(details.getSubContext())
                .sequenceId(details.getSequenceId())
                .title(details.getTitle())
                .build();
        return Flux.just(questionDetails);
    }

    /**
     * This method will take QuestionareRequest and extract the questions recursively from the input.
     *
     * @param questionsDTO - This is the object containing the questions data
     * @return - Flux of Questions
     */
    public Flux<QuestionsEntity> extractQuestionsRecursive(Questions questionsDTO) {
        QuestionsEntity question = QuestionsEntity.builder()
                .questionId(questionsDTO.getQuestionId())
                .questionText(questionsDTO.getText())
                .answerType(questionsDTO.getAnswerType())
                .actionId(questionsDTO.getActionId())
                .answerOptionId(questionsDTO.getAnswerOptionIds())
                .errorMessage(questionsDTO.getErrorMessage())
                .helpText(questionsDTO.getHelpText())
                .stacked(questionsDTO.isStacked())
                .required(questionsDTO.isRequired())
                .sequence_id(questionsDTO.getSequenceId())
                .linkText(questionsDTO.getLinkText())
                .questionnumber(questionsDTO.getQuestionNumber() != null ? questionsDTO.getQuestionNumber() : 0)
                .skiplegend(questionsDTO.getSkipLegend())
                .subcontext(questionsDTO.getSubContext())
                .characterLimit(questionsDTO.getCharacterLimit() != null ? questionsDTO.getCharacterLimit() : 0)
                .build();

        Flux<QuestionsEntity> questionsFlux = Flux.just(question);

        if (questionsDTO.getAnswerOptions() != null) {
            Flux<QuestionsEntity> nestedQuestionsFlux = Flux.fromIterable(questionsDTO.getAnswerOptions())
                    .flatMap(answerOptionsData -> {
                        if (answerOptionsData.getRelatedQuestions() != null && !answerOptionsData.getRelatedQuestions().isEmpty()) {
                            return Flux.fromIterable(answerOptionsData.getRelatedQuestions())
                                    .flatMap(this::extractQuestionsRecursive);
                        } else {
                            return Flux.empty();
                        }
                    });
            questionsFlux = questionsFlux.concatWith(nestedQuestionsFlux);
        }

        return questionsFlux;
    }


    /**
     * This method will take QuestionareRequest and extract the answer options recursively from the input.
     *
     * @param questionsDTO - This is the object containing the questions data
     * @return - Flux of AnswerOptions
     */
    public Flux<AnswerOptionsEntity> extractAnswerOptionsRecursive(Questions questionsDTO) {

        List<AnswerOptionsEntity> answerOptions = questionsDTO.getAnswerOptions()
                .stream()
                .map(answerOptionDTO -> AnswerOptionsEntity.builder()
                        .actionId(answerOptionDTO.getActionId())
                        .questionId(answerOptionDTO.getQuestionId())
                        .answerOptionId(answerOptionDTO.getAnswerOptionId())
                        .answerText(answerOptionDTO.getText())
                        .answerValue(answerOptionDTO.getValue())
                        .relatedQuestions(answerOptionDTO.getRelatedQuestionIds())
                        .sequence_id(answerOptionDTO.getSequenceId())
                        .additionalDetailText(answerOptionDTO.getAdditionalDetailText())
                        .build())
                .collect(Collectors.toList());

        if (questionsDTO.getAnswerOptions() != null) {
            answerOptions.addAll(questionsDTO.getAnswerOptions()
                    .stream()
                    .filter(answerOptionDTO -> answerOptionDTO.getRelatedQuestions() != null)
                    .flatMap(answerOptionDTO -> answerOptionDTO.getRelatedQuestions().stream())
                    .flatMap(nestedQuestion -> extractAnswerOptionsRecursive(nestedQuestion).toStream())
                    .toList());
        }

        return Flux.fromIterable(answerOptions);
    }


    /**
     * This method sets the audit data in the RulesByFlowData object.
     * It takes the request object and the http request header map as parameters.
     * It sets the createdTs and createdBy fields in the audit object.
     * createdTs is set to the current timestamp and createdBy is set to the userId from the request headers.
     * If userId is not present in the request headers, it is set to a default value.
     *
     * @param request   - The RulesByFlowData object in which the audit data is to be set.
     * @param reqHdrMap - The http request header map.
     */
    public void setAuditData(RulesByFlow request, Map<String, String> reqHdrMap) {
        Audit audit;
        LocalDateTime timestampString = LocalDateTime.now(ZoneOffset.UTC);
        String scheduleStartDate = timestampString.format(DATE_TIME_MILLI_SECONDS_FORMATTER);
        //If update operation, then update the audit data with modified details
        if (request.isUpdate()) {
            audit = Audit.builder()
                    .createdTs(request.getAudit().getCreatedTs())
                    .createdBy(request.getAudit().getCreatedBy())
                    .modifiedBy(reqHdrMap.get(CONST_USER_ID) != null ? reqHdrMap.get(CONST_USER_ID) : DEFAULT_USER)
                    .modifiedTs(scheduleStartDate)
                    .build();
            request.setAudit(audit);
        } else {
            audit = Audit.builder()
                    .createdTs(scheduleStartDate)
                    .createdBy(reqHdrMap.get(CONST_USER_ID) != null ? reqHdrMap.get(CONST_USER_ID) : DEFAULT_USER)
                    .build();
            request.setAudit(audit);
        }
    }


    /**
     * Process the questions, answer options and nested questions recursively.
     * <p>
     * This method takes a questions entity, answer options list and a list of questions.
     * It processes the questions and answer options recursively and returns a Mono containing a QuestionsData object.
     *
     * @param questionsEntity   the questions entity
     * @param answerOptionsList the answer options list
     * @param questions         the list of questions
     * @return a Mono containing a QuestionsData object
     */
    public Mono<Questions> processQuestionnaire(

            QuestionsEntity questionsEntity,
            List<AnswerOptionsEntity> answerOptionsList,
            List<QuestionsEntity> questions) {
        Questions question = Questions.builder()
                .actionId(questionsEntity.getActionId())
                .questionId(questionsEntity.getQuestionId())
                .text(questionsEntity.getQuestionText())
                .errorMessage(questionsEntity.getErrorMessage())
                .answerType(questionsEntity.getAnswerType())
                .stacked(questionsEntity.isStacked())
                .required(questionsEntity.isRequired())
                .helpText(questionsEntity.getHelpText() != null ? questionsEntity.getHelpText() : "")
                .characterLimit(questionsEntity.getCharacterLimit())
                .sequenceId(questionsEntity.getSequence_id() != null ? questionsEntity.getSequence_id() : 0)
                .linkText(questionsEntity.getLinkText() != null ? questionsEntity.getLinkText() : "")
                .skipLegend(questionsEntity.getSkiplegend()!= null ? questionsEntity.getSkiplegend() : "")
                .questionNumber(questionsEntity.getQuestionnumber() != null ? questionsEntity.getQuestionnumber() : 0)
                .subContext(questionsEntity.getSubcontext() != null ? questionsEntity.getSubcontext() : "")
                .answerOptionIds(questionsEntity.getAnswerOptionId() != null ? questionsEntity.getAnswerOptionId() : new ArrayList<>())
                .build();
        // Sort the answerOptionsList in ascending order
        answerOptionsList.sort(Comparator.comparing(AnswerOptionsEntity::getAnswerText));
        return Flux.fromIterable(answerOptionsList)
                .filter(answerOption ->
                        questionsEntity.getAnswerOptionId() != null &&
                                questionsEntity.getAnswerOptionId().contains(answerOption.getAnswerOptionId()))
                .flatMap(answerOption -> processAnswerOption(answerOption, answerOptionsList, questions))
                .collectList()
                .map(answerOptions -> {
                    answerOptions.sort(Comparator.comparing(ao -> ao.getSequenceId() != null ? ao.getSequenceId() : 0));
                    question.setAnswerOptions(answerOptions);
                    return question;
                })
                .onErrorResume(e -> {
                    log.info("Exception occurred in the method {} error : {}", PROCESS_DATA, e.getMessage());
                    return Mono.just(question);
                });
    }

    /**
     * Process a single answer option.
     * <p>
     * This method takes an answer option, answer options list and a list of questions.
     * It processes the answer option and its related questions recursively and returns a Mono containing an AnswerOptionsData object.
     *
     * @param answerOption      the answer option to process
     * @param answerOptionsList the answer options list
     * @param questions         the list of questions
     * @return a Mono containing an AnswerOptionsData object
     */
    public Mono<AnswerOptions> processAnswerOption(

            AnswerOptionsEntity answerOption,
            List<AnswerOptionsEntity> answerOptionsList,
            List<QuestionsEntity> questions) {
        AnswerOptions answerOptionsDTO = AnswerOptions.builder()
                .actionId(answerOption.getActionId())
                .questionId(answerOption.getQuestionId())
                .answerOptionId(answerOption.getAnswerOptionId())
                .text(answerOption.getAnswerText())
                .value(answerOption.getAnswerValue())
                .sequenceId(answerOption.getSequence_id() != null ? answerOption.getSequence_id() : 0)
                .relatedQuestionIds(answerOption.getRelatedQuestions())
                .additionalDetailText(answerOption.getAdditionalDetailText() != null ? answerOption.getAdditionalDetailText() : "")
                .build();
        return Flux.fromIterable(questions)
                .filter(questionsDTO -> answerOption.getRelatedQuestions() != null &&
                        answerOption.getRelatedQuestions().contains(questionsDTO.getQuestionId()))
                .flatMap(relatedQuestion -> processRelatedQuestion(relatedQuestion, answerOptionsList, questions))
                .collectList()
                .map(relatedQuestionsDTO -> {
                    if (!relatedQuestionsDTO.isEmpty()) {
                        // Sort the relatedQuestionsDTO by sequenceId
                        List<Questions> sortedRelatedQuestionsDTO =
                                relatedQuestionsDTO.stream()
                                        .sorted(Comparator.comparing(q -> q.getSequenceId() != null ? q.getSequenceId() : 0))
                                        .toList();
                        answerOptionsDTO.setRelatedQuestions(sortedRelatedQuestionsDTO);
                    }
                    return answerOptionsDTO;
                });
    }


    /**
     * Process a related question.
     * <p>
     * This method takes a related question and answer options list and a list of questions.
     * It processes the related question and its answer options recursively and returns a Mono containing a QuestionsData object.
     *
     * @param relatedQuestion   the related question to process
     * @param answerOptionsList the answer options list
     * @param questions         the list of questions
     * @return a Mono containing a QuestionsData object
     */
    public Mono<Questions> processRelatedQuestion(

            QuestionsEntity relatedQuestion,
            List<AnswerOptionsEntity> answerOptionsList,
            List<QuestionsEntity> questions) {
        Questions relatedQuestionDTO = Questions.builder()
                .actionId(relatedQuestion.getActionId())
                .questionId(relatedQuestion.getQuestionId())
                .text(relatedQuestion.getQuestionText())
                .errorMessage(relatedQuestion.getErrorMessage())
                .answerType(relatedQuestion.getAnswerType())
                .stacked(relatedQuestion.isStacked())
                .required(relatedQuestion.isRequired())
                .helpText(relatedQuestion.getHelpText() != null ? relatedQuestion.getHelpText() : "")
                .characterLimit(relatedQuestion.getCharacterLimit())
                .sequenceId(relatedQuestion.getSequence_id() != null ? relatedQuestion.getSequence_id() : 0)
                .linkText(relatedQuestion.getLinkText() != null ? relatedQuestion.getLinkText() : "")
                .skipLegend(relatedQuestion.getSkiplegend()!= null ? relatedQuestion.getSkiplegend() : "")
                .questionNumber(relatedQuestion.getQuestionnumber() != null ? relatedQuestion.getQuestionnumber() : 0)
                .subContext(relatedQuestion.getSubcontext() != null ? relatedQuestion.getSubcontext() : "")
                .answerOptionIds(relatedQuestion.getAnswerOptionId() != null ? relatedQuestion.getAnswerOptionId() : new ArrayList<>())
                .build();
        return Flux.fromIterable(answerOptionsList)
                .filter(relatedAnswerOption ->
                        relatedQuestion.getAnswerOptionId() != null &&
                                relatedQuestion.getAnswerOptionId().contains(relatedAnswerOption.getAnswerOptionId()))
                .flatMap(relatedAnswerOption -> processAnswerOption(relatedAnswerOption, answerOptionsList, questions))
                .collectList()
                .map(relatedAnswerOptions -> {
                    // Sort the relatedAnswerOptions by sequenceId
                    List<AnswerOptions> sortedRelatedAnswerOptions =
                            relatedAnswerOptions.stream()
                                    .sorted(Comparator.comparing(ao -> ao.getSequenceId() != null ? ao.getSequenceId() : 0))
                                    .toList();
                    relatedQuestionDTO.setAnswerOptions(sortedRelatedAnswerOptions);
                    return relatedQuestionDTO;
                });
    }


    /**
     * Assign sequenceIds to the questions and answer options in the questionare request.
     * <p>
     * This method takes a QuestionareRequest object and assigns sequenceIds to the questions and answer options in the object.
     * It returns a Mono containing a QuestionareRequest object with the sequenceIds assigned.
     *
     * @param request the QuestionareRequest object
     * @return a Mono containing a QuestionareRequest object
     */
    public Mono<QuestionareRequest> assignSequenceIds(QuestionareRequest request) {
        return Mono.just(request)
                .flatMap(req -> assignSequenceIds(req.getQuestions())
                        .map(questions -> {
                            req.setQuestions(questions);
                            log.info("Questionare request with sequenceIds assigned");
                            return req;
                        }).onErrorResume(e -> {
                            log.error("Error occurred while assigning sequenceIds", e);
                            return Mono.error(e);
                        }));
    }

    /**
     * Assign sequenceIds to the list of questions and their answer options.
     * This method takes a list of QuestionsData objects and assigns sequential
     * integers as sequenceIds to each question and their respective answer options.
     * The sequenceIds start from 1 for each list of questions or answer options.
     * For nested questions within answer options, the sequenceIds are assigned
     * recursively.
     *
     * @param questions the list of QuestionsData objects to which sequenceIds
     *                  are to be assigned
     * @return a Mono containing a list of QuestionsData objects with sequenceIds
     * assigned
     */
    private Mono<List<Questions>> assignSequenceIds(List<Questions> questions) {
        return Flux.fromIterable(questions)
                .flatMap(question -> Flux.just(question)
                        .map(questionEntity -> {
                            questionEntity.setSequenceId(questions.indexOf(question) + 1);
                            return questionEntity;
                        })
                        .flatMap(questionObject -> {
                            if (questionObject.getAnswerOptions() != null) {
                                return Flux.fromIterable(questionObject.getAnswerOptions())
                                        .flatMap(answerOption -> Flux.just(answerOption)
                                                .map(answerOptionsEntity -> {
                                                    answerOptionsEntity.setSequenceId(questionObject.getAnswerOptions().indexOf(answerOption) + 1);
                                                    return answerOptionsEntity;
                                                })
                                                .flatMap(answerOptionsDTO -> {
                                                    if (answerOptionsDTO.getRelatedQuestions() != null) {
                                                        return assignSequenceIds(answerOptionsDTO.getRelatedQuestions())
                                                                .thenReturn(answerOptionsDTO);
                                                    } else {
                                                        return Mono.just(answerOptionsDTO);
                                                    }
                                                }))
                                        .collectList()
                                        .map(answerOptions -> {
                                            questionObject.setAnswerOptions(answerOptions);
                                            return questionObject;
                                        });
                            } else {
                                return Mono.just(questionObject);
                            }
                        }))
                .collectList();
    }


}
