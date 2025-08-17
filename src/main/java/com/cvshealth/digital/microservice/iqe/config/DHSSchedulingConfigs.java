package com.cvshealth.digital.microservice.iqe.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "custom")
@Data
public class DHSSchedulingConfigs {
    private String env;
    private String persistTransactionsURL;
    private String retrieveTransactionsURL;
    private int requestTimeOut;
    private int connectionTimeOut;
    private int socketTimeOut;
    private int timeout;
    private int maxTotalConnections;
    private int defaultConnectionsPerRoute;
    private String profileIdDycryptionKeyOfAccessToken;
    private String accountDecryptionKey;
    private String scheduleEncryptDecryptKey;
    private String imzSubmitAppointmentUrl;
   // @Value("mcSubmitAppointmentUrl")
    private String mcSubmitAppointmentUrl;
    //@Value("${dhs.statemgmt-service.baseUrl}${dhs.statemgmt-service.uri}")
    private String stateMachineUrl;
    private String mfaDecryptionKey;
    private List<String> serviceIdMappingForVaccines;
    private List<String> ineligiblityException;
    private int imzGracePeriodInMins;
    private int imzCheckInTimeInMins;
    private boolean novaVaxInterim;
    private List<String> scheduleProgram;
    private int imzStoreCancelGracePeriodInDays;
    private List<String> exclusionServices;
    private String imzClinicInfoSource;
    private Map<String,String> consentsConfig;

}