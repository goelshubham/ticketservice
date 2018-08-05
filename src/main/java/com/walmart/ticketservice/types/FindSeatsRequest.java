package com.walmart.ticketservice.types;

/**
 * @author sgoel201
 *
 */
public class FindSeatsRequest {
	
	String venueId;
	String levelNumber;
	
	public String getVenueId() {
		return venueId;
	}
	public void setVenueId(String venueId) {
		this.venueId = venueId;
	}
	public String getLevelNumber() {
		return levelNumber;
	}
	public void setLevelNumber(String levelNumber) {
		this.levelNumber = levelNumber;
	}
}
