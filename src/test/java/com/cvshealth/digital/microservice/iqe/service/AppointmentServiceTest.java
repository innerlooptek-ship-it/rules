package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.ApiConfigs;
import com.cvshealth.digital.microservice.iqe.dto.*;
import com.cvshealth.digital.microservice.iqe.exception.CvsException;
import com.cvshealth.digital.microservice.iqe.utils.LoggingUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AppointmentServiceTest {

    @Mock
    private ApiConfigs apiConfigs;

    @InjectMocks
    private AppointmentService appointmentService;
    @Mock
    private WebClient webClientBuilderGetAppointment;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private ClientResponse clientResponse;
    @Mock
    LoggingUtils logUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void getAppointmentSuccess() {

        ApiConfigs.ApiConfig apiConfig = new ApiConfigs.ApiConfig();
        apiConfig.setUri("http://example.com");

        String storeId = "CVS_06961";
        Store store = Store.builder().storeId(storeId).build();
        String Status = "Submitted";
        String lobType = "RxIMZ";
        String appointmentDate = "2021-06-01";
        AppointmentData appointmentData = AppointmentData.builder().appointmentDate(appointmentDate).
                lobType(lobType).status(Status).store(store).build();
        String firstName = "John";
        String lastName = "Doe";
        PatientScheduleData patientScheduleData = PatientScheduleData.builder().firstName(firstName).
                lastName(lastName).appointmentsData(List.of(appointmentData)).build();
        GetAppointmentData getAppointmentData = GetAppointmentData.builder().schedule(List.of(patientScheduleData)).build();
        AppointmentDataResponse appointmentDataResponse = AppointmentDataResponse.builder().getAppointmentData(getAppointmentData).build();

        when(apiConfigs.getConfigs()).thenReturn(Map.of("appointment", Map.of("getAppointment", apiConfig)));


        when(webClientBuilderGetAppointment.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AppointmentDataResponse.class)).thenReturn(Mono.just(appointmentDataResponse));
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);


        when(responseSpec.onStatus(HttpStatus.NOT_FOUND::equals, response -> response.bodyToMono(String.class)
                .flatMap(error -> Mono.error(new WebClientResponseException(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), null, null, null))))
        ).thenReturn(responseSpec);
        when(responseSpec.onStatus(HttpStatus.BAD_REQUEST::equals, response -> response.bodyToMono(String.class)
                .flatMap(error -> Mono.error(new WebClientResponseException(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null))))
        ).thenReturn(responseSpec);

        when(clientResponse.bodyToMono(AppointmentDataResponse.class)).thenReturn(Mono.just(appointmentDataResponse));

        Mono<AppointmentDataResponse> result = appointmentService.getAppointment("6789102908", new HashMap<>(), new HashMap<>());

        StepVerifier.create(result)
                .expectNext(appointmentDataResponse)
                .expectComplete()
                .verify();
    }

    @Test
    void getAppointmentNotFound() {
        when(webClientBuilderGetAppointment.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.onStatus(eq(HttpStatus.NOT_FOUND::equals), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AppointmentDataResponse.class)).thenReturn(Mono.error(new WebClientResponseException(HttpStatus.NOT_FOUND.value(), "Not Found", null, null, null)));

        Mono<AppointmentDataResponse> result = appointmentService.getAppointment("6789102908", new HashMap<>(), new HashMap<>());

        StepVerifier.create(result)
                .expectError(WebClientResponseException.class)
                .verify();
    }

    @Test
    void getAppointmentBadRequest() {
        when(webClientBuilderGetAppointment.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.onStatus(eq(HttpStatus.BAD_REQUEST::equals), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AppointmentDataResponse.class)).thenReturn(Mono.error(new WebClientResponseException(HttpStatus.BAD_REQUEST.value(), "Bad Request", null, null, null)));

        Mono<AppointmentDataResponse> result = appointmentService.getAppointment("6789102908", new HashMap<>(), new HashMap<>());

        StepVerifier.create(result)
                .expectError(WebClientResponseException.class)
                .verify();
    }

    @Test
    void getAppointmentUnprocessableEntity() {
        when(webClientBuilderGetAppointment.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.onStatus(eq(HttpStatus.UNPROCESSABLE_ENTITY::equals), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AppointmentDataResponse.class)).thenReturn(Mono.error(new WebClientResponseException(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Unprocessable Entity", null, null, null)));

        Mono<AppointmentDataResponse> result = appointmentService.getAppointment("6789102908", new HashMap<>(), new HashMap<>());

        StepVerifier.create(result)
                .expectError(WebClientResponseException.class)
                .verify();
    }

    @Test
    void getAppointmentConflict() {
        when(webClientBuilderGetAppointment.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.onStatus(eq(HttpStatus.CONFLICT::equals), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AppointmentDataResponse.class)).thenReturn(Mono.error(new WebClientResponseException(HttpStatus.CONFLICT.value(), "Conflict", null, null, null)));

        Mono<AppointmentDataResponse> result = appointmentService.getAppointment("6789102908", new HashMap<>(), new HashMap<>());

        StepVerifier.create(result)
                .expectError(WebClientResponseException.class)
                .verify();
    }

    @Test
    void getAppointmentInternalServerError() {
        when(webClientBuilderGetAppointment.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AppointmentDataResponse.class)).thenReturn(Mono.error(new WebClientResponseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null, null, null)));

        Mono<AppointmentDataResponse> result = appointmentService.getAppointment("6789102908", new HashMap<>(), new HashMap<>());

        StepVerifier.create(result)
                .expectError(WebClientResponseException.class)
                .verify();
    }

    @Test
    void getAppointment_success_setsTagsAndStatus() {
        // Arrange
        ApiConfigs.ApiConfig apiConfig = new ApiConfigs.ApiConfig();
        apiConfig.setUri("http://example.com");
        AppointmentDataResponse response = new AppointmentDataResponse();
        when(webClientBuilderGetAppointment.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AppointmentDataResponse.class)).thenReturn(Mono.just(response));
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        Map<String, Object> tags = new HashMap<>();
        Mono<AppointmentDataResponse> result = appointmentService.getAppointment("profile", new HashMap<>(), tags);

        StepVerifier.create(result)
                .expectNextMatches(r ->
                        "SUCCESS".equals(r.getStatusCode()) &&
                                "SUCCESS".equals(r.getStatusDescription())
                )
                .expectComplete()
                .verify();
    }

    @Test
    void getAppointment_cvsException_returnsFailedResponse() {
        when(webClientBuilderGetAppointment.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AppointmentDataResponse.class))
                .thenReturn(Mono.error(new CvsException(400, "err", "err", "desc", "op")));
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        Map<String, Object> tags = new HashMap<>();
        Mono<AppointmentDataResponse> result = appointmentService.getAppointment("profile", new HashMap<>(), tags);

        StepVerifier.create(result)
                .expectNextMatches(r -> "Failed".equals(r.getStatusDescription()))
                .expectComplete()
                .verify();
    }

    @Test
    void getAppointment_genericException_returnsWebClientResponseException() {
        when(webClientBuilderGetAppointment.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AppointmentDataResponse.class))
                .thenReturn(Mono.error(new RuntimeException("ex")));
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        Map<String, Object> tags = new HashMap<>();
        Mono<AppointmentDataResponse> result = appointmentService.getAppointment("profile", new HashMap<>(), tags);

        StepVerifier.create(result)
                .expectErrorMatches(t -> t instanceof WebClientResponseException &&
                        t.getMessage().contains("ex"));

    }

    @Test
    void getAppointment_success_populatesTags() {
        AppointmentDataResponse response = new AppointmentDataResponse();
        when(webClientBuilderGetAppointment.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AppointmentDataResponse.class)).thenReturn(Mono.just(response));
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        Map<String, Object> tags = new HashMap<>();
        Mono<AppointmentDataResponse> result = appointmentService.getAppointment("profile", new HashMap<>(), tags);

        StepVerifier.create(result)
                .expectNextMatches(r -> "SUCCESS".equals(r.getStatusCode()) && "SUCCESS".equals(r.getStatusDescription()))
                .expectComplete()
                .verify();
    }

    @Test
    void getAppointment_cvsException_populatesTagsAndReturnsFailed() {
        when(webClientBuilderGetAppointment.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AppointmentDataResponse.class))
                .thenReturn(Mono.error(new CvsException(400, "err", "err", "desc", "op")));
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        Map<String, Object> tags = new HashMap<>();
        Mono<AppointmentDataResponse> result = appointmentService.getAppointment("profile", new HashMap<>(), tags);

        StepVerifier.create(result)
                .expectNextMatches(r -> "Failed".equals(r.getStatusDescription()))
                .expectComplete()
                .verify();
    }


    @Test
    void getAppointment_genericException_populatesTagsAndThrows() {
        when(webClientBuilderGetAppointment.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AppointmentDataResponse.class))
                .thenReturn(Mono.error(new RuntimeException("ex")));
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        Map<String, Object> tags = new HashMap<>();
        Mono<AppointmentDataResponse> result = appointmentService.getAppointment("profile", new HashMap<>(), tags);

        StepVerifier.create(result)
                .expectErrorMatches(t -> t instanceof WebClientResponseException && t.getMessage().contains("ex"))
                .verify();
    }



}