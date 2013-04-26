package com.cloudbees.gasp.services;

import static org.junit.Assert.*;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.cloudbees.gasp.model.Location;
import com.cloudbees.gasp.model.LocationQuery;
import com.cloudbees.gasp.model.MongoConnection;
import com.cloudbees.gasp.model.SpatialQuery;

public class LocationServiceTest {
	// Test data for /lookup, /latlng and /new test cases
	private final String testName = "Home";
	private final String testAddress = "1285 Altschul Ave, Menlo Park CA";
	private final String testGeoLocation = "{\"name\":\"Home\",\"formattedAddress\":\"1285 Altschul Avenue, Menlo Park, CA 94025, USA\",\"location\":{\"lat\":37.431523,\"lng\":-122.206428}}";
	private final String testLatLng = "LatLng{lat=37.4315230, lng=-122.2064280}";
	private final String testGeocoderResult = "GeocoderResult{types=[street_address], formattedAddress='1285 Altschul Avenue, Menlo Park, CA 94025, USA', addressComponents=[GeocoderAddressComponent{longName='1285', shortName='1285', types=[street_number]}, GeocoderAddressComponent{longName='Altschul Avenue', shortName='Altschul Ave', types=[route]}, GeocoderAddressComponent{longName='Sharon Heights', shortName='Sharon Heights', types=[neighborhood, political]}, GeocoderAddressComponent{longName='Menlo Park', shortName='Menlo Park', types=[locality, political]}, GeocoderAddressComponent{longName='San Mateo', shortName='San Mateo', types=[administrative_area_level_2, political]}, GeocoderAddressComponent{longName='California', shortName='CA', types=[administrative_area_level_1, political]}, GeocoderAddressComponent{longName='United States', shortName='US', types=[country, political]}, GeocoderAddressComponent{longName='94025', shortName='94025', types=[postal_code]}], geometry=GeocoderGeometry{location=LatLng{lat=37.4315230, lng=-122.2064280}, locationType=ROOFTOP, viewport=LatLngBounds{southwest=LatLng{lat=37.43017401970850, lng=-122.2077769802915}, northeast=LatLng{lat=37.43287198029149, lng=-122.2050790197085}}, bounds=null}, partialMatch=false}";

	// Additional location for /geocenter test
	private final String testName2 = "The Dutch Goose";
	private final String testAddress2 = "3567 Alameda De Las Pulgas  Menlo Park, CA 94025";
	
	// This location not included in geoSpatialTestResult
	private final String testName3 = "Work";
	private final String testAddress3 = "289 S San Antonio, Los Altos 94022";
	
	// Geospatial query params for /geocenter test
	private final String testLat = "37.431523";
	private final String testLng = "-122.206428";
	private final String testRadius = "0.005";
	
	// Expected output for /geocenter test case (excludes testAddress3)
	private final String geoQueryResult = 
			"[{\"name\":\"Home\",\"formattedAddress\":\"1285AltschulAvenue,MenloPark,CA94025,USA\"," +
			"\"location\":{\"lng\":-122.206428,\"lat\":37.431523}}," +
			"{\"name\":\"TheDutchGoose\",\"formattedAddress\":\"3567AlamedaDeLasPulgas,MenloPark,CA94025,USA\"," +
			"\"location\":{\"lng\":-122.2016498,\"lat\":37.431867}}]";
	
	@Test 
	public void uniqueResultTest() {
		try {
			LocationService locationService = new LocationService();
			LocationQuery location = new LocationQuery(testName, testAddress);
			Response response = locationService.checkLocation(location);
		
			// Validate HTTP Return Code and Body
			assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
			assertEquals(response.getEntity().toString(), testGeocoderResult );
		}
		catch (Exception e) {
			fail();
		}
	}
	
	@Test
	// tests /lookup where more than one match
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
		try {
			LocationService locationService = new LocationService();
			LocationQuery location = new LocationQuery(testName, testAddress);
			Response response = locationService.getLatLng(location);
		
			// Validate HTTP Return Code and Body
			assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
			assertEquals(response.getEntity().toString(), testLatLng );
		}
		catch (Exception e) {
			fail();
		}
	}
	
	@Test
	public void addLocationTest() {
		try {
			LocationService locationService = new LocationService();
			LocationQuery location = new LocationQuery(testName, testAddress);
			Response response = locationService.addLocation(location);
		
			// Validate HTTP Return Code and Body
			assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
			assertEquals(response.getEntity().toString(), testGeoLocation );
		}
		catch (Exception e) {
			fail();
		}
	}

	private void removeAll() {
		MongoConnection mongoConnection = null;
		try {
			mongoConnection = new MongoConnection();
			mongoConnection.connect();
			mongoConnection.deleteLocations();
		}
		catch (Exception e) {
			fail();
		}
		finally{
			mongoConnection.getMongo().close();
		}
	}

	private void addAll() {
		try {
			LocationService locationService = new LocationService();
			LocationQuery location = new LocationQuery(testName, testAddress);
			Response response = locationService.addLocation(location);
			assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
			
			locationService = new LocationService();
			location = new LocationQuery(testName2, testAddress2);
			response = locationService.addLocation(location);
			assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
			
			// This location not included in geoSpatialTest result
			locationService = new LocationService();
			location = new LocationQuery(testName3, testAddress3);		
			response = locationService.addLocation(location);
			assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
		}
		catch (Exception e) {
			fail();
		}
	}

	@Test
	public void geoSpatialQueryTest() {
		try {
			// Set up: addAll() will set testGeoQueryResult
			removeAll();
			addAll();
			
			LocationService locationService = new LocationService();
			Location center = new Location(Double.valueOf(testLng), Double.valueOf(testLat));
			SpatialQuery query = new SpatialQuery(center, Double.valueOf(testRadius));
			Response response = locationService.getGeoSpatialCenter(query);	
			
			// Strip whitespace and backslashes from response
			String output = response
							.getEntity()
							.toString()
							.replaceAll("\\s", "")
							.replaceAll("\\\\", "");
			
			System.out.println(output);
			
			assertEquals(output, geoQueryResult);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}