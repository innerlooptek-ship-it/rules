package com.cvshealth.digital.microservice.iqe.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsentConfig {

        private String lob;
        private String modality;
        private String brand;
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
                private String rule;
                private String value;
                private String valueType;
                private Boolean isHidden;
                private boolean isConditional;
                private List<Consent> relatedConsent;
                private List<Consent> consentOptions;
                private ConsentDetailsInfo consent;

                public Consent(Consent consentData) {
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
                        this.rule = consentData.getRule();
                        this.value = consentData.getValue();
                        this.valueType = consentData.getValueType();
                        this.isHidden = consentData.getIsHidden();
                        this.isConditional = consentData.isConditional();
                        this.relatedConsent = consentData.getRelatedConsent();
                        this.consentOptions = consentData.getConsentOptions();
                        if (consentData.getConsent() != null) {
                                this.consent = new ConsentDetailsInfo(consentData.getConsent().getType(), consentData.getConsent().getConsents());
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