package com.walmart.ticketservice.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="seathold")
public class SeatHold {

	@Id
	private String bookingId;
    private String customerEmail;
    private Long bookingTime;
    private List<Seat> seatList;
    private int level;
    private String venueId;
    private int totalSeats;
    private Status status;
    
    public SeatHold(String bookingId, String customerEmail, List<Seat> seatList, int level, String venueId,
			int totalSeats, Status status) {
		this.customerEmail = customerEmail;
		this.seatList = seatList;
		this.level = level;
		this.venueId = venueId;
		this.totalSeats = totalSeats;
		this.bookingId = bookingId;
		this.bookingTime = System.currentTimeMillis();
		this.status = status;
	}
    

	public SeatHold() {
		super();
		// TODO Auto-generated constructor stub
	}


	public String getCustomerEmail() {
		return customerEmail;
	}
	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}
	public List<Seat> getSeatList() {
		return seatList;
	}
	public void setSeatList(List<Seat> seatList) {
		this.seatList = seatList;
	}
	public int getTotalSeats() {
		return totalSeats;
	}
	public void setTotalSeats(int totalSeats) {
		this.totalSeats = totalSeats;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getVenueId() {
		return venueId;
	}
	public void setVenueId(String venueId) {
		this.venueId = venueId;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String getBookingId() {
		return bookingId;
	}
	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}


	public Long getBookingTime() {
		return bookingTime;
	}


	public void setBookingTime(Long bookingTime) {
		this.bookingTime = bookingTime;
	}
}
