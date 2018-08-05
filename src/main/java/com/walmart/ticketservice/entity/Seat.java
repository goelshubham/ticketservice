package com.walmart.ticketservice.entity;

public class Seat {

	private String seatId;
	private String venueId;
	private int levelId;
	/*
	 * Default seat status is AVAILABLE
	 */
	private Status status = Status.AVAILABLE;

	public Seat(String seatId, String venueId, int level) {
		this.seatId = seatId;
		this.venueId = venueId;
		this.levelId = level;
		this.status = Status.AVAILABLE;
	}

	public Seat() {

	}

	public String getSeatId() {
		return seatId;
	}

	public void setSeatId(String seatId) {
		this.seatId = seatId;
	}

	public String getVenueId() {
		return venueId;
	}

	public void setVenueId(String venueId) {
		this.venueId = venueId;
	}

	public int getLevelId() {
		return levelId;
	}

	public void setLevelId(int levelId) {
		this.levelId = levelId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

}
