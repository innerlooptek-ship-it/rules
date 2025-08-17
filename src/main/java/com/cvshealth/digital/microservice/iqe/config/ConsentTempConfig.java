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
public class ConsentTempConfig {
        private String consentContext;
        private ConsentConfig.Consent consents;
}