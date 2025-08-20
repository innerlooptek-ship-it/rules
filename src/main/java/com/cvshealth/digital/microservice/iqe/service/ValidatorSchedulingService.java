package com.cvshealth.digital.microservice.iqe.service;


import com.cvshealth.digital.microservice.iqe.QuestionnaireContextEnum;
import com.cvshealth.digital.microservice.iqe.config.ConsentRelationEnum;
import com.cvshealth.digital.microservice.iqe.config.MessageConfig;
import com.cvshealth.digital.microservice.iqe.constants.DhsCoreConstants;
import com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants;
import com.cvshealth.digital.microservice.iqe.dto.QuestionnaireUIRequest;
import com.cvshealth.digital.microservice.iqe.dto.SchedulingConsentInput;
import com.cvshealth.digital.microservice.iqe.dto.VaccineInput;
import com.cvshealth.digital.microservice.iqe.enums.*;
import com.cvshealth.digital.microservice.iqe.exception.CvsException;
import com.cvshealth.digital.microservice.iqe.utils.DateUtil;
import com.cvshealth.digital.microservice.iqe.utils.ValidationUtils;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.GenericValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;


@Service
@RequiredArgsConstructor
public class ValidatorSchedulingService {

    private final String className = this.getClass().getName();

    private final MessageConfig messagesConfig;
    private final ValidationUtils validationUtils;

    List<String> validOperations = Arrays.asList("update", "submit", "commit");


