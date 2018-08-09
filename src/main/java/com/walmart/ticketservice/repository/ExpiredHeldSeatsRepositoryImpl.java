package com.walmart.ticketservice.repository;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.walmart.ticketservice.entity.SeatHold;
import com.walmart.ticketservice.entity.Status;

public class ExpiredHeldSeatsRepositoryImpl implements ExpiredHeldSeatsRepository {

	
	private final MongoTemplate mongoTemplate;
	
	@Autowired
	public ExpiredHeldSeatsRepositoryImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
	
	@DateTimeFormat(iso=ISO.DATE_TIME)
	java.util.Date date;
	
	@Override
	public List<SeatHold> findAllExpiredHeldSeats(int holdTime, String venueID) {
		final Query query = new Query();
		final List<Criteria> criteria = new ArrayList<Criteria>();
		Long expiredTimeLimit = System.currentTimeMillis() - (holdTime *1000);
		criteria.add(Criteria.where("venueId").is(venueID));
		criteria.add(Criteria.where("bookingTime").lte(expiredTimeLimit));
		criteria.add(Criteria.where("status").is(Status.HOLD.toString()));
		
		
		if(!criteria.isEmpty())
			query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));
		
		List<SeatHold> list =  mongoTemplate.find(query, SeatHold.class);
		return list;
	}

}
