# Ticket Service

Technology Stack: Java 8, Spring, Spring Boot, Spring Data, Embeded MongoDB, REST, Junit, Mockito, Eclipse IDE

## Steps to build and execute via command line

1. Open git bash and execute below command to clone git repository
		 	
      	git clone https://github.com/goelshubham/ticketservice.git
        
2. Go to directory path where project is cloned and execute below command to run test cases

		gradlew test
		
3. Run below command to clean and build 

		gradlew clean build
        
4. Execute below command to start the application

		java -jar ticketservice-0.0.1-SNAPSHOT.jar
		
You should see message like below when application starts successfully. 
		
	2018-08-07 19:28:59.053  INFO 30232 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
	2018-08-07 19:28:59.109  INFO 30232 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
	2018-08-07 19:28:59.112  INFO 30232 --- [           main] c.w.t.TicketserviceApplication           : Started TicketserviceApplication in 10.515 seconds (JVM running for 10.934)


Note: Please note that the application is using embedded MongoDB which sometimes doesn't shutdown gracefully when the application is stopped so there might be a situation when you try to stop and then start the app and mongoDB might through error that port is still running. MongoDB is configured to run on port - 12347. If applications fails to start due to MongoDB error, try starting the app again or closing the port manually using TASKKILL command. 

## Running APIs via Command Line

Ticket Service exposes three APIs - numSeatsAvailable, findAndHoldSeats, and reserveSeats. All of which are RESTful and can be executed from command line using cURL commands. Install cURL on your machine as per your operating system's requirement.

### numSeatsAvailable API 
This POST API will find the number of seats available within the venue. It returns the number of seats that are neither held nor reserved. 

<b>Endpoint</b>
		
    http://localhost:8080/ticketservive/api/numSeatsAvailable

<b>Request format</b>

	{
		"venueId":"string",
		"levelNumber": "string"
	}
	
*venueId - Required field.   levelNumber - Optional field.*

If both input parameters are passed then API return the available number of seats at requested level of input venue.
If only venueId is passed then API return the total number of available seats.

<b>Sample Request</b>

	curl -H "Content-Type: application/json" -d '{"venueId":"CityHall", "levelNumber":"2"}' -X POST http://localhost:8080/ticketservive/api/numSeatsAvailable | json_pp

<b>Sample Response</b>
	
    {
  		"numberOfSeats" : 50
	}



### findAndHoldSeats API
This POST API will find and hold the best available seats for a customer. It returns a SeatHold object identifying the specific seats and related information.

<b>Endpoint</b>

	http://localhost:8080/ticketservive/api/findAndHoldSeats
    

<b>Request Format</b>

	{
		"venueId": "string",
		"customerEmailId": "string",
		"numberOfSeats" : Integer
	}
    

<b>Sample Request</b>

	curl -H "Content-Type: application/json" -d '{"venueId":"CityHall", "customerEmailId": "goyalshub@gmail.com", "numberOfSeats" : 2}' -X POST http://localhost:8080/ticketservive/api/findAndHoldSeats | json_pp
    

<b>Sample Response</b>

	{
    "bookingId": "ZXYTRMWT322C",
    "customerEmail": "goyalshub@gmail.com",
    "bookingTime": 1533795541261,
    "seatList": [
        {
            "seatId": "CITYHALL-L1-OCJZ",
            "venueId": "CityHall",
            "levelId": 1,
            "status": "HOLD"
        },
        {
            "seatId": "CITYHALL-L1-MAUC",
            "venueId": "CityHall",
            "levelId": 1,
            "status": "HOLD"
        }
    ],
    "venueId": "CityHall",
    "totalSeats": 2,
    "status": "HOLD"
}


### reserveSeats API
This API Commit seats held for a specific customer. 

<b>Endpoint</b>

	http://localhost:8080/ticketservive/api/reserveSeats
    
<b>Request Format</b>

	{
		"seatHoldId": "string",
		"customerEmail" : "string"
	}
    
    
<b>Sample Request</b>
		
      curl -H "Content-Type: application/json" -d '{"seatHoldId": "BKFJRPLWLU46", "customerEmail": "goyalshub@gmail.com"}' -X POST http://localhost:8080/ticketservive/api/reserveSeats | json_pp 


