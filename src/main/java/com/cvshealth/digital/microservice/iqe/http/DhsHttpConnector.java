package com.cvshealth.digital.microservice.iqe.http;

import com.cvshealth.digital.microservice.iqe.error.ServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Processor class which actually makes the out-bound service calls.
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DhsHttpConnector  {

	private final WebClient dhsSchedulingIqeAPPClient ;


	/**
	 * Invoke GET service.
	 *
	 * @param serviceUrl the service url
	 * @param eventMap the event map
	 * @return the string
	 */
	public Mono<String> invokeGETService(String serviceUrl, Map<String, String> eventMap) {
		return Mono.deferContextual(ctx -> {
					log.debug("invokeGETService ServiceUrl....{}", serviceUrl);
					return dhsSchedulingIqeAPPClient.get().uri(serviceUrl)
							.retrieve()
							.bodyToMono(String.class)
							.doOnNext(response -> log.debug("invokeGETService response....{}", response))
							.onErrorResume(e -> {
								log.info("Record Not Found in Caching" );
								return Mono.empty();
							})
							.doFinally(signal -> log.debug("Exiting invokeGETService method of {}", this.getClass().getName()));
				});
	}

	/**
	 * Invoke POST service.
	 *
	 * @param request   the request
	 * @param serviceUrl the service url
	 * @param eventMap  the event map
	 * @return the Mono containing the response
	 */
	public Mono<DhsHttpResponseMapper> invokePOSTService(String request, String serviceUrl, Map<String, String> eventMap) {
		return Mono.deferContextual(ctx -> {
					log.info("Calling invokePOSTService");
					return dhsSchedulingIqeAPPClient.post().uri(serviceUrl)
							.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
							.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
							.bodyValue(request)
							.retrieve()
							.bodyToMono(String.class)
							.map(response -> {
								return new DhsHttpResponseMapper(response, 200);
							})
							.onErrorResume(WebClientResponseException.class, e -> {
								if (e.getStatusCode().is4xxClientError()) {
									return Mono.error(new ServerErrorException("5009",e.getMessage()));
								} else if (e.getStatusCode().is5xxServerError()) {

									return Mono.error(new ServerErrorException("5009",e.getMessage()));
								} else {

									return Mono.error(new Exception("Unexpected status code: " + e.getStatusCode().value()));
								}
							});
				});
	}

	/**
	 * Invoke DELETE service.
	 *
	 * @param serviceUrl the service url
	 * @param eventMap   the event map
	 * @return the Mono containing the response
	 */
	public Mono<DhsHttpResponseMapper> invokeDELETEService(String serviceUrl, Map<String, String> eventMap) {
		return Mono.deferContextual(ctx -> {
			log.info("Calling invokeDELETEService");
			return dhsSchedulingIqeAPPClient.delete().uri(serviceUrl)
					.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
					.retrieve()
					.bodyToMono(String.class)
					.map(response -> {
						log.info("invokeDELETEService response....{}", response);
						return new DhsHttpResponseMapper(response, 200);
					})
					.onErrorResume(WebClientResponseException.class, e -> {
						if (e.getStatusCode().is4xxClientError()) {
							return Mono.error(new ServerErrorException("5009",e.getMessage()));
						} else if (e.getStatusCode().is5xxServerError()) {

							return Mono.error(new ServerErrorException("5009",e.getMessage()));
						} else {

							return Mono.error(new Exception("Unexpected status code: " + e.getStatusCode().value()));
						}
					});
		});
	}

}