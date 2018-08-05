package com.walmart.ticketservice.config;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import cz.jirutka.spring.embedmongo.EmbeddedMongoFactoryBean;

import com.google.gson.Gson;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.walmart.ticketservice.entity.Seat;
import com.walmart.ticketservice.entity.SeatHold;
import com.walmart.ticketservice.entity.Status;
import com.walmart.ticketservice.entity.Venue;

@Configuration
public class MongoConfig {
    private static final String MONGO_DB_URL = "localhost";
    private static final int MONGO_PORT = 23411;
    private static final String MONGO_DB_NAME = "embeded_db";
    @Bean
    public MongoTemplate mongoTemplate() throws IOException {
    	
        System.out.println("****************MongoDB configuration*******************");

        EmbeddedMongoFactoryBean mongo = new EmbeddedMongoFactoryBean();
        mongo.setBindIp(MONGO_DB_URL);
        mongo.setPort(MONGO_PORT);
        MongoClient mongoClient = mongo.getObject();
        
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, MONGO_DB_NAME);
        
        Venue venueDocument = this.loadVenueDocument();
        Gson gson = new Gson();
        
        Document document = Document.parse(gson.toJson(venueDocument));
        document.append("_id", venueDocument.getVenueId());
        document.append("time", new java.util.Date());
        
        MongoCollection<Document> venueCollection  = mongoTemplate.createCollection("venue");
        venueCollection.createIndex(Indexes.ascending("venueId"));
        venueCollection.insertOne(document);

        return mongoTemplate;
    }

    /*
     * This method create a Venue object which will be inserted into mongo db
     */
	private Venue loadVenueDocument() {

		Venue cityMusicHall = new Venue();
		cityMusicHall.setVenueId("CityHall");
		cityMusicHall.setNumberOfLevels(5);
		cityMusicHall.setNumberOfSeats(250);

		HashMap<Integer, List<Seat>> venueMap = new HashMap<Integer, List<Seat>>();
		HashMap<Integer, Integer> levelSeatMap = new HashMap<Integer, Integer>();
		for (int level = 1; level <= 5; level++) {

			List<Seat> seatList = new ArrayList<Seat>();
			for (int seatIndex = 1; seatIndex <= 50; seatIndex++) {
				Seat seat = new Seat(
						cityMusicHall.getVenueId().toUpperCase() + "-L" + level + "-"
								+ RandomStringUtils.randomAlphanumeric(4).toUpperCase(),
						cityMusicHall.getVenueId(), level);
				seatList.add(seat);
			}
			venueMap.put(level, seatList);
			levelSeatMap.put(level, seatList.size());

		}
		cityMusicHall.setSeatMap(venueMap);
		cityMusicHall.setAvailableSeats(levelSeatMap);

		return cityMusicHall;
	}
    

}
