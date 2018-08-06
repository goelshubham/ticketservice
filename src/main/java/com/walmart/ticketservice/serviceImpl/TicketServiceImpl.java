package com.walmart.ticketservice.serviceImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.walmart.ticketservice.repository.VenueRepository;
import com.walmart.ticketservice.service.TicketService;
import com.walmart.ticketservice.utility.TicketServiceUtil;
import com.walmart.ticketservice.validator.RequestValidator;

@Component
public class TicketServiceImpl implements TicketService {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RequestValidator requestValidator;
	
	@Autowired
	private VenueRepository venueRepository;
	
	@Autowired
	private BookingRepository bookingRepository;
	
	@Autowired
	private TicketServiceUtil ticketServiceUtil;
	
	@Override
	public int numSeatsAvailable(String venueId, String venueLevel) {

		log.debug("Start of numSeatsAvailable method");

		requestValidator.numSeatsAvailableValidator(venueId, venueLevel);

		Venue venue = this.ticketServiceUtil.getVenue(venueId);

		if (StringUtils.isNotEmpty(venueLevel) && Integer.parseInt(venueLevel) > venue.getNumberOfLevels()) {
			throw new InvalidRequest("Requested Level Not Found In Venue: " + venue.getVenueId(),
					ErrorConstants.INVALID_LEVEL_NUMBER);
		}

		this.releaseExpiredHoldSeats(venue);

		Venue ven = this.ticketServiceUtil.getVenue(venueId);

		return this.ticketServiceUtil.getAvailableSeats(ven, venueLevel);
	}

	@Override
	public SeatHold findAndHoldSeats(int numSeats, String venueId, String customerEmail) {
		
		log.debug("Start of findAndHoldSeats method");
		requestValidator.findAndHoldSeatsValidator(numSeats, venueId, customerEmail);

		Venue ven = this.ticketServiceUtil.getVenue(venueId);

		int seatHoldLimit = this.ticketServiceUtil.getSeatHoldLimitForVenue(venueId);
		
		//check if venue allows these many seats to be held by a customer
		if(numSeats > seatHoldLimit )
		{
			throw new InvalidRequest(
					"Requested number of seats for hold is more than allowed limit of " + seatHoldLimit,
					ErrorConstants.INVALID_SEAT_NUMBER);
		}
		
		//Release All Expired Seat Holds
		this.releaseExpiredHoldSeats(ven);
		
		Venue venue = this.ticketServiceUtil.getVenue(venueId);
		
		if (numSeats > this.ticketServiceUtil.getAvailableSeats(venue,null)) {
			throw new BookingException("Insufficient Seats Available", ErrorConstants.BOOKING_ERROR);
		}

		// get best available seats
		List<Seat> bestAvailableSeats = new ArrayList<Seat>();
		bestAvailableSeats.addAll(this.findBestAvailableSeats(ven, numSeats, customerEmail));

		// Hold seats
		//String seatHoldId = UUID.randomUUID().toString();
		String seatHoldId = RandomStringUtils.randomAlphanumeric(12).toUpperCase();
		SeatHold newSeatHold = new SeatHold();
		newSeatHold.setBookingId(seatHoldId);
		newSeatHold.setCustomerEmail(customerEmail);
		newSeatHold.setSeatList(bestAvailableSeats);
		newSeatHold.setStatus(Status.HOLD);
		newSeatHold.setTotalSeats(bestAvailableSeats.size());
		newSeatHold.setVenueId(venueId);
		newSeatHold.setBookingTime(System.currentTimeMillis());
		
		//insert new seathold object into database
		this.bookingRepository.insert(newSeatHold);
		
		return newSeatHold;
	}


	/**
	 * 
	 */
	@Override
	public SeatHold reserveSeats(String seatHoldId, String customerEmail) {

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
			throw new BookingException("Seat hold reference " + seatHoldId + " is already reserved", ErrorConstants.BOOKING_ERROR);
		}

		// check if SeatHold booking time is expired or not
		Long expiredTimeLimit = System.currentTimeMillis()
				- (this.ticketServiceUtil.getHoldTimeForVenue(seatHoldObj.getVenueId()) * 1000);

		if (seatHoldObj.getBookingTime() < expiredTimeLimit) {
			throw new BookingException("Seat hold time has expired", ErrorConstants.HOLD_TIME_EXPIRED);
		}

		// check if input customer email ID matches with seat hold ID's email id
		if (!seatHoldObj.getCustomerEmail().equalsIgnoreCase(customerEmail)) {
			throw new InvalidRequest("Invalid EmailId: Provided email Id do not match");
		}
		
		// change seat hold to reserved and persist in database
		seatHoldObj.setBookingTime(System.currentTimeMillis());
		seatHoldObj.setStatus(Status.RESERVED);
		
	
		//Seat hold will become reserved booking
		this.bookingRepository.save(seatHoldObj);

