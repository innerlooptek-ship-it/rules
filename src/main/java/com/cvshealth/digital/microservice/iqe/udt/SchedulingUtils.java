package com.cvshealth.digital.microservice.iqe.udt;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.*;
import java.util.function.Consumer;

import com.cvshealth.digital.microservice.iqe.dto.SchedulingMetricsService;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import reactor.core.publisher.Mono;

@Component
public class SchedulingUtils implements SchedulingConstants{

    /** The Constant CLASS_NAME. */
    private static final String CLASS_NAME = "SchedulingUtils";

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(SchedulingUtils.class);
    
    @Autowired
    private LoggingUtils logUtils;

	@Autowired
    SchedulingMetricsService schedulingMetricsService;

    /** The mapper. */
    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

		mapper.addMixIn(Throwable.class, ThrowableMixin.class);
	}

	@JsonIgnoreProperties({"stackTrace", "cause", "suppressed", "detailMessage"})
	abstract static class ThrowableMixin {
		@JsonProperty("message")
		abstract String getMessage();
	}


	/**
     * Method to convert JSON to Object.
     *
     * @param <T> the generic type
     * @param jsonStr the json str
     * @param type the type
     * @return JSON object
     */
    public static <T> T fromJSON(String jsonStr, Class<T> type) {
        if (StringUtils.isEmpty(jsonStr)) {
            return null;
        }

        try {
            return mapper.readValue(jsonStr, type);
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException in method fromJSON....{0}", e);
        }

        return null;
    }

    
	/**
	 * Method to convert JSON to Object.
	 *
	 * @param jsonStr
	 * @param valueTypeRef
	 * @return JSON object
	 * @throws JsonProcessingException
	 */
	public static <T> T fromJSON(String jsonStr, TypeReference<T> valueTypeRef) {
		if (StringUtils.isEmpty(jsonStr)) {
			return null;
		}
		try {
			return mapper.readValue(jsonStr, valueTypeRef);
		} catch (JsonProcessingException e) {
			logger.error("JsonProcessingException in method fromJSON....{0}", e);
		}

		return null;
	}
    
    /**
     * Method to convert an object to JSON.
     *
     * @param obj the obj
     * @param prettyPrint the pretty print
     * @return json string
     */
    public static String toJSON(Object obj, boolean prettyPrint) {
        String jsonString = "";

        try {
            jsonString = prettyPrint ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
                    : mapper.writer().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error(CLASS_NAME + "JsonProcessingException in method toJSON....{0}", e);
        }

        return jsonString;
    }

    /**
     * Base 64 encode.
     *
     * @param bytes the bytes
     * @return the string
     */
    public String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

   
	/**
	 * Base 64 decode.
	 *
	 * @param s the s
	 * @return the byte[]
	 */
	public static byte[] base64Decode(String s) {
		return s != null ? Base64.getDecoder().decode(s) : null;
	}
	

	
	public String getHeaderValue(String headerName, Map<String,String> headerMap) {
		Optional<String> optional = getHeaderValueFromMap(headerName,headerMap);
		return optional.isPresent() ?  optional.get() : EMPTY;
	}

	private Optional<String> getHeaderValueFromMap(String headerName , Map<String, String> headerValues) {
		Optional<String> headerValue = headerValues.entrySet().stream()
				.filter(e -> headerName.equalsIgnoreCase(e.getKey()))
				.map(Map.Entry::getValue)
				.findFirst();
		return headerValue;

	}



    public Consumer<HttpHeaders> getHttpHeadersForLogging( Map<String,String> headers) {
        HttpHeaders httpHeaderValues =new HttpHeaders();
        httpHeaderValues.add( SchedulingConstants.CONST_CATEGORY,headers.get(SchedulingConstants.CONST_CATEGORY));
        httpHeaderValues.add( SchedulingConstants.CONST_X_GRID,headers.get(SchedulingConstants.CONST_X_GRID));
        httpHeaderValues.add( SchedulingConstants.CONST_GRID,headers.get(SchedulingConstants.CONST_GRID));
        httpHeaderValues.add( SchedulingConstants.CONST_EXP_ID,headers.get(SchedulingConstants.CONST_EXP_ID));
        httpHeaderValues.add( SchedulingConstants.CONST_CLIENTREFID,headers.get(SchedulingConstants.CONST_CLIENTREFID));

        return httpHeaders -> {
            httpHeaders.addAll(httpHeaderValues);
        };
    }

	public static <T extends Enum<T>> boolean enumContains(Class<T> enumClass, List<String> values) {
		return values
				.stream()
				.allMatch(value ->
						stream(enumClass.getEnumConstants()).anyMatch(enumValue -> enumValue.name().equalsIgnoreCase(value)));
	}

	public static <T extends Enum<T>> boolean enumContainsByMappedValue(Class<T> enumClass, List<String> values) {
		return values
				.stream()
				.allMatch(value ->
						stream(enumClass.getEnumConstants()).anyMatch(enumValue -> enumValue.toString().equalsIgnoreCase(value)));
	}

	public static void addTags (Map<String, Object> tags, String statusCode, String statusMessage, String statusDescription, HttpStatus httpStatusCode) {
		tags.put(STATUS_CDE, statusCode);
		tags.put(STATUS_MESSAGE, statusMessage);
		tags.put(STATUS_DESC, statusDescription);
		tags.put(HTTP_STATUS_CDE, httpStatusCode.value());
	}

	public static String getBrand(String flow) {
		return isNotBlank(flow) && flow.equalsIgnoreCase(EVC_B2B_FLOW) ? CVSH_BRAND : MC_BRAND;
	}



	public static String encode(Object object) {

		String data;

		try {
			data = mapper.writeValueAsString(object);

			return Base64.getUrlEncoder().encodeToString(data.getBytes());
		} catch (JsonProcessingException e) {
			return null;
		}
	}



	public static long getElapsedTime(long startTime) {
		return System.currentTimeMillis() - startTime;
	}
}