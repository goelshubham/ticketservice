package com.walmart.ticketservice.validator;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.walmart.ticketservice.exceptions.InvalidRequest;
import com.walmart.ticketservice.types.FindSeatsRequest;
import com.walmart.ticketservice.types.HoldSeatsRequest;
import com.walmart.ticketservice.types.ReserveSeatsRequest;

import groovyjarjarantlr.StringUtils;

@RunWith(MockitoJUnitRunner.class)
public class RequestValidatorTest {

	@InjectMocks
	RequestValidator request;
	
	
	@Test
	public void testNumSeatsAvailableValidator() {
		Mockito.mock(StringUtils.class);
		
		try {
			
			FindSeatsRequest findSeatsRequest = new FindSeatsRequest();
			request.numSeatsAvailableValidator(findSeatsRequest);
		} catch (Exception ex) {
			assertThatExceptionOfType(InvalidRequest.class);
		}
		try {
			FindSeatsRequest findSeatsRequest = new FindSeatsRequest();
			findSeatsRequest.setVenueId("CityHall");
			findSeatsRequest.setLevelNumber("Five");
			request.numSeatsAvailableValidator(findSeatsRequest);
		} catch (Exception ex) {
			assertThatExceptionOfType(InvalidRequest.class);
		}
		
		try {
			
			FindSeatsRequest findSeatsRequest = new FindSeatsRequest();
			findSeatsRequest.setVenueId("CityHall");
			findSeatsRequest.setLevelNumber("-10");
			request.numSeatsAvailableValidator(findSeatsRequest);
		} catch (Exception ex) {
			assertThatExceptionOfType(InvalidRequest.class);
		}
	}
	
	@Test
	public void findAndHoldSeatsValidator()
	{
		try {
			HoldSeatsRequest holdSeatsRequest = null;
			request.findAndHoldSeatsValidator(holdSeatsRequest);
		} catch (Exception ex) {
			assertThatExceptionOfType(InvalidRequest.class);
		}
		
		try {
			HoldSeatsRequest holdSeatsRequest = new HoldSeatsRequest();
			holdSeatsRequest.setCustomerEmailId("");
			holdSeatsRequest.setVenueId("");
			holdSeatsRequest.setNumberOfSeats(-5);
			request.findAndHoldSeatsValidator(holdSeatsRequest);
		} catch (Exception ex) {
			assertThatExceptionOfType(InvalidRequest.class);
		}
		
		try {
			HoldSeatsRequest holdSeatsRequest = new HoldSeatsRequest();
			holdSeatsRequest.setCustomerEmailId("");
			holdSeatsRequest.setVenueId("");
			request.findAndHoldSeatsValidator(holdSeatsRequest);
		} catch (Exception ex) {
			assertThatExceptionOfType(InvalidRequest.class);
		}
		
		try {
			HoldSeatsRequest holdSeatsRequest = new HoldSeatsRequest();
			//holdSeatsRequest.setCustomerEmailId("");
			holdSeatsRequest.setVenueId("");
			holdSeatsRequest.setNumberOfSeats(5);
			request.findAndHoldSeatsValidator(holdSeatsRequest);
		} catch (Exception ex) {
			assertThatExceptionOfType(InvalidRequest.class);
		}
		
		try {
			HoldSeatsRequest holdSeatsRequest = new HoldSeatsRequest();
			holdSeatsRequest.setCustomerEmailId("test##");
			holdSeatsRequest.setVenueId("");
			holdSeatsRequest.setNumberOfSeats(5);
			request.findAndHoldSeatsValidator(holdSeatsRequest);
		} catch (Exception ex) {
			assertThatExceptionOfType(InvalidRequest.class);
		}
		
		try {
			HoldSeatsRequest holdSeatsRequest = new HoldSeatsRequest();
			holdSeatsRequest.setCustomerEmailId("");
			request.findAndHoldSeatsValidator(holdSeatsRequest);
		} catch (Exception ex) {
			assertThatExceptionOfType(InvalidRequest.class);
		}
		
		try {
			HoldSeatsRequest holdSeatsRequest = new HoldSeatsRequest();
			holdSeatsRequest.setVenueId("");
			request.findAndHoldSeatsValidator(holdSeatsRequest);
		} catch (Exception ex) {
			assertThatExceptionOfType(InvalidRequest.class);
		}
	}
	
	@Test
	public void reserverSeatsValidator()
	{
		ReserveSeatsRequest reserveSeatsRequest = new ReserveSeatsRequest();
		try {
			request.reserverSeatsValidator(reserveSeatsRequest);
		} catch (Exception ex) {
			assertThatExceptionOfType(InvalidRequest.class);
		}
		
		try {
			reserveSeatsRequest.setSeatHoldId("ABC");
			request.reserverSeatsValidator(reserveSeatsRequest);
		} catch (Exception ex) {
			assertThatExceptionOfType(InvalidRequest.class);
		}
		
		try {
			reserveSeatsRequest.setSeatHoldId("ABC");
			reserveSeatsRequest.setCustomerEmail("##@@##");
			request.reserverSeatsValidator(reserveSeatsRequest);
		} catch (Exception ex) {
			assertThatExceptionOfType(InvalidRequest.class);
		}
	}

}
