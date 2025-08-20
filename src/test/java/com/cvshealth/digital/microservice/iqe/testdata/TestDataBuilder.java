package com.cvshealth.digital.microservice.iqe.testdata;

import com.cvshealth.digital.microservice.iqe.entity.ActionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.AnswerOptionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.model.GetConsentInput;
import com.cvshealth.digital.microservice.iqe.dto.QuestionnaireUIRequest;
import com.cvshealth.digital.microservice.iqe.dto.QuestionnaireUIResponse;
import com.cvshealth.digital.microservice.iqe.dto.MCITGetPatientConsentsResponse;
import com.cvshealth.digital.microservice.iqe.dto.IQEMcCoreQuestionnarieResponse;
import com.cvshealth.digital.microservice.iqe.dto.IQEDetail;
import com.cvshealth.digital.microservice.iqe.dto.RelatedQuestionsRequest;
import com.cvshealth.digital.microservice.iqe.dto.Questions;
import com.cvshealth.digital.microservice.iqe.dto.AnswerOptions;
import com.cvshealth.digital.microservice.iqe.model.GetConsent;
import com.cvshealth.digital.microservice.iqe.model.ConsentData;
import com.cvshealth.digital.microservice.iqe.config.ConsentConfig;
import com.cvshealth.digital.microservice.iqe.udt.AuditEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Component
public class TestDataBuilder {
    
    public static ActionsEntity createTestAction() {
        return ActionsEntity.builder()
                .actionId("test-action-id")
                .actionText("Test Action")
                .questionId(List.of("question-1", "question-2"))
                .detailId(List.of("detail-1"))
                .build();
    }
    
    public static ActionsEntity createTestActionWithId(String actionId) {
        return ActionsEntity.builder()
                .actionId(actionId)
                .actionText("Test Action for " + actionId)
                .questionId(List.of("question-1", "question-2"))
                .detailId(List.of("detail-1"))
                .build();
    }
    
    public static QuestionsEntity createTestQuestion() {
        return QuestionsEntity.builder()
                .actionId("test-action-id")
                .questionId("question-1")
                .questionText("Test Question?")
                .answerType("radio")
                .answerOptionId(List.of("option-1", "option-2"))
                .sequence_id(1)
                .required(true)
                .helpText("Test help text")
                .characterLimit(100)
                .stacked(false)
                .build();
    }
    
    public static QuestionsEntity createTestQuestionWithIds(String actionId, String questionId) {
        return QuestionsEntity.builder()
                .actionId(actionId)
                .questionId(questionId)
                .questionText("Test Question for " + questionId + "?")
                .answerType("radio")
                .answerOptionId(List.of("option-1", "option-2"))
                .sequence_id(1)
                .required(true)
                .helpText("Test help text")
                .characterLimit(100)
                .stacked(false)
                .build();
    }
    
    public static AnswerOptionsEntity createTestAnswerOption() {
        return AnswerOptionsEntity.builder()
                .actionId("test-action-id")
                .questionId("question-1")
                .answerOptionId("option-1")
                .answerText("Test Option")
                .answerValue("test-value")
                .sequence_id(1)
                .relatedQuestions(List.of("question-2"))
                .build();
    }
    
    public static AnswerOptionsEntity createTestAnswerOptionWithIds(String actionId, String questionId, String optionId) {
        return AnswerOptionsEntity.builder()
                .actionId(actionId)
                .questionId(questionId)
                .answerOptionId(optionId)
                .answerText("Test Option " + optionId)
                .answerValue("test-value-" + optionId)
                .sequence_id(1)
                .relatedQuestions(List.of())
                .build();
    }
    
    public static RulesByFlowEntity createTestRule() {
        return RulesByFlowEntity.builder()
                .flow("test-flow")
                .ruleId("rule-1")
                .ruleName("Test Rule")
                .actionId("test-action-id")
                .condition("requiredQuestionnaireContext==\"test\"")
                .salience(100)
                .lob("TEST")
                .isActive(true)
                .audit(createTestAudit())
                .build();
    }
    
