package com.cvshealth.digital.microservice.iqe.service;


import com.cvshealth.digital.microservice.iqe.QuestionnaireContextEnum;
import com.cvshealth.digital.microservice.iqe.dto.QuestionnaireUIRequest;
import com.cvshealth.digital.microservice.iqe.dto.QuestionnaireUIResponse;
import com.cvshealth.digital.microservice.iqe.exception.CvsException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ISchedulingService {


    Mono<QuestionnaireUIResponse.GetQuestionnaire> getMCCoreResponse(QuestionnaireUIRequest.ScheduleQuestionnaireInput questionnaireInput, QuestionnaireContextEnum questionnaireContextEnum, Map<String, String> headerMap, Map<String, Object> eventMap) throws CvsException, IOException;


    Mono<QuestionnaireUIResponse.GetQuestionnaire> getIQEQuestionnaire(QuestionnaireUIRequest.ScheduleQuestionnaireInput questionnaireInput, QuestionnaireContextEnum questionnaireContextEnum, Map<String, String> headerMap, Map<String, Object> eventMap) throws CvsException, IOException;
    Mono<QuestionnaireUIResponse.GetQuestionnaire> getIQEQuestionnaireIntakeContext(QuestionnaireUIRequest.ScheduleQuestionnaireInput questionnaireInput, QuestionnaireUIRequest.QuestionnaireDataInput dataInput, QuestionnaireContextEnum questionnaireContextEnum, Map<String, String> headerMap, Map<String, Object> eventMap) throws CvsException, IOException;

}