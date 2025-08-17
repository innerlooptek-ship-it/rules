package com.cvshealth.digital.microservice.iqe.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;


@Getter
@AllArgsConstructor
public enum ErrorKey {
    RESCHEDULE("reschedule"),
    CANCEL("cancelAppointment"),
    GET_MC_APPOINTMENTS("getMcAppointments"),
    GET_VM_CONSENTS("getVMConsents"),
    VM_APPOINTMENT_STATUS("getVMAppointmentStatus"),
    SET_VM_CONSENTS("setVMConsents"),
    SET_VM_QUESTIONNAIRE("setVMQuestionnaire"),
    SOFT_RESERVATION("softReservation"),
    GET_STORES("getStores"),
    SAVE_QUESTIONNAIRE("saveQuestionnaire"),
    USER_APPOINTMENT("userAppointment"),
    ALL_VISIT_CODES("allVisitCodes"),
    CONSENTS("consent"),
    PATIENT_DETAILS("patientDetails"),
    WAITLIST("waitlist"),
    ENTITLEMENT_CHECK("entitlementCheck"),
    DUPLICATE_CHECK("duplicateCheck"),
    DUPLICATE_CHECK_V2("duplicateCheckV2"),
    GET_CARE_GAPS("getCareGaps"),
    AVAILABLE_DATES("availableDates"),
    PERSIST_SCHEDULE_INFO("persistScheduleInfo"),
    PERSIST_IMZ_SCHEDULE("persistImzSchedule"),
    CANCEL_SCHEDULE_INFO("cancelScheduleInfo"),
    PERSIST_RESCHEDULE_INFO("persistRescheduleInfo"),
    UPDATE_SCHEDULE_INFO("updateScheduleInfo"),
    FETCH_ALL_VISIT_CODES("fetchAllVisitCodes"),
    LOCATION_AVAILABILITY("locationAvailability"),
    STATE_MANAGEMENT("stateManagement"),
    MCIT_GET_PATIENT_CONSENTS("mcitGetPatientConsents"),
    GET_SCHEDULING_CONSENTS("getSchedulingConsents"),
    CONFIRMATION("confirmation"),
    INSURANCE("insurance"),
    AVAILABLE_TIME_SLOTS("availableTimeSlots"),
    GET_CONSENTS("getConsents"),
    ONE_CLICK_LOCATOR("oneClickLocator"),
    CREATE_QR_CODE("createQRCode"),
    READ_QR_CODE("readQRCode");

    private final String key;

    @Override
    public String toString() {
        return key;
    }

}