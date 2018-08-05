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
	private Environment env;
	
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

		List<Seat> releasedSeats = this.releaseExpiredHoldSeats(venueId, venueLevel);

		Optional<Venue> venue = venueRepository.findById(venueId);
		Venue ven = null;
		if (venue.isPresent()) {
			ven = venue.get();

			return this.getAvailableSeats(releasedSeats, ven);
		}
		return 0;
	}

	@Override
	public SeatHold findAndHoldSeats(int numSeats, String venueId, String customerEmail) {
		requestValidator.findAndHoldSeatsValidator(numSeats, venueId, customerEmail);

		Optional<Venue> venue = this.venueRepository.findById(venueId);
		if (!venue.isPresent()) {
			throw new InvalidRequest("Invalid Venue. Value cannot be found.");
		}

		Venue ven = venue.get();
		
		//check if venue allows these many seats to be held by a customer
		if(numSeats > ven.getHoldLimit() )
		{
			throw new InvalidRequest("Requested number of seats for hold is more than allowed limit of " + ven.getHoldLimit());
		}
		
		if (numSeats >= this.ticketServiceUtil.getAvailableSeats(ven)) {
			throw new InvalidRequest("Insufficiant seats.");
		}

		// get best available seats
		List<Seat> bestAvailableSeats = new ArrayList<Seat>();
		bestAvailableSeats.addAll(this.findBestAvailableSeats(ven, numSeats, customerEmail));

		// Hold seats
		String seatHoldId = UUID.randomUUID().toString();
		/*SeatHold seatHold = new SeatHold(seatHoldId, customerEmail, bestAvailableSeats, bestAvailableSeats.get(0).getLevel(),
				venueId, bestAvailableSeats.size());
		*/
		SeatHold newSeatHold = new SeatHold();
		newSeatHold.setBookingId(seatHoldId);
		newSeatHold.setCustomerEmail(customerEmail);
		newSeatHold.setLevel(bestAvailableSeats.get(0).getLevel());
		newSeatHold.setSeatList(bestAvailableSeats);
		newSeatHold.setStatus(Status.HELD);
		newSeatHold.setTotalSeats(bestAvailableSeats.size());
		newSeatHold.setVenueId(venueId);
		
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
			throw new InvalidRequest("Invalid Hold confirmation code");
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
		this.bookingRepository.insert(seatHoldObj);

		return seatHoldObj.getBookingId();
	}
	
	/*
	 * This method does following
	 * 1. updates the Venue object with hold seats whixh had expired
	 * 2. returns the total number of available seats in the Venue
	 * 3. Updates the venue object in database
	 */
	private int getAvailableSeats(List<Seat> releasedSeats, Venue venue) {

		HashMap<Integer, Integer> levelSeatMap = new HashMap<Integer, Integer>();
		levelSeatMap.putAll(venue.getAvailableSeats());

		for (Seat seat : releasedSeats) {
			int oldNumSeat = levelSeatMap.get(seat.getLevel());
			levelSeatMap.put(seat.getLevel(), ++oldNumSeat);
		}

		Collection<Integer> col = levelSeatMap.values();
		int totalAvailableSeats = 0;
		for (Integer integer : col) {
			totalAvailableSeats += integer;
		}

		venue.setAvailableSeats(levelSeatMap);
		venueRepository.save(venue);
		return totalAvailableSeats;
	}

	/*
	 * @description: This method will check if there are seats which were held by customers but hold time limit has expired.
	 * Get hold time limit value from application properties. This property is configurable externally.
	 * @param venueId (required)
	 * @param venueLevel (optional)
	 */
	private List<Seat> releaseExpiredHoldSeats(String venueId, String venueLevel) {

		int timeInSeconds = this.ticketServiceUtil.getHoldTimeForVenue(venueId);
		List<SeatHold> allHeldSeats = bookingRepository.findAllExpiredHeldSeats(timeInSeconds);

		List<Seat> seatList = new ArrayList<Seat>();

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

	private List<Seat> findBestAvailableSeats(Venue venue, int numSeats, String customerEmail) {

		Map<Integer, Integer> levelSeatMap = new HashMap<Integer, Integer>();
		levelSeatMap.putAll(venue.getAvailableSeats());
		int bestAvailableLevel = 0;

		Iterator<Map.Entry<Integer, Integer>> iterator = levelSeatMap.entrySet().iterator();
		for (int index = 0; index < levelSeatMap.size(); index++) {
			if (levelSeatMap.get(index + 1) > numSeats) {
				// hold seats at this level
				bestAvailableLevel = index + 1;
				levelSeatMap.put(bestAvailableLevel, levelSeatMap.get(bestAvailableLevel) - numSeats);
				break;
			}
		}

		venue.setAvailableSeats(levelSeatMap);
		Map<Integer, List<Seat>> seatMap = new HashMap<Integer, List<Seat>>();
		seatMap.putAll(venue.getSeatMap());
		List<Seat> allSeats = seatMap.get(bestAvailableLevel);
		List<Seat> selectedSeats = new ArrayList<Seat>();

		for (Seat seat : allSeats) {
			if (seat.getStatus().equals(Status.AVAILABLE)) {
				seat.setStatus(Status.HELD);
				selectedSeats.add(seat);
			}
		}

		// save updated venue
		this.venueRepository.save(venue);

		//return new SeatHold(customerEmail, selectedSeats, bestAvailableLevel, venue.getVenueId(), selectedSeats.size());
		return selectedSeats;
	}
}
