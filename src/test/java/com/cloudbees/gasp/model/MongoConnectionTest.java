/*
 * Copyright (c) 2013 Mark Prichard, CloudBees
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudbees.gasp.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MongoConnectionTest {
	
	private final String testName = "Work";
	private final String testName2 = "CloudBees";
	private final String testAddress = "289 South San Antonio Road, Los Altos, CA 94022, USA";

	private final double testLng = -122.1139858;
	private final double testLat = 37.3774655;
	
	private final Location testLocation = new Location(testLng, testLat);
	private final GeoLocation testGaspLocation = new GeoLocation(testName, 
																   testAddress,
																   testLocation);
	private final GeoLocation testGaspLocation2 = new GeoLocation(testName2, 
			   														testAddress,
			   														testLocation);	
	private final String testResult1 = "[{ \"name\" : \"Work\" , " +
			"\"formattedAddress\" : \"289 South San Antonio Road, Los Altos, CA 94022, USA\" ," +
			" \"location\" : { \"lng\" : -122.1139858 , \"lat\" : 37.3774655}}]";
	private final String testResult2 = "[{ \"name\" : \"Work\" , " +
			"\"formattedAddress\" : \"289 South San Antonio Road, Los Altos, CA 94022, USA\" ," +
			" \"location\" : { \"lng\" : -122.1139858 , \"lat\" : 37.3774655}}, " +
			"{ \"name\" : \"CloudBees\" , " +
			"\"formattedAddress\" : \"289 South San Antonio Road, Los Altos, CA 94022, USA\" ," +
			" \"location\" : { \"lng\" : -122.1139858 , \"lat\" : 37.3774655}}]";
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