    public static RulesByFlowEntity createTestRuleWithFlow(String flow) {
        return RulesByFlowEntity.builder()
                .flow(flow)
                .ruleId("rule-1")
                .ruleName("Test Rule for " + flow)
                .actionId("test-action-id")
                .condition("requiredQuestionnaireContext==\"test\"")
                .salience(100)
                .lob("TEST")
                .isActive(true)
                .audit(createTestAudit())
                .build();
    }
    
    public static AuditEntity createTestAudit() {
        return AuditEntity.builder()
                .createdTs(LocalDateTime.now().toString())
                .createdBy("test-user")
                .modifiedTs(LocalDateTime.now().toString())
                .modifiedBy("test-user")
                .build();
    }
    
    public static RulesDetails createTestRulesDetails() {
        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow("test-flow");
        rulesDetails.setRequiredQuestionnaireContext("test");
        return rulesDetails;
    }
    
    public static RulesDetails createTestRulesDetailsWithFlow(String flow) {
        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow(flow);
        rulesDetails.setRequiredQuestionnaireContext("test");
        return rulesDetails;
    }
    
    public static GetConsentInput createTestConsentInput() {
        GetConsentInput input = new GetConsentInput();
        input.setFlow("VACCINE");
        input.setLob("CLINIC");
        input.setModality("BnMInPerson");
        input.setBrand("MC");
        input.setState("CA");
        input.setAuthType("GUEST");
        
        GetConsentInput.ConsentDataInput dataInput = new GetConsentInput.ConsentDataInput();
        dataInput.setEncMCPatientId("encrypted-patient-id");
        dataInput.setDateOfBirth("1990-01-01");
        dataInput.setPatientReferenceId("patient-ref-1");
        dataInput.setConsentContext(List.of("Review", "StateRegistry"));
        
        input.setConsentsDataInput(List.of(dataInput));
        return input;
    }
    
    public static QuestionnaireUIRequest.ScheduleQuestionnaireInput createTestQuestionnaireInput() {
        QuestionnaireUIRequest.ScheduleQuestionnaireInput input = new QuestionnaireUIRequest.ScheduleQuestionnaireInput();
        input.setFlow("TEST_TREAT");
        input.setState("CA");
        input.setAppointmentId("appt-123");
        input.setModality("BnMInPerson");
        
        QuestionnaireUIRequest.QuestionnaireDataInput dataInput = new QuestionnaireUIRequest.QuestionnaireDataInput();
        dataInput.setPatientReferenceId("patient-ref-1");
        dataInput.setDateOfBirth("1990-01-01");
        
        QuestionnaireUIRequest.Services service = new QuestionnaireUIRequest.Services();
        service.setReasonId(123);
        service.setReasonMappingId("456");
        dataInput.setServices(List.of(service));
        
        dataInput.setRequiredQuestionnaireContext(List.of(com.cvshealth.digital.microservice.iqe.QuestionnaireContextEnum.KNOWLEDGE_CHECK));
        
        input.setQuestionnaireDataInput(List.of(dataInput));
        return input;
    }
    
    public static MCITGetPatientConsentsResponse createMockMcitResponse() {
        MCITGetPatientConsentsResponse.Response responseData = new MCITGetPatientConsentsResponse.Response();
        MCITGetPatientConsentsResponse.StatusRec statusRec = new MCITGetPatientConsentsResponse.StatusRec();
        statusRec.setStatusCode(0);
        statusRec.setStatusDesc("Success");
        responseData.setStatusRec(statusRec);
        
        MCITGetPatientConsentsResponse.GetPatientConsent2021Response consentResponse = 
            new MCITGetPatientConsentsResponse.GetPatientConsent2021Response();
        consentResponse.setConsentAcknowledgementList(new ArrayList<>());
        responseData.setGetPatientConsent2021Response(consentResponse);
        
        return MCITGetPatientConsentsResponse.builder()
                .response(responseData)
                .build();
    }
    
