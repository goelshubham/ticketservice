package com.walmart.ticketservice.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.walmart.ticketservice.entity.SeatHold;
import com.walmart.ticketservice.service.TicketService;
import com.walmart.ticketservice.types.FindSeatsRequest;
import com.walmart.ticketservice.types.FindSeatsResponse;
import com.walmart.ticketservice.types.HoldSeatsRequest;
import com.walmart.ticketservice.types.ReserveSeatsRequest;

@RestController
public class TicketServiceRestController {
	
	@Autowired
	private TicketService ticketService;
	
	@RequestMapping(value= "/api/findSeats", method=RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<FindSeatsResponse> numSeatsAvailable(@RequestBody FindSeatsRequest findSeatsRequest)
	{
		if(findSeatsRequest != null)
		{
			System.out.println("Venue is -> " + findSeatsRequest.getVenueId());
			ticketService.numSeatsAvailable(findSeatsRequest.getVenueId(), findSeatsRequest.getLevelNumber());
		}
		else
		{
			//request is null
		}
		return null;
	}
	
	@RequestMapping(value= "/api/holdSeats", method=RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<SeatHold> findAndHoldSeats(@RequestBody HoldSeatsRequest holdSeatsRequest)
	{
		ticketService.findAndHoldSeats(holdSeatsRequest.getNumberOfSeats(), holdSeatsRequest.getVenueId(), holdSeatsRequest.getCustomerEmailId());
		return null;
	}
	
	@RequestMapping(value= "/api/reserveSeats", method=RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<SeatHold> reserveSeats(@RequestBody ReserveSeatsRequest reserveSeatsRequest)
	{
		System.out.println("/api/reserveSeats/" + reserveSeatsRequest.getSeatHoldId() + "/" + reserveSeatsRequest.getCustomerEmail());
		SeatHold obj = new SeatHold();
		//obj.setBookingCode("1111111111111");
		obj.setCustomerEmail(reserveSeatsRequest.getCustomerEmail());
		return new ResponseEntity<SeatHold>(obj, HttpStatus.ACCEPTED);
	}
	
	
	

}