		return seatHoldObj;
	}
	
	/*
	 * This method does following
	 * 1. Updates the Venue object with hold seats which had expired
	 * 3. Updates the venue object in database
	 */
	private void updateVenueWithExpiredSeats(List<Seat> releasedSeats, Venue venue) {

		log.debug("Start of getAvailableSeats method");
		HashMap<Integer, Integer> levelSeatMap = new HashMap<Integer, Integer>();
		HashMap<Integer, List<Seat>> seatMap = new HashMap<Integer, List<Seat>>(); 
		
		if(venue.getAvailableSeats() != null) {
			levelSeatMap.putAll(venue.getAvailableSeats());
		}
		
		if(venue.getSeatMap() != null){
			seatMap.putAll(venue.getSeatMap());
		}

		for (Seat seat : releasedSeats) {
			int oldNumSeat = levelSeatMap.get(seat.getLevelId());
			levelSeatMap.put(seat.getLevelId(), oldNumSeat + 1);
		}

/*		Collection<Integer> col = levelSeatMap.values();
		int totalAvailableSeats = 0;
		for (Integer integer : col) {
			totalAvailableSeats += integer;
		}*/
		
		// Change Seat status from HOLD to AVAILABLE of all expired seat holds
		// When seatCounter has become equals to requestSeats then we have changed
		// status
		int seatCounter = 0;
		for (Seat seat : releasedSeats) {
			if (seat.getStatus().equals(Status.AVAILABLE)) {
				seat.setStatus(Status.HOLD);
				selectedSeats.add(seat);
				seatCounter++;

				if (seatCounter == seatsRequested)
					break;
			}
		}

		// update Venue object with released seats
		venue.setAvailableSeats(levelSeatMap);
		venueRepository.save(venue);
	}

	/*
	 * @description: 
	 * This method will check if Seat Status is HOLD and BookingTime has expired
	 * Get hold time limit value from application properties. This property is configurable externally.
	 * Get all SeatHold objects and delete them
	 * Update Venue object with AVAILABLE seat status
	 * @param venueId (required)
	 */
	private void releaseExpiredHoldSeats(Venue venue) {
		
		log.debug("Start of releaseExpiredHoldSeats method");

		int timeInSeconds = this.ticketServiceUtil.getHoldTimeForVenue(venue.getVenueId());
		
		//get all expired SeatHolds for the requested venue
		List<SeatHold> expiredSeatHolds = bookingRepository.findAllExpiredHeldSeats(timeInSeconds, venue.getVenueId());

		
		List<Seat> expiredSeatList = new ArrayList<Seat>();

		//shubham:Write logic to update Venue object with released seats.
		
		Iterator<SeatHold> iterator = expiredSeatHolds.iterator();
		while (iterator.hasNext()) {
			expiredSeatList.addAll(iterator.next().getSeatList());
		}
		//Delete all seatHold entries in database because they expired
		this.bookingRepository.deleteAll(expiredSeatHolds);
		this.updateVenueWithExpiredSeats(expiredSeatList, venue);
	
		log.debug("releaseExpiredHoldSeats: " + expiredSeatList.size() + " seats found");
		
	}

	/**
	 * 
	 * @param venue
	 * @param numSeats
	 * @param customerEmail
	 * @return List of best available seats
	 */
	private List<Seat> findBestAvailableSeats(Venue venue, int seatsRequested, String customerEmail) {

		log.debug("Start of findBestAvailableSeats method");
		
		Map<Integer, Integer> levelSeatMap = new HashMap<Integer, Integer>();
		levelSeatMap.putAll(venue.getAvailableSeats());
		int bestAvailableLevel = 1;
		int availableSeatsAtLevel = 0;
		boolean seatsFound = false;
		for (int index = 0; index < levelSeatMap.size(); index++) {
			availableSeatsAtLevel = levelSeatMap.get(bestAvailableLevel);
			if (availableSeatsAtLevel >= seatsRequested) {
				seatsFound = true;
				levelSeatMap.put(bestAvailableLevel, (availableSeatsAtLevel - seatsRequested));
				break;
			}
			bestAvailableLevel++;
		}

		//If no level has enough number of seats 
		if(!seatsFound)
		{
			throw new BookingException("Requested number of seats is greater than available seats at any level", ErrorConstants.BOOKING_ERROR);
		}
		
		//update Venue object with adjusted number of seats
		venue.setAvailableSeats(levelSeatMap);
		
		Map<Integer, List<Seat>> seatMap = new HashMap<Integer, List<Seat>>();
		seatMap.putAll(venue.getSeatMap());
		List<Seat> allSeats = seatMap.get(bestAvailableLevel);
		List<Seat> selectedSeats = new ArrayList<Seat>();

		//Change Seat status from AVAILEBLE to HELD for the number of requested seats only
		//When seatCounter has become equals to requestSeats then we have changed status
		int seatCounter = 0;
		for (Seat seat : allSeats) {
			if (seat.getStatus().equals(Status.AVAILABLE)) {
				seat.setStatus(Status.HOLD);
				selectedSeats.add(seat);
				seatCounter++;
				
				if(seatCounter == seatsRequested)
					break;
			}
		}

		/*
		 * Update the Venue object with updated seat Status
		 * Persist updated venue object in database 
		 */
		seatMap.put(bestAvailableLevel, allSeats);
		venue.setSeatMap(seatMap);
		this.venueRepository.save(venue);

		return selectedSeats;
	}
}
