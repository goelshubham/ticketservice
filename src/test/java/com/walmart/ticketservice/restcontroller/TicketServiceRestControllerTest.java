package com.walmart.ticketservice.restcontroller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web.Server;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.walmart.ticketservice.entity.SeatHold;
import com.walmart.ticketservice.entity.Status;
import com.walmart.ticketservice.repository.BookingRepository;
import com.walmart.ticketservice.service.TicketService;
import com.walmart.ticketservice.types.FindSeatsRequest;
import com.walmart.ticketservice.types.FindSeatsResponse;
import com.walmart.ticketservice.types.HoldSeatsRequest;
import com.walmart.ticketservice.types.ReserveSeatsRequest;
import com.walmart.ticketservice.types.ReserveSeatsResponse;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.minidev.json.JSONObject;


@RunWith(MockitoJUnitRunner.class)
public class TicketServiceRestControllerTest {

	
	@InjectMocks
	private TicketServiceRestController ticketServiceRestController;
	
	@Mock
	private TicketService ticketService;
	
	@Mock
	private BookingRepository bookingRepo;
	
	@Mock
	private ReserveSeatsResponse reserverSeatResponse;
	
	@Before
	public void contextLoads() {
	}


	@Test
	public void numSeatsAvailable()
	{
		FindSeatsRequest findSeatsRequest = new FindSeatsRequest();
		findSeatsRequest.setVenueId("CityHall");
		findSeatsRequest.setLevelNumber("1");
		ticketServiceRestController.numSeatsAvailable(new FindSeatsRequest());
		ResponseEntity<FindSeatsResponse> r = ticketServiceRestController.numSeatsAvailable(new FindSeatsRequest());
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}
	
	@Test
	public void findAndHoldSeats()
	{
		HoldSeatsRequest holdSeatsRequest = new HoldSeatsRequest();
		holdSeatsRequest.setCustomerEmailId("goyal@gmail.com");
		holdSeatsRequest.setNumberOfSeats(5);
		holdSeatsRequest.setVenueId("CityHall");
		ResponseEntity<SeatHold> r = ticketServiceRestController.findAndHoldSeats(holdSeatsRequest);
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}
	
	
	
	
	@Ignore
	@Test
	public void numSeatsAvailable_restCallValidation() {
		
		//Server server = new Server(8080);
		
		RestAssured.baseURI ="http://localhost:8080/ticketservive/api";
		RequestSpecification request = RestAssured.given();
		JSONObject requestParams = new JSONObject();
		requestParams.put("venueId", "CityHall"); // Cast
		requestParams.put("levelNumber", "1");
		request.header("Content-Type", "application/json");
		 
		// Add the Json to the body of the request
		request.body(requestParams.toJSONString());
		 
		// Post the request and check the response
		Response response = request.post("/numSeatsAvailable");
	//	assertEquals("{\"numberOfSeats\":50}",response.asString());
		assertEquals(200,response.statusCode());
		
	}

	@Ignore
	@Test
	public void testFindAndHoldSeats() {
		RestAssured.baseURI ="http://localhost:8080/ticketservive/api";
		RequestSpecification request = RestAssured.given();
		JSONObject requestParams = new JSONObject();
		requestParams.put("venueId", "CityHall"); // Cast
		requestParams.put("customerEmailId", "goyalshub@gmail.com");
		requestParams.put("numberOfSeats", "5");
		request.header("Content-Type", "application/json");
		 
		// Add the Json to the body of the request
		request.body(requestParams.toJSONString());
		 
		// Post the request and check the response
		Response response = request.post("/findAndHoldSeats");
		assertEquals(200,response.statusCode());
	}
	
	@Ignore
	@Test
	public void testNumSeatsAvailable_afterHold() {
		
		RestAssured.baseURI ="http://localhost:8080/ticketservive/api";
		RequestSpecification request = RestAssured.given();
		JSONObject requestParams = new JSONObject();
		requestParams.put("venueId", "CityHall"); // Cast
		requestParams.put("levelNumber", "1");
		request.header("Content-Type", "application/json");
		 
		// Add the Json to the body of the request
		request.body(requestParams.toJSONString());
		 
		// Post the request and check the response
		Response response = request.post("/numSeatsAvailable");
		assertEquals("{\"numberOfSeats\":40}",response.asString());
		assertEquals(200,response.statusCode());
		
	}

	@Ignore
	@Test
	public void testReserveSeats() {
		RestAssured.baseURI ="http://localhost:8080/ticketservive/api";
		RequestSpecification request = RestAssured.given();
		JSONObject requestParams = new JSONObject();
		requestParams.put("seatHoldId", "INVALID-ID"); // Cast
		requestParams.put("customerEmail", "goyalshub@gmail.com");
		request.header("Content-Type", "application/json");
		 
		// Add the Json to the body of the request
		request.body(requestParams.toJSONString());
		 
		// Post the request and check the response
		Response response = request.post("/reserveSeats");
		assertEquals(400,response.statusCode());
	}

}
