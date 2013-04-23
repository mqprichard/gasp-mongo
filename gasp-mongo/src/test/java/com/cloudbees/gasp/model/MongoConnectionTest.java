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

}
