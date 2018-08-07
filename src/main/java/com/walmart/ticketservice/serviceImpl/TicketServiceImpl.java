package com.walmart.ticketservice.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.walmart.ticketservice.entity.Seat;
import com.walmart.ticketservice.entity.SeatHold;
import com.walmart.ticketservice.entity.Status;
import com.walmart.ticketservice.entity.Venue;
import com.walmart.ticketservice.exceptions.BookingException;
import com.walmart.ticketservice.exceptions.ErrorConstants;
import com.walmart.ticketservice.exceptions.InvalidRequest;
import com.walmart.ticketservice.repository.BookingRepository;
import com.walmart.ticketservice.service.TicketService;
import com.walmart.ticketservice.servicehandler.TicketServiceHandler;
import com.walmart.ticketservice.utility.TicketServiceUtil;
import com.walmart.ticketservice.validator.RequestValidator;

@Component
public class TicketServiceImpl implements TicketService {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RequestValidator requestValidator;
	
	@Autowired
	private BookingRepository bookingRepository;
	
	@Autowired
	private TicketServiceUtil ticketServiceUtil;
	
	@Autowired
	private TicketServiceHandler ticketServiceHandler;
	
	@Override
	public int numSeatsAvailable(String venueId, String venueLevel) {

		log.debug("Start of numSeatsAvailable method");

		requestValidator.numSeatsAvailableValidator(venueId, venueLevel);

		Venue venue = this.ticketServiceUtil.getVenue(venueId);

		if (StringUtils.isNotEmpty(venueLevel) && Integer.parseInt(venueLevel) > venue.getNumberOfLevels()) {
			throw new InvalidRequest("Requested Level Not Found In Venue: " + venue.getVenueId(),
					ErrorConstants.INVALID_LEVEL_NUMBER);
		}

		this.ticketServiceHandler.releaseExpiredHoldSeats(venue);

		Venue ven = this.ticketServiceUtil.getVenue(venueId);

		return this.ticketServiceUtil.getAvailableSeats(ven, venueLevel);
	}

	@Override
	public SeatHold findAndHoldSeats(int numSeats, String venueId, String customerEmail) {

		log.debug("Start of findAndHoldSeats method");
		requestValidator.findAndHoldSeatsValidator(numSeats, venueId, customerEmail);

		Venue ven = this.ticketServiceUtil.getVenue(venueId);

		// get seat hold limit for venue. This is property configured
		int seatHoldLimit = this.ticketServiceUtil.getSeatHoldLimitForVenue(venueId);

		// check if venue allows these many seats to be held by a customer
		if (numSeats > seatHoldLimit) {
			throw new InvalidRequest(
					"Requested number of seats for hold is more than allowed limit of " + seatHoldLimit,
					ErrorConstants.INVALID_SEAT_NUMBER);
		}

		// Release All Expired Seat Holds
		this.ticketServiceHandler.releaseExpiredHoldSeats(ven);

		Venue venue = this.ticketServiceUtil.getVenue(venueId);

		if (numSeats > this.ticketServiceUtil.getAvailableSeats(venue, null)) {
			throw new BookingException("Insufficient Seats Available", ErrorConstants.BOOKING_ERROR);
		}

		// get best available seats
		List<Seat> bestAvailableSeats = new ArrayList<Seat>();
		bestAvailableSeats.addAll(this.ticketServiceHandler.findBestAvailableSeats(venue, numSeats, customerEmail));

		// Hold seats
		// String seatHoldId = UUID.randomUUID().toString();
		String seatHoldId = RandomStringUtils.randomAlphanumeric(12).toUpperCase();
		SeatHold newSeatHold = new SeatHold();
		newSeatHold.setBookingId(seatHoldId);
		newSeatHold.setCustomerEmail(customerEmail);
		newSeatHold.setSeatList(bestAvailableSeats);
		newSeatHold.setStatus(Status.HOLD);
		newSeatHold.setTotalSeats(bestAvailableSeats.size());
		newSeatHold.setVenueId(venueId);
		newSeatHold.setBookingTime(System.currentTimeMillis());

		// insert new seathold object into database
		this.bookingRepository.insert(newSeatHold);

		return newSeatHold;
	}


	/**
	 * 
	 */
	@Override
	public String reserveSeats(String seatHoldId, String customerEmail) {

		log.debug("Start of reserveSeats method");
		this.requestValidator.reserverSeatsValidator(seatHoldId, customerEmail);

		Optional<SeatHold> seatHold = this.bookingRepository.findById(seatHoldId);

		// check if SeatHold id exists
		if (!seatHold.isPresent()) {
			throw new InvalidRequest("Invalid Seat Hold confirmation code", ErrorConstants.HOLD_ID_NOT_FOUND);
		}

		SeatHold seatHoldObj = seatHold.get();

		// check if this booking is already reserved
		if (seatHoldObj.getStatus().equals(Status.RESERVED)) {
			throw new BookingException("Seat hold reference " + seatHoldId + " is already reserved",
					ErrorConstants.BOOKING_ERROR);
		}

		// check if SeatHold booking time is expired or not
		Long expiredTimeLimit = System.currentTimeMillis()
				- (this.ticketServiceUtil.getHoldTimeForVenue(seatHoldObj.getVenueId()) * 1000);

		if (seatHoldObj.getBookingTime() < expiredTimeLimit) {
			throw new BookingException("Seat hold time has expired", ErrorConstants.HOLD_TIME_EXPIRED);
		}

		// check if input customer email ID matches with seat hold ID's email id
		if (!seatHoldObj.getCustomerEmail().equalsIgnoreCase(customerEmail)) {
			throw new InvalidRequest("Invalid EmailId: Provided email-ID do not match", ErrorConstants.INVALID_EMAIL_ID);
		}

		// change seat hold to reserved and persist in database
		seatHoldObj.setBookingTime(System.currentTimeMillis());
		seatHoldObj.setStatus(Status.RESERVED);

		// Seat hold will become reserved booking
		this.bookingRepository.save(seatHoldObj);

		return seatHoldObj.getBookingId();
	}
	
}
