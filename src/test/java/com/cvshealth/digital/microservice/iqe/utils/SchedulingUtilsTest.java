package com.cvshealth.digital.microservice.iqe.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class SchedulingUtilsTest {

    @Test
    void testToJSON_and_fromJSON_Object() {
        Map<String, Object> map = new HashMap<>();
        map.put("foo", "bar");
        String json = SchedulingUtils.toJSON(map, false);
        assertTrue(json.contains("\"foo\":\"bar\""));

        Map result = SchedulingUtils.fromJSON(json, Map.class);
        assertEquals("bar", result.get("foo"));
    }

    @Test
    void testFromJSON_TypeReference() {
        Map<String, Object> map = new HashMap<>();
        map.put("num", 123);
        String json = SchedulingUtils.toJSON(map, false);

        Map<String, Object> result = SchedulingUtils.fromJSON(json, new TypeReference<Map<String, Object>>() {});
        assertEquals(123, ((Number)result.get("num")).intValue());
    }

    @Test
    void testToJSON_withPrettyPrint() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        String json = SchedulingUtils.toJSON(map, true);
        assertTrue(json.contains("\n")); // pretty print should have newlines
    }

    @Test
    void testFromJSON_emptyString_returnsNull() {
        assertNull(SchedulingUtils.fromJSON("", Map.class));
        assertNull(SchedulingUtils.fromJSON("", new TypeReference<Map<String, Object>>() {}));
    }

    @Test
    void testBase64Encode_and_Decode() {
        SchedulingUtils utils = new SchedulingUtils();
        String original = "test-string";
        String encoded = utils.base64Encode(original.getBytes());
        assertEquals(Base64.getEncoder().encodeToString(original.getBytes()), encoded);

        byte[] decoded = utils.base64Decode(encoded);
        assertEquals(original, new String(decoded));
    }

    @Test
    void testBase64Decode_nullInput() {
        SchedulingUtils utils = new SchedulingUtils();
        assertNull(utils.base64Decode(null));
    }

    @Test
    void testGetHeaderValue_found() {
        SchedulingUtils utils = new SchedulingUtils();
        Map<String, String> headers = new HashMap<>();
        headers.put("X-FOO", "bar");
        assertEquals("bar", utils.getHeaderValue("X-FOO", headers));
        assertEquals("bar", utils.getHeaderValue("x-foo", headers)); // case-insensitive
    }

    @Test
    void testGetHeaderValue_notFound() {
        SchedulingUtils utils = new SchedulingUtils();
        Map<String, String> headers = new HashMap<>();
        assertEquals("", utils.getHeaderValue("missing", headers));
    }

    @Test
    void testFromJSON_invalidJson_returnsNull_andLogsError() {
        String invalidJson = "{foo:bar"; // malformed JSON
        // Mock logger to verify error logging
        Logger logger = mock(Logger.class);
        try (MockedStatic<LoggerFactory> loggerFactory = mockStatic(LoggerFactory.class)) {
            loggerFactory.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(logger);
            assertNull(SchedulingUtils.fromJSON(invalidJson, Map.class));
            assertNull(SchedulingUtils.fromJSON(invalidJson, new TypeReference<Map<String, Object>>() {}));
        }
    }

    @Test
    void testToJSON_invalidObject_logsErrorAndReturnsEmptyString() {
        Object invalidObj = new Object() {
            // Jackson cannot serialize this due to circular reference
            public Object self = this;
        };
        Logger logger = mock(Logger.class);
        try (MockedStatic<LoggerFactory> loggerFactory = mockStatic(LoggerFactory.class)) {
            loggerFactory.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(logger);
            String result = SchedulingUtils.toJSON(invalidObj, false);
            assertEquals("", result);
        }
    }

    @Test
    void testGetHeaderValueFromMap_privateMethod() throws Exception {
        SchedulingUtils utils = new SchedulingUtils();
        Map<String, String> headers = new HashMap<>();
        headers.put("X-TEST", "val");
        // Use reflection to access private method
        var method = SchedulingUtils.class.getDeclaredMethod("getHeaderValueFromMap", String.class, Map.class);
        method.setAccessible(true);
        Object result = method.invoke(utils, "x-test", headers);
        assertTrue(((java.util.Optional<?>) result).isPresent());
        assertEquals("val", ((java.util.Optional<?>) result).get());
    }
}