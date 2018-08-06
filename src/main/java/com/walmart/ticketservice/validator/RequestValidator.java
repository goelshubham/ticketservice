package com.walmart.ticketservice.validator;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.walmart.ticketservice.exceptions.ErrorConstants;
import com.walmart.ticketservice.exceptions.InvalidRequest;

@Component
public class RequestValidator {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * 
	 * @param venueId
	 * @param level
	 * @description: method to validate request
	 */
	public void numSeatsAvailableValidator(String venueId, String level)
	{
		log.debug("Start of numSeatsAvailableValidator method");
		if (StringUtils.isEmpty(venueId)) {
			
			log.debug("Invalid input venue: " + venueId);
			throw new InvalidRequest("Provide a valid Venue Id to get requested details", ErrorConstants.VENUE_NOT_FOUND);
		}
		if (StringUtils.isNotEmpty(level)) {
			try {
				Integer.parseInt(level);
			} catch (NumberFormatException ex) {
				
				log.debug("Invalid input level: " + level);
				throw new InvalidRequest("Level Number Should Be Numeric", ErrorConstants.INVALID_LEVEL_NUMBER);
			}
			if (Integer.valueOf(level) <= 0) {
				
				log.debug("Invalid input level: " + level);
				throw new InvalidRequest("Level Number Should Be Greater Than 0", ErrorConstants.INVALID_LEVEL_NUMBER);
			}
		}
	}
	
	public void findAndHoldSeatsValidator(int numSeats, String venueId, String customerEmail)
	{
		log.debug("Start of findAndHoldSeatsValidator method");
		// numSeats should be valid and greater than 0
		if (Integer.valueOf(numSeats) == null) {
			
			log.debug("Invalid input number of seats: " + numSeats);
			throw new InvalidRequest("Number Of Seats Is Required", ErrorConstants.REQUIRED_FIELD);
		}
		if (Integer.valueOf(numSeats) != null && Integer.valueOf(numSeats) <= 0) {
				log.debug("Invalid input number of seats: " + numSeats);
				throw new InvalidRequest("Number Of Seats Should Be More Than 0", ErrorConstants.INVALID_SEAT_NUMBER);
		}

		// email should be valid
		if (StringUtils.isEmpty(customerEmail)) {
			
			log.debug("Invalid input email id : " + customerEmail);
			throw new InvalidRequest("Email ID Is Required", ErrorConstants.INVALID_EMAIL_ID);
		} else {
			if (!EmailValidator.getInstance().isValid(customerEmail)) {
				
				log.debug("Invalid input email id : " + customerEmail);
				throw new InvalidRequest("Invalid Email ID", ErrorConstants.INVALID_EMAIL_ID);
			}
		}

		//validate Venue ID
		if (StringUtils.isEmpty(venueId)) {
			throw new InvalidRequest("Provide a valid Venue Id to get requested details",
					ErrorConstants.VENUE_NOT_FOUND);
		}

	}
	
	public void reserverSeatsValidator(String seatHoldId, String customerEmail)
	{
		log.debug("Start of reserverSeatsValidator method");
		if(StringUtils.isEmpty(seatHoldId))
		{
			log.debug("Invalid input seat hold number : " + seatHoldId);
			throw new InvalidRequest("SeatHold Number Is Required", ErrorConstants.HOLD_ID_NOT_FOUND);
		}
		//validate email
		// email should be valid
		if (StringUtils.isEmpty(customerEmail)) {
			
			log.debug("Invalid input email id : " + customerEmail);
			throw new InvalidRequest("Email ID Is Required", ErrorConstants.INVALID_EMAIL_ID);
		} else {
			if (!EmailValidator.getInstance().isValid(customerEmail)) {
				
				log.debug("Invalid input email id : " + customerEmail);
				throw new InvalidRequest("Invalid Email ID", ErrorConstants.INVALID_EMAIL_ID);
			}
		}
	}
	
	

}
