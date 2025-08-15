package com.cvshealth.digital.microservice.iqe.udt;


import com.datastax.oss.driver.shaded.guava.common.base.Ascii;

public interface SchedulingConstants {

	String CACHE_FLUSHED = "Cache-Flushed";
	int BAD_REQUEST = 400;
    String MC_CORE = "MC_CORE";
	int INTERNAL_SERVER_ERROR = 500;
	String  UPCOMING_APPT_XID_RESP_TIME = "UpcomingAppointmentsXidResponseTime";
    String  VM_GET_APPT_DETAILS_RESP_TIME = "VmGetAppointmentDetailsResponseTime";
	String SUCCESS_CODE = "0000";
	String SUCCESS_MSG = "SUCCESS";
	String RESP_TIME = "respTime";
	String CACHE_FOUND = "cacheFound";
	String STATUS_CDE = "statusCde";
	String FAILURE_MSG = "FAILURE";
	String FAILURE_CD = "9999";
	String STATUS_MESSAGE = "statusMessage";
	String ADDITIONAL_STATUS_MESSAGE = "additionalStatus";
	String STATUS_DESC = "statusDescription";
	String AVAILABLE_VACCINE_MESSAGE = "availableVaccines";
	String UNAVAILABLE_VACCINE_MESSAGE = "unAvailableVaccines";
	String HTTP_STATUS_CDE = "httpStatusCode";
	String LOGGED_IN = "loggedIn";
	String GUEST = "guest";
	String WARNING_STATUS_CODE = "0001";
	String WARNING_MSG = "WARNING";
	String GENERIC_ERROR_CD="9999";
	String CONST_GRID = "grid";
	String X_CAT = "x-cat";
	String CONST_X_GRID = "x-grid";
	String CONST_EXP_ID = "x-experienceid";
	String CONST_EXP = "experienceId";
	String CONST_SRC_LOC_CD = "src_loc_cd";
	String CONST_MSG_SRC_CD = "msg_src_cd";
	String CONST_ORIGIN = "origin";
	String CONST_REQ_ORIGIN = "req_origin";
	String CONST_USER_ID = "user_id";
	String CONST_CATEGORY = "cat";
	String CONST_X_CATEGORY = "x-cat";
	String CONST_DEFAULT_CATEGORY = "NGS";
	String CONST_NGS_DASHBOARD_CATEGORY = "NGS_DBD";
    String CONST_NGS_VISIT_MANGER = "NGS_VM";
	String CONST_CHAN_PLAT = "chPlat";
	String CONST_APP_NAME = "appName";
	String CONST_DEVICE_TYPE = "deviceType";
	String CONST_REQ_APP_VERSION = "AppVersion";
	String CONST_APP_VERSION = "appVersion";
	String CONST_CLIENT_IP = "clientIP";
	String CONST_AKAMAI_CLIENT_IP = "cvs-akclient-ip";
	String CONST_X_B3_PARENTSPANID = "x_b3_parentspanid";
	String CONST_X_B3_SAMPLED = "x_b3_sampled";
	String CONST_X_B3_SPANID = "x_b3_spanid";
	String CONST_X_B3_TRACEID = "x_b3_traceid";
	String CONST_ENV = "env";
	String CONST_USER_AGENT = "user-agent";
	String CONST_REFERER = "referer";
	String PATIENT_DATA_KEY = "patientData";
	String PAYMENT_MODE_DATA_KEY = "paymentModeData";
	String PAYMENT_DATA_KEY = "paymentData";
	String SCHEDULING_DATA_KEY = "schedulingData";
	String INSURANCE_DATA_KEY = "insuranceData";
	String QUESTIONNAIRE_DATA_KEY = "questionnaireData";
	String CONSENT_DATA_KEY = "consentData";
	String ADDITIONAL_DATA_KEY = "additionalData";
	String VM_CONSENT_DATA_KEY = "vmConsentData";
	String VM_QUESTIONNAIRE_DATA_KEY = "vmQuestionnaireData";
	String DUPLICATE_APPOINTMENT_DATA_KEY = "duplicateCheckData";
	String DUPLICATE_CHECK_DATA_KEY = "duplicateCheckData";
	String XID_DATA_KEY = "xidData";
	String XID_AUTH = "XID_AUTH";
	String VACCINE = "VACCINE";
	String PATIENT_DATA_FOUND = "patientDataFound";
	String PAYMENT_MODE_DATA_FOUND = "paymentModeDataFound";
	String SCHEDULING_DATA_FOUND = "schedulingDataFound";
	String INSURANCE_DATA_FOUND = "insuranceDataFound";
	String QUESTIONNAIRE_DATA_FOUND = "questionnaireDataFound";
	String CONSENT_DATA_FOUND = "consentDataFound";
	String ADDITIONAL_DATA_FOUND = "additionalDataFound";
	String CLINIC = "CLINIC";
	// Added new constants for TEST_TREAT
	String RX_IMZ = "RxIMZ";
	String RX_ERP = "RxERP";
	String TEST_TREAT ="TEST_TREAT";
	String TEST ="TEST";
	String MHC = "MHC";
	String EVC_B2B = "EVC_B2B";
	String RxERP = "RxERP";
	String JSON_PROCESSING_EXCEPTION = "jsonProcessingException";
	// Soft Reservation Constants
	String  SOFT_RESERVATION_RESERVE_METHOD_NAME="reserveSlot";
	String  SOFT_RESERVATION_SERVICE_NAME="softReservation";
	String  SOFT_RESERVATION_RESERVE_SERVICE_DESC="This service is used to soft reserve slot for Pharmacy and Minute Clinic locations";
	String  SOFT_RESERVATION_RELEASE_METHOD_NAME="releaseSlot";
	String  SOFT_RESERVATION_RELEASE_SERVICE_DESC="This service is used to soft release slot for Pharmacy and Minute Clinic locations";
	String  PHARMACY_RESERVE_SLOT_RESP_TIME = "pharmacyReserveSlotResponseTime";
	String  PHARMACY_RELEASE_SLOT_RESP_TIME = "pharmacyReleaseSlotResponseTime";
	String  MC_RESERVE_SLOT_RESP_TIME = "mcReserveSlotResponseTime";
	String  MC_RELEASE_SLOT_RESP_TIME = "mcReleaseSlotResponseTime";
	String  MC_RESERVE_DATE_RES_FROM_PATTERN = "MM/dd/yyyy HH:mm:ss";
	String  MC_RESERVE_DATE_RES_TO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	int MC_EXTEND_SLOT_COUNT = 0;
	String  APPLICATION_NAME = "Digital";
	String  USER_ID = "Digital";

