package com.cloudbees.gasp.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class MongoConnectionTest {
	
	private final String testName = "Work";
	private final String testName2 = "CloudBees";
	private final String testAddress = "289 South San Antonio Road, Los Altos, CA 94022, USA";
	private final double testLat = 37.3774655;
	private final double testLng = -122.1139858;
	private final GeoLocation testLocation = new GeoLocation(testLat, testLng);
	private final Location testGaspLocation = new Location(testName, 
																   testAddress,
																   testLocation);
	private final Location testGaspLocation2 = new Location(testName2, 
			   														testAddress,
			   														testLocation);	
	private final String testResult1 = "[{ \"name\" : \"Work\" , " +
			"\"formattedAddress\" : \"289 South San Antonio Road, Los Altos, CA 94022, USA\" ," +
			" \"location\" : { \"lat\" : 37.3774655 , \"lng\" : -122.1139858}}]";
	private final String testResult2 = "[{ \"name\" : \"Work\" , " +
			"\"formattedAddress\" : \"289 South San Antonio Road, Los Altos, CA 94022, USA\" ," +
			" \"location\" : { \"lat\" : 37.3774655 , \"lng\" : -122.1139858}}, " +
			"{ \"name\" : \"CloudBees\" , " +
			"\"formattedAddress\" : \"289 South San Antonio Road, Los Altos, CA 94022, USA\" ," +
			" \"location\" : { \"lat\" : 37.3774655 , \"lng\" : -122.1139858}}]";
	private final String testResult3 = "[]";
	
	private MongoConnection mongoConnection = new MongoConnection();

	@Test
	public void connectionTest() {
		try {
			mongoConnection.connect();
		}
		catch (Exception e) {
			fail();
		}
		finally {
			mongoConnection.getMongo().close();
		}
	}
	
	@Test
	public void locationCRUDTest() {
		try {
			// Start by deleting all documents from collection
			mongoConnection.connect();
			mongoConnection.deleteLocations();
			
			// Add a document
			mongoConnection.newLocation(testGaspLocation);
			assertEquals(mongoConnection.getLocations(), testResult1);
			
			// Test upsert: add the same document, no change
			mongoConnection.newLocation(testGaspLocation);
			assertEquals(mongoConnection.getLocations(), testResult1);
			
			// Add a second document
			mongoConnection.newLocation(testGaspLocation2);
			assertEquals(mongoConnection.getLocations(), testResult2);
			
			// Delete the second document
			mongoConnection.deleteLocationByName("CloudBees");
			assertEquals(mongoConnection.getLocations(), testResult1);
			
			// Delete the first document
			mongoConnection.deleteLocationByAddress(testAddress);
			assertEquals(mongoConnection.getLocations(), testResult3);
		}
		catch (Exception e) {
			fail();
		}
		finally {
			mongoConnection.getMongo().close();
		}
	}	
}
