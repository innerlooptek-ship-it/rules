package com.cvshealth.digital.microservice.iqe.http;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HttpResponseMapper class
 *
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DhsHttpResponseMapper {

	private String httpResponse;
	private Integer statusCode;
	private String message;

	public DhsHttpResponseMapper(String httpResponse, Integer statusCode) {
		this.httpResponse = httpResponse;
		this.statusCode = statusCode;
	}
}