<b>Sample Response</b>

	{
    	"bookingCode": "BKFJRPLWLU46",
    	"customerEmail": "goyalshub@gmail.com",
    	"status": "RESERVED",
    	"numberOfSeats": 2,
    	"venueId": "CityHall"
	}


## Design And Implementation

For the purpose of performance and scalability, I have used NoSQL database to persist data. For the sake of simplicity of this project, I have used an embedded MongoDB version.

Ticket Service has three main entities:
1. Venue
2. SeatHold
3. Seat

A mongoDB collection is created for Venue which will have details such as unique venueID,
total number of seats, total number of levels, embeded list of Seats at all level.

Venue collection design in Mongo DB is as below:

	{
    	"venueId": "String",
        "numberOfLevels": "Integer",
        "totalNumberOfSeats": "Integer",
        "availableSeatsAtLevel" : 
        {
        	[
        		{"levelNumber": "Number of Seats"},
            	{"levelNumber": "Number of Seats"},
            	.
            	.
            	.
            ]
        },
        "seatMap":
        {
        	[
            	{"levelNumber" : 
            				{
            					[
                                	{
                                    	"_id": "ObjectId",
            							"seatId": "String",
                                        "venueId": "String",
                                        "levelId" : "Integer"
                                	},
                                    {
                                    	"_id": "ObjectId",
            							"seatId": "String",
                                        "venueId": "String",
                                        "levelId" : "Integer"
                                    },
                                    .
                                    .
                                    .
                     			]
                     		}
                  }
                  {"levelNumber" :
                  
                  }
                  .
                  .
             ]
         }
            

SeatHold collection design in MongoDB is as below:

	{
    	"bookingId": "String",
        "customerEmail": "String",
        "bookingTime" : "Integer",
        "seatList": {
                      [
                           {
                              "_id": "ObjectId",
                              "seatId": "String",
                              "venueId": "String",
                              "levelId" : "Integer"
                           },
                           {
                              "_id": "ObjectId",
                              "seatId": "String",
                              "venueId": "String",
                              "levelId" : "Integer"
                           },
                           .
                           .
                       ]
                     },
          "venueId": "String",
          "totalSeats": "Integer",
          "status": "String"
     }
    
    
 ![DB-image](https://github.com/goelshubham/ticketservice/blob/master/img/db-image.png)
 
 
#### Service Flow Diagram

![serviceflow](https://github.com/goelshubham/ticketservice/blob/master/img/Flow%20Diagram.png)


### Assumptions
1. Best available seats will be determined by first come first basis. All seats at level 1 are equally considered best. Seats at level 2 will be considered next best available. Seats at level 3 will be considered next best available and so on.
2. Sample data loaded into MongoDB at application startup has only one venue with venueID as 'CityHall' with 5 levels and total number of seats at each level is 50.
3. Seat Hold time limit and Number of Seat Hold are configured in property file which can be configured in an external property file. These properties can be modified externally and depending on user traffic and other real time scenarios, without having to restart the application. Current time limit is 120 seconds and 10 seat hold limit.
4. For numSeatsAvailable, user can pass either venueID alone or bothe venueID and levelNumber. If only VenueId is passed then total number of available seats at venueID is returned. When bothe venueID and levelNumber is passed then only seats available at that level are returned.
5. For findAndHoldSeats, user will be allocated best available seats starting from first level till last level. If enough seats are not available at a level then all seats are booked at next best level. All seats are booked together at any one level.
6. For reserveSeats, if user corfirms the booking, booking and seat status is changed from HOLD to RESERVED and seatHold ID becomes the Booking Code.
7. I am assuming that Ticket Service will be consumed by another application. Ticket service is returning error code such as TICKET-SERVICE-1000 in case of exceptions which will be used by calling system to display user friendly message on UI.
8. Ticket prices and billing is not taken into consideration.
9. SeatHold structure has SeatHold ID, Status, List of Seats, Total Number of Seats, Venue ID, Booking Time, and Customer Email. 


## Scope of Improvements

1. We can implement synchronization and transaction management to avoid deadlocks and have consistent system state.
2. Best Seat Availability logic can be improved considering rows and columns in the Venue. 
3. More extensive JUnit testing. 

        
    
