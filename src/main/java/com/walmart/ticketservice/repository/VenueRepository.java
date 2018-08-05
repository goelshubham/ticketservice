package com.walmart.ticketservice.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.walmart.ticketservice.entity.Venue;

public interface VenueRepository extends MongoRepository<Venue, String>{
	
	public Optional<Venue> findById(String id);

}
