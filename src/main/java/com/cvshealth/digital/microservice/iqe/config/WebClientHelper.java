package com.cvshealth.digital.microservice.iqe.config;


import com.cvshealth.digital.microservice.iqe.dto.DomainApiError;
import com.cvshealth.digital.microservice.iqe.enums.ErrorKey;
import com.cvshealth.digital.microservice.iqe.exception.CvsException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.cvshealth.digital.microservice.iqe.constants.DhsCoreConstants.CONST_CATEGORY;
import static com.cvshealth.digital.microservice.iqe.constants.DhsCoreConstants.CONST_X_CAT;
import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.*;


@Component
@AllArgsConstructor
public class WebClientHelper {

    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, String> errorMessages;

    @Autowired
    public WebClientHelper(MessageConfig messagesConfig) {
        this.errorMessages = messagesConfig.getMessages();
    }

    /**
     * Creates and configures a WebClient response specification with standard headers, request body,
     * and comprehensive error handling for HTTP status codes.
     *
     * @param <T> The type of the request body
     * @param webClient The WebClient instance to use for the request
     * @param requestBody The body content to be sent with the request
     * @param requestBodyClass The class type of the request body for serialization
     * @param errorKey The error key used for error message lookup and status description generation
     * @param headers Map of custom headers to be applied to the request
     * @param tags Map containing metadata for error tracking and monitoring
     *
     * @return A WebClient.ResponseSpec configured with headers, body, and error handlers
     *
     * @apiNote Standard headers are set with fallback values if not provided:
     *          - category: defaults to CONST_DEFAULT_CATEGORY
     *          - x-grid: defaults to "noXGrid"
     *          - grid: defaults to "noGrid"
     *          - exp-id: defaults to "noExpId"
     *          - client-ref-id: defaults to "noClientRefId"
     *
     * @implNote Error handling is configured for the following HTTP status codes:
     *          - 500 Internal Server Error
     *          - 503 Service Unavailable
     *          - 502 Bad Gateway
     *          - 504 Gateway Timeout
     *          - 409 Conflict
     *          - 401 Unauthorized (handled as CvsException)
     *
     * @see org.springframework.web.reactive.function.client.WebClient
     * @see org.springframework.http.HttpMethod
     * @see org.springframework.http.HttpStatus
     */
    public <T> WebClient.ResponseSpec createWebClient(
        WebClient webClient,
        T requestBody,
        Class<T> requestBodyClass,
        ErrorKey errorKey,
        Map<String, String> headers,
        Map<String, Object> tags
    ) {

        return webClient
            .method(HttpMethod.POST)
            .header(CONST_CATEGORY, getHeaderOrDefault(headers, CONST_CATEGORY, CONST_DEFAULT_CATEGORY))
            .header(CONST_X_CAT, getHeaderOrDefault(headers, CONST_X_CAT, CONST_DEFAULT_CATEGORY))
            .header(CONST_X_GRID, getHeaderOrDefault(headers, CONST_X_GRID, "noXGrid"))
            .header(CONST_GRID, getHeaderOrDefault(headers, CONST_GRID, "noGrid"))
            .header(CONST_EXP_ID, getHeaderOrDefault(headers, CONST_EXP_ID, "noExpId"))
            .header(CONST_CLIENTREFID, getHeaderOrDefault(headers, CONST_CLIENTREFID, "noClientRefId"))
            .body(Mono.just(requestBody), requestBodyClass)
            .retrieve()
            .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, onStatusWebClientException(HttpStatus.INTERNAL_SERVER_ERROR, tags))
            .onStatus(HttpStatus.SERVICE_UNAVAILABLE::equals, onStatusWebClientException(HttpStatus.SERVICE_UNAVAILABLE, tags))
            .onStatus(HttpStatus.BAD_GATEWAY::equals, onStatusWebClientException(HttpStatus.BAD_GATEWAY, tags))
            .onStatus(HttpStatus.GATEWAY_TIMEOUT::equals, onStatusWebClientException(HttpStatus.GATEWAY_TIMEOUT, tags))
            .onStatus(HttpStatus.CONFLICT::equals, onStatusWebClientException(HttpStatus.CONFLICT, tags))
            .onStatus(HttpStatus.UNAUTHORIZED::equals, onStatusCvsException(HttpStatus.UNAUTHORIZED.name(), errorKey, HttpStatus.UNAUTHORIZED, tags));
    }

    /**
     * Builds and configures a {@link WebClient.ResponseSpec} for performing an HTTP request with the specified parameters.
     * <p>
     * This method allows flexible setup of the WebClient, supporting custom HTTP methods, query parameters, request body, headers,
     * and error handling. Custom error handlers are registered for several common HTTP error statuses.
     *
     * @param webClient          the {@link WebClient} instance used to perform the HTTP request
     * @param httpMethod         the {@link HttpMethod} (GET, POST, etc.) for this request
     * @param queryParams        a map of query parameter names and values to be appended to the URI. May be {@code null}.
     * @param requestBody        the body to send with the request (for POST/PUT/PATCH)
     * @param requestBodyClass   the class type of the request body
     * @param errorKey           the {@link ErrorKey} used for exception handling and tracking
     * @param headers            a map containing the header names and values to set for the request
     * @param tags               a map of tag names to values for logging, tracing, or error handling context
     * @param <T>                the type of the request body
     * @return                   a configured {@link WebClient.ResponseSpec} ready for further response handling
     */
    public <T> WebClient.ResponseSpec createWebClient(
        WebClient webClient,
        HttpMethod httpMethod,
        Map<String, String> queryParams,
        T requestBody,
        Class<T> requestBodyClass,
        ErrorKey errorKey,
        Map<String, String> headers,
        Map<String, Object> tags
    ) {

        return webClient
            .method(httpMethod)
            .uri(uriBuilder -> uriBuilder(queryParams, uriBuilder))
            .header(CONST_CATEGORY, getHeaderOrDefault(headers, CONST_CATEGORY, CONST_DEFAULT_CATEGORY))
            .header(CONST_X_GRID, getHeaderOrDefault(headers, CONST_X_GRID, "noXGrid"))
            .header(CONST_GRID, getHeaderOrDefault(headers, CONST_GRID, "noGrid"))
            .header(CONST_EXP_ID, getHeaderOrDefault(headers, CONST_EXP_ID, "noExpId"))
            .header(CONST_CLIENTREFID, getHeaderOrDefault(headers, CONST_CLIENTREFID, "noClientRefId"))
            .body(Mono.just(requestBody), requestBodyClass)
            .retrieve()
            .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, onStatusWebClientException(HttpStatus.INTERNAL_SERVER_ERROR, tags))
            .onStatus(HttpStatus.SERVICE_UNAVAILABLE::equals, onStatusWebClientException(HttpStatus.SERVICE_UNAVAILABLE, tags))
            .onStatus(HttpStatus.BAD_GATEWAY::equals, onStatusWebClientException(HttpStatus.BAD_GATEWAY, tags))
            .onStatus(HttpStatus.GATEWAY_TIMEOUT::equals, onStatusWebClientException(HttpStatus.GATEWAY_TIMEOUT, tags))
            .onStatus(HttpStatus.CONFLICT::equals, onStatusWebClientException(HttpStatus.CONFLICT, tags))
            .onStatus(HttpStatus.UNAUTHORIZED::equals, onStatusCvsException(HttpStatus.UNAUTHORIZED.name(), errorKey, HttpStatus.UNAUTHORIZED, tags));
    }

    /**
     * Builds a {@link URI} by applying the provided query parameters to the given {@link UriBuilder}.
     * <p>
     * Each entry in the {@code queryParams} map is added as a query parameter to the URI if the value is not {@code null}.
     *
     * @param queryParams a map of query parameter names and their values; if {@code null}, no parameters are added
     * @param uriBuilder  the {@link UriBuilder} instance used to construct the URI
     * @return the constructed {@link URI} with query parameters applied
     */
    private static URI uriBuilder(Map<String, String> queryParams, UriBuilder uriBuilder) {
        if (queryParams != null)
            queryParams.forEach((key, value) -> {
                if (value != null) uriBuilder.queryParam(key, value);
            });

        return uriBuilder.build();
    }

    /**
     * Creates an error handling function for WebClient responses that converts error responses
     * into WebClientResponseExceptions.
     *
     * @param httpStatus The HTTP status code that triggered the error handler
     * @param tags Map for monitoring and tracking information; will be updated with:
     *            - HTTP status name
     *            - "WebClientException" as status code
     *            - Error response body
     *            - HTTP status value
     *
     * @return A function that processes ClientResponse and returns a Mono containing
     *         WebClientResponseException with:
     *         - The specified HTTP status code value
     *         - The HTTP status name as reason phrase
     *         - Null values for headers, body, and charset
     *
     * @implNote The function extracts the response body as a String and adds it to the tags
     *          before creating the exception. This enables tracking of the original error
     *          while providing a standardized exception format.
     *
     * @see org.springframework.web.reactive.function.client.WebClientResponseException
     * @see org.springframework.web.reactive.function.client.ClientResponse
     * @see org.springframework.http.HttpStatus
     */
    public Function<ClientResponse, Mono<? extends Throwable>> onStatusWebClientException(
        HttpStatus httpStatus,
        Map<String, Object> tags
    ) {

        return clientResponse -> clientResponse.bodyToMono(String.class).flatMap(error -> {
            addTags(tags, httpStatus.name(), "WebClientException", error, httpStatus);

            return Mono.error(new WebClientResponseException(httpStatus.value(), httpStatus.name(), null, null, null));
        });
    }

    /**
     * Creates and configures a WebClient response specification with standard headers and request body.
     * Sets default values for required headers if not provided in the header map.
     *
     * @param <T> The type of the request body
     * @param webClient The WebClient instance to use for the request
     * @param requestBody The body of the request to be sent
     * @param requestBodyClass The class type of the request body for proper serialization
     * @param headers Map of custom headers to be applied to the request.
     * @return A WebClient.ResponseSpec configured with the specified headers and body
     *
     * @implNote
     * Default header values are applied when headers are null or specific keys are missing:
     * - category: defaults to configured default category
     * - x-grid: defaults to "noXGrid"
     * - grid: defaults to "noGrid"
     * - exp-id: defaults to "noExpId"
     * - client-ref-id: defaults to "noClientRefId"
     *
     * @see WebClient
     * @see WebClient.ResponseSpec
     */
    public <T> WebClient.ResponseSpec createWebClient(
        WebClient webClient,
        T requestBody,
        Class<T> requestBodyClass,
        Map<String, String> headers
    ) {

        return webClient
            .method(HttpMethod.POST)
            .header(CONST_X_CAT, getHeaderOrDefault(headers, CONST_X_CAT, CONST_DEFAULT_CATEGORY))
            .header(CONST_CATEGORY, getHeaderOrDefault(headers, CONST_CATEGORY, CONST_DEFAULT_CATEGORY))
            .header(CONST_X_GRID, getHeaderOrDefault(headers, CONST_X_GRID, "noXGrid"))
            .header(CONST_GRID, getHeaderOrDefault(headers, CONST_GRID, "noGrid"))
            .header(CONST_EXP_ID, getHeaderOrDefault(headers, CONST_EXP_ID, "noExpId"))
            .header(CONST_CLIENTREFID, getHeaderOrDefault(headers, CONST_CLIENTREFID, "noClientRefId"))
            .body(Mono.just(requestBody), requestBodyClass)
            .retrieve();
    }

    /**
     * Retrieves a value from a header map using the specified key, returning a default value if
     * either the map is null or the key is not present.
     *
     * @param header The map containing header key-value pairs
     * @param key The key to look up in the header map
     * @param defaultValue The value to return if the header map is null or the key is not found
     * @return The value associated with the key in the header map, or the defaultValue if not found
     */
    private String getHeaderOrDefault(Map<String, String> header, String key, String defaultValue) {
        return header != null && header.get(key) != null ? header.get(key) : defaultValue;
    }

    /**
     * Creates a function that transforms client error responses into CvsException errors.
     * This method is typically used with WebClient's error status handlers to provide
     * consistent error handling across API calls.
     *
     * @param statusCode The business-specific status code to be used in the error response
     * @param errorKey The key used to look up error messages and generate status descriptions
     * @param httpStatus The HTTP status code that triggered this error handler
     * @param tags A map that will be updated with error tracking information including
     *            - Status code
     *            - Status description
     *            - Error details
     *            - HTTP status
     *            - Class name reference
     *
     * @return A Function that transforms ClientResponse into a Mono containing a CvsException.
     *         The CvsException will contain:
     *         - HTTP status value
     *         - Business status code
     *         - Status description from error messages
     *         - Original error response body
     *         - Error key for reference
     *
     * @implNote
     * - Retrieves error message using format: "{errorKey}.{statusCode}"
     * - Adds error tracking information to the provided tags map
     * - Includes class name in tags for debugging purposes
     *
     * @see
     * @see ClientResponse
     * @see HttpStatus
     */
    public Function<ClientResponse, Mono<? extends Throwable>> onStatusCvsException(
        String statusCode,
        ErrorKey errorKey,
        HttpStatus httpStatus,
        Map<String, Object> tags
    ) {

        return clientResponse -> clientResponse
            .bodyToMono(String.class)
            .flatMap(error -> {

                String msgKey = errorKey.getKey() + "." + statusCode;
                String statusDescription = errorMessages.get(msgKey);

                addTags(tags, statusCode, statusDescription, error, httpStatus);

                tags.put(this.getClass().getName(), "onStatusCvsException");

                return Mono.error(
                    new CvsException(
                        httpStatus.value(),
                        statusCode,
                        statusDescription,
                        error,
                        errorKey.name()
                    )
                );
            });
    }

    /**
     * Creates an error handling function for domain-specific API errors that processes
     * the error response and converts it into a CvsException.
     *
     * @param httpStatus The HTTP status code associated with the error response
     * @param responseType The type of response being processed (used for error tracking and tagging)
     * @param tags A map for collecting monitoring and tracking information about the error
     * @return A function that processes the ClientResponse, extracts the domain error information,
     *         and returns a Mono containing a CvsException with detailed error information
     *
     * @implNote The function:
     *          - Deserializes the response body into a DomainApiError object
     *          - Extracts status code, description, and fault information
     *          - Records error details in the monitoring tags
     *          - Includes the class name in tags for error source tracking
     *          - Creates a CvsException with the complete error context
     *
     * @see
     * @see CvsException
     * @see ClientResponse
     */
    public Function<ClientResponse, Mono<? extends Throwable>> onStatusApiError(
        HttpStatus httpStatus,
        String responseType,
        Map<String, Object> tags
    ) {

        return clientResponse -> clientResponse
            .bodyToMono(DomainApiError.class)
            .flatMap(error -> {
                String statusCode = error.getStatusCode();
                String statusDescription = error.getStatusDescription();
                String statusMessage = parseFault(error.getFault());

                addTags(httpStatus, responseType, tags, statusCode, statusDescription, statusMessage);

                tags.put(this.getClass().getName(), "onStatusApiError");

                return Mono.error(
                    new CvsException(
                        httpStatus.value(),
                        statusCode,
                        statusDescription,
                        statusMessage,
                        responseType
                    )
                );
            });
    }

    /**
     * Adds response-specific tags to the monitoring tags map with prefixed keys.
     * Each tag key is prefixed with the responseType to distinguish between different
     * response categories in the monitoring system.
     *
     * @param httpStatus The HTTP status code of the response
     * @param responseType The type of response (e.g., "REQUEST", "RESPONSE") used as prefix for tag keys
     * @param tags The map to which the prefixed tags will be added
     * @param statusCode The business or application status code
     * @param statusDescription A human-readable description of the status
     * @param statusMessage Additional message providing context about the status
     *
     * @implNote The following key-value pairs are added to the tag map with "{responseType}_" prefix:
     *          - {responseType}_STATUS_CDE: The status code
     *          - {responseType}_STATUS_DESC: The status description
     *          - {responseType}_STATUS_MESSAGE: The status message
     *          - {responseType}_HTTP_STATUS_CDE: The numeric HTTP status code value
     *
     * @throws NullPointerException if tags map is null
     */
    private static void addTags(
        HttpStatus httpStatus,
        String responseType,
        Map<String, Object> tags,
        String statusCode,
        String statusDescription,
        String statusMessage
    ) {

        tags.put(String.format("%s_%s", responseType, STATUS_CDE), statusCode);
        tags.put(String.format("%s_%s", responseType, STATUS_DESC), statusDescription);
        tags.put(String.format("%s_%s", responseType, STATUS_MESSAGE), statusMessage);
        tags.put(String.format("%s_%s", responseType, HTTP_STATUS_CDE), httpStatus.value());
    }

    /**
     * Adds standard status and HTTP response tags to the provided tags map.
     * These tags are typically used for monitoring, logging, and metrics purposes.
     *
     * @param tags The map to which the status tags will be added
     * @param statusCode The business or application status code
     * @param statusDescription A human-readable description of the status
     * @param statusMessage Additional message providing context about the status
     * @param httpStatusCode The HTTP status code of the response
     *
     * @implNote The following key-value pairs are added to the tag map:
     *          - STATUS_CDE: The status code
     *          - STATUS_DESC: The status description
     *          - STATUS_MESSAGE: The status message
     *          - HTTP_STATUS_CDE: The numeric HTTP status code value
     */
    public void addTags(
        Map<String, Object> tags,
        String statusCode,
        String statusDescription,
        String statusMessage,
        HttpStatus httpStatusCode
    ) {

        tags.put(STATUS_CDE, statusCode);
        tags.put(STATUS_DESC, statusDescription);
        tags.put(STATUS_MESSAGE, statusMessage);
        tags.put(HTTP_STATUS_CDE, httpStatusCode.value());
    }

    /**
     * Parses a DomainApiError.Fault an object into a human-readable string representation.
     * Formats both the main fault information and any associated error details into a
     * structured string format.
     *
     * @param fault The fault object to parse. Can be null.
     * @return A formatted string containing the fault details with the following structure:
     *         - Basic fault information: type, title, and additional info
     *         - If errors are present, includes a list of error details with their type, title, and field
     *         Example:
     *         "Fault: Type: ValidationError, Title: Invalid Input, More Info: Check parameters;
     *          Errors: [{Type: FieldError, Title: Invalid Format, Field: email}]"
     *         If fault is null, returns "Fault: No information available."
     *
     * @implNote
     * - Handles null values for all fault properties by using "Unknown" or "N/A" as fallbacks
     * - Constructs the string using StringBuilder for efficiency
     * - Removes trailing comma and space from the error list if present
     */
    public static String parseFault(DomainApiError.Fault fault) {
        if (fault == null) {
            return "Fault: No information available.";
        }

        StringBuilder sb = new StringBuilder();

        // Extract basic fault information
        sb.append("Fault: ");
        sb.append("Type: ").append(fault.getType() != null ? fault.getType() : "Unknown").append(", ");
        sb.append("Title: ").append(fault.getTitle() != null ? fault.getTitle() : "Unknown").append(", ");
        sb.append("More Info: ").append(fault.getMoreInfo() != null ? fault.getMoreInfo() : "N/A");

        // Handle the "errors" field, if present
        List<DomainApiError.Fault.Error> errors = fault.getErrors();
        if (errors != null && !errors.isEmpty()) {
            sb.append("; Errors: [");

            for (DomainApiError.Fault.Error error : errors) {
                sb.append("{Type: ").append(error.getType() != null ? error.getType() : "Unknown").append(", ");
                sb.append("Title: ").append(error.getTitle() != null ? error.getTitle() : "Unknown").append(", ");
                sb.append("Field: ").append(error.getField() != null ? error.getField() : "Unknown").append("}, ");
            }

            // Remove last comma and space
            sb.setLength(sb.length() - 2);

            sb.append("]");
        }

        return sb.toString();
    }
}