	String  DIGITAL_SCHEDULE_TYPE = "Digital";
	String  RESERVE_DATE_REQ_FROM_PATTERN = "yyyy-MM-dd hh:mm a";
	String TIME_HOUR_FORMAT ="h:mm a";
	String  MC_RESERVE_DATE_REQ_TO_PATTERN = "MM/dd/yyyy HH:mm";
	String  OPERATION_TYPE_ADD = "add";
	String  OPERATION_TYPE_UPDATE = "update";
	String  OPERATION_TYPE_DELETE = "delete";
	String  OPERATION_TYPE_UPSERT = "upsert";
	String  LOB_TYPE_IMZ = "RxIMZ";
	String  LOB_TYPE_MC = "CLINIC";
	String  LOB_TYPE_MHC = "MHC";
	String  LOB_TYPE_RxERP = "RxERP";
	int  MC_SUCCESS_CODE = 200;
	int NOT_FOUND = 404;
	int UNPROCESSABLE_ENTITY = 422;
	int CONFLICT = 409;
	int SERVICE_UNAVAILABLE = 503;
	String NOT_FOUND_CODE = "5013";
	String BAD_REQUEST_CODE="5004";
	String NOT_ELIGIBLE_CODE = "9998";
	int   REASON_ID_22 = 22;
	int   REASON_ID_800000064 = 800000064;
	int   REASON_ID_800000053 = 800000053;
	//soft Reservation Error Keys

