package com.walmart.ticketservice.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;


public class InvalidRequest extends RuntimeException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String message;
	private String errorCode;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public InvalidRequest(String message) {
		super();
		this.message = message;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public InvalidRequest(String message, String errorCode) {
		super();
		this.message = message;
		this.errorCode = errorCode;
	}
	
	
	
}
