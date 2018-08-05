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
	private int numberOfSeats;
	private Map<Integer, Integer> availableSeats;
	private int holdLimit;
	private Map<Integer, List<Seat>> seatMap;
	private LocalDateTime time;
	
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
		return numberOfSeats;
	}
	public void setNumberOfSeats(int numberOfSeats) {
		this.numberOfSeats = numberOfSeats;
	}
	public int getHoldLimit() {
		return holdLimit;
	}
	public void setHoldLimit(int holdLimit) {
		this.holdLimit = holdLimit;
	}
	public Map<Integer, List<Seat>> getSeatMap() {
		return seatMap;
	}
	public void setSeatMap(Map<Integer, List<Seat>> seatMap) {
		this.seatMap = seatMap;
	}
	public LocalDateTime getTime() {
		return time;
	}
	public void setTime(LocalDateTime time) {
		this.time = time;
	}
	public Map<Integer, Integer> getAvailableSeats() {
		return availableSeats;
	}
	public void setAvailableSeats(Map<Integer, Integer> availableSeats) {
		this.availableSeats = availableSeats;
	}

}
