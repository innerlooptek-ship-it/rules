package com.cvshealth.digital.microservice.iqe.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class CvsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The error code. */
	private String statusCode;

	/** The error desc. */
	private String statusDescription;

	private String source;
	
	private int httpStatusCode;

	public CvsException() {
		super();
	}

	public CvsException(String message) {
		super(message);
	}
	
	
	public CvsException(String message ,int httpStatusCode) {
		super(message);
		this.httpStatusCode = httpStatusCode;
	}
	
	public CvsException(String statusCode, String StatusDescription) {
		super();
		this.statusCode = statusCode;
		this.statusDescription = StatusDescription;
	}

	/**
	 *
	 * @param httpStatusCode - HTTP Status code
	 * @param statusCode - Status Code e.g., BAD_REQUEST
	 * @param StatusDescription - Status Description e.g., Bad Request
	 * @param message - Domain response or more details
	 * @param source - exception source e.g., MC,IMZ
	 */
	public CvsException(int httpStatusCode, String statusCode, String StatusDescription, String message, String source) {
		super(message);
		this.httpStatusCode = httpStatusCode;
		this.statusCode = statusCode;
		this.statusDescription = StatusDescription;
		this.source = source;
	}
}