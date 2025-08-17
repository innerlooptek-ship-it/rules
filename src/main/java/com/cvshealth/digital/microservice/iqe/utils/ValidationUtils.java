package com.cvshealth.digital.microservice.iqe.utils;

import com.cvshealth.digital.microservice.iqe.config.MessageConfig;
import com.cvshealth.digital.microservice.iqe.enums.ErrorKey;
import com.cvshealth.digital.microservice.iqe.exception.CvsException;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


import static com.cvshealth.digital.microservice.iqe.utils.CvsLoggingUtil.logErrorEvent;
import static com.cvshealth.digital.microservice.iqe.utils.SchedulingUtils.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
@RequiredArgsConstructor
public class ValidationUtils {

    private final MessageConfig messagesConfig;

    private Map<String, String> errorMessages;

    @PostConstruct
    private void postConstruct() {
        errorMessages = messagesConfig.getMessages();
    }

    /**
     * Validates if a given time string matches the specified format pattern.
     *
     * @param time The time string to validate
     * @param format The expected format pattern (following DateTimeFormatter patterns)
     * @param statusCode The error code to use if validation fails
     * @param errorKey The error key to get statusDescription
     * @param tags Map containing metadata for error tracking and logging
     *
     * @throws CvsException when:
     *         - The time string cannot be parsed using the specified format
     *         - The format pattern is invalid
     *
     * @see java.time.format.DateTimeFormatter
     * @see java.time.LocalTime
     */
    public void validateTimeFormat(String time, String format, String statusCode, ErrorKey errorKey, Map<String, Object> tags) throws CvsException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalTime.parse(time, formatter);
        } catch (DateTimeParseException e) {
            throwValidationException(statusCode, errorKey, tags);
        }
    }

    /**
     * Validates if a given date string matches the specified format pattern.
     *
     * @param date The date string to validate
     * @param format The expected format pattern (following DateTimeFormatter patterns)
     * @param statusCode The error code to use if validation fails
     * @param errorKey The error key to get statusDescription
     * @param tags Map containing metadata for error tracking and logging
     *
     * @throws CvsException when:
     *         - The date string cannot be parsed using the specified format
     *         - The format pattern is invalid
     *
     * @see java.time.format.DateTimeFormatter
     * @see java.time.LocalDate
     */
    public void validateDateFormat(String date, String format, String statusCode, ErrorKey errorKey, Map<String, Object> tags) throws CvsException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throwValidationException(statusCode, errorKey, tags);
        }
    }

    public void validateDateFormatInList(List<String> dateList, String format, String statusCode, ErrorKey errorKey, Map<String, Object> tags) throws CvsException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            if(!CollectionUtils.isEmpty(dateList)) {
                dateList.forEach(date-> {
                    LocalDate.parse(date, formatter);
                });
            }

        } catch (DateTimeParseException e) {
            throwValidationException(statusCode, errorKey, tags);
        }
    }

    /**
     * Validates that a string value is not null or empty.
     *
     * @param value The string value to validate
     * @param statusCode The error code to use if validation fails
     * @param errorKey The error key to get statusDescription
     * @param tags Map containing metadata for error tracking and logging
     *
     * @throws CvsException when the string is null or empty (length = 0)
     */
    public void validateString(String value, String statusCode, ErrorKey errorKey, Map<String, Object> tags) throws CvsException {
        if (StringUtils.isEmpty(value)) throwValidationException(statusCode, errorKey, tags);
    }

    /**
     * Validates that all provided string values correspond to valid enum constants in the specified enum class.
     *
     * @param values List of string values to validate against enum constants
     * @param enumClass The enum class to validate against
     * @param statusCode The error code to use if validation fails
     * @param errorKey The error key to get statusDescription
     * @param tags Map containing metadata for error tracking and logging
     *
     * @throws CvsException when:
     *         - The value list is null or empty
     *         - Any value in the list does not match an enum constant
     */
    public void validateEnumValues(List<String> values, Class<? extends Enum> enumClass, String statusCode, ErrorKey errorKey, Map<String, Object> tags) throws CvsException {
        if (CollectionUtils.isEmpty(values) || !enumContains(enumClass, values)) throwValidationException(statusCode, errorKey, tags);
    }

    /**
     * Validates that all provided string values correspond to valid enum mapped values to the constants in the specified enum class.
     *
     * @param values List of string values to validate against enum constants
     * @param enumClass The enum class to validate against
     * @param statusCode The error code to use if validation fails
     * @param errorKey The error key to get statusDescription
     * @param tags Map containing metadata for error tracking and logging
     *
     * @throws CvsException when:
     *         - The value list is null or empty
     *         - Any value in the list does not match an enum constant
     */
    public void validateEnumValuesByMappedValue(List<String> values, Class<? extends Enum> enumClass, String statusCode, ErrorKey errorKey, Map<String, Object> tags) throws CvsException {
        if (CollectionUtils.isEmpty(values) || !enumContainsByMappedValue(enumClass, values)) throwValidationException(statusCode, errorKey, tags);
    }

    /**
     * Validates a specific property across all elements in a collection using a provided property extractor function.
     *
     * @param <T> The type of elements in the collection
     * @param <R> The type of the property to be validated
     * @param data The collection of elements to validate
     * @param propertyExtractor Function to extract the property to validate from each element
     * @param statusCode The error code to use if validation fails
     * @param errorKey The error key to get statusDescription
     * @param eventMap Map containing metadata for error tracking and logging
     *
     * @throws CvsException when any extracted property is invalid (null or empty)
     *
     * @see java.util.function.Function
     * @see java.util.Collection
     */
    public <T, R> void validatePropertyInList(
        Collection<T> data,
        Function<T, R> propertyExtractor,
        String statusCode,
        ErrorKey errorKey,
        Map<String, Object> eventMap) throws CvsException {

        List<T> invalidEntries = data.stream()
            .filter(item -> isInvalid(propertyExtractor.apply(item)))
            .toList();

        if (!CollectionUtils.isEmpty(invalidEntries)) throwValidationException(statusCode, errorKey, eventMap);
    }

    /**
     * Validates that a collection is not null or empty.
     *
     * @param data The collection to validate
     * @param statusCode The status code for the error message
     * @param errorKey The error key for message.yaml
     * @param eventMap Map containing event tracking information for error reporting
     *
     * @throws CvsException with the specified error code if the collection is null or empty
     *
     * @apiNote This method is used for input validation to ensure required collections contain data
     *          before processing. It uses Spring's CollectionUtils for empty checking which handles
     *          both null and empty collections.
     *
     * @implNote The method uses throwValidationException internally, which is expected to create
     *          an appropriate CvsException with the provided error code and event map data
     */
    public void validateCollection(Collection<?> data, String statusCode, ErrorKey errorKey, Map<String, Object> eventMap) throws CvsException {
        if (CollectionUtils.isEmpty(data)) throwValidationException(statusCode, errorKey, eventMap);
    }

    /**
     * Determines if a property value is considered invalid based on its type.
     *
     * @param property The property value to validate, can be any Object type
     * @return true if the property is invalid, according to these rules:
     *         - true if the property is null
     *         - true if the property is a String and is blank (using StringUtils.isBlank)
     *         - true if the property is a Collection and is empty (using CollectionUtils.isEmpty)
     *         - false for all other cases
     *
     * @see org.apache.commons.lang3.StringUtils#isBlank
     * @see org.apache.commons.collections4.CollectionUtils#isEmpty
     */
    private boolean isInvalid(Object property) {
        if (property == null) {
            return true;
        }
        if (property instanceof String) {
            return StringUtils.isBlank((String) property);
        }
        if (property instanceof Collection<?>) {
            return CollectionUtils.isEmpty((Collection<?>) property);
        }
        return false;
    }

    /**
     * Throws a CvsException with BAD_REQUEST status when validation fails.
     * @param statusCode The status code for the error message
     * @param errorKey The error key for message.yaml
     * @param eventMap Map for logging validation error events, updated with error details
     * @throws CvsException with BAD_REQUEST status, containing:
     *         - HTTP status code 400 (BAD_REQUEST)
     *         - Status code derived from errorKey
     *         - Error message from errorMessages configuration
     *         - BAD_REQUEST as the error source
     */
    public void throwValidationException(String statusCode, ErrorKey errorKey, Map<String, Object> eventMap) throws CvsException {

        String msgKey = errorKey.getKey() + "." + statusCode;
        String statusDescription = errorMessages.get(msgKey);

        addTags(eventMap, BAD_REQUEST.name(), statusCode, statusCode, BAD_REQUEST);

        logErrorEvent(eventMap, 0);

        throw new CvsException(
            BAD_REQUEST.value(),
            statusCode,
            statusDescription,
            statusDescription,
            BAD_REQUEST.name()
        );
    }

    /**
     * Throws a CvsException with BAD_REQUEST status when validation fails.
     * @param statusCode The status code for the error message
     * @param statusDescription The status description for the error message
     * @param eventMap Map for logging validation error events, updated with error details
     * @throws CvsException with BAD_REQUEST status, containing:
     *         - HTTP status code 400 (BAD_REQUEST)
     *         - Status code derived statusCode parameter
     *         - Error message from statusDescription parameter
     *         - BAD_REQUEST as the error source
     */
    public void throwValidationException(String statusCode, String statusDescription, Map<String, Object> eventMap) throws CvsException {
        addTags(eventMap, BAD_REQUEST.name(), statusCode, statusCode, BAD_REQUEST);

        logErrorEvent(eventMap, 0);

        throw new CvsException(
                BAD_REQUEST.value(),
                statusCode,
                statusDescription,
                statusDescription,
                BAD_REQUEST.name()
        );
    }
}