    public void validateQuestionnaireInput(QuestionnaireUIRequest.ScheduleQuestionnaireInput questionnaireInput, Map<String, Object> tags) throws CvsException {
       Map<String, String> messageConfigs = messagesConfig.getMessages();

       for (QuestionnaireUIRequest.QuestionnaireDataInput questionnaireDataInput : questionnaireInput.getQuestionnaireDataInput()) {
          if (CollectionUtils.isEmpty(questionnaireDataInput.getRequiredQuestionnaireContext())) {
             throw new CvsException(HttpStatus.BAD_REQUEST.value(),
                    "CONTEXT_IS_MANDATORY", messageConfigs.get("getQuestionnarie.CONTEXT_IS_MANDATORY"), messageConfigs.get("getQuestionnarie.CONTEXT_IS_MANDATORY"), SchedulingConstants.ERROR_BAD_REQUEST);
          }
          for (QuestionnaireContextEnum questionnaireContextEnum : questionnaireDataInput.getRequiredQuestionnaireContext()) {
             switch (questionnaireContextEnum) {
                case IMZ_ELIGIBILITY_QUESTION -> {
                   if (org.apache.commons.lang3.StringUtils.isBlank(questionnaireDataInput.getDateOfBirth()) || !GenericValidator.isDate(questionnaireDataInput.getDateOfBirth(), "yyyy-MM-dd", true)) {
                      throw new CvsException(HttpStatus.BAD_REQUEST.value(),
                             "DOB_MANDATORY", messageConfigs.get("getQuestionnarie.DOB_MANDATORY"), messageConfigs.get("getQuestionnarie.DOB_MANDATORY"), SchedulingConstants.ERROR_BAD_REQUEST);
                   }
                   for (VaccineInput vaccine : questionnaireDataInput.getVaccines()) {
                      if (org.apache.commons.lang3.StringUtils.isBlank(vaccine.getCode())) {
                         throw new CvsException(HttpStatus.BAD_REQUEST.value(),
                               "VACCINE_CODE_IS_MANDATORY", messageConfigs.get("getQuestionnarie.VACCINE_CODE_IS_MANDATORY"), messageConfigs.get("getQuestionnarie.VACCINE_CODE_IS_MANDATORY"), SchedulingConstants.ERROR_BAD_REQUEST);
                      }
                   }
                   // Age Check for group appointment
                   if(questionnaireDataInput.getDateOfBirth()!=null
                         && DateUtil.calculateAge(questionnaireDataInput.getDateOfBirth()) < 12) {
                      tags.put("isSkipProductAssignment",true);
                      if(questionnaireDataInput.getCategory()!=null && questionnaireDataInput.getCategory().size()==1
                            && questionnaireDataInput.getCategory().contains("PRODUCT_ASSIGNMENT")){
                         tags.put("isSkipQuestionnaire",true);
                      }
                   }
                }
                case IMZ_SCREENING_QUESTION -> {
                   if (org.apache.commons.lang3.StringUtils.isBlank(questionnaireInput.getStoreId())
                         || org.apache.commons.lang3.StringUtils.isBlank(questionnaireDataInput.getDateOfBirth())
                         || !GenericValidator.isDate(questionnaireDataInput.getDateOfBirth(), "yyyy-MM-dd", true)) {
                      throw new CvsException(HttpStatus.BAD_REQUEST.value(),
                            "FACILITY_IS_MANDATORY", messageConfigs.get("getQuestionnarie.FACILITY_IS_MANDATORY"), messageConfigs.get("getQuestionnarie.FACILITY_IS_MANDATORY"), SchedulingConstants.ERROR_BAD_REQUEST);
                   }
                   for (VaccineInput vaccine : questionnaireDataInput.getVaccines()) {
                      if (org.apache.commons.lang3.StringUtils.isBlank(vaccine.getCode()) || CollectionUtils.isEmpty(vaccine.getNdc())) {
                         throw new CvsException(HttpStatus.BAD_REQUEST.value(),
                               "VACCINE_CODE_IS_MANDATORY", messageConfigs.get("getQuestionnarie.VACCINE_CODE_IS_MANDATORY"), messageConfigs.get("getQuestionnarie.VACCINE_CODE_IS_MANDATORY"), SchedulingConstants.ERROR_BAD_REQUEST);
                      }
                   }
                }
                case IMZ_LEGAL_QUESTION -> {
                   if (org.apache.commons.lang3.StringUtils.isBlank(questionnaireInput.getStoreId())
                         || org.apache.commons.lang3.StringUtils.isBlank(questionnaireDataInput.getDateOfBirth())
                         || !GenericValidator.isDate(questionnaireDataInput.getDateOfBirth(), "yyyy-MM-dd", true)) {
                      throw new CvsException(HttpStatus.BAD_REQUEST.value(),
                             "DOB_MANDATORY", messageConfigs.get("getQuestionnarie.DOB_MANDATORY"), messageConfigs.get("getQuestionnarie.DOB_MANDATORY"), SchedulingConstants.ERROR_BAD_REQUEST);
                   }
                }
             }


          }
       }
    }
    public void validateMCCoreQuestionnaireInput(QuestionnaireUIRequest.ScheduleQuestionnaireInput questionnaireInput) throws CvsException {
       Map<String, String> messageConfigs = messagesConfig.getMessages();
       for (QuestionnaireUIRequest.QuestionnaireDataInput questionnaireDataInput : questionnaireInput.getQuestionnaireDataInput()) {
          if (CollectionUtils.isEmpty(questionnaireDataInput.getRequiredQuestionnaireContext())) {
             throw new CvsException(HttpStatus.BAD_REQUEST.value(),
                   "CONTEXT_IS_MANDATORY", messageConfigs.get("getQuestionnarie.CONTEXT_IS_MANDATORY"), messageConfigs.get("getQuestionnarie.CONTEXT_IS_MANDATORY"), SchedulingConstants.ERROR_BAD_REQUEST);
          }
       }
    }







    /**
     * Validates the consent input parameters for setting visitor management consents.
     *
     * @param schedulingConsentInput The input object containing consent information to validate
     * @param eventMap Map containing event tracking information for error reporting
     *
     * @throws CvsException with the error key "SET_VM_CONSENTS.MISSING_APPOINTMENT_ID" if the
     *         appointment ID is null or empty
     *
     * @apiNote This method ensures that a valid appointment ID is provided before
     *          proceeding with consent operations in the visitor management system
     *
     * @implNote Uses ValidationUtils.validateString internally to check for null or empty values,
     *          with ErrorKey.SET_VM_CONSENTS as the base error key
     */
    public void validateSetConsent(SchedulingConsentInput schedulingConsentInput, Map<String, Object> eventMap) throws CvsException {
       validationUtils.validateString(schedulingConsentInput.getAppointmentId(), "MISSING_APPOINTMENT_ID", ErrorKey.SET_VM_CONSENTS, eventMap);
    }




}
