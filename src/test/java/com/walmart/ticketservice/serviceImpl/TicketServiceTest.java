package com.walmart.ticketservice.serviceImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.InstanceOf;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import com.walmart.ticketservice.entity.Seat;
import com.walmart.ticketservice.entity.SeatHold;
import com.walmart.ticketservice.entity.Status;
import com.walmart.ticketservice.entity.Venue;
import com.walmart.ticketservice.exceptions.BookingException;
import com.walmart.ticketservice.exceptions.InvalidRequest;
import com.walmart.ticketservice.repository.BookingRepository;
import com.walmart.ticketservice.servicehandler.TicketServiceHandler;
import com.walmart.ticketservice.servicehandler.TicketServiceHandlerTest;
import com.walmart.ticketservice.utility.TicketServiceUtil;
import com.walmart.ticketservice.validator.RequestValidator;

import net.bytebuddy.implementation.StubMethod;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceTest {

	@InjectMocks
	private TicketServiceImpl ticketService;

	@Mock
	private RequestValidator requestValidator;

	@Mock
	private TicketServiceUtil ticketServiceUtil;
	
	@Mock
	private TicketServiceHandler ticketServiceHandler;
	
	@Mock
	private BookingRepository bookingRepository;
	
	@Test
	public void numSeatsAvailableTest_withVenueNoSeats() {
		String venueId = "CityHall";
		String levelNumber = "1";
		Venue venue = new Venue();
		venue.setNumberOfLevels(1);
		venue.setVenueId(venueId);
		
		when(ticketServiceUtil.getVenue(venueId)).thenReturn(venue);
		when(ticketServiceUtil.getAvailableSeats(venue, levelNumber)).thenReturn(0);
		int numOfSeats = ticketService.numSeatsAvailable(venueId, levelNumber);
		assertEquals(0, numOfSeats);
	}

	@Test
	public void numSeatsAvailableTest_withOneSeatHoldNonExpired() {
		String venueId = "CityHall";
		String levelNumber = "1";
		Seat seat = new Seat();
		List<Seat> seatList = new ArrayList<Seat>();
		seatList.add(seat);

		SeatHold seatHold = new SeatHold();
		seatHold.setBookingId("ABCDEFGH");
		seatHold.setBookingTime(System.currentTimeMillis());
		seatHold.setCustomerEmail("test@gmail.com");
		seatHold.setSeatList(seatList);
		seatHold.setStatus(Status.HOLD);
		seatHold.setTotalSeats(1);
		seatHold.setVenueId("CityHall");

		HashMap<Integer, List<Seat>> seatMap = new HashMap<Integer, List<Seat>>();
		HashMap<Integer, Integer> availableSeats = new HashMap<Integer, Integer>();
		availableSeats.put(1, 5);
		availableSeats.put(2, 5);
		seatMap.put(1, seatList);

		Venue venue = new Venue();
		venue.setNumberOfLevels(2);
		venue.setNumberOfSeats(10);
		venue.setSeatMap(seatMap);
		venue.setVenueId("CityHall");
		venue.setAvailableSeats(availableSeats);

		when(ticketServiceUtil.getVenue(venueId)).thenReturn(venue);
		when(ticketServiceUtil.getAvailableSeats(venue, levelNumber)).thenCallRealMethod();
		int numOfSeats = ticketService.numSeatsAvailable(venueId, levelNumber);
		assertEquals(5, numOfSeats);
	}
	
	@Test
	public void findAndHoldSeatsTest_InvalidRequestExceptionWhenHoldLimitExceeds()
	{
		boolean exception = false;
		String venueId = "CityHall";
		Venue venue = new Venue();
		when(ticketServiceUtil.getVenue(venueId)).thenReturn(venue);
		when(ticketServiceUtil.getSeatHoldLimitForVenue(venueId)).thenReturn(1);
		try {
			ticketService.findAndHoldSeats(10, "CityHall", "goyalshub@gmail.com");
		} catch (InvalidRequest ex) {
			exception = true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void findAndHoldSeatsTest_ValidRespone()
	{
		String venueId = "CityHall";
		Venue venue = new Venue();
		when(ticketServiceUtil.getVenue(venueId)).thenReturn(venue);
		when(ticketServiceUtil.getSeatHoldLimitForVenue(venueId)).thenReturn(10);
		when(ticketServiceUtil.getAvailableSeats(venue, null)).thenReturn(20);
		
		List<Seat> bestAvailableSeats = new ArrayList<Seat>();
		String seatHoldId = RandomStringUtils.randomAlphanumeric(12).toUpperCase();
		SeatHold newSeatHold = new SeatHold();
		newSeatHold.setBookingId(seatHoldId);
		newSeatHold.setCustomerEmail("Test@gmail.com");
		newSeatHold.setSeatList(bestAvailableSeats);
		newSeatHold.setStatus(Status.HOLD);
		newSeatHold.setTotalSeats(bestAvailableSeats.size());
		newSeatHold.setVenueId(venueId);
		newSeatHold.setBookingTime(System.currentTimeMillis());

		SeatHold response = ticketService.findAndHoldSeats(10, "CityHall", "goyalshub@gmail.com");
		assertEquals(true, response instanceof SeatHold);
		assertEquals(true, StringUtils.isAlphanumeric(response.getBookingId()));
	}
	
	@Test
	public void reserveSeats_validateRequestParams1()
	{
		boolean exceptionHappend = false;
		SeatHold seatHold = new SeatHold();
		seatHold.setBookingId("ABCDEGGH");
		seatHold.setCustomerEmail("test@gmail.com");
		try {
			requestValidator.reserverSeatsValidator(seatHold.getBookingId(), seatHold.getCustomerEmail());
		} catch (Exception e) {
			exceptionHappend = true;
		}
		assertFalse(exceptionHappend);
	}

	@Test
	public void reserveSeats_alreadyReservedException()
	{
		boolean exceptionHappend = false;
		SeatHold seatHold = new SeatHold();
		seatHold.setBookingId("ABCDEGGH");
		seatHold.setCustomerEmail("shubham#gmail.com");
		seatHold.setStatus(Status.RESERVED);
		Optional<SeatHold> testHold = Optional.of(seatHold);

		when(bookingRepository.findById(seatHold.getBookingId())).thenReturn(testHold);
		try {
			this.ticketService.reserveSeats(seatHold.getBookingId(), seatHold.getCustomerEmail());
		} catch (BookingException bx) {
			exceptionHappend = true;
		}
		assertTrue(exceptionHappend);
	}

	
}
