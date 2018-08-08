# Ticket Service

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


Note: Please note that the application is using embedded MongoDB which sometimes doesn't shutdown gracefully when the application is stopped so there might be a situation when you try to stop and then start the app and mongoDB might through error that port is still running. If that happens, try starting the app again.

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



    
