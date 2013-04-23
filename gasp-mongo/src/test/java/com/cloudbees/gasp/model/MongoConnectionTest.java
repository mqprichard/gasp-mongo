package com.cloudbees.gasp.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class MongoConnectionTest {
	MongoConnection mongoConnection = new MongoConnection();

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
	public void addGaspLocationsTest() {
		try {
			mongoConnection.connect();
			System.out.println(mongoConnection.getGaspLocations());
			// TODO
			// Define Tests
		}
		catch (Exception e) {
			fail();
		}
		finally {
			mongoConnection.getMongo().close();
		}
	}	

	@Test
	public void getGaspLocationsTest() {
		try {
			mongoConnection.connect();
			System.out.println(mongoConnection.getGaspLocations());
			// TODO
			// Define Tests
		}
		catch (Exception e) {
			fail();
		}
		finally {
			mongoConnection.getMongo().close();
		}
	}
}
