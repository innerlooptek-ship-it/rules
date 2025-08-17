package com.cvshealth.digital.microservice.iqe.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.cvshealth.digital.microservice.iqe.dto.CVSJSONMapper;
import lombok.Generated;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class CvsLoggingUtil {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(CvsLoggingUtil.class);
    private static final Map<String, String> keyMappings = Map.ofEntries(Map.entry("x-experienceid", "x-experienceid"), Map.entry("x-cat", "x-cat"), Map.entry("x-state-id", "x-state-id"), Map.entry("x-client-fingerprint-id", "x-client-fingerprint-id"), Map.entry("src_loc_cd", "src_loc_cd"), Map.entry("origin", "origin"), Map.entry("user_id", "user_id"), Map.entry("msg_src_cd", "msg_src_cd"), Map.entry("cat", "cat"), Map.entry("appName", "chPlat"), Map.entry("deviceType", "deviceType"), Map.entry("appVersion", "AppVersion"), Map.entry("req_origin", "req_origin"), Map.entry("cvs-akclient-ip", "clientIP"), Map.entry("x_b3_parentspanid", "x_b3_parentspanid"), Map.entry("x_b3_sampled", "x_b3_sampled"), Map.entry("x_b3_spanid", "x_b3_spanid"), Map.entry("x_b3_traceid", "x_b3_traceid"), Map.entry("env", "env"), Map.entry("user-agent", "user-agent"), Map.entry("referer", "referer"), Map.entry("grid", "grid"), Map.entry("x-grid", "x-grid"), Map.entry("x-clientrefid", "x-clientrefid"));
    @Value("${custom.logHttpHeaders}")
    private List<String> logHttpHeaders;

    public CvsLoggingUtil() {
    }

    public static void populateHeaderInfo(Map<String, Object> eventMap, Map<String, String> reqHdrMap) {
        if (!CollectionUtils.isEmpty(reqHdrMap)) {
            keyMappings.forEach((reqKey, eventKey) -> {
                String value = (String)reqHdrMap.get(reqKey);
                if (StringUtils.isNotBlank(value)) {
                    eventMap.put(eventKey, value);
                }

            });
        }
    }

    private static void logEvent(Logger logger, String eventType, Map<String, Object> params) {
        if (params != null && logger != null) {
            Map<String, Object> logMessage = new LinkedHashMap(Map.of("CVSEVENT", eventType));
            logMessage.putAll(params);
            logger.info(CVSJSONMapper.toJSON(logMessage, false));
        }
    }

    private static void logEvent(String eventType, Map<String, Object> params) {
        if (params != null) {
            Map<String, Object> logMessage = new LinkedHashMap(Map.of("CVSEVENT", eventType));
            logMessage.putAll(params);
            log.info(CVSJSONMapper.toJSON(logMessage, false));
        }
    }

    public static void entryEventLogging(Logger logger, Map<String, Object> params) {
        logEvent(logger, "ENTRY", params);
    }

    public static void exitEventLogging(Logger logger, Map<String, Object> params, long startTime) {
        params.put("respTime", System.currentTimeMillis() - startTime);
        logEvent(logger, "EXIT", params);
    }

    public static void exitEventLogging(Logger logger, Map<String, Object> params) {
        logEvent(logger, "EXIT", params);
    }

    public static void infoEventLogging(Logger logger, Map<String, Object> params, long startTime) {
        params.put("respTime", System.currentTimeMillis() - startTime);
        logEvent(logger, "INFO", params);
    }

    public static void infoEventLogging(Logger logger, Map<String, Object> params) {
        logEvent(logger, "INFO", params);
    }

    public static void errorEventLogging(Logger logger, Map<String, Object> params) {
        logEvent(logger, "ERROR", params);
    }

    public static void errorEventLogging(Logger logger, Map<String, Object> params, long startTime) {
        params.put("respTime", System.currentTimeMillis() - startTime);
        logEvent(logger, "ERROR", params);
    }

    public static void logErrorEvent(Map<String, Object> params, long startTime) {
        params.put("respTime", System.currentTimeMillis() - startTime);
        logEvent("ERROR", params);
    }

    public static void logInfoEvent(Map<String, Object> params, long startTime) {
        params.put("respTime", System.currentTimeMillis() - startTime);
        logEvent("INFO", params);
    }

    public static void logExitEvent(Map<String, Object> params, long startTime) {
        params.put("respTime", System.currentTimeMillis() - startTime);
        logEvent("EXIT", params);
    }

    public static void logErrorEvent(Map<String, Object> params) {
        logEvent("ERROR", params);
    }

    public static void logInfoEvent(Map<String, Object> params) {
        logEvent("INFO", params);
    }

    public static void logExitEvent(Map<String, Object> params) {
        logEvent("EXIT", params);
    }

    public static Map<String, Object> populateEventMap(String className, String methodName, String serviceName, String serviceDesc, Map<String, String> headers) {
        Map<String, Object> eventMap = new LinkedHashMap(Map.of("className", className, "serviceName", serviceName, "methodName", methodName, "serviceDesc", serviceDesc, "opName", methodName, "statusCde", "0000", "statusMessage", "SUCCESS"));
        populateHeaderInfo(eventMap, headers);
        return eventMap;
    }
}