    public static Map<String, List<ConsentConfig>> createMockConsentConfig() {
        ConsentConfig config = new ConsentConfig();
        config.setLob("CLINIC");
        config.setModality("BnMInPerson");
        config.setBrand("MC");
        config.setConsents(new ArrayList<>());
        
        return Map.of("VACCINE", List.of(config));
    }
    
    public static RelatedQuestionsRequest createTestRelatedQuestionsRequest() {
        RelatedQuestionsRequest request = new RelatedQuestionsRequest();
        Questions question = new Questions();
        question.setActionId("test-action-id");
        question.setQuestionId("question-1");
        request.setRelatedQuestions(List.of(question));
        return request;
    }
    
    public static Questions createTestQuestionDto() {
        Questions question = new Questions();
        question.setActionId("test-action-id");
        question.setQuestionId("question-1");
        question.setText("Test Question?");
        question.setAnswerType("radio");
        question.setSequenceId(1);
        question.setRequired(true);
        
        AnswerOptions option = new AnswerOptions();
        option.setActionId("test-action-id");
        option.setQuestionId("question-1");
        option.setAnswerOptionId("option-1");
        option.setText("Test Option");
        option.setValue("test-value");
        option.setSequenceId(1);
        
        question.setAnswerOptions(List.of(option));
        return question;
    }
    
    public static QuestionnaireUIResponse.GetQuestionnaire createMockQuestionnaireResponse() {
        QuestionnaireUIResponse.GetQuestionnaire response = new QuestionnaireUIResponse.GetQuestionnaire();
        response.setStatusCode("SUCCESS");
        response.setStatusDescription("SUCCESS");
        response.setFlow("TEST_TREAT");
        response.setQuestionnaireData(new ArrayList<>());
        return response;
    }
    
    public static GetConsent createMockConsentResponse() {
        GetConsent response = new GetConsent();
        response.setStatusCode("SUCCESS");
        response.setStatusDescription("SUCCESS");
        response.setConsentsData(new ArrayList<>());
        return response;
    }
    
    public static IQEMcCoreQuestionnarieResponse createMockMcCoreResponse() {
        IQEMcCoreQuestionnarieResponse response = new IQEMcCoreQuestionnarieResponse();
        response.setQuestions(new ArrayList<>());
        response.setDetails(new ArrayList<>());
        return response;
    }
    
    public static ConsentConfig.Consent createMockFilteredConsent() {
        ConsentConfig.Consent consent = new ConsentConfig.Consent();
        consent.setConsentName("Test Consent");
        consent.setText("Test consent text");
        consent.setConsentContext("REVIEW");
        consent.setRequired(true);
        ConsentConfig.ConsentDetailsInfo detailsInfo = ConsentConfig.ConsentDetailsInfo.builder()
                .type("combined")
                .consents(java.util.List.of())
                .build();
        consent.setConsent(detailsInfo);
        return consent;
    }
    
    public static ConsentData createMockConsentData() {
        ConsentData consentData = new ConsentData();
        consentData.setPatientReferenceId("patient-ref-1");
        return consentData;
    }
    
    public static com.cvshealth.digital.microservice.iqe.entity.QuestionnaireRules createTestQuestionnaireRule() {
        com.cvshealth.digital.microservice.iqe.entity.QuestionnaireRules rule = new com.cvshealth.digital.microservice.iqe.entity.QuestionnaireRules();
        rule.setFlow("test-flow");
        rule.setId("rule-1");
        rule.setRuleName("Test Questionnaire Rule");
        rule.setAction("\"{\\\"questions\\\":[{\\\"questionId\\\":\\\"test-question-1\\\",\\\"text\\\":\\\"Test Question\\\",\\\"answerType\\\":\\\"radio\\\",\\\"required\\\":true}]}\"");
        rule.setCondition("requiredQuestionnaireContext==\"testAndTreatMedications\"");
        rule.setSalience(100);
        rule.setLob("TEST");
        return rule;
    }
    
    public static com.cvshealth.digital.microservice.iqe.dto.RulesByFlow createTestRulesByFlow() {
        com.cvshealth.digital.microservice.iqe.dto.RulesByFlow rulesByFlow = new com.cvshealth.digital.microservice.iqe.dto.RulesByFlow();
        rulesByFlow.setFlow("test-flow");
        rulesByFlow.setRuleId("rule-1");
        rulesByFlow.setRuleName("Test Rule");
        rulesByFlow.setActionId("test-action-id");
        rulesByFlow.setCondition("requiredQuestionnaireContext==\"test\"");
        rulesByFlow.setSalience(100);
        rulesByFlow.setLob("TEST");
        rulesByFlow.setActive(true);
        
        com.cvshealth.digital.microservice.iqe.dto.Audit audit = new com.cvshealth.digital.microservice.iqe.dto.Audit();
        audit.setCreatedTs(LocalDateTime.now().toString());
        audit.setCreatedBy("test-user");
        audit.setModifiedTs(LocalDateTime.now().toString());
        audit.setModifiedBy("test-user");
        rulesByFlow.setAudit(audit);
        
        return rulesByFlow;
    }
    
    public static com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest createTestQuestionareRequest() {
        com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest request = new com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest();
        request.setQuestions(List.of(createTestQuestionDto()));
        request.setActiveRules(List.of(createTestRule()));
        request.setInactiveRules(List.of());
        
        request.setRulesByFlow(createTestRulesByFlow());
        
        com.cvshealth.digital.microservice.iqe.dto.Actions actions = new com.cvshealth.digital.microservice.iqe.dto.Actions();
        actions.setActionId("test-action-id");
        actions.setActionText("Test Action");
        actions.setQuestionIds(List.of("question-1", "question-2"));
        actions.setDetailIds(List.of("detail-1"));
        
        request.setActions(actions);
        
        request.setDetails(List.of(createTestDetail()));
        
        return request;
    }
    
    public static com.cvshealth.digital.microservice.iqe.dto.AnswerOptions createTestAnswerOptionDTO() {
        com.cvshealth.digital.microservice.iqe.dto.AnswerOptions option = new com.cvshealth.digital.microservice.iqe.dto.AnswerOptions();
        option.setActionId("test-action-id");
        option.setQuestionId("question-1");
        option.setAnswerOptionId("option-1");
        option.setText("Test Option");
        option.setValue("test-value");
        option.setSequenceId(1);
        return option;
    }
    
    public static com.cvshealth.digital.microservice.iqe.dto.Details createTestDetail() {
        com.cvshealth.digital.microservice.iqe.dto.Details detail = new com.cvshealth.digital.microservice.iqe.dto.Details();
        detail.setDetailId("detail-1");
        detail.setActionId("test-action-id");
        detail.setTitle("Test Detail");
        detail.setSequenceId(1);
        detail.setInstructions("Test instructions");
        detail.setHelper("Test helper");
        return detail;
    }
    
    public static com.cvshealth.digital.microservice.iqe.entity.QuestionsDetailsEntity createTestQuestionnaireDetail() {
        return com.cvshealth.digital.microservice.iqe.entity.QuestionsDetailsEntity.builder()
                .actionId("test-action-id")
                .detailId("detail-1")
                .title("Test Detail")
                .sequenceId(1)
                .instructions("Test instructions")
                .helper("Test helper")
                .build();
    }
    
    public static com.cvshealth.digital.microservice.iqe.model.Questions createTestQuestionsModel() {
        com.cvshealth.digital.microservice.iqe.model.Questions questions = new com.cvshealth.digital.microservice.iqe.model.Questions();
        // Questions is a wrapper class that contains List<Question> questions
        questions.setQuestions(List.of());
        return questions;
    }
}
