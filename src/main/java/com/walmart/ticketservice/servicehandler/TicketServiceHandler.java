package com.walmart.ticketservice.servicehandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import com.walmart.ticketservice.repository.BookingRepository;
import com.walmart.ticketservice.repository.VenueRepository;
import com.walmart.ticketservice.utility.TicketServiceUtil;

@Component
public class TicketServiceHandler {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private VenueRepository venueRepository;
	
	@Autowired
	private BookingRepository bookingRepository;
	
	@Autowired
	private TicketServiceUtil ticketServiceUtil;

	
	/*
	 * This method does following
	 * 1. Updates the Venue object with hold seats which had expired
	 * 3. Updates the venue object in database
	 */
	public void updateVenueWithExpiredSeats(List<Seat> releasedSeats, Venue venue) {

		log.debug("Start of getAvailableSeats method");
		HashMap<Integer, Integer> levelSeatMap = new HashMap<Integer, Integer>();
		HashMap<Integer, List<Seat>> seatMap = new HashMap<Integer, List<Seat>>();

		if (venue.getAvailableSeats() != null) {
			levelSeatMap.putAll(venue.getAvailableSeats());
		}

		if (venue.getSeatMap() != null) {
			seatMap.putAll(venue.getSeatMap());
		}

		int numOfLevels = venue.getNumberOfLevels();

		HashMap<String, Seat> allSeatMap = null;

		for (int levelIndex = 1; levelIndex <= numOfLevels; levelIndex++) {
			allSeatMap = new HashMap<String, Seat>();
			int updateSeatCounter = 0;
			for (Seat seat : seatMap.get(levelIndex)) {
				allSeatMap.put(seat.getSeatId(), seat);
			}

			for (Seat releasedSeat : releasedSeats) {
				if (releasedSeat.getLevelId() == levelIndex) {
					Seat findSeat = allSeatMap.get(releasedSeat.getSeatId());
					findSeat.setStatus(Status.AVAILABLE);
					allSeatMap.put(findSeat.getSeatId(), findSeat);
					updateSeatCounter++;
				}
			}

			Collection<Seat> updatedSeats = allSeatMap.values();
			List<Seat> updatedSeatList = new ArrayList<Seat>();
			for (Seat seat : updatedSeats) {
				updatedSeatList.add(seat);
			}

			seatMap.put(levelIndex, updatedSeatList);
			levelSeatMap.put(levelIndex, levelSeatMap.get(levelIndex) + updateSeatCounter);
		}

		// update Venue object with released seats
		venue.setAvailableSeats(levelSeatMap);
		venue.setSeatMap(seatMap);
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
	public void releaseExpiredHoldSeats(Venue venue) {
		
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
		
		//Update Venue with released seats
		this.updateVenueWithExpiredSeats(expiredSeatList, venue);
	
		log.debug("releaseExpiredHoldSeats: " + expiredSeatList.size() + " seats found");
		
	}

	/**
	 * 
	 * @param venue, @param numSeats, @param customerEmail
	 * @return List of best available seats
	 * @Description: This method finds the best available seats in a venue.
	 * All seats at level 1 are considered best seats. 
	 * Seats at level 2 are considered next best seats and so on. If enough seats are 
	 * not available at a particular level then seats are next level are check.
	 * 
	 */
	public List<Seat> findBestAvailableSeats(Venue venue, int seatsRequested, String customerEmail) {

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
