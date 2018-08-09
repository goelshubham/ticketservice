package com.walmart.ticketservice.restcontroller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.walmart.ticketservice.entity.SeatHold;
import com.walmart.ticketservice.repository.BookingRepository;
import com.walmart.ticketservice.service.TicketService;
import com.walmart.ticketservice.types.FindSeatsRequest;
import com.walmart.ticketservice.types.FindSeatsResponse;
import com.walmart.ticketservice.types.HoldSeatsRequest;
import com.walmart.ticketservice.types.ReserveSeatsRequest;
import com.walmart.ticketservice.types.ReserveSeatsResponse;

@RestController
@RequestMapping("/ticketservive")
public class TicketServiceRestController {
	
	@Autowired
	private TicketService ticketService;
	
	@Autowired
	private BookingRepository bookingRepo;
	
	@RequestMapping(value= "/api/numSeatsAvailable", method=RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<FindSeatsResponse> numSeatsAvailable(@RequestBody FindSeatsRequest findSeatsRequest)
	{
		int numOfSeats = 0;
		if (findSeatsRequest != null) {
			System.out.println("Venue is -> " + findSeatsRequest.getVenueId());
			numOfSeats = ticketService.numSeatsAvailable(findSeatsRequest.getVenueId(),
					findSeatsRequest.getLevelNumber());
		}

		FindSeatsResponse findSeatsResponse = new FindSeatsResponse();
		findSeatsResponse.setNumberOfSeats(numOfSeats);
		return new ResponseEntity<FindSeatsResponse>(findSeatsResponse, HttpStatus.OK);
	}
	
	@RequestMapping(value= "/api/findAndHoldSeats", method=RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<SeatHold> findAndHoldSeats(@RequestBody HoldSeatsRequest holdSeatsRequest)
	{
		SeatHold seatHoldResponse = null;
		if (holdSeatsRequest != null) {
			seatHoldResponse = new SeatHold();
			seatHoldResponse = ticketService.findAndHoldSeats(holdSeatsRequest.getNumberOfSeats(),
					holdSeatsRequest.getVenueId(), holdSeatsRequest.getCustomerEmailId());
		}
		
		return new ResponseEntity<SeatHold>(seatHoldResponse, HttpStatus.OK);
	}
	
	@RequestMapping(value= "/api/reserveSeats", method=RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<ReserveSeatsResponse> reserveSeats(@RequestBody ReserveSeatsRequest reserveSeatsRequest)
	{
		String bookingID = null;
		SeatHold seatHold = null;
		if (reserveSeatsRequest != null) {
			bookingID = this.ticketService.reserveSeats(reserveSeatsRequest.getSeatHoldId(),
					reserveSeatsRequest.getCustomerEmail());
		}

		Optional<SeatHold> booking = this.bookingRepo.findById(bookingID);
		if (booking.isPresent())
			seatHold = booking.get();

		ReserveSeatsResponse reserveSeatsResponse = new ReserveSeatsResponse();
		reserveSeatsResponse.setBookingCode(bookingID);
		reserveSeatsResponse.setCustomerEmail(seatHold.getCustomerEmail());
		reserveSeatsResponse.setNumberOfSeats(seatHold.getTotalSeats());
		reserveSeatsResponse.setStatus("RESERVED");
		reserveSeatsResponse.setVenueId(seatHold.getVenueId());

		return new ResponseEntity<ReserveSeatsResponse>(reserveSeatsResponse, HttpStatus.OK);
	}
	
	
	

}
