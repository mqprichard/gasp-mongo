package com.cloudbees.gasp.services;

import static org.junit.Assert.*;

import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.cloudbees.gasp.model.Location;
import com.cloudbees.gasp.model.LocationQuery;
import com.google.gson.Gson;

public class LocationServiceTest {
	private final String testName = "Home";
	private final String testAddress = "1285 Altschul Ave, Menlo Park CA";
	private final String testFormattedAddress = "1285 Altschul Avenue, Menlo Park, CA 94025, USA";
	private final String testLat = "37.431523";
	private final String testLng = "-122.206428";
	
	// Utility method to validate and output Response body as JSONObject
	private static JSONObject getResponseJson( Response response ) 
			throws JSONException {
		String jsonResponse = response.getEntity().toString();
		assertFalse( jsonResponse.isEmpty() );
		
		String strJson = "{response:" + jsonResponse + "}";
		JSONObject jObject = new JSONObject(strJson);
		JSONObject json = jObject.getJSONObject("response");
		assertFalse( json == null );
		
		return ( json );
	}

	@Test
	public void uniqueResultTest() {
		
		LocationService locationService = new LocationService();
		LocationQuery location = new LocationQuery(testName, testAddress);
		Response response = locationService.checkLocation(location);
		
		try {
			// Validate HTTP Return Code
			assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
			
			// Validate formatted address
			JSONObject json = getResponseJson( response );
			assertEquals(json.get("formattedAddress"), testFormattedAddress);
			
			// Validate location
			JSONObject geometry = json.getJSONObject("geometry");
			Location myLoc = new Gson().fromJson(geometry.get("location").toString(), Location.class);
			assertEquals(String.valueOf(myLoc.getLat()),testLat);
			assertEquals(String.valueOf(myLoc.getLng()), testLng);
		}
		catch (Exception e) {
			fail();
		}
	}
	
	@Test
	public void ambiguousResultTest() {
		// Ambiguous input: Geocoder will return OK with multiple Results
		// Expect: service returns 204 No Content
		final String testAddress = "Main Street";

		try {
			LocationService locationService = new LocationService();
			LocationQuery location = new LocationQuery(testName, testAddress);
			Response response = locationService.checkLocation(location);

			// Check for 204 No Content status code
			assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
		}
		catch (Exception e) {
			fail();
		}
	}
	
	@Test
	public void noMatchTest() {
		// Bad address input: Geocoder will return ZERO_RESULTS
		// Expect: service returns 204 No Content
		final String testAddress = "xxxxxxxxxxxxx";

		try {
			LocationService locationService = new LocationService();
			LocationQuery location = new LocationQuery(testName, testAddress);
			Response response = locationService.checkLocation(location);

			// Check for 204 No Content status code
			assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
		}
		catch (Exception e) {
			fail();
		}
	}
	
	@Test
	public void latLngTest() {

		LocationService locationService = new LocationService();
		LocationQuery location = new LocationQuery(testName, testAddress);
		Response response = locationService.getLatLng(location);
		
		try {
			// Validate HTTP Return Code
			assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
			
			// Validate formatted address
			JSONObject json = getResponseJson( response );
			Location myLoc = new Gson().fromJson(json.toString(), Location.class);
			assertEquals(String.valueOf(myLoc.getLat()),testLat);
			assertEquals(String.valueOf(myLoc.getLng()), testLng);
		}
		catch (Exception e) {
			fail();
		}
	}
	
	@Test
	public void addLocationTest() {

		LocationService locationService = new LocationService();
		LocationQuery location = new LocationQuery(testName, testAddress);
		Response response = locationService.addLocation(location);
		
		try {
			// Validate HTTP Return Code
			assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
			
			// Validate formatted address
			JSONObject json = getResponseJson( response );
			assertEquals(json.get("formattedAddress"), testFormattedAddress);
			
			// Validate location 
			Location myLoc = new Gson().fromJson(json.get("location").toString(), Location.class);
			assertEquals(String.valueOf(myLoc.getLat()),testLat);
			assertEquals(String.valueOf(myLoc.getLng()), testLng);
		}
		catch (Exception e) {
			fail();
		}
	}
}
