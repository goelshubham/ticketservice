package com.walmart.ticketservice.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "venue")
public class Venue {
	
	@Id
	private String venueId;
	private int numberOfLevels;
	private int totalNumberOfSeats;
	private Map<Integer, Integer> availableSeatsAtLevel;
	private Map<Integer, List<Seat>> seatMap;
	
	public String getVenueId() {
		return venueId;
	}
	public void setVenueId(String venueId) {
		this.venueId = venueId;
	}
	public int getNumberOfLevels() {
		return numberOfLevels;
	}
	public void setNumberOfLevels(int numberOfLevels) {
		this.numberOfLevels = numberOfLevels;
	}
	public int getNumberOfSeats() {
		return totalNumberOfSeats;
	}
	public void setNumberOfSeats(int numberOfSeats) {
		this.totalNumberOfSeats = numberOfSeats;
	}
	public Map<Integer, List<Seat>> getSeatMap() {
		return seatMap;
	}
	public void setSeatMap(Map<Integer, List<Seat>> seatMap) {
		this.seatMap = seatMap;
	}
	public Map<Integer, Integer> getAvailableSeats() {
		return availableSeatsAtLevel;
	}
	public void setAvailableSeats(Map<Integer, Integer> availableSeats) {
		this.availableSeatsAtLevel = availableSeats;
	}

}
