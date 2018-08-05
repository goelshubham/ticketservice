package com.walmart.ticketservice.exceptions;

public class InvalidRequest extends RuntimeException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String errorMessage;
	
	public InvalidRequest(String message)
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
