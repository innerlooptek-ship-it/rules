package com.cvshealth.digital.microservice.iqe.config;


import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
public class GetConsentConfigLoader {

    /** The Constant CLASS_NAME. */
    private static final String CLASS_NAME = "GetConsentConfigLoader";

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    @Autowired
    DHSSchedulingConfigs dhsSchedulingConfigs;
    @Getter
    Map<String, List<ConsentConfig>> consentDataMap;

    @Getter
    Map<String, ConsentTempConfig> consentByContextMap;

    @PostConstruct
    private void cacheConsentConfigData(){ //building consent data
        Map<String, List<ConsentConfig>> tempDataMap = new ConcurrentHashMap<>();
        Map<String, ConsentTempConfig> tempDataByContextMap = new ConcurrentHashMap<>();
        dhsSchedulingConfigs.getConsentsConfig().forEach((key,value) ->{

            File consentConfigNames = new File(value);
            InputStream inputStream;

            try {
                inputStream = new BufferedInputStream(new FileInputStream(consentConfigNames));
                byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
                String data = new String(bdata, StandardCharsets.UTF_8);
                // Load Consumer Configs from file
                List<ConsentConfig> consentConfigList = DHSSchedulerUtils.fromJSON(data, new TypeReference<List<ConsentConfig>>() {});

                logger.debug("Raw data {}", DHSSchedulerUtils.toJSON(consentConfigList,true));

                if(consentConfigList != null && !consentConfigList.isEmpty()){
                    tempDataMap.put(key,Collections.unmodifiableList(consentConfigList));
                }

                if(!CollectionUtils.isEmpty(consentConfigList)){
                    consentConfigList.forEach(consentConfig -> {
                        consentConfig.getConsents().forEach(consentConfig1 -> {
                            consentConfig1.getConsent().getConsents().forEach(consentConfig2 -> {
                                tempDataByContextMap.put(consentConfig2.getConsentName(),ConsentTempConfig.builder().consentContext(consentConfig1.getConsentContext()).consents(consentConfig2).build() );
                            });
                        });
                    });
                }


            } catch (FileNotFoundException e) {
                logger.error("FileNotFoundException....", e);
            } catch (IOException e) {
                logger.error("IOException....", e);
            }

            logger.debug("Exiting postConstruct method of cacheConsentConfigData");
        });
        consentDataMap = Collections.unmodifiableMap(tempDataMap);
        consentByContextMap = Collections.unmodifiableMap(tempDataByContextMap);
    }

}