package com.cvshealth.digital.microservice.iqe.controller;

import com.cvshealth.digital.microservice.iqe.entity.ActionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.AnswerOptionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionsDetailsEntity;
import com.cvshealth.digital.microservice.iqe.repository.ActionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.AnswerOptionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionnaireDetailsRepository;
import com.cvshealth.digital.microservice.iqe.service.AuditingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule/iqe/v1/audit")
@Slf4j
@CrossOrigin
public class AuditController {

    private final ActionsRepository actionsRepository;
    private final QuestionsRepository questionsRepository;
    private final AnswerOptionsRepository answerOptionsRepository;
    private final QuestionnaireDetailsRepository questionnaireDetailsRepository;
    private final AuditingService auditingService;

    @Operation(summary = "Soft delete action", description = "Marks an action as inactive instead of deleting it")
    @DeleteMapping("/actions/{actionId}")
    public Mono<ResponseEntity<Void>> softDeleteAction(
            @PathVariable String actionId,
            @RequestHeader(value = "user_id", required = false, defaultValue = "system") String userId,
            @RequestHeader(value = "change_reason", required = false, defaultValue = "API_DELETE") String reason) {
        
        return actionsRepository.softDeleteByActionId(actionId, 
                auditingService.createAuditForNew(userId, reason))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Restore action", description = "Marks an inactive action as active")
    @PutMapping("/actions/{actionId}/restore")
    public Mono<ResponseEntity<Void>> restoreAction(
            @PathVariable String actionId,
            @RequestHeader(value = "user_id", required = false, defaultValue = "system") String userId,
            @RequestHeader(value = "change_reason", required = false, defaultValue = "API_RESTORE") String reason) {
        
        return actionsRepository.restoreByActionId(actionId,
                auditingService.createAuditForNew(userId, reason))
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Soft delete question", description = "Marks a question as inactive instead of deleting it")
    @DeleteMapping("/questions/{actionId}/{questionId}")
    public Mono<ResponseEntity<Void>> softDeleteQuestion(
            @PathVariable String actionId,
            @PathVariable String questionId,
            @RequestHeader(value = "user_id", required = false, defaultValue = "system") String userId,
            @RequestHeader(value = "change_reason", required = false, defaultValue = "API_DELETE") String reason) {
        
        return questionsRepository.softDeleteByActionIdAndQuestionId(actionId, questionId,
                auditingService.createAuditForNew(userId, reason))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Restore question", description = "Marks an inactive question as active")
    @PutMapping("/questions/{actionId}/{questionId}/restore")
    public Mono<ResponseEntity<Void>> restoreQuestion(
            @PathVariable String actionId,
            @PathVariable String questionId,
            @RequestHeader(value = "user_id", required = false, defaultValue = "system") String userId,
            @RequestHeader(value = "change_reason", required = false, defaultValue = "API_RESTORE") String reason) {
        
        return questionsRepository.restoreByActionIdAndQuestionId(actionId, questionId,
                auditingService.createAuditForNew(userId, reason))
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
    
    @Operation(summary = "Soft delete answer option", description = "Marks an answer option as inactive instead of deleting it")
    @DeleteMapping("/answer-options/{actionId}/{questionId}/{answerOptionId}")
    public Mono<ResponseEntity<Void>> softDeleteAnswerOption(
            @PathVariable String actionId,
            @PathVariable String questionId,
            @PathVariable String answerOptionId,
            @RequestHeader(value = "user_id", required = false, defaultValue = "system") String userId,
            @RequestHeader(value = "change_reason", required = false, defaultValue = "API_DELETE") String reason) {
        
        return answerOptionsRepository.softDeleteByActionIdAndQuestionIdAndAnswerOptionId(
                actionId, questionId, answerOptionId, auditingService.createAuditForNew(userId, reason))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
    
    @Operation(summary = "Restore answer option", description = "Marks an inactive answer option as active")
    @PutMapping("/answer-options/{actionId}/{questionId}/{answerOptionId}/restore")
    public Mono<ResponseEntity<Void>> restoreAnswerOption(
            @PathVariable String actionId,
            @PathVariable String questionId,
            @PathVariable String answerOptionId,
            @RequestHeader(value = "user_id", required = false, defaultValue = "system") String userId,
            @RequestHeader(value = "change_reason", required = false, defaultValue = "API_RESTORE") String reason) {
        
        return answerOptionsRepository.restoreByActionIdAndQuestionIdAndAnswerOptionId(
                actionId, questionId, answerOptionId, auditingService.createAuditForNew(userId, reason))
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
    
    @Operation(summary = "Soft delete question detail", description = "Marks a question detail as inactive instead of deleting it")
    @DeleteMapping("/question-details/{actionId}/{detailId}")
    public Mono<ResponseEntity<Void>> softDeleteQuestionDetail(
            @PathVariable String actionId,
            @PathVariable String detailId,
            @RequestHeader(value = "user_id", required = false, defaultValue = "system") String userId,
            @RequestHeader(value = "change_reason", required = false, defaultValue = "API_DELETE") String reason) {
        
        return questionnaireDetailsRepository.softDeleteByActionIdAndDetailId(
                actionId, detailId, auditingService.createAuditForNew(userId, reason))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
    
    @Operation(summary = "Restore question detail", description = "Marks an inactive question detail as active")
    @PutMapping("/question-details/{actionId}/{detailId}/restore")
    public Mono<ResponseEntity<Void>> restoreQuestionDetail(
            @PathVariable String actionId,
            @PathVariable String detailId,
            @RequestHeader(value = "user_id", required = false, defaultValue = "system") String userId,
            @RequestHeader(value = "change_reason", required = false, defaultValue = "API_RESTORE") String reason) {
        
        return questionnaireDetailsRepository.restoreByActionIdAndDetailId(
                actionId, detailId, auditingService.createAuditForNew(userId, reason))
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}
