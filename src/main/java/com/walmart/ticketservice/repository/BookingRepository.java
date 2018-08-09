package com.walmart.ticketservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.walmart.ticketservice.entity.SeatHold;



public interface BookingRepository extends MongoRepository<SeatHold, String>, ExpiredHeldSeatsRepository{
	
	public Optional<SeatHold> findById(String bookingId);

	public List<SeatHold> findAllExpiredHeldSeats(int holdTime, String venueID);
}
