package com.cvshealth.digital.microservice.iqe.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class LoggingUtilsTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private LoggingUtils loggingUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loggingUtils = new LoggingUtils();
    }

    @Test
    void entryEventLogging_logsInfo_whenParamsNotNull() {
        Map<String, Object> params = new HashMap<>();
        params.put("key", "value");

        try (MockedStatic<SchedulingUtils> schedulingUtils = mockStatic(SchedulingUtils.class)) {
            schedulingUtils.when(() -> SchedulingUtils.toJSON(any(), eq(false))).thenReturn("{\"CVSEVENT\":\"ENTRY\",\"key\":\"value\"}");

            loggingUtils.entryEventLogging(logger, params);

            verify(logger).info("{\"CVSEVENT\":\"ENTRY\",\"key\":\"value\"}");
        }
    }

    @Test
    void entryEventLogging_doesNothing_whenParamsNull() {
        loggingUtils.entryEventLogging(logger, null);
        verifyNoInteractions(logger);
    }

    @Test
    void exitEventLogging_logsInfo_whenParamsNotNull() {
        Map<String, Object> params = new HashMap<>();
        params.put("foo", "bar");

        try (MockedStatic<SchedulingUtils> schedulingUtils = mockStatic(SchedulingUtils.class)) {
            schedulingUtils.when(() -> SchedulingUtils.toJSON(any(), eq(false))).thenReturn("{\"CVSEVENT\":\"EXIT\",\"foo\":\"bar\"}");

            loggingUtils.exitEventLogging(logger, params);

            verify(logger).info("{\"CVSEVENT\":\"EXIT\",\"foo\":\"bar\"}");
        }
    }

    @Test
    void exitEventLogging_doesNothing_whenParamsNull() {
        loggingUtils.exitEventLogging(logger, null);
        verifyNoInteractions(logger);
    }

    @Test
    void infoEventLogging_logsInfo_whenParamsNotNull() {
        Map<String, Object> params = new HashMap<>();
        params.put("a", 1);

        try (MockedStatic<SchedulingUtils> schedulingUtils = mockStatic(SchedulingUtils.class)) {
            schedulingUtils.when(() -> SchedulingUtils.toJSON(any(), eq(false))).thenReturn("{\"CVSEVENT\":\"INFO\",\"a\":1}");

            loggingUtils.infoEventLogging(logger, params);

            verify(logger).info("{\"CVSEVENT\":\"INFO\",\"a\":1}");
        }
    }

    @Test
    void infoEventLogging_doesNothing_whenParamsNull() {
        loggingUtils.infoEventLogging(logger, null);
        verifyNoInteractions(logger);
    }

    @Test
    void testPopulateHeaderInfo_allHeadersPresent() {
        Map<String, Object> eventMap = new HashMap<>();
        Map<String, String> reqHdrMap = new HashMap<>();
        reqHdrMap.put("SRC_LOC_CD", "loc");
        reqHdrMap.put("ORIGIN", "origin");
        reqHdrMap.put("USER_ID", "user");
        reqHdrMap.put("MSG_SRC_CD", "msg");
        reqHdrMap.put("CATEGORY", "cat");
        reqHdrMap.put("APP_NAME", "app");
        reqHdrMap.put("DEVICE_TYPE", "dev");
        reqHdrMap.put("APP_VERSION", "1.0");
        reqHdrMap.put("REQ_ORIGIN", "reqOrigin");
        reqHdrMap.put("AKAMAI_CLIENT_IP", "ip");
        reqHdrMap.put("X-B3-PARENTSPANID", "pspan");
        reqHdrMap.put("X-B3-SAMPLED", "sampled");
        reqHdrMap.put("X-B3-SPANID", "span");
        reqHdrMap.put("X-B3-TRACEID", "trace");
        reqHdrMap.put("ENV", "env");
        reqHdrMap.put("USER_AGENT", "agent");
        reqHdrMap.put("REFERER", "ref");
        reqHdrMap.put("GRID", "grid");
        reqHdrMap.put("X-GRID", "xgrid");
        reqHdrMap.put("EXP_ID", "expid");
        reqHdrMap.put("experienceId", "expid3");

        Map<String, Object> result = LoggingUtils.populateHeaderInfo(eventMap, reqHdrMap);
    }

    @Test
    void testPopulateHeaderInfo_missingCategoryAndGrid() {
        Map<String, Object> eventMap = new HashMap<>();
        Map<String, String> reqHdrMap = new HashMap<>();
        // No CATEGORY, GRID, X-GRID
        Map<String, Object> result = LoggingUtils.populateHeaderInfo(eventMap, reqHdrMap);

        assertNull(result.get("CATEGORY")); // Expect null
        assertNull(result.get("GRID"));     // Expect null
        assertNull(result.get("X-GRID"));
    }

    @Test
    void testPopulateHeaderInfo_experienceIdSmallCase() {
        Map<String, Object> eventMap = new HashMap<>();
        Map<String, String> reqHdrMap = new HashMap<>();
        reqHdrMap.put("experienceId", "expidSmall");
        Map<String, Object> result = LoggingUtils.populateHeaderInfo(eventMap, reqHdrMap);
        assertNull(result.get("EXPERIENCE_ID")); // Expect null if not supported in implementation
    }

    @Test
    void testPopulateHeaderInfo_nullOrEmptyReqHdrMap() {
        Map<String, Object> eventMap = new HashMap<>();
        // Null map
        Map<String, Object> result1 = LoggingUtils.populateHeaderInfo(eventMap, null);
        assertSame(eventMap, result1);

        // Empty map
        Map<String, String> reqHdrMap = new HashMap<>();
        Map<String, Object> result2 = LoggingUtils.populateHeaderInfo(eventMap, reqHdrMap);
        reqHdrMap.put("EXPERIENCE_ID", "expid2");
        assertSame(eventMap, result2);
    }
}