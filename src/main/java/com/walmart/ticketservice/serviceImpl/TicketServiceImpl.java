package com.walmart.ticketservice.serviceImpl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.walmart.ticketservice.entity.Seat;
import com.walmart.ticketservice.entity.SeatHold;
import com.walmart.ticketservice.entity.Status;
import com.walmart.ticketservice.entity.Venue;
import com.walmart.ticketservice.exceptions.BookingException;
import com.walmart.ticketservice.exceptions.InvalidRequest;
import com.walmart.ticketservice.repository.BookingRepository;
import com.walmart.ticketservice.repository.VenueRepository;
import com.walmart.ticketservice.service.TicketService;
import com.walmart.ticketservice.utility.TicketServiceUtil;
import com.walmart.ticketservice.validator.RequestValidator;

@Component
public class TicketServiceImpl implements TicketService {

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
		// log
		try {
			requestValidator.numSeatsAvailableValidator(venueId, venueLevel);
		} catch (InvalidRequest e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Optional<Venue> venue = venueRepository.findById(venueId);
		if(!venue.isPresent())
		{
			throw new InvalidRequest("Requested Venue Not Found!");
		}
		
		List<Seat> releasedSeats = this.releaseExpiredHoldSeats(venueId);

		Venue ven = venue.get();
		return this.getAvailableSeats(releasedSeats, ven, venueLevel);
	}

	@Override
	public SeatHold findAndHoldSeats(int numSeats, String venueId, String customerEmail) {
		requestValidator.findAndHoldSeatsValidator(numSeats, venueId, customerEmail);

		Optional<Venue> venue = this.venueRepository.findById(venueId);
		if (!venue.isPresent()) {
			throw new InvalidRequest("Invalid Venue. Value cannot be found.");
		}

		Venue ven = venue.get();
		int seatHoldLimit = this.ticketServiceUtil.getSeatHoldLimitForVenue(venueId);
		
		//check if venue allows these many seats to be held by a customer
		if(numSeats > seatHoldLimit )
		{
			throw new InvalidRequest("Requested number of seats for hold is more than allowed limit of " + seatHoldLimit);
		}
		
		if (numSeats >= this.ticketServiceUtil.getAvailableSeats(ven)) {
			throw new InvalidRequest("Insufficiant seats.");
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
		newSeatHold.setStatus(Status.HELD);
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
	public String reserveSeats(String seatHoldId, String customerEmail) {

		this.requestValidator.reserverSeatsValidator(seatHoldId, customerEmail);

		Optional<SeatHold> seatHold = this.bookingRepository.findById(seatHoldId);
		
		// check if SeatHold id exists
		if (!seatHold.isPresent()) {
			throw new InvalidRequest("Invalid Seat Hold confirmation code");
		}
		
		SeatHold seatHoldObj = seatHold.get();

		// check if this booking is already reserved
		if (seatHoldObj.getStatus().equals(Status.RESERVED)) {
			throw new BookingException("Seat hold reference " + seatHoldId + " is already reserved");
		}

		// check if SeatHold booking time is expired or not
		Long expiredTimeLimit = System.currentTimeMillis()
				- (this.ticketServiceUtil.getHoldTimeForVenue(seatHoldObj.getVenueId()) * 1000);

		if (seatHoldObj.getBookingTime() < expiredTimeLimit) {
			throw new BookingException("Seat hold time has expired");
		}

		// check if input customer email ID matches with seat hold ID's email id
		if (!seatHoldObj.getCustomerEmail().equalsIgnoreCase(customerEmail)) {
			throw new InvalidRequest("Invalid EmailId: Provided email Id do not match with");
		}
		
		// change seat hold to reserved and persist in database
		seatHoldObj.setBookingTime(System.currentTimeMillis());
		seatHoldObj.setStatus(Status.RESERVED);
		
	
		//Seat hold will become reserved booking
		this.bookingRepository.save(seatHoldObj);

		return seatHoldObj.getBookingId();
	}
	
	/*
	 * This method does following
	 * 1. updates the Venue object with hold seats which had expired
	 * 2. returns the total number of available seats in the Venue
	 * 3. Updates the venue object in database
	 */
	private int getAvailableSeats(List<Seat> releasedSeats, Venue venue, String level) {

		HashMap<Integer, Integer> levelSeatMap = new HashMap<Integer, Integer>();
		
		if(venue.getAvailableSeats() != null) {
			levelSeatMap.putAll(venue.getAvailableSeats());
		}

		for (Seat seat : releasedSeats) {
			int oldNumSeat = levelSeatMap.get(seat.getLevelId());
			levelSeatMap.put(seat.getLevelId(), oldNumSeat + 1);
		}

		Collection<Integer> col = levelSeatMap.values();
		int totalAvailableSeats = 0;
		for (Integer integer : col) {
			totalAvailableSeats += integer;
		}

		// update Venue object with released seats
		venue.setAvailableSeats(levelSeatMap);
		venueRepository.save(venue);

		// If level number is not present, return all available seats in Venue
		if (StringUtils.isEmpty(level)) {
			return totalAvailableSeats;
		}
		// If level number is present, return available seats at that level
		else {
			return levelSeatMap.get(Integer.valueOf(level));
		}
	}

	/*
	 * @description: This method will check if there are seats which were held by customers but hold time limit has expired.
	 * Get hold time limit value from application properties. This property is configurable externally.
	 * @param venueId (required)
	 */
	private List<Seat> releaseExpiredHoldSeats(String venueId) {

		int timeInSeconds = this.ticketServiceUtil.getHoldTimeForVenue(venueId);
		
		//get all expired SeatHolds for the requested venue
		List<SeatHold> allHeldSeats = bookingRepository.findAllExpiredHeldSeats(timeInSeconds, venueId);

		//Delete all seatHold entries in database because they expired
		this.bookingRepository.deleteAll(allHeldSeats);
		
		List<Seat> seatList = new ArrayList<Seat>();

		//shubham:Write logic to update Venue object with released seats.
		
		Iterator<SeatHold> iterator = allHeldSeats.iterator();
		while (iterator.hasNext()) {
			SeatHold seatHold = iterator.next();
			if (seatHold.getVenueId().equalsIgnoreCase(venueId)) {
				seatList.addAll(seatHold.getSeatList());
			}
		}
		System.out.println(allHeldSeats.size());
		
		return seatList;
	}

	/**
	 * 
	 * @param venue
	 * @param numSeats
	 * @param customerEmail
	 * @return List of best available seats
	 */
	private List<Seat> findBestAvailableSeats(Venue venue, int seatsRequested, String customerEmail) {

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
		}

		//If no level has enough number of seats 
		if(!seatsFound)
		{
			throw new BookingException("Requested number of seats is greater than available seats at any level");
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
				seat.setStatus(Status.HELD);
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
