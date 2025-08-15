package com.cvshealth.digital.microservice.iqe.service;


import com.cvshealth.digital.microservice.iqe.QuestionnaireContextEnum;
import com.cvshealth.digital.microservice.iqe.config.MessageConfig;
import com.cvshealth.digital.microservice.iqe.dto.QuestionnaireUIRequest;
import com.cvshealth.digital.microservice.iqe.dto.VaccineInput;
import com.cvshealth.digital.microservice.iqe.exception.CvsException;
import com.cvshealth.digital.microservice.iqe.udt.SchedulingConstants;
import com.cvshealth.digital.microservice.iqe.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.ValidationUtils;
import org.apache.commons.validator.GenericValidator;
import java.util.*;

import static java.util.Collections.singletonList;

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


//
//	/**
//	 * Validates the operation type for questionnaire mutations.
//	 *
//	 * @param questionnaireInput The input containing questionnaire operation details
//	 * @param eventMap Tracking an information map for error reporting
//	 *
//	 * @throws CvsException with the error key "saveQuestionnaire.INVALID_OPERATION_TYPE"
//	 *         when the operation type is not among the valid operations
//	 *
//	 * @apiNote Operation type validation is case-insensitive
//	 */
//	public void validateQuestionnaireMutation(SchedulingQuestionnaireInput questionnaireInput, Map<String, Object> eventMap) throws CvsException {
//
//		if (!validOperations.contains(questionnaireInput.getOperation().toLowerCase()))
//			validationUtils.throwValidationException("INVALID_OPERATION_TYPE", ErrorKey.SAVE_QUESTIONNAIRE, eventMap);
//	}

//	/**
//	 * Validates the input parameters for canceling an appointment.
//	 * Performs validation checks on the Line of Business (LOB) and vaccine IDs when applicable.
//	 *
//	 * @param cancelAppointmentInput The input object containing cancellation details
//	 * @param eventMap Map for storing event tracking information
//	 * @throws CvsException in the following cases:
//	 *         - If LOB is null, empty, or not a valid enum value
//	 *         - If LOB is RxIMZ and vaccineId list is null or empty,
//	 *         The specific error messages are determined by the error key constants:
//	 *         - ERROR_INVALID_LOB: When LOB validation fails
//	 *         - "MISSING_VACCINE_ID": When vaccine IDs are required but missing
//	 *
//	 * @implNote
//	 * - For RxIMZ (immunization) appointments, vaccine IDs are mandatory
//	 * - LOB values are validated against the {@link LobEnum} enumeration
//	 * - Validation is delegated to {@link ValidationUtils} for string and enum checks
//	 */
//	public void validateCancelAppointment (CancelAppointmentInput cancelAppointmentInput, Map<String, Object> eventMap) throws CvsException {
//
//		String lob = cancelAppointmentInput.getLob();
//		List<String> vaccineIdList = cancelAppointmentInput.getVaccineId();
//
//		validationUtils.validateString(lob, ERROR_INVALID_LOB, ErrorKey.CANCEL, eventMap);
//		validationUtils.validateEnumValues(singletonList(lob), LobEnum.class, ERROR_INVALID_LOB, ErrorKey.CANCEL, eventMap);
//
//		if (lob.equals(LobEnum.RxIMZ.name()))
//			validationUtils.validateCollection(vaccineIdList, "MISSING_VACCINE_ID", ErrorKey.CANCEL, eventMap);
//	}
//
//	/**
//	 * Validates the input parameters for retrieving visitor management consents.
//	 *
//	 * @param consentInput The input object containing consent retrieval parameters to validate
//	 * @param eventMap Map containing event tracking information for error reporting
//	 *
//	 * @throws CvsException with error key "GET_VM_CONSENTS" in the following cases:
//	 *         - MISSING_APPOINTMENT_ID: if appointment ID is null or empty
//	 *         - MISSING_FLOW: if flow value is null or empty
//	 *         - INVALID_FLOW: if flow value is not "VM" (case-insensitive)
//	 *         - MISSING_CONSENT_DATA_INPUT: if a consent data input list is null or empty
//	 *         - MISSING_PATIENT_REFERENCE_ID: if any consent data input is missing patient reference ID
//	 *         - MISSING_CONSENT_CONTEXT: if any consent data input is missing consent context
//	 *
//	 * @apiNote This method performs comprehensive validation of consent retrieval parameters
//	 *          including appointment ID, flow type, and consent data inputs. The flow must
//	 *          specifically be "VM" (Visitor Management) to be valid.
//	 *
//	 * @implNote Uses ValidationUtils for string validation and list property validation.
//	 *          Performs additional custom validation for the flow value to ensure it matches
//	 *          the required "VM" value. List validation ensures all consent data entries
//	 *          have required fields populated.
//	 */
//	public void validateGetConsent(GetConsentInput consentInput, Map<String, Object> eventMap) throws CvsException {
//
//		validationUtils.validateString(consentInput.getAppointmentId(), "MISSING_APPOINTMENT_ID", ErrorKey.GET_VM_CONSENTS, eventMap);
//		validationUtils.validateString(consentInput.getFlow(), "MISSING_FLOW", ErrorKey.GET_VM_CONSENTS, eventMap);
//
//		if(!consentInput.getFlow().equalsIgnoreCase("VM"))
//			validationUtils.throwValidationException("INVALID_FLOW", ErrorKey.GET_VM_CONSENTS, eventMap);
//
//		List<GetConsentInput.ConsentDataInput> consentDataInput = consentInput.getConsentsDataInput();
//
//		if(CollectionUtils.isEmpty(consentDataInput))
//			validationUtils.throwValidationException("MISSING_CONSENT_DATA_INPUT", ErrorKey.GET_VM_CONSENTS, eventMap);
//
//		validationUtils.validatePropertyInList(consentDataInput, GetConsentInput.ConsentDataInput::getPatientReferenceId, "MISSING_PATIENT_REFERENCE_ID", ErrorKey.GET_VM_CONSENTS, eventMap);
//		validationUtils.validatePropertyInList(consentDataInput, GetConsentInput.ConsentDataInput::getConsentContext, "MISSING_CONSENT_CONTEXT", ErrorKey.GET_VM_CONSENTS, eventMap);
//	}
//
//	/**
//	 * Validates the consent input parameters for setting visitor management consents.
//	 *
//	 * @param schedulingConsentInput The input object containing consent information to validate
//	 * @param eventMap Map containing event tracking information for error reporting
//	 *
//	 * @throws CvsException with the error key "SET_VM_CONSENTS.MISSING_APPOINTMENT_ID" if the
//	 *         appointment ID is null or empty
//	 *
//	 * @apiNote This method ensures that a valid appointment ID is provided before
//	 *          proceeding with consent operations in the visitor management system
//	 *
//	 * @implNote Uses ValidationUtils.validateString internally to check for null or empty values,
//	 *          with ErrorKey.SET_VM_CONSENTS as the base error key
//	 */
//	public void validateSetConsent(SchedulingConsentInput schedulingConsentInput, Map<String, Object> eventMap) throws CvsException {
//		validationUtils.validateString(schedulingConsentInput.getAppointmentId(), "MISSING_APPOINTMENT_ID", ErrorKey.SET_VM_CONSENTS, eventMap);
//	}
//
//
//	public void validateGetConsentsForScheduling(GetConsentInput consentInput, Map<String, Object> eventMap) throws CvsException {
//
//		validationUtils.validateString(consentInput.getFlow(), "MISSING_FLOW", ErrorKey.GET_CONSENTS, eventMap);
//
//		if(!consentInput.getFlow().equalsIgnoreCase("vaccine"))
//			validationUtils.throwValidationException("INVALID_FLOW", ErrorKey.GET_CONSENTS, eventMap);
//
//		if(StringUtils.isNotBlank(consentInput.getLob()))
//			validationUtils.validateEnumValues(List.of(consentInput.getLob().toUpperCase()), LobEnum.class , "INVALID_LOB", ErrorKey.GET_CONSENTS, eventMap);
//		if(StringUtils.isNotBlank(consentInput.getBrand()))
//			validationUtils.validateEnumValues(List.of(consentInput.getBrand().toUpperCase()), BrandEnum.class , "INVALID_BRAND", ErrorKey.GET_CONSENTS, eventMap);
//		if(StringUtils.isNotBlank(consentInput.getModality()))
//			validationUtils.validateEnumValues(List.of(consentInput.getModality()), ModalityEnum.class , "INVALID_MODALITY", ErrorKey.GET_CONSENTS, eventMap);
//		if(StringUtils.isNotBlank(consentInput.getState()))
//			validationUtils.validateEnumValues(List.of(consentInput.getState().toUpperCase()), StatesEnum.class , "INVALID_STATE", ErrorKey.GET_CONSENTS, eventMap);
//		if(StringUtils.isNotBlank(consentInput.getAuthType()))
//			validationUtils.validateEnumValues(List.of(consentInput.getAuthType().toUpperCase()), AuthTypeEnum.class , "INVALID_AUTH_TYPE", ErrorKey.GET_CONSENTS, eventMap);
//
//		List<GetConsentInput.ConsentDataInput> consentDataInput = consentInput.getConsentsDataInput();
//
//		if(CollectionUtils.isEmpty(consentDataInput))
//			validationUtils.throwValidationException("MISSING_CONSENT_DATA_INPUT", ErrorKey.GET_CONSENTS, eventMap);
//		validationUtils.validatePropertyInList(consentDataInput, GetConsentInput.ConsentDataInput::getPatientReferenceId, "MISSING_PATIENT_REFERENCE_ID", ErrorKey.GET_CONSENTS, eventMap);
//		validationUtils.validatePropertyInList(consentDataInput, GetConsentInput.ConsentDataInput::getDateOfBirth, "MISSING_DATE_OF_BIRTH", ErrorKey.GET_CONSENTS, eventMap);
//		validationUtils.validatePropertyInList(consentDataInput, GetConsentInput.ConsentDataInput::getConsentContext, "MISSING_CONSENT_CONTEXT", ErrorKey.GET_CONSENTS, eventMap);
//		validationUtils.validateEnumValues(
//				consentDataInput.stream()
//						.flatMap(input -> input.getConsentContext().stream()).toList() , ConsentContextEnum.class,
//				"INVALID_CONSENT_CONTEXT",
//				ErrorKey.GET_CONSENTS,
//				eventMap
//		);
//		validationUtils.validateEnumValuesByMappedValue(
//				consentDataInput.stream()
//						.map(GetConsentInput.ConsentDataInput::getRelation)
//						.filter(Objects::nonNull)
//						.toList(),
//				ConsentRelationEnum.class,
//				"INVALID_RELATION",
//				ErrorKey.GET_CONSENTS,
//				eventMap
//		);
//
//		validationUtils.validateDateFormatInList(
//				consentDataInput.stream()
//						.map(GetConsentInput.ConsentDataInput::getDateOfBirth)
//						.filter(Objects::nonNull)
//						.toList(),
//				DHSSchedulingServiceConstants.YYYY_MM_DD,
//				"INVALID_DATE_OF_BIRTH_FORMAT",
//				ErrorKey.GET_CONSENTS,
//				eventMap
//		);
//	}
//
//	/**
//	 * Validates the input parameters for all visit codes requests.
//	 *
//	 * @param allVisitCodesInput The input object containing flow and LOB (Line of Business) information to validate
//	 * @param eventMap Map containing event tracking information for error reporting
//	 *
//	 * @throws CvsException if:
//	 *         - Flow ID is not a valid value in the Flow enum (with error key "INVALID_FLOW")
//	 *         - LOB is not a valid value in the LobType enum (with error key "INVALID_LOB")
//	 *
//	 * @apiNote This method performs enum validation for both the flow ID and LOB values
//	 *          using the ValidationUtils helper, ensuring they match predefined valid values
//	 *          in their respective enums.
//	 *
//	 * @implNote Uses ValidationUtils.validateEnumValues internally with ErrorKey.ALL_VISIT_CODES
//	 *          as the base error key for both validations
//	 */
//	public void validateAllVisitCodes(AllVisitCodesInput allVisitCodesInput, Map<String, Object> eventMap) throws CvsException {
//		validationUtils.validateEnumValues(allVisitCodesInput.getFlowId(), Flow.class, "INVALID_FLOW", ErrorKey.ALL_VISIT_CODES, eventMap);
//		validationUtils.validateEnumValues(allVisitCodesInput.getLob(), LobType.class, "INVALID_LOB", ErrorKey.ALL_VISIT_CODES, eventMap);
//	}
}