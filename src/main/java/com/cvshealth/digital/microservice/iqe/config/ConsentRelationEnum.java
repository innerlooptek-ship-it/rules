package com.cvshealth.digital.microservice.iqe.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ConsentRelationEnum {
    SELF("self"),
    AUTH_REPRESENTATIVE("Auth Representative");
    private final String relation;

    @Override
    public String toString() {
        return this.relation;
    }

    public static ConsentRelationEnum fromString(String relation) {
        for (ConsentRelationEnum consentRelation : ConsentRelationEnum.values()) {
            if (consentRelation.relation.equalsIgnoreCase(relation)) {
                return consentRelation;
            }
        }
        return null;
    }
}