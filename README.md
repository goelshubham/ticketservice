# Ticket Service

### Steps to build and execute via command line

1. Open git bash and execute below command to clone git repository
		 	
      		git clone https://github.com/goelshubham/ticketservice.git
        
2. Go to directory path where project is cloned and execute below command to run test cases

		gradlew test
		
3. Run below command to clean and build 

		gradlew clean build
        
4. Execute below command to start the application

		java -jar ticketservice-0.0.1-SNAPSHOT.jar
		
You should see message like below when application starts successfully. 
		
![App Startup](https://github.com/goelshubham/ticketservice/blob/master/app-start.PNG)

Note: Please note that the application is using embedded MongoDB which sometimes doesn't shutdown gracefully when the application is stopped so there might be a situation when you try to stop and then start the app and mongoDB might through error that port is still running. If that happens, try starting the app again.

### Running APIs

Ticket Service exposes three APIs - findSeats, holdSeats, and reserveSeats. All of which are RESTful and can be executed from command line using cURL commands. Install cURL on your machine as per your operating system's requirement.

#### findSeats API
This POST API will find the number of seats available within the venue. It returns the number of seats that are neither held nor reserved. Request format:

	{
		"venueId":"string",
		"levelNumber": "string"
	}
	
venueId - Required field. 
levelNumber - Optional field.

If both input parameters are passed then API return the available number of seats at requested level of input venue.
If only venueId is passed then API return the total number of available seats.

Sample Request
	curl -H "Content-Type: application/json" -d '{"venueId":"CityHall", "levelNumber":"2"}' -X POST          http://localhost:8080/ticketservive/api/findSeats | json_pp

Sample Response
	{
  		"numberOfSeats" : 50
	}


	


    
