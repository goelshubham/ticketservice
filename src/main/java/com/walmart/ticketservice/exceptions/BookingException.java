package com.walmart.ticketservice.exceptions;

public class BookingException extends RuntimeException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String errorMessage;
	
	public BookingException(String message)
	{
		super(message);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	

}
