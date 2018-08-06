package com.walmart.ticketservice.types;

import java.util.List;

import com.walmart.ticketservice.entity.Seat;

public class ReserveSeatsResponse {
	
	String bookingCode;
	String customerEmail;
	String status;
	int numberOfSeats;
	String VenueId;
	List<Seat> seatList;
	public String getBookingCode() {
		return bookingCode;
	}
	public void setBookingCode(String bookingCode) {
		this.bookingCode = bookingCode;
	}
	public String getCustomerEmail() {
		return customerEmail;
	}
	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getNumberOfSeats() {
		return numberOfSeats;
	}
	public void setNumberOfSeats(int numberOfSeats) {
		this.numberOfSeats = numberOfSeats;
	}
	public String getVenueId() {
		return VenueId;
	}
	public void setVenueId(String venueId) {
		VenueId = venueId;
	}
	public List<Seat> getSeatList() {
		return seatList;
	}
	public void setSeatList(List<Seat> seatList) {
		this.seatList = seatList;
	}
	
}
