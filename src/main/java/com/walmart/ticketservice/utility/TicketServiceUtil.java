package com.walmart.ticketservice.utility;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.walmart.ticketservice.entity.Venue;
import com.walmart.ticketservice.exceptions.ErrorConstants;
import com.walmart.ticketservice.exceptions.InvalidRequest;
import com.walmart.ticketservice.repository.VenueRepository;

@Component
public class TicketServiceUtil {

	private final String HOLD_TIME = "hold.time.limit";
	private final String HOLD_LIMIT = "hold.seat.limit";
	
	private final int DEFAULT_HOLD_TIME = 120;
	private final int DEFAULT_HOLD_LIMIT = 10;
	
	@Autowired
	private VenueRepository venueRepository;
	
	@Autowired
	private Environment env;
	/**
	 * 
	 * @param venue
	 * @return total number of seats available at a venue
	 */
	public int getAvailableSeats(Venue venue, String level)
	{
		HashMap<Integer, Integer> levelSeatMap = new HashMap<Integer, Integer>();
		levelSeatMap.putAll(venue.getAvailableSeats());

		Collection<Integer> seats = levelSeatMap.values();
		int totalSeats = 0;
		for (Integer integer : seats) {
			totalSeats += integer;
		}
		
		if(StringUtils.isEmpty(level))
			return totalSeats;
		else
			return levelSeatMap.get(Integer.parseInt(level));
	}
	
	/**
	 * 
	 * @param venueId
	 * @return: time in seconds
	 * @description:Ticket hold time can vary for each Venue also ticket hold time 
	 * can vary from peak season to off season so we have made it configurable
	 * in external property file where it can changed without having to restart
	 * application. If there is an error getting hold time then return default 
	 * hold time of 120 seconds
	 */
	public int getHoldTimeForVenue(String venueId)
	{
		String holdTimeProp = venueId + "." + HOLD_TIME;
		String timeInSeconds = env.getProperty(holdTimeProp);
		int timeSeconds = 0;
		try {
			timeSeconds = Integer.parseInt(timeInSeconds);
		} catch (Exception ex) {
			ex.printStackTrace();
			return DEFAULT_HOLD_TIME;
		}

		return timeSeconds;
	}

	/**
	 * 
	 * @param venueId
	 * @return
	 */
	public int getSeatHoldLimitForVenue(String venueId) {

		String holdSeatProp = venueId + "." + HOLD_LIMIT;
		String seatLimit = env.getProperty(holdSeatProp);
		int seatHoldLimit = 0;
		try {
			seatHoldLimit = Integer.parseInt(seatLimit);
		} catch (Exception ex) {
			ex.printStackTrace();
			return DEFAULT_HOLD_LIMIT;
		}

		return seatHoldLimit;
	}
	
	/**
	 * 
	 * @param venueId
	 * @return
	 */
	public Venue getVenue(String venueId)
	{
		Optional<Venue> venue = venueRepository.findById(venueId);
		if (!venue.isPresent()) {
			throw new InvalidRequest("Requested Venue Not Found!", ErrorConstants.VENUE_NOT_FOUND);
		}
		return venue.get();
	}
}
