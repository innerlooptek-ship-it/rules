package com.cvshealth.digital.microservice.iqe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsentData {
    @JsonProperty("isSummarized")
    private boolean isSummarized;
    private String patientReferenceId;
    private List<Consent> consents;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Consent {
        private String text;
        private String subText;
        private String consentType;
        private Boolean required;
        private String consentContext;
        private String consentId;
        private String consentName;
        private Boolean consentValue;
        private String consentString;
        private String consentLink;
        private String consentLinkText;
        private String value;
        private String valueType;
        private Boolean isHidden;
        private List<Consent> relatedConsent;
        private List<Consent> consentOptions;
        private ConsentDetailsInfo consent;
        private Set<String> patientReferenceIds;

        public Consent(ConsentData.Consent consentData) {
            this.text = consentData.getText();
            this.subText = consentData.getSubText();
            this.consentType = consentData.getConsentType();
            this.required = consentData.getRequired();
            this.consentContext = consentData.getConsentContext();
            this.consentId = consentData.getConsentId();
            this.consentName = consentData.getConsentName();
            this.consentValue = consentData.getConsentValue();
            this.consentString = consentData.getConsentString();
            this.consentLink = consentData.getConsentLink();
            this.consentLinkText = consentData.getConsentLinkText();
            this.value = consentData.getValue();
            this.valueType = consentData.getValueType();
            this.isHidden = consentData.getIsHidden();
            this.relatedConsent = consentData.getRelatedConsent();
            this.consentOptions = consentData.getConsentOptions();
            if (consentData.getConsent() != null) {
                this.consent = new ConsentData.ConsentDetailsInfo(consentData.getConsent().getType(), consentData.getConsent().getConsents());
            }
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConsentDetailsInfo {
        private String type;
        private List<Consent> consents;
    }
}