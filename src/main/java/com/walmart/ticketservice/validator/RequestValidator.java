package com.walmart.ticketservice.validator;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.walmart.ticketservice.exceptions.ErrorConstants;
import com.walmart.ticketservice.exceptions.InvalidRequest;
import com.walmart.ticketservice.types.FindSeatsRequest;
import com.walmart.ticketservice.types.HoldSeatsRequest;
import com.walmart.ticketservice.types.ReserveSeatsRequest;

@Component
public class RequestValidator {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * 
	 * @param venueId
	 * @param level
	 * @description: method to validate request
	 */
	public void numSeatsAvailableValidator(FindSeatsRequest findSeatsRequest)
	{
		log.debug("Start of numSeatsAvailableValidator method");
		
		if(findSeatsRequest == null)
		{
			throw new InvalidRequest("Invalid Request Error", ErrorConstants.REQUIRED_FIELD);
		}
		
		if (StringUtils.isEmpty(findSeatsRequest.getVenueId())) {
			
			log.debug("Invalid input venue: " + findSeatsRequest.getVenueId());
			throw new InvalidRequest("Provide a valid Venue Id to get requested details", ErrorConstants.VENUE_NOT_FOUND);
		}
		if (StringUtils.isNotEmpty(findSeatsRequest.getLevelNumber())) {
			try {
				Integer.parseInt(findSeatsRequest.getLevelNumber());
			} catch (NumberFormatException ex) {
				
				log.debug("Invalid input level: " + findSeatsRequest.getLevelNumber());
				throw new InvalidRequest("Level Number Should Be Numeric", ErrorConstants.INVALID_LEVEL_NUMBER);
			}
			if (Integer.valueOf(findSeatsRequest.getLevelNumber()) <= 0) {
				
				log.debug("Invalid input level: " + findSeatsRequest.getLevelNumber());
				throw new InvalidRequest("Level Number Should Be Greater Than 0", ErrorConstants.INVALID_LEVEL_NUMBER);
			}
		}
	}
	
	public void findAndHoldSeatsValidator(HoldSeatsRequest holdSeatsRequest)
	{
		log.debug("Start of findAndHoldSeatsValidator method");
		try {
			if (Integer.valueOf(holdSeatsRequest.getNumberOfSeats()) <= 0) {
				log.debug("Invalid input number of seats: " + holdSeatsRequest.getNumberOfSeats());
				throw new InvalidRequest("Number Of Seats Should Be More Than 0", ErrorConstants.INVALID_SEAT_NUMBER);
			}
		} catch (Exception ex) {
			log.debug("Invalid input number of seats: " + holdSeatsRequest.getNumberOfSeats());
			throw new InvalidRequest("Number Of Seats Is Required", ErrorConstants.REQUIRED_FIELD);
		}

		// email should be valid
		if (StringUtils.isEmpty(holdSeatsRequest.getCustomerEmailId())) {

			log.debug("Invalid input email id : " + holdSeatsRequest.getCustomerEmailId());
			throw new InvalidRequest("Email ID Is Required", ErrorConstants.INVALID_EMAIL_ID);
		} else {
			if (!EmailValidator.getInstance().isValid(holdSeatsRequest.getCustomerEmailId())) {

				log.debug("Invalid input email id : " + holdSeatsRequest.getCustomerEmailId());
				throw new InvalidRequest("Invalid Email ID", ErrorConstants.INVALID_EMAIL_ID);
			}
		}

		// validate Venue ID
		if (StringUtils.isEmpty(holdSeatsRequest.getVenueId())) {
			throw new InvalidRequest("Provide a valid Venue Id to get requested details",
					ErrorConstants.VENUE_NOT_FOUND);
		}

	}
	
	public void reserverSeatsValidator(ReserveSeatsRequest reserveSeatsRequest)
	{
		log.debug("Start of reserverSeatsValidator method");
		if(StringUtils.isEmpty(reserveSeatsRequest.getSeatHoldId()))
		{
			log.debug("Invalid input seat hold number : " + reserveSeatsRequest.getSeatHoldId());
			throw new InvalidRequest("SeatHold Number Is Required", ErrorConstants.HOLD_ID_NOT_FOUND);
		}
		//validate email
		// email should be valid
		if (StringUtils.isEmpty(reserveSeatsRequest.getCustomerEmail())) {
			
			log.debug("Invalid input email id : " + reserveSeatsRequest.getCustomerEmail());
			throw new InvalidRequest("Email ID Is Required", ErrorConstants.INVALID_EMAIL_ID);
		} else {
			if (!EmailValidator.getInstance().isValid(reserveSeatsRequest.getCustomerEmail())) {
				
				log.debug("Invalid input email id : " + reserveSeatsRequest.getCustomerEmail());
				throw new InvalidRequest("Invalid Email ID", ErrorConstants.INVALID_EMAIL_ID);
			}
		}
	}
	
	

}
