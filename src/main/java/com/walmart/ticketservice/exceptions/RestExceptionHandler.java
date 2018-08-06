package com.walmart.ticketservice.exceptions;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@ControllerAdvice
@RestController
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

   @Override
   protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
       String error = "Test Message";
       return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, error, ex));
   }

   private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
       return new ResponseEntity<>(apiError, apiError.getStatus());
   }

   @ExceptionHandler(InvalidRequest.class)
   protected ResponseEntity<Object> handleInvalidRequest(InvalidRequest ex, WebRequest webrequest) 
   {
       ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST);
       apiError.setMessage(ex.getMessage());
       apiError.setErrorCode(ex.getErrorCode());
       return buildResponseEntity(apiError);
   }
   
   @ExceptionHandler(BookingException.class)
   protected ResponseEntity<Object> handleBookingException(BookingException ex, WebRequest webrequest) 
   {
       ApiError apiError = new ApiError(HttpStatus.NOT_FOUND);
       apiError.setMessage(ex.getMessage());
       apiError.setErrorCode(ex.getErrorCode());
       return buildResponseEntity(apiError);
   }
   
   //other exception handlers below

}
