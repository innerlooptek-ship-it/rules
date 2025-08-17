package com.cvshealth.digital.microservice.iqe.enums;

public enum StatesEnum {
    AK("Alaska"),
    AL("Alabama"),
    AR("Arkansas"),
    AS("American Samoa"),
    AZ("Arizona"),
    CA("California"),
    CO("Colorado"),
    CT("Connecticut"),
    DE("Delaware"),
    DC("District of Columbia"),
    FL("Florida"),
    GA("Georgia"),
    GU("Guam"),
    HI("Hawaii"),
    IA("Iowa"),
    ID("Idaho"),
    IL("Illinois"),
    IN("Indiana"),
    KS("Kansas"),
    KY("Kentucky"),
    LA("Louisiana"),
    MA("Massachusetts"),
    MD("Maryland"),
    ME("Maine"),
    MI("Michigan"),
    MN("Minnesota"),
    MS("Mississippi"),
    MO("Missouri"),
    MT("Montana"),
    NE("Nebraska"),
    NV("Nevada"),
    NH("New Hampshire"),
    NJ("New Jersey"),
    NM("New Mexico"),
    NY("New York"),
    NC("North Carolina"),
    ND("North Dakota"),
    OH("Ohio"),
    OK("Oklahoma"),
    OR("Oregon"),
    PA("Pennsylvania"),
    PR("Puerto Rico"),
    RI("Rhode Island"),
    SC("South Carolina"),
    SD("South Dakota"),
    TN("Tennessee"),
    TX("Texas"),
    UT("Utah"),
    VT("Vermont"),
    VA("Virginia"),
    WA("Washington"),
    WV("West Virginia"),
    WI("Wisconsin"),
    VI("'Virgin Islands"),
    WY("Wyoming");

    private final String state;

    private StatesEnum(String state) {
        this.state = state;
    }

    public String getFullStateValue() {
        return this.state;
    }

    public static String getFullState(String states) {
        for(StatesEnum state : values()) {
            if (state.name().equalsIgnoreCase(states)) {
                return state.getFullStateValue();
            }
        }

        return null;
    }
}