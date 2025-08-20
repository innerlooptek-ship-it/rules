package com.cvshealth.digital.microservice.iqe.utils;

import com.cvshealth.digital.microservice.iqe.config.ConsentConfig;
import com.cvshealth.digital.microservice.iqe.model.ConsentData;
import com.cvshealth.digital.microservice.iqe.model.GetConsentInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ConsentServiceHelperTest {
    
    @InjectMocks
    private ConsentServiceHelper consentServiceHelper;
    
    @Test
    void shouldFilterConsentsBasedOnRules() {
        ConsentConfig.Consent consent = new ConsentConfig.Consent();
        consent.setConsentName("test-consent");
        consent.setConditional(true);
        consent.setRule("age >= 18");
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("age", 25);
        
        ConsentConfig.Consent result = consentServiceHelper.filterConsents(consent, variables);
        
        assertThat(result).isNotNull();
        assertThat(result.getConsentName()).isEqualTo("test-consent");
    }
    
    @Test
    void shouldFilterOutInvalidConsents() {
        ConsentConfig.Consent consent = new ConsentConfig.Consent();
        consent.setConsentName("test-consent");
        consent.setConditional(true);
        consent.setRule("age >= 18");
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("age", 16);
        
        ConsentConfig.Consent result = consentServiceHelper.filterConsents(consent, variables);
        
        assertThat(result).isNull();
    }
    
    @Test
    void shouldReturnNonConditionalConsents() {
        ConsentConfig.Consent consent = new ConsentConfig.Consent();
        consent.setConsentName("test-consent");
        consent.setConditional(false);
        
        Map<String, Object> variables = new HashMap<>();
        
        ConsentConfig.Consent result = consentServiceHelper.filterConsents(consent, variables);
        
        assertThat(result).isNotNull();
        assertThat(result.getConsentName()).isEqualTo("test-consent");
    }
    
    @Test
    void shouldSummarizeConsentsForGroup() {
        List<ConsentData> consentDataList = new ArrayList<>();
        
        ConsentData.Consent consent1 = new ConsentData.Consent();
        consent1.setPatientReferenceIds(Set.of("patient-1"));
        
        ConsentData.Consent consent2 = new ConsentData.Consent();
        consent2.setPatientReferenceIds(Set.of("patient-2"));
        
        ConsentData consentData1 = new ConsentData();
        consentData1.setPatientReferenceId("patient-1");
        consentData1.setConsents(List.of(consent1));
        consentDataList.add(consentData1);
        
        ConsentData consentData2 = new ConsentData();
        consentData2.setPatientReferenceId("patient-2");
        consentData2.setConsents(List.of(consent2));
        consentDataList.add(consentData2);
        
        GetConsentInput input = new GetConsentInput();
        
        List<ConsentData> result = consentServiceHelper.summarizeConsentsForGroup(consentDataList, input);
        
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
    }
}
