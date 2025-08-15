package com.cvshealth.digital.microservice.iqe.dto;

import com.cvshealth.digital.microservice.iqe.config.MessageConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SchedulingMetricsService {
    /**
     * Notes :
     * Metrics Library is used instead of Micrometer Timed Annotation to have more control over the metrics.
     * Spring 3.x uses Micrometer 1.10.x implementation which gives unreliable metrics.
     * @Timed works fine upto 1.9.4 version of Micrometer.
     * In the future, if we plan to use a new way to instrument metrics we can make that change over here.
     */

    private final MeterRegistry meterRegistry;
    private final Map<String,String> errorDescriptionToErrorCodeMap;
    public static final String APP_NAME = "dhs-scheduling-app";
    public static final String API_NAME_TAG = "api_name";
    public static final String APP_NAME_TAG = "application";
    public static final String ERROR_NAME_TAG = "errorName";
    public static final String API_ERRORS_METRIC_NAME = "ngs_api_errors";
    public static final String API_TRANSACTIONS_METRIC_NAME = "ngs_api_transactions";
    public static final String API_RESPONSE_TIME_METRIC_NAME = "ngs_api_response_time";
    public static final String UNKNOWN_API_LABEL = "unknownAPI";
    public static final String OPERATION_NAME_TAG = "operationName";
    public static final String CATEGORY_NAME = "category_name";


    public SchedulingMetricsService(MeterRegistry meterRegistry, MessageConfig messagesConfig) {
        this.meterRegistry = meterRegistry;
        errorDescriptionToErrorCodeMap = new HashMap<>();
        Map<String , String> messages = messagesConfig.getMessages();
        messages.forEach((key, value) -> errorDescriptionToErrorCodeMap.put(value, key));
    }

    public void incrementErrorCounter(String statusDescription , String category) {
        if(statusDescription == null) {
            return;
        }
        Optional<String> errorCodeOptional = Optional.ofNullable(errorDescriptionToErrorCodeMap.
                get(statusDescription));

        errorCodeOptional.ifPresent(errorCode -> {
            String[] errorInfo = errorCode.split("\\.");
            if (errorInfo.length >= 2) {
                String errorName = errorInfo[1];
                String apiName = errorInfo[0];
                Counter errorCounter = meterRegistry.counter(API_ERRORS_METRIC_NAME, APP_NAME_TAG,
                        APP_NAME, ERROR_NAME_TAG, errorName , API_NAME_TAG, apiName , CATEGORY_NAME , category);
                errorCounter.increment();
            }
        });

    }

    public void incrementTransactionCounter(String operationName, String apiName) {
        if(apiName == null) {
            return;
        }
        Counter transactionCounter = meterRegistry.counter(API_TRANSACTIONS_METRIC_NAME, APP_NAME_TAG,
                APP_NAME, API_NAME_TAG, apiName , OPERATION_NAME_TAG, operationName);
        transactionCounter.increment();
    }

    public void trackMetrics(String apiName, long startTime) {
        if(apiName == null) return;

        recordResponseTime(apiName, System.currentTimeMillis() - startTime, apiName);
        incrementTransactionCounter(apiName, apiName);
    }

    public void recordResponseTime(String operationName, long responseTimeMillis, String apiName) {
        if(apiName == null) {
            return;
        }

        DistributionSummary summary = DistributionSummary.builder(API_RESPONSE_TIME_METRIC_NAME)
                .tags(APP_NAME_TAG, APP_NAME, API_NAME_TAG, apiName , OPERATION_NAME_TAG, operationName)
                .publishPercentileHistogram(true)
                .register(meterRegistry);
        summary.record(responseTimeMillis);
    }

}