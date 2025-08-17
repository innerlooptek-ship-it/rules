package com.cvshealth.digital.microservice.iqe.dto;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CVSJSONMapper {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String CLASS_NAME = "CustomObjectMapper";
    private static final Logger logger = LoggerFactory.getLogger(CVSJSONMapper.class);

    public CVSJSONMapper() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }

    public static String toJSON(Object obj, boolean prettyPrint) {
        String jsonString = "";

        try {
            jsonString = prettyPrint ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj) : mapper.writer().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("CustomObjectMapperJsonProcessingException in method toJSON....{0}", e);
        }

        return jsonString;
    }
}