	String CB_STATUS_CD = "FAILED_DEPENDENCY";
	String CB_SOFT_RESERVATION_STATUS_DESC = "Soft Reservation Process did not complete";
	String IMZ_RESERVE_SLOT_ERROR = "IMZ_RESERVE_SLOT_ERROR";
	String IMZ_SOURCE_IN_STORE_CLINIC= "instore-clinic";
	String IMZ_FLOW_STORE_AUTH = "store_auth";
	String NO_RESERVATION_CODES_FOUND_IN_CACHE = "NO_RESERVATION_CODES_FOUND_IN_CACHE";
	String IMZ_RELEASE_SLOT_ERROR = "IMZ_RELEASE_SLOT_ERROR";
	String INVALID_REQUEST = "INVALID_REQUEST";
	String RESERVATION_NOT_FOUND = "RESERVATION_NOT_FOUND";
	String PHARMACY_RELEASE_SLOT_NOT_IN_RESERVED = "PHARMACY_RELEASE_SLOT_NOT_IN_RESERVED";
	String MC_RESERVE_RESERVATION_CANNOT_BE_EXTENDED = "MC_RESERVE_RESERVATION_CANNOT_BE_EXTENDED";
	String MC_RELEASE_RESERVATION_CANNOT_BE_RELEASED = "MC_RELEASE_RESERVATION_CANNOT_BE_RELEASED";
	String RESERVATION_EXPIRED = "RESERVATION_EXPIRED";
	String SLOT_NOT_AVAILABLE = "SLOT_NOT_AVAILABLE";
	String PHARMACY_RESERVE_SLOT_IN_PAST = "PHARMACY_RESERVE_SLOT_IN_PAST";
	String MISSING_CLINIC_ID = "MISSING_CLINIC_ID";
	String INVALID_TIME_FORMAT = "INVALID_TIME_FORMAT";
	String INVALID_DATE_FORMAT = "INVALID_DATE_FORMAT";
	String MISSING_CLINIC_TYPE = "MISSING_CLINIC_TYPE";
	String MISSING_APT_DATE = "MISSING_APT_DATE";
	String MISSING_APT_TIME = "MISSING_APT_TIME";
	String MISSING_APT_DURATION = "MISSING_APT_DURATION";
	String INVALID_USER_LOCATION = "INVALID_USER_LOCATION";
	String MISSING_REASON_FOR_VISIT = "MISSING_REASON_FOR_VISIT";
	String MISSING_RESERVATION_CODE = "MISSING_RESERVATION_CODE";
	String MISSING_LOB = "MISSING_LOB";
	String INVALID_MODALITY = "INVALID_MODALITY";
	String  SERVICE_NAME_CONFIRM_APPT = "confirmAppointment";
	String  SERVICE_DESC_CONFIRM_APPT = "This service is used to confirm pharmacy and clinic appointment.";
	String  SERVICE_NAME_CONSENT_APPT = "schedulingConsent";
	String  SERVICE_NAME_PATIENT_DETAILS = "patientDetails";
	String  SERVICE_DESC_PATIENT_DETAILS = "This service is used to add and update patient details in cache.";
	String  SERVICE_NAME_RESCHEDULE_APPT = "rescheduleAppointment";
	String  SERVICE_DESC_RESCHEDULE_APPT = "This service is used to reschedule pharmacy and clinic appointment.";
	String  SERVICE_NAME_CANCEL_APPOINTMENT = "cancelAppointment";
	String  SERVICE_DESC_CANCEL_APPOINTMENT = "This service is used to cancel an appointment.";
	String  SERVICE_NAME_UPCOMING_APPT_XID = "upcomingAppointmentsForXid";
	String  SERVICE_DESC_UPCOMING_APPT_XID = "This service is used to get appointment details for an xid";
	String  SERVICE_NAME_VERIFY_XID = "verifyXid";
	String  SERVICE_NAME_GET_PREFERRED_PHARMACY = "getPreferredPharmacy";

	String  SERVICE_NAME_SET_PREFERRED_PHARMACY = "setPreferredPharmacy";
	String  SERVICE_DESC_VERIFY_XID = "This service is used to fetch confirmation number using xid.";
	String  SERVICE_NAME_ALL_VISIT_CODES = "allVisitCodes";
	String  SERVICE_DESC_ALL_VISIT_CODES = "This service is used to fetch all visit codes.";

	String  SERVICE_NAME_ONE_CLICK_LOCATOR = "oneClickLocator";
	String  SERVICE_DESC_ONE_CLICK_LOCATOR = "This service is used to fetch latest past appointment and set same details in cache and fetch location details.";
	String ERROR_APPOINTMENT_LOOKUP_FAILED = "APPOINTMENT_LOOKUP_FAILED";
	String ERROR_XID_NOT_FOUND = "XID_NOT_FOUND";
	String ERROR_CANCEL_NOT_ALLOWED = "CANCEL_NOT_ALLOWED";
	String  CLASSNAME = "className";
	String  SERVICE_NAME = "serviceName";
	String  METHOD_NAME = "methodName";
	String  SERVICE_DESC = "serviceDesc";
	String  OPNAME = "opName";
	int UNAUTHORIZED = 401;
	String ERROR_LAST_APPOINTMENT_NOT_FOUND = "LAST_APPOINTMENT_NOT_FOUND";

