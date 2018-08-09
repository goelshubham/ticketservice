package com.walmart.ticketservice.validator;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.walmart.ticketservice.exceptions.InvalidRequest;

import groovyjarjarantlr.StringUtils;

@RunWith(MockitoJUnitRunner.class)
public class RequestValidatorTest {

	@InjectMocks
	RequestValidator request;
	
	
	@Test
	public void testNumSeatsAvailableValidator() {
		Mockito.mock(StringUtils.class);
		
		try {
			request.numSeatsAvailableValidator("" , "");
		} catch (Exception ex) {
			assertThatExceptionOfType(InvalidRequest.class);
		}
		
		
		
		
	}

}
