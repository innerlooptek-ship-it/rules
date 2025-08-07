package com.cvshealth.digital.microservice.iqe.constants;

public interface DBConstants {
    String SCHEDULE = "schedule";
    String SCHEDULEID = "schedule_id";
    String LOB = "lob";
    String APPOINTMENT_CODE = "appointment_code";
    String APPOINTMENT_STATUS = "appointment_status";
    String PATIENT_REFERENCE_ID = "patient_reference_id";
    String PATIENT_DATA = "patient_data";
    String SCHEDULING_DATA = "scheduling_data";
    String ADDITIONAL_DATA = "additional_data";

    String CONSENT_DATA = "consent_data";
    String QUESTIONNAIRE_DATA = "questionnaire_data";
    String PAYMENT_MODE_DATA = "payment_mode_data";
    String SCHEDULE_BY_PROFILE_ID = "schedule_by_profile_id";
    String PROFILE_ID = "profile_id";

    String XID = "xid";
    String GUEST_ID = "guest_id";

    String CREATED_AT = "created_at_timestamp";
    String MODIFIED_AT = "modified_at_timestamp";

    String  STATE_MANAGEMENT = "statemanagement" ;

    String  STATE_MANAGEMENT_ID = "state_id" ;

    String HEALTH_HUB_SERVICES = "cvs_health_hub_services";
    String STATE_MANAGEMENT_VALUE = "state_value" ;
    String SCHEDULE_BY_MRN_ID = "schedule_by_mrn_id";
    String SCHEDULE_BY_RX_CONNECT_ID = "schedule_by_rx_connect_id";
    String SCHEDULE_BY_GUEST_ID = "schedule_by_guest_id";
    String SCHEDULE_BY_CONFIRM_ID = "schedule_by_confirm_xid";
    String SCHEDULE_BY_CHECKIN_ID = "schedule_by_checkin_xid";
    String SCHEDULE_BY_SCHEDULE_SHORT_ID = "schedule_by_schedule_short_id";
    String QUESTIONNAIRE_RULES = "questionnaire_rules";


    String RULEID="rule_id";

    String FLOW="flow";

//EVC B2B

    String PATIENT = "patient";
    String ENTITLEMENT = "entitlements";
    String PAYOR = "payor";
    String DEVICE = "devices";
    String SCHEDULE_BY_PATIENT_DEMOGRAPHIC = "schedule_by_patient_demographic";

    String RULES_BY_FLOW = "rules_by_flow";
    String ACTIONS= "actions";
    String QUESTIONS= "questions";
    String QUESTIONS_DETAILS= "questions_details";
    String ANSWER_OPTIONS= "answer_options";
    String SERVICES="services";

//IMZ event
    String XID_BY_APPOINTMENT_ID ="xid_by_appointment_id";

    // For OAK street Location
    String CVS_HEALTH_LOCATIONS = "cvs_health_locations";
    String PROGRAM_NAME = "program_name";
    String CONSENT_STATUS = "consent_status";
}