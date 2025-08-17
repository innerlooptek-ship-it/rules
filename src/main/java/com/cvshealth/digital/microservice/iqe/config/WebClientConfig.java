package com.cvshealth.digital.microservice.iqe.config;

import io.netty.channel.ChannelOption;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@AllArgsConstructor
public class WebClientConfig {
    ApiConfigs apiConfig;

    @Bean
    @Primary
    public WebClient webClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl("${service.context-path}")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return webClient;
    }
    @Bean(name = "iQeMcCoreQuestionnarie")
    public WebClient webClientIQEMcCoreQuestionnarie() {
        ApiConfigs.ApiConfig config = apiConfig.getConfigs().get("iqe").get("getIQEMcCoreQuestionnarie");
        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(addTimeout(config.getReadTimeout(),config.getConnectionTimeout()))
                .build();

    }
    @Bean(name = "mcitGetPatientConsents")
    public WebClient webClientMCITGetPatientConsents() {
        ApiConfigs.ApiConfig config = apiConfig.getConfigs().get("consents").get("getPatientConsents");
        return WebClient.builder()
                .baseUrl(config.getBaseUrl() + config.getUri())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeaders(headers -> headers.setBasicAuth(config.getUserName(), config.getPassword()))
                .clientConnector(addTimeout(config.getReadTimeout(), config.getConnectionTimeout()))
                .build();
    }
    @Bean(name = "dynamicFlowConditionEvaluation")
    public WebClient webClientIQEMcCoreGetQuestionnarie() {
        ApiConfigs.ApiConfig config = apiConfig.getConfigs().get("iqe").get("dynamicFlowConditionEvaluation");
        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(addTimeout(config.getReadTimeout(),config.getConnectionTimeout()))
                .build();

    }
    @Bean(name = "webClientBuilderGetAppointmentData")
    public WebClient webClientBuilderGetAppointmentData() {
        ApiConfigs.ApiConfig config = apiConfig.getConfigs().get("appointment").get("getAppointment");
        return WebClient.builder()
                .baseUrl(config.getBaseUrl() + config.getUri())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(addTimeout(config.getReadTimeout(), config.getConnectionTimeout()))
                .build();
    }

    /**
     *
     * Adds the timeout.
     *
     * @param readTimeout the readTimeout timeout
     * @return the reactor client http connector
     */
    public ReactorClientHttpConnector addTimeout(int readTimeout, int connectionTimeout) {
        return new ReactorClientHttpConnector(getHttpClient(readTimeout, connectionTimeout));
    }
    /**
     * Gets the http client.
     *
     * @param readTimeout the response timeout
     * @return the http client
     */
    private HttpClient getHttpClient(int readTimeout, int connectionTimeout) {
        return HttpClient.create()
                .responseTimeout(Duration.ofMillis(readTimeout))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout);
    }
}