package com.cvshealth.digital.microservice.iqe.http;


import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.CONNECTION_TIMEOUT;
import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.TIMEOUT;

/**
 * Processor class which actually makes the out-bound service calls.
 *
 */

@Configuration
public class DhsRestTemplateConfig  {

	/**
	 * Web client.
	 *
	 * @return the web client
	 */
	@Bean(name = "dhsSchedulingIqeAPPClient")
	public WebClient webClient() {
		return WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(
						HttpClient.create()
								.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
								.responseTimeout(Duration.ofMillis(CONNECTION_TIMEOUT))
								.doOnConnected(conn -> conn
										.addHandler(new ReadTimeoutHandler(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS))
										.addHandler(new WriteTimeoutHandler(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS))
								)
				))
				.build();
	}

}