	String  DISTANCE = "distance";
	String  LOCATOR_RESP_TIME = "locatorResponseTime";
	String  AVAILABLE_DATES_RESP_TIME = "availableDatesResponseTime";
	String  PROVIDER_TIME_SLOTS_RESP_TIME = "providerTimeSlotsResponseTime";

	//Common Exception handling
	String ERROR_FAULT_TYPE = "type";
	String ERROR_FAULT_TITE = "title";
	String ERROR_FAULT_DETAIL = "moreInfo";
	String HTTP_ERROR_RESPONSE = "httpErrorResponse";
	String HTTP_ERROR_STATUS = "httpErrorStatus";

	String FAULT = "fault";
	String ERROR_BAD_REQUEST = "BAD_REQUEST";
	String IQE_MC_CORE_OPS_NAME = "getIQEMcCoreQuestions";
	String IQE_OPS_NAME = "getIQEGetQuestions";
	String ERROR_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
	String ERROR_UNAUTHORIZED = "UNAUTHORIZED";
	String ERROR_CONFLICT = "CONFLICT";
	String ERROR_GATEWAY_TIMEOUT = "GATEWAY_TIMEOUT";
	String ERROR_BAD_GATEWAY = "BAD_GATEWAY";

	String GRAPHQL_TRANSPORT_EXCEPTION = "GRAPHQL_TRANSPORT_EXCEPTION";
	String FAILED_DEPENDENCY = "FAILED_DEPENDENCY";
	String ERROR_UNPROCESSABLE_ENTITY= "UNPROCESSABLE_ENTITY";
	String ERROR_SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
	String ERROR_NOT_FOUND = "NOT_FOUND";
	String ERROR_SLOT_UNAVAILABLE = "SLOT_UNAVAILABLE";
	String ERROR_INVALID_LOB = "INVALID_LOB";
	String MISSING_MANDATORY_PARAM = "MISSING_MANDATORY_PARAM";
	String ERROR_NO_CACHE_DATA = "NO_CACHE_DATA";
	String ERROR_PATIENT_NOT_FOUND = "PATIENT_NOT_FOUND";
	String ERROR_PATIENT_ALREADY_EXISTS = "PATIENT_ALREADY_EXISTS";
	String ERROR_PATIENT_REF_ID_MISSING = "PATIENT_REF_ID_MISSING";
	String ERROR_APPOINTMENT_NOT_FOUND = "APPOINTMENT_NOT_FOUND";
	String STATE_MACHINE_LOB = "HEALTH";
	String STATE_MACHINE_NEXT_GEN_CONSUMER = "NEXT_GEN_WEB";
	String STATE_MACHINE_VISIT_MANAGER_CONSUMER = "VISIT_MANAGER";
	String STATE_MACHINE_GUEST_AUTH_TYPE = "GUEST";
	String STATE_MACHINE_LOGGED_IN_AUTH_TYPE = "LOGGED_IN";
	String X_FINGERPRINT_ID = "x-client-fingerprint-id";
	String X_STATE_ID = "x-state-id";
	String PUBLIC_AUTH_ID_TYPE = "GUEST_ID_TYPE";
	String CONST_CLIENTREFID = "x-clientrefid";
	String RETAIL_PROFILE_ID_TYPE = "RETAIL_PROFILE_ID_TYPE";
	String RETAIL_MRN_PROFILE_ID_TYPE = "RETAIL_MRN_PROFILE_ID_TYPE";
	String RXC_RX_PATIENT_ID_TYPE = "RXC_RX_PATIENT_ID_TYPE";
	String AUTH ="Auth";
	String GUEST_AUTH = "Guest_Auth";
	String  INITIATE_MFA_CALL_RESPONSE_TIME = "initiateMFAResponseTime";
	String  RX_PHONE_DOB_LOOKUP_RESPONSE_TIME = "rxPhoneDobLookupResponseTime";
	String  ACCOUNT_GET_CACHE_LOOKUP_RESPONSE_TIME = "accountGetCacheLookupResponseTime";

	String  ACCOUNT_GET_PROFILE_LOOKUP_RESPONSE_TIME = "accountGetProfileLookupResponseTime";

	String  MC_DOMAIN_SERVICE_RESPONSE_TIME = "mcDomainServiceResponseTime";
	String  MFA_LOOKUP_AUTH_RESPONSE_TIME= "mfaLookupAuthResponseTime";
	String DUPLICATE_RECORDS = "DUPLICATE_RECORDS";
	int AGE_65=65;
	String CVD_DOSE_SEASONAL = "CVD_DOSE_SEASONAL";
	String STORE_GUEST = "store_guest";
	String CAREGIVING_LIST_FLOW_NAME_HEADER = "flowName";
	String CAREGIVING_LIST_FLOW_NAME = "NXT_GEN_HEALTH";

	String  EVC_CAREGIVING_LIST_FLOW_NAME = "EVC_SCHEDULING";
	String SL_EMPTY_STORES = "No Stores";
	String SERVICE_NAME_CHECK_IN_APPOINTMENT = "checkInAppointment";
	String SERVICE_DESC_CHECK_IN_APPOINTMENT = "This service is used to checkin appointments for individual or group Pharmacy locations";
	String ERROR_XID = "XID_ERROR";
	String ERROR_INVALID_XID_PROGRAM = "INVALID_XID_PROGRAM";
	String ERROR_XID_NO_CACHE = "NO_CACHE_DATA";
	String VACCINE_CACHE_NOT_FOUND = "VACCINE_CACHE_NOT_FOUND";
	String ERROR_FETCHING_PROFILE = "ERROR_FETCHING_PROFILE";
	String  SERVICE_NAME_GAP_DISPOSITION = "gapDisposition";
	String  SERVICE_DESC_GAP_DISPOSITION = "This service is used to send Gap disposition.";
	String  PRODUCT_QUESTION_CATEGORY = "PRODUCT_ASSIGNMENT";
	String  NOVA_VAX_ANSWER_VALUE = "Non-mRNA";
	String MC_CORE_FLOW = "MC_CORE";

	String VACCINE_FLOW = "VACCINE";

	String EVC_B2B_FLOW = "EVC_B2B";

	String MC_BRAND = "MC";

	String CVSH_BRAND = "CVSH";

	String PCP = "PCP";
	// MC VM Checkin apt
	String MC_VM_SUBMIT_CHECK_IN_APPT_RESP_TIME = "processMCVMCheckInAppointmentRespTime";
	String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
	String CACHE_NOT_FOUND = "CACHE_NOT_FOUND";
	String APPOINTMENT_NOT_FOUND = "APPOINTMENT_NOT_FOUND";
	String PATIENT_ID_NOT_FOUND = "PATIENT_ID_NOT_FOUND";
	String PATIENT_DEMOGRAPHICS_NOT_FOUND = "PATIENT_DEMOGRAPHICS_NOT_FOUND";
	String CLINIC_NOT_FOUND = "CLINIC_NOT_FOUND";
	String UNPROCESSABLE_CANCELLED_APPOINTMENT = "UNPROCESSABLE_CANCELLED_APPOINTMENT";
	String UNPROCESSABLE_CHECKED_IN_APPOINTMENT = "UNPROCESSABLE_CHECKED_IN_APPOINTMENT";
	String UPDATE_CONSENTS_FAILURE_MCIT = "UPDATE_CONSENTS_FAILURE_MCIT";
	String GET_PATIENT_FAILURE_MCIT = "GET_PATIENT_FAILURE_MCIT";
	String GET_APPOINTMENT_FAILURE_MCIT = "GET_APPOINTMENT_FAILURE_MCIT";
	String GET_CLINIC_FAILURE_MCIT = "GET_CLINIC_FAILURE_MCIT";
	String GET_CONSENTS_FAILURE_MCIT = "GET_CONSENTS_FAILURE_MCIT";
	String GET_SCHEDULING_INFO_FAILURE_MCIT = "GET_SCHEDULING_INFO_FAILURE_MCIT";
	String GET_LINKED_IDS_FAILURE_ACCOUNT = "GET_LINKED_IDS_FAILURE_ACCOUNT";
	String GET_TOKEN_FAILURE_ACCOUNT = "GET_TOKEN_FAILURE_ACCOUNT";

	String MCIT_GET_PROFILE_RESP_TIME = "MCITGetProfileRespTIme";

	String  SERVICE_NAME_MSFT_OAUTH_TOKEN = "getMSFTOAuthToken";
	String  SERVICE_DESC_MSFT_OAUTH_TOKEN = "This service is used to fetch Microsoft entra oAuth token.";
	String CLIENT_RESPONSE = "clientResponse";
	String CLIENT_STATUS_CDE = "clientStatus";
	String EXCEPTION_TYPE = "exceptionType";

	String ENTITLEMENTS_NOT_FOUND = "ENTITLEMENTS_NOT_FOUND";

	String COVID_CODE = "CVD";

	String  COVID_VACCINE_NAME = "COVID";

	String DIGITAL_NGS_SOURCE = "DIGITAL_NGS";


	String FETCH_XID_STATUS_CODE = "FETCH_XID_STATUS_CODE";
	String FETCH_XID_STATUS_DESC = "FETCH_XID_STATUS_DESC";
	String FETCH_XID_RESPTIME = "fetchXidRespTime";
	String FETCH_XID_ERR_RESPTIME = "fetchXidErrorIdRespTime";

	String FETCH_XID_OP_NAME = "fetchXidOperationName";
	String FETCH_XID_OP_DESC = "fetchXidDescription";
	String FETCH_XID_OP_FAILED = "fetchXidFailed";

	String TEST_AND_TREAT_EMERGENCYCARE = "TEST_AND_TREAT_EMERGENCYCARE";
	String TEST_AND_TREAT_NOT_ELIGIBLE = "TEST_AND_TREAT_NOT_ELIGIBLE";
	String NOT_ELIGIBLE = "NOT_ELIGIBLE";

	String RTE_STATUS_EPIC_VERIFIED = "EPIC_VERIFIED";

	String INSURANCE_IMAGE_UPLOAD_ERR = "InsuranceImageUpload call failed with following error";
	String INSURANCE_DETAILS_UPDATE_ERR = "InsuranceDetailsUpdate call failed with following error";
	String INSURANCE_DETAILS_UPDATE_DOMAIN = "Insurance_Details_Update_Domain";
	String RETAIL_IMZ_PREGAP ="RETAIL_IMZ_PREGAP";
	String RETAIL_IMZ_POSTGAP ="RETAIL_IMZ_POSTGAP";
	public static  final String GAP_FLOW_IMZ_INTAKE_PREGAP = "IMZ_INTAKE_PREGAP";

    String RETAIL_IMZ_WALKIN ="RETAIL_IMZ_WALKIN";

	String APPT_CODE_ID_TYPE = "APPT_CODE_ID_TYPE";
	String GET_APPT_DATA_RESPTIME = "getAppointmentDataRespTime";
	String ORIGINATOR_STORE = "STORE";
	String ORIGINATOR_CORP = "CORP";

	String ORIGINATOR_USER = "USER";
	String IMZ_STATUS_SCHEDULED = "SCHEDULED";
	String IMZ_STATUS_CANCELED = "CANCELED";
	String IMZ_FLOW_VM = "VM";
    String FIND_LAST_APPT_ERR_RESPTIME = "findLastAppointmentRespTime";
    String IMZ_FLOW_VCS_EMAIL = "VCS-Email";
	String IMZ_FLOW_XID_WALK_IN = "XID_WalkIn";


	String OPERATION_TYPE = "operationType";
	String PHARMACY_RESERVE_RESPONSE = "PHARMACY_RESERVE_RESPONSE";
	String RESERVATION_CODE_CLEARED = "ReservationCodesCleared";
	String MC_RELEASE_UNPROCESSABLE_ENTITY = "MC_RELEASE_UNPROCESSABLE_ENTITY";
	String MC_RESERVE_UNPROCESSABLE_ENTITY = "MC_RESERVE_UNPROCESSABLE_ENTITY";
	String CLINIC_ID_TYPE = "CLINIC_ID_TYPE";
	String CLINIC_DATA_KEY = "clinicData";
	String DEFAULT_CLIENTREFID = "default-clientrefid";
	String B2B_CLINIC_ID_STARTS_CI = "CI";
	String READY_TO_CHECKIN = "readyToCheckIn";
	String CHECKED_IN = "checkedIn";

	String ID_TYPE = "idType";
	String ID_VALUE = "id";

  // Test and Treatment flow
	String TEST_AND_TREATMENT_PAYER_ID = "10826";
	String TEST_AND_TREATMENT_PLAN_ID = "10826001";

    String FLOW_VM = "VM";
    String IQE_MC_CORE_RESP_TIME ="IQE_MC_CORE_RESP_TIME" ;
    String IQE_RESP_TIME = "IQE_RESP_TIME";
}