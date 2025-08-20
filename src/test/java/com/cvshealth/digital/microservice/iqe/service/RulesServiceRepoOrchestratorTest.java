package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.dto.RulesByFlow;
import com.cvshealth.digital.microservice.iqe.dto.Actions;
import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import com.cvshealth.digital.microservice.iqe.error.InvalidRequestException;
import com.cvshealth.digital.microservice.iqe.error.ResourceNotFoundException;
import com.cvshealth.digital.microservice.iqe.model.IQEResponse;
import com.cvshealth.digital.microservice.iqe.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RulesServiceRepoOrchestratorTest {

    @Mock
    private RulesByFlowRepository rulesByFlowRepo;

    @InjectMocks
    private RulesServiceRepoOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private QuestionareRequest buildRequest(boolean isUpdate, boolean hasActionId, boolean hasActionsActionId) {
        RulesByFlow rulesByFlow = new RulesByFlow();
        rulesByFlow.setUpdate(isUpdate);
        rulesByFlow.setFlow("flow");
        rulesByFlow.setCondition("cond");
        if (hasActionId) rulesByFlow.setActionId("A1");
        Actions actions = new Actions();
        if (hasActionsActionId) actions.setActionId("A2");
        QuestionareRequest req = new QuestionareRequest();
        req.setRulesByFlow(rulesByFlow);
        req.setActions(actions);
        return req;
    }

    @Test
    void validateRequest_shouldReturnError_onValidationFailure() {
        // Provide an invalid request (e.g., missing required fields)
        QuestionareRequest req = new QuestionareRequest();
        IQEResponse resp = new IQEResponse();
        StepVerifier.create(orchestrator.validateRequest(req, resp))
                .expectError(InvalidRequestException.class)
                .verify();
    }



    @Test
    void validateRequest_shouldReturnError_whenNotUpdateAndHasElementOrActionId() {
        // hasElement = true
        QuestionareRequest req = buildRequest(false, false, false);
        IQEResponse resp = new IQEResponse();
        when(rulesByFlowRepo.findByFlowAndCondition(any(), any()))
                .thenReturn(Mono.just(new RulesByFlowEntity()));
        StepVerifier.create(orchestrator.validateRequest(req, resp))
                .expectError(InvalidRequestException.class)
                .verify();

        // hasElement = false, but actionId present
        req = buildRequest(false, true, false);
        when(rulesByFlowRepo.findByFlowAndCondition(any(), any()))
                .thenReturn(Mono.empty());
        StepVerifier.create(orchestrator.validateRequest(req, resp))
                .expectError(InvalidRequestException.class)
                .verify();

        // hasElement = false, but actions.actionId present
        req = buildRequest(false, false, true);
        when(rulesByFlowRepo.findByFlowAndCondition(any(), any()))
                .thenReturn(Mono.empty());
        StepVerifier.create(orchestrator.validateRequest(req, resp))
                .expectError(InvalidRequestException.class)
                .verify();
    }

}