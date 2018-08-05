package com.walmart.ticketservice.types;

/**
 * @author sgoel201
 *
 */
public class FindSeatsResponse {
	
	Integer numberOfAvailableSeats;

	public Integer getNumberOfSeats() {
		return numberOfAvailableSeats;
	}

	public void setNumberOfSeats(Integer numberOfSeats) {
		this.numberOfAvailableSeats = numberOfSeats;
	}
	
	

}
