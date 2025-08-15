package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.QuestionnaireContextEnum;
import com.cvshealth.digital.microservice.iqe.config.MessageConfig;
import com.cvshealth.digital.microservice.iqe.config.StateMinorAgeConfig;
import com.cvshealth.digital.microservice.iqe.dto.IQEMcCoreQuestionnarieRequest;
import com.cvshealth.digital.microservice.iqe.dto.ImzQuestionnarieResponse;
import com.cvshealth.digital.microservice.iqe.dto.QuestionnaireUIRequest;
import com.cvshealth.digital.microservice.iqe.dto.QuestionnaireUIResponse;
import com.cvshealth.digital.microservice.iqe.exception.CvsException;
import com.cvshealth.digital.microservice.iqe.mapper.DetailMapper;
import com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.EVC_B2B;
import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.MC_CORE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
@Service
@RequiredArgsConstructor
public class SchedulingService implements  ISchedulingService{
    private static final String MC_CORE_CONTEXT = "MC_CORE_CONTEXT";
    private static final String STATE_MA = "MA";
    private static final List<Integer> MA_REASON_IDS = Arrays.asList(1,2);
    private static final Integer REASON_ID_600000014 = 600000014;
    private static final String MHC_SCHEDULING_CONTEXT_EAP_QUESTION_ID = "MHD_D";
    private final MessageConfig getMessages;
   private final ValidatorSchedulingService validatorSchedulingService;
    private final IQEMcCoreQuestionnarieService iqeMcCoreQuestionnarieService;
    private List<QuestionnaireUIResponse.QuestionnaireData> mcCoreJson;
    private List<QuestionnaireUIResponse.QuestionnaireData> mhcLegalJson;
    private List<QuestionnaireUIResponse.QuestionnaireData> mcCoreJsonLegal;
    private List<QuestionnaireUIResponse.QuestionnaireData> mcCoreJsonScheduling;
    private List<QuestionnaireUIResponse.QuestionnaireData> mhcPsychiatryJsonScheduling;
    private Map<String, String> errorMessages;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResourceLoader resourceLoader;
    private final DetailMapper detailMapper;
    private final StateMinorAgeConfig stateMinorAgeConfig;
//    @PostConstruct
//    private void postConstruct() throws IOException {
//        errorMessages = getMessages.getMessages();
//
//        final String MHC_LEGAL_JSON = "classpath:questionnaire/mhc/mhc_legal.json";
//        final String MCCORE_LEGAL_JSON = "classpath:questionnaire/mccore/mc_legal.json";
//        final String MCCORE_WOUND_JSON = "classpath:questionnaire/mccore/mccore-wound.json";
//        final String MCCORE_SCHEDULING_JSON = "classpath:questionnaire/mccore/mc_scheduling.json";
//        final String MHC_PSYCHIATRY_SCHEDULING_JSON = "classpath:questionnaire/mhc/mhc_psychiatry_scheduling.json";
//
//        mhcLegalJson = loadJson(MHC_LEGAL_JSON, new TypeReference<>() {});
//        mcCoreJson = loadJson(MCCORE_WOUND_JSON, new TypeReference<>() {});
//        mcCoreJsonLegal = loadJson(MCCORE_LEGAL_JSON, new TypeReference<>() {});
//        mcCoreJsonScheduling = loadJson(MCCORE_SCHEDULING_JSON, new TypeReference<>() {});
//        mhcPsychiatryJsonScheduling = loadJson(MHC_PSYCHIATRY_SCHEDULING_JSON, new TypeReference<>() {});
//    }
//    private <T> T loadJson(String resourcePath, TypeReference<T> typeReference) throws IOException {
//        Resource resource = resourceLoader.getResource(resourcePath);
//
//        return objectMapper.readValue(resource.getContentAsString(StandardCharsets.UTF_8), typeReference);
//    }
    public Mono<QuestionnaireUIResponse.GetQuestionnaire> getIQEQuestionnaire(QuestionnaireUIRequest.ScheduleQuestionnaireInput questionnaireInput,
                                                                              QuestionnaireContextEnum context,
                                                                              Map<String, String> headerMap, Map<String, Object> eventMap) throws CvsException, IOException {
        validatorSchedulingService.validateMCCoreQuestionnaireInput(questionnaireInput);

        return Flux.fromIterable(questionnaireInput.getQuestionnaireDataInput())
                .flatMap(dataInput -> Flux.fromIterable(dataInput.getRequiredQuestionnaireContext())
                        .flatMap(con -> {
                            if (!con.equals(QuestionnaireContextEnum.MC_VACCINE_SCREENING_QUESTION)) {
                                int age = 0;
                                if (dataInput.getDateOfBirth() != null && !dataInput.getDateOfBirth().isEmpty()) {
                                    LocalDate dob = LocalDate.parse(dataInput.getDateOfBirth());
                                    age = Period.between(dob, LocalDate.now()).getYears();
                                }
                                IQEMcCoreQuestionnarieRequest iqeMcCoreQuestionnarieRequest = IQEMcCoreQuestionnarieRequest.builder()
                                        .flow(questionnaireInput.getFlow().toUpperCase())
                                        .requiredQuestionnaireContext(con.name())
                                        .reasonId(dataInput.getServices() != null && !dataInput.getServices().isEmpty() && dataInput.getServices().get(0) != null ? dataInput.getServices().get(0).getReasonId() : null)
                                        .reasonMappingId(dataInput.getServices() != null && !dataInput.getServices().isEmpty() && dataInput.getServices().get(0) != null && dataInput.getServices().get(0).getReasonMappingId() != null ? Integer.parseInt(dataInput.getServices().get(0).getReasonMappingId()) : null)
                                        .state(questionnaireInput.getState() != null ? questionnaireInput.getState().toUpperCase() : null)
                                        .modality(questionnaireInput.getModality() != null ? questionnaireInput.getModality().toUpperCase() : null)
                                        .build();
                                if (age != 0) {
                                    iqeMcCoreQuestionnarieRequest.setAge(age);
                                }
                                return iqeMcCoreQuestionnarieService.getIQEGetQuestions(iqeMcCoreQuestionnarieRequest, eventMap, headerMap)
                                        .map(resp -> {
                                            QuestionnaireUIResponse.QuestionnaireData questionnaireData = new QuestionnaireUIResponse.QuestionnaireData();
                                            if (resp.getQuestions() != null && !resp.getQuestions().isEmpty()) {
                                                processQuestions(resp.getQuestions());
                                                questionnaireData.setQuestions(resp.getQuestions());
                                                if (resp.getDetails() != null && !resp.getDetails().isEmpty()) {
                                                    questionnaireData.setDetails(detailMapper.toDetailList(resp.getDetails()));
                                                }
                                                questionnaireData.setContext(con.name());
                                                questionnaireData.setPatientReferenceId(dataInput.getPatientReferenceId());
                                                if (SchedulingConstants.FLOW_VM.equalsIgnoreCase(questionnaireInput.getFlow())) {
                                                    questionnaireData.setAppointmentId(questionnaireInput.getAppointmentId());
                                                }

                                            }
                                            return questionnaireData;
                                        });
                            } else {
                                try {
                                    return getIQEQuestionnaireIntakeContext(questionnaireInput, dataInput, con, headerMap, eventMap).map(k -> {
                                        if(k.getQuestionnaireData() != null && !k.getQuestionnaireData().isEmpty()) {
                                            return k.getQuestionnaireData().get(0);
                                        }
                                        return new QuestionnaireUIResponse.QuestionnaireData();
                                    });
                                } catch (CvsException | IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                        }))
                .collectList()
                .map(questionnaireDataList -> {
                    QuestionnaireUIResponse.GetQuestionnaire getQuestionnaire = new QuestionnaireUIResponse.GetQuestionnaire();
                    getQuestionnaire.setQuestionnaireData(
                            questionnaireDataList.stream()
                                    .filter(data -> data != null && data.getQuestions() != null && !data.getQuestions().isEmpty())
                                    .collect(Collectors.toList())
                    );
                    if (getQuestionnaire.getQuestionnaireData() == null || getQuestionnaire.getQuestionnaireData().isEmpty()) {
                        getQuestionnaire.setStatusDescription("No questions found");
                    } else {
                        getQuestionnaire.setStatusDescription("SUCCESS");
                    }
                    getQuestionnaire.setStatusCode("SUCCESS");
                    getQuestionnaire.setFlow(questionnaireInput.getFlow() != null ? questionnaireInput.getFlow() : "");
                    return getQuestionnaire;
                })
                .onErrorResume(Mono::error);

    }


    public static void processQuestions(List<QuestionnaireUIResponse.Question> questions) {
        for (QuestionnaireUIResponse.Question question : questions) {
            if (question.getQuestionId() != null && !question.getQuestionId().isEmpty()) {
                question.setId(question.getQuestionId());
            }

            processAnswerOptions(question.getAnswerOptions());
        }
    }



    private static QuestionnaireUIResponse.GetQuestionnaire getQuestionnaireUIResponse(List<QuestionnaireUIResponse.QuestionnaireData> questionnaireData, String... flow) {
        QuestionnaireUIResponse.GetQuestionnaire getQuestionnaire = new QuestionnaireUIResponse.GetQuestionnaire();
        getQuestionnaire.setStatusDescription("SUCCESS");
        getQuestionnaire.setStatusCode("SUCCESS");
        getQuestionnaire.setFlow(flow.length > 0 ? flow[0]: "");
        getQuestionnaire.setQuestionnaireData(questionnaireData);
        // Set patientReferenceId for each questionnaireData
        for (QuestionnaireUIResponse.QuestionnaireData data : questionnaireData) {
            String patientReferenceId = data.getPatientReferenceId();
            data.setPatientReferenceId(patientReferenceId);
        }
        return getQuestionnaire;
    }


    public Mono<QuestionnaireUIResponse.GetQuestionnaire> getMCCoreResponse(QuestionnaireUIRequest.ScheduleQuestionnaireInput questionnaireInput,
                                                                            QuestionnaireContextEnum context,
                                                                            Map<String, String> headerMap, Map<String, Object> eventMap) throws CvsException, IOException {
        validatorSchedulingService.validateMCCoreQuestionnaireInput(questionnaireInput);

        if(Objects.equals(context, QuestionnaireContextEnum.MC_CORE_ELIGIBILITY_QUESTION)) {
            IQEMcCoreQuestionnarieRequest iqeMcCoreQuestionnarieRequest = IQEMcCoreQuestionnarieRequest.builder()
                    .flow(MC_CORE)
                    .reasonId(questionnaireInput.getQuestionnaireDataInput().get(0).getServices().get(0).getReasonId())
                    .reasonMappingId(questionnaireInput.getQuestionnaireDataInput().get(0).getServices().get(0).getReasonMappingId() != null ? Integer.parseInt(questionnaireInput.getQuestionnaireDataInput().get(0).getServices().get(0).getReasonMappingId()) : null)
                    .requiredQuestionnaireContext(MC_CORE_CONTEXT)
                    .build();

            return iqeMcCoreQuestionnarieService.getIQEMcCoreQuestions(iqeMcCoreQuestionnarieRequest, eventMap, headerMap).map(resp -> {
                QuestionnaireUIResponse.GetQuestionnaire getQuestionnaire = new QuestionnaireUIResponse.GetQuestionnaire();
                QuestionnaireUIResponse.QuestionnaireData questionnaireData = new QuestionnaireUIResponse.QuestionnaireData();
                questionnaireData.setQuestions(resp.getQuestions());
                questionnaireData.setContext(MC_CORE_CONTEXT);
                questionnaireData.setPatientReferenceId(questionnaireInput.getQuestionnaireDataInput().get(0).getPatientReferenceId());                    getQuestionnaire.setQuestionnaireData(List.of(questionnaireData));
                return getQuestionnaire;
            });

        }
        else if(Objects.equals(context, QuestionnaireContextEnum.MHC_SCHEDULING_QUESTION)){
            List<QuestionnaireUIResponse.QuestionnaireData> questionList = new ArrayList<>(mcCoreJsonScheduling);
            if (questionnaireInput.getFlow().equals(EVC_B2B) && ((questionnaireInput.getState() != null && questionnaireInput.getState().equals(STATE_MA)) || (isNotEmpty(questionnaireInput.getQuestionnaireDataInput().get(0).getServices()) && MA_REASON_IDS.contains(questionnaireInput.getQuestionnaireDataInput().get(0).getServices().get(0).getReasonId())))){
                QuestionnaireUIResponse.QuestionnaireData questionnaireModel = new QuestionnaireUIResponse.QuestionnaireData();
                questionnaireModel.setContext(mcCoreJsonScheduling.get(0).getContext());
                questionnaireModel.setQuestions(new ArrayList<>(mcCoreJsonScheduling.get(0).getQuestions().stream().filter(question -> !question.getId().equals(MHC_SCHEDULING_CONTEXT_EAP_QUESTION_ID)).toList()));
                questionList.clear();
                questionList.add(questionnaireModel);
            }
            return Mono.just(
                    getQuestionnaireUIMCResponse(
                            questionnaireInput,
                            equalsIgnoreCase(questionnaireInput.getFlow(),EVC_B2B) && isNotEmpty(questionnaireInput.getQuestionnaireDataInput().get(0).getServices()) && questionnaireInput.getQuestionnaireDataInput().get(0).getServices().get(0).getReasonId().equals(REASON_ID_600000014) ? mhcPsychiatryJsonScheduling : questionList,
                            questionnaireInput.getFlow()
                    )
            );
        } else if (Objects.equals(context, QuestionnaireContextEnum.MC_LEGAL_QUESTION)) {
            return handleLegalQuestions(questionnaireInput, context, mcCoreJsonLegal);
        } else if (Objects.equals(context, QuestionnaireContextEnum.MHC_LEGAL_QUESTION)) {
            return handleLegalQuestions(questionnaireInput, context, mhcLegalJson);
        }
        else {
            return Mono.empty();
        }
    }




    public static void processAnswerOptions(List<ImzQuestionnarieResponse.AnswerOption> answerOptions) {
        for (ImzQuestionnarieResponse.AnswerOption answerOption : answerOptions) {
            if (answerOption.getRelatedQuestions() != null && !answerOption.getRelatedQuestions().isEmpty()) {
                answerOption.setRelatedQuestion(answerOption.getRelatedQuestions());
                answerOption.setRelatedQuestions(null);

                for (ImzQuestionnarieResponse.RelatedQuestions relatedQuestion : answerOption.getRelatedQuestion()) {
                    if (relatedQuestion.getQuestionId() != null && !relatedQuestion.getQuestionId().isEmpty()) {
                        relatedQuestion.setId(relatedQuestion.getQuestionId());
                    }

                    if (relatedQuestion.getAnswerOptions() != null && !relatedQuestion.getAnswerOptions().isEmpty()) {
                        processAnswerOptions(relatedQuestion.getAnswerOptions());
                    }
                }
            }
        }
    }




    private Mono<QuestionnaireUIResponse.GetQuestionnaire> handleLegalQuestions(QuestionnaireUIRequest.ScheduleQuestionnaireInput questionnaireInput, QuestionnaireContextEnum context, List<QuestionnaireUIResponse.QuestionnaireData> legalJson) {
        LocalDate dob = LocalDate.parse(questionnaireInput.getQuestionnaireDataInput().get(0).getDateOfBirth());
        String state = questionnaireInput.getState();
        boolean isMinor = isMinor(state, dob);
        QuestionnaireUIResponse.GetQuestionnaire questionnaire = new QuestionnaireUIResponse.GetQuestionnaire();
        questionnaire.setQuestionnaireData(new ArrayList<>());
        questionnaire.setStatusDescription("No questions found");
        questionnaire.setStatusCode("SUCCESS");
        if (isMinor || (stateMinorAgeConfig.getConfig().get(state) == null && Period.between(dob, LocalDate.now()).getYears() < 18)) {
            return Mono.just(getQuestionnaireUIMCResponse(questionnaireInput, legalJson, questionnaireInput.getFlow()));
        }
        return Mono.just(questionnaire);
    }
    private static QuestionnaireUIResponse.GetQuestionnaire getQuestionnaireUIMCResponse(QuestionnaireUIRequest.ScheduleQuestionnaireInput questionnaireInput,List<QuestionnaireUIResponse.QuestionnaireData> questionnaireData, String... flow) {
        QuestionnaireUIResponse.GetQuestionnaire getQuestionnaire = new QuestionnaireUIResponse.GetQuestionnaire();
        getQuestionnaire.setStatusDescription("SUCCESS");
        getQuestionnaire.setStatusCode("SUCCESS");
        getQuestionnaire.setFlow(flow.length > 0 ? flow[0]: "");
        getQuestionnaire.setQuestionnaireData(questionnaireData);
        for (int i = 0; i < questionnaireData.size(); i++) {
            String patientReferenceId = questionnaireInput.getQuestionnaireDataInput().get(i).getPatientReferenceId();
            questionnaireData.get(i).setPatientReferenceId(patientReferenceId);
        }
        return getQuestionnaire;
    }

    public boolean isMinor(String state, LocalDate dateOfBirth) {
        Integer minorAge = stateMinorAgeConfig.getConfig().get(state);
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (minorAge == null) {
            return age < 18;
        }
        return age < minorAge;
    }

    private static Set<String> findCommonLastElements(List<QuestionnaireUIResponse.QuestionnaireData> patientDataList) {
        if (patientDataList == null || patientDataList.isEmpty()) {
            return new HashSet<>();
        }
        
        Set<String> lastElementCandidates = null;
        
        for (QuestionnaireUIResponse.QuestionnaireData data : patientDataList) {
            if (data.getQuestions() != null && !data.getQuestions().isEmpty()) {
                List<QuestionnaireUIResponse.Question> questions = data.getQuestions();
                String lastQuestionId = questions.get(questions.size() - 1).getId();
                
                if (lastElementCandidates == null) {
                    lastElementCandidates = new HashSet<>();
                    lastElementCandidates.add(lastQuestionId);
                } else {
                    lastElementCandidates.retainAll(Set.of(lastQuestionId));
                }
            }
        }
        
        Set<String> positionalLastElements = lastElementCandidates != null ? lastElementCandidates : new HashSet<>();
        
        if (positionalLastElements.isEmpty()) {
            Set<String> textPatternElements = new HashSet<>();
            
            for (QuestionnaireUIResponse.QuestionnaireData data : patientDataList) {
                if (data.getQuestions() != null) {
                    for (QuestionnaireUIResponse.Question question : data.getQuestions()) {
                        if (question.getText() != null && 
                            question.getText().toLowerCase().contains("none of the statements apply")) {
                            textPatternElements.add(question.getId());
                        }
                    }
                }
            }
            
            return textPatternElements;
        }
        
        return positionalLastElements;
    }

public Mono<QuestionnaireUIResponse.GetQuestionnaire> getIQEQuestionnaireIntakeContext(QuestionnaireUIRequest.ScheduleQuestionnaireInput questionnaireInput,
                                                                                       QuestionnaireUIRequest.QuestionnaireDataInput dataInput,
                                                                                       QuestionnaireContextEnum context,
                                                                                       Map<String, String> headerMap, Map<String, Object> eventMap) throws CvsException, IOException {
    validatorSchedulingService.validateMCCoreQuestionnaireInput(questionnaireInput);

    return Flux.fromIterable(dataInput.getRequiredQuestionnaireContext())
            .flatMap(con -> {
                        if (context != null && context.equals(con)) {
                            return Flux.fromIterable(dataInput.getServices())
                                    .flatMap(service -> {
                                        int age = 0;
                                        if (dataInput.getDateOfBirth() != null && !dataInput.getDateOfBirth().isEmpty()) {
                                            LocalDate dob = LocalDate.parse(dataInput.getDateOfBirth());
                                            age = Period.between(dob, LocalDate.now()).getYears();
                                        }
                                        IQEMcCoreQuestionnarieRequest iqeMcCoreQuestionnarieRequest = IQEMcCoreQuestionnarieRequest.builder()
                                                .flow(questionnaireInput.getFlow().toUpperCase())
                                                .requiredQuestionnaireContext(con.name())
                                                .reasonId(service.getReasonId())
                                                .reasonMappingId(service.getReasonMappingId() != null ? Integer.parseInt(service.getReasonMappingId()) : null)
                                                .state(questionnaireInput.getState() != null ? questionnaireInput.getState().toUpperCase() : null)
                                                .build();
                                        if (age != 0) {
                                            iqeMcCoreQuestionnarieRequest.setAge(age);
                                        }
                                        return iqeMcCoreQuestionnarieService.getIQEGetQuestions(iqeMcCoreQuestionnarieRequest, eventMap, headerMap)
                                                .map(resp -> {
                                                    QuestionnaireUIResponse.QuestionnaireData questionnaireData = new QuestionnaireUIResponse.QuestionnaireData();
                                                    if (resp.getQuestions() != null && !resp.getQuestions().isEmpty()) {
                                                        processQuestions(resp.getQuestions());

                                                        questionnaireData.setQuestions(resp.getQuestions());
                                                        if (resp.getDetails() != null && !resp.getDetails().isEmpty()) {
                                                            questionnaireData.setDetails(detailMapper.toDetailList(resp.getDetails()));
                                                        }
                                                        questionnaireData.setContext(con.name());
                                                        questionnaireData.setPatientReferenceId(dataInput.getPatientReferenceId());
                                                        if (SchedulingConstants.VACCINE.equalsIgnoreCase(questionnaireInput.getFlow())) {
                                                            questionnaireData.setAppointmentId(questionnaireInput.getAppointmentId());
                                                        }
                                                    }
                                                    return questionnaireData;
                                                });
                                    });
                        } else {
                            return Mono.just(new QuestionnaireUIResponse.QuestionnaireData());
                        }
                    }
            )
            .collectList()
            .map(questionnaireDataList -> {
                QuestionnaireUIResponse.GetQuestionnaire getQuestionnaire = new QuestionnaireUIResponse.GetQuestionnaire();
                List<QuestionnaireUIResponse.QuestionnaireData> filteredList = questionnaireDataList.stream()
                        .filter(data -> data != null && data.getQuestions() != null && !data.getQuestions().isEmpty())
                        .collect(Collectors.groupingBy(QuestionnaireUIResponse.QuestionnaireData::getPatientReferenceId))
                        .values().stream()
                        .map(patientDataList -> {
                            Map<String, QuestionnaireUIResponse.Question> mergedMap = new LinkedHashMap<>();
                            List<QuestionnaireUIResponse.Question> combinedQuestions = new ArrayList<>();
                            for (QuestionnaireUIResponse.QuestionnaireData questionnaireData : patientDataList) {
                                combinedQuestions.addAll(questionnaireData.getQuestions());
                            }
                            
                            Set<String> commonLastElements = findCommonLastElements(patientDataList);
                            
                            QuestionnaireUIResponse.QuestionnaireData mergedData = new QuestionnaireUIResponse.QuestionnaireData();
                            if(!patientDataList.isEmpty()) {
                                for (QuestionnaireUIResponse.Question question : combinedQuestions) {
                                    if (!mergedMap.containsKey(question.getId()) || (mergedMap.containsKey(question.getId()) && mergedMap.get(question.getId()).getSequenceId() > question.getSequenceId())) {
                                        mergedMap.put(question.getId(), question);
                                    }
                                }

                                List<QuestionnaireUIResponse.Question> mergedQuestions = new ArrayList<>(mergedMap.values());
                                
                                List<QuestionnaireUIResponse.Question> regularQuestions = new ArrayList<>();
                                List<QuestionnaireUIResponse.Question> lastElementQuestions = new ArrayList<>();
                                
                                for (QuestionnaireUIResponse.Question question : mergedQuestions) {
                                    if (commonLastElements.contains(question.getId())) {
                                        lastElementQuestions.add(question);
                                    } else {
                                        regularQuestions.add(question);
                                    }
                                }
                                
                                regularQuestions.sort(Comparator.comparingInt(QuestionnaireUIResponse.Question::getSequenceId));
                                lastElementQuestions.sort(Comparator.comparingInt(QuestionnaireUIResponse.Question::getSequenceId));
                                
                                List<QuestionnaireUIResponse.Question> finalQuestions = new ArrayList<>();
                                finalQuestions.addAll(regularQuestions);
                                finalQuestions.addAll(lastElementQuestions);
                                
                                mergedData = patientDataList.get(0);
                                mergedData.setQuestions(finalQuestions);
                            }
                            return mergedData;
                        })
                        .toList();

                getQuestionnaire.setQuestionnaireData(filteredList);
                if (getQuestionnaire.getQuestionnaireData() == null || getQuestionnaire.getQuestionnaireData().isEmpty()) {
                    getQuestionnaire.setStatusDescription("No questions found");
                } else {
                    getQuestionnaire.setStatusDescription("SUCCESS");
                }
                getQuestionnaire.setStatusCode("SUCCESS");
                getQuestionnaire.setFlow(questionnaireInput.getFlow() != null ? questionnaireInput.getFlow() : "");
                return getQuestionnaire;
            })
            .onErrorResume(Mono::error);
}
}
