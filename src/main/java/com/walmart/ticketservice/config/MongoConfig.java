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
        EmbeddedMongoFactoryBean mongo = new EmbeddedMongoFactoryBean();
        mongo.setBindIp(MONGO_DB_URL);
        mongo.setPort(MONGO_PORT);
        MongoClient mongoClient = mongo.getObject();
        
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, MONGO_DB_NAME);
        
        //MongoOperations obj = mongoClient.getMongoClientOptions();
        //Venue ven = new Venue();
        Venue venueDocument = this.loadVenueDocument();
        SeatHold seatHold = new SeatHold(UUID.randomUUID().toString(),  "goyalshub@gmail.com", new ArrayList<Seat>(), 1, venueDocument.getVenueId(), 5, Status.HELD);
        //ven.setNumberOfSeats(1000);
        Gson gson = new Gson();
        
        Document seatDocument = Document.parse(gson.toJson(seatHold));
        Timestamp time = new Timestamp(System.currentTimeMillis());
		time.setTime(time.getTime() - TimeUnit.MINUTES.toMillis(300));
		java.util.Date date = time;
        seatDocument.append("_id", "12345").append("bookingTime",  new java.util.Date());
        
        
        Document document = Document.parse(gson.toJson(venueDocument));
        document.append("_id", venueDocument.getVenueId());
        document.append("time", new java.util.Date());
        
       
        MongoCollection<Document> venueCollection  = mongoTemplate.createCollection("venue");
        venueCollection.createIndex(Indexes.ascending("venueId"));
        venueCollection.insertOne(document);

        MongoCollection<Document> seatholdCollection  = mongoTemplate.createCollection("seathold");
        seatholdCollection.insertOne(seatDocument);
        
        System.out.println("Mongo config");
        System.out.println(mongoTemplate.getCollection("seathold").find());
        FindIterable<Document> doc =  mongoTemplate.getCollection("seathold").find();
        System.out.println(doc.first().toJson());
        
        return mongoTemplate;
    }

    /*
     * This method create a Venue object which will be inserted into mongo db
     */
	private Venue loadVenueDocument() {

		Venue cityMusicHall = new Venue();
		cityMusicHall.setVenueId("CityMusicHall");
		cityMusicHall.setNumberOfLevels(5);
		cityMusicHall.setHoldLimit(5);
		cityMusicHall.setNumberOfSeats(250);

		HashMap<Integer, List<Seat>> venueMap = new HashMap<Integer, List<Seat>>();
		for (int level = 1; level <= 5; level++) {

			List<Seat> seatList = new ArrayList<Seat>();
			for (int seatIndex = 1; seatIndex <= 50; seatIndex++) {
				Seat seat = new Seat(UUID.randomUUID().toString(), "CityMusicHall", level);
				seatList.add(seat);
			}
			venueMap.put(level, seatList);
		}
		cityMusicHall.setSeatMap(venueMap);

		return cityMusicHall;
	}
    

}
