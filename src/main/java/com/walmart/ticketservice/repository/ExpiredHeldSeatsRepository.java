package com.walmart.ticketservice.repository;

import java.util.List;

import com.walmart.ticketservice.entity.SeatHold;

public interface ExpiredHeldSeatsRepository {
	
	public List<SeatHold> findAllExpiredHeldSeats(int holdTime);

}
