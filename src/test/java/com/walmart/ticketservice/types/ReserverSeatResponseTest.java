package com.walmart.ticketservice.types;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.walmart.ticketservice.entity.Status;

@RunWith(MockitoJUnitRunner.class)
public class ReserverSeatResponseTest {

	@InjectMocks
	ReserveSeatsResponse reserveSeatsResponse;
	
	
	@Test
	public void testGetBookingCode() {
		
		String bookingCode = "ABCDEF";
		reserveSeatsResponse.setBookingCode(bookingCode);
		assertEquals(reserveSeatsResponse.getBookingCode(), "ABCDEF");
		
	}

	@Test
	public void testSetBookingCode() {
		reserveSeatsResponse.setBookingCode("ABC");
		assertEquals(reserveSeatsResponse.getBookingCode(), "ABC");
	}

	@Test
	public void testGetCustomerEmail() {
		reserveSeatsResponse.setCustomerEmail("test@gmail.com");
		assertEquals(reserveSeatsResponse.getCustomerEmail(), "test@gmail.com");
	}

	@Test
	public void testSetCustomerEmail() {
		reserveSeatsResponse.setCustomerEmail("email");
		assertEquals(reserveSeatsResponse.getCustomerEmail(), "email");
	}

	@Test
	public void testGetStatus() {
		reserveSeatsResponse.setStatus(Status.HOLD.toString());
		assertEquals(reserveSeatsResponse.getStatus(), Status.HOLD.toString());
	}

	@Test
	public void testSetStatus() {
		reserveSeatsResponse.setStatus(Status.HOLD.toString());
		assertEquals(reserveSeatsResponse.getStatus(), Status.HOLD.toString());
	}

	@Test
	public void testGetNumberOfSeats() {
		reserveSeatsResponse.setNumberOfSeats(10);
		assertEquals(reserveSeatsResponse.getNumberOfSeats(), 10);
	}

	@Test
	public void testSetNumberOfSeats() {
		reserveSeatsResponse.setNumberOfSeats(10);
		assertEquals(reserveSeatsResponse.getNumberOfSeats(), 10);
	}

	@Test
	public void testGetVenueId() {
		reserveSeatsResponse.setVenueId("CityHall");
		assertEquals(reserveSeatsResponse.getVenueId(), "CityHall");
	}

	@Test
	public void testSetVenueId() {
		reserveSeatsResponse.setVenueId("CityHall");
		assertEquals(reserveSeatsResponse.getVenueId(), "CityHall");
	}

	@Test
	public void testGetSeatList() {
		reserveSeatsResponse.setSeatList(null);
		assertNull(reserveSeatsResponse.getSeatList());
	}

	@Test
	public void testSetSeatList() {
		reserveSeatsResponse.setSeatList(null);
		assertNull(reserveSeatsResponse.getSeatList());
	}

}
