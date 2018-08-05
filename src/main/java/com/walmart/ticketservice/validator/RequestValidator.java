package com.walmart.ticketservice.validator;


import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.walmart.ticketservice.exceptions.InvalidRequest;

@Component
public class RequestValidator {
	
	public void numSeatsAvailableValidator(String venueId, String level)
	{
		if (StringUtils.isEmpty(venueId)) {
			throw new InvalidRequest("Provide a valid Venue Id to get requested details.");
		}
		try {
			int levelNumber = Integer.parseInt(level);
		} catch (Exception ex) {
			throw new InvalidRequest("Requested level number is invalid");
		}

		if (Integer.valueOf(level) < 0) {
			throw new InvalidRequest("Requested level number is invalid");
		}

	}
	
	public void findAndHoldSeatsValidator(int numSeats, String venueLevel, String customerEmail)
	{
		//numSeats should be valid and greater than 0
		//email should be valid
		//venueLevel should be valid
	}
	
	public void reserverSeatsValidator(String seatHoldId, String customerEmail)
	{
		if(!StringUtils.isNotBlank(seatHoldId))
		{
			throw new InvalidRequest("Seat Hold number is invalid.");
		}
		//validate email
		
		
	}
	
	

}
