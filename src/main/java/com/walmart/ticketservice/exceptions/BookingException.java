package com.walmart.ticketservice.exceptions;

public class BookingException extends RuntimeException{
	
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
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public BookingException(String message, String errorCode) {
		super();
		this.message = message;
		this.errorCode = errorCode;
	}
	

}
