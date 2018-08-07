package com.walmart.ticketservice.service;

import java.util.Optional;

import com.walmart.ticketservice.entity.SeatHold;

public interface TicketService {

	/**
	* The number of seats in the venue that are neither held nor reserved
	*
	* @return the number of tickets available in the venue
	*/
    int numSeatsAvailable(String venueId, String venueLevel);

    
    /**
    * Find and hold the best available seats for a customer
    *
    * @param numSeats the number of seats to find and hold
    * @param customerEmail unique identifier for the customer
    * @param venueLevel optional level value where customer wants to hold
    * @return a SeatHold object identifying the specific seats and related
    information
    */
    SeatHold findAndHoldSeats(int numSeats, String venueLevel, String customerEmail);

    /**
    * Commit seats held for a specific customer
    *
    * @param seatHoldId the seat hold identifier
    * @param customerEmail the email address of the customer to which the
    seat hold is assigned
    * @return a reservation confirmation code
    */
    String reserveSeats(String seatHoldId, String customerEmail);



}
