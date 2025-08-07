package com.cvshealth.digital.microservice.iqe.service;


import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.error.InvalidRequestException;
import com.cvshealth.digital.microservice.iqe.error.ResourceNotFoundException;
import com.cvshealth.digital.microservice.iqe.model.IQEResponse;
import com.cvshealth.digital.microservice.iqe.repository.RulesByFlowRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.*;


@Component

/** The Constant log. */
@Slf4j
@RequiredArgsConstructor
public class RulesServiceRepoOrchestrator {


    private final RulesByFlowRepository rulesByFlowRepo;


    /**
     * Validates the input request.
     *
     * This method validates the input request and
     * if any validation error is found, it throws a
     * ResponseStatusException with 400 status code.
     *
     * @param request The request to validate
     * @param iqeResponse The response object
     * @return The Mono object containing the validated request
     */
    public Mono<QuestionareRequest> validateRequest(QuestionareRequest request, IQEResponse iqeResponse) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        String methodName="validateRequest";
        Set<ConstraintViolation<QuestionareRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            log.info("Exception occurred in the method {}", methodName);
            return Mono.error(new InvalidRequestException(INVALID_INPUT, "Validation failed: " + violations));
        }
        return Mono.deferContextual(ctx -> {
            if (request.getRulesByFlow().isUpdate()) {
                return rulesByFlowRepo.findByFlowAndCondition(request.getRulesByFlow().getFlow(), request.getRulesByFlow().getCondition())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException(DATA_NOT_FOUND_CODE, DATA_NOT_FOUND_MESSAGE)))
                        .flatMap(hasElement -> Mono.just(request));
            } else {
                return rulesByFlowRepo.findByFlowAndCondition(request.getRulesByFlow().getFlow(), request.getRulesByFlow()
                                .getCondition())
                        .hasElement()
                        .flatMap(hasElement -> {
                            if (hasElement || ((request.getRulesByFlow().getActionId() !=null && !request.getRulesByFlow().getActionId().isEmpty()) ||
                                    (request.getActions().getActionId() !=null && !request.getActions().getActionId().isEmpty()))) {
                                log.info("Exception occurred in the method {}", methodName);
                                return Mono.error(new InvalidRequestException(DATA_ALREADY_EXISTS_CODE, DATA_ALREADY_EXISTS_MESSAGE));
                            }
                            else {
                                return Mono.just(request);
                            }
                        });
            }
        });
    }
}