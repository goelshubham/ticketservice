package com.walmart.ticketservice.servicehandler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.stereotype.Component;

import com.walmart.ticketservice.entity.Seat;
import com.walmart.ticketservice.entity.SeatHold;
import com.walmart.ticketservice.entity.Status;
import com.walmart.ticketservice.entity.Venue;
import com.walmart.ticketservice.repository.BookingRepository;
import com.walmart.ticketservice.utility.TicketServiceUtil;

@Component
@RunWith(MockitoJUnitRunner.class)
public class TicketServiceHandlerTest {

	@InjectMocks
	private TicketServiceHandler ticketServiceHandler;
	
	@Mock
	private TicketServiceUtil ticketServiceUtil;
	
	@Mock
	private BookingRepository bookingRepository;
	

	@Test
	public void testReleaseExpiredHoldSeats() {
		String venueId = "CityHall";
		String levelNumber = "1";
		Seat seat = new Seat();
		List<Seat> seatList = new ArrayList<Seat>();
		List<SeatHold> seatHoldList = new ArrayList<SeatHold>();
		
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

		
		when(ticketServiceUtil.getHoldTimeForVenue(venue.getVenueId())).thenReturn(60);
		when(bookingRepository.findAllExpiredHeldSeats(60, venue.getVenueId())).thenReturn(seatHoldList);
/*		when(ticketServiceUtil.getVenue(venueId)).thenReturn(venue);
		when(ticketServiceUtil.getAvailableSeats(venue, levelNumber)).thenCallRealMethod();
*/	
		ticketServiceHandler.releaseExpiredHoldSeats(venue);
	}

}
