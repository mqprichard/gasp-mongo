package com.cloudbees.gasp.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import com.mongodb.WriteConcern;


public class MongoConnection {
	private final Logger logger = LoggerFactory.getLogger(MongoConnection.class);

	protected String envMongoURI = "";
	//protected String strURI = "mongodb://cloudbees:6dce0b9d30f52ac73bfa74c492aa3382@alex.mongohq.com:10064/ELSfmlamgpGNTqD6jFEw";
	protected String strURI = "mongodb://guest:welcome1@localhost:27017/mydb";
	protected DB mongoDB = null;
	protected Mongo mongo = null;
	protected DBCollection locations = null;
	protected String mongoLocations = "locations";
	
	public MongoConnection() {
		// Get MongoURI from system property/environment
		envMongoURI = System.getProperty( "mongoURI" );
		if (envMongoURI==null)
			envMongoURI = System.getenv( "mongoURI" );
		
		if ( ! (envMongoURI == null) ) {
			logger.info( "Using MongoURI from system environment: " 
								+ envMongoURI);
			strURI = envMongoURI;
		}
		else
			System.out.println( "MongoURI system environment not set - " 
								+ "Using default: " + strURI);
	}
	
	public DB getMongoDB() {
		return mongoDB;
	}

	public Mongo getMongo() {
		return mongo;
	}
	
	public DBCollection getLocations() {
		return locations;
	}

	public void connect() throws Exception {
		try {
			// Connect to Mongo and Authenticate
		    MongoURI mongoURI = new MongoURI( strURI );
		    mongo = new Mongo( mongoURI );
		    mongoDB = mongo.getDB( mongoURI.getDatabase() );
		    mongoDB.authenticate(mongoURI.getUsername(), mongoURI.getPassword());
		    
		    // Get Mongo collections and set WriteConcern
			locations = getMongoDB().getCollection(mongoLocations);
			mongoDB.setWriteConcern(WriteConcern.SAFE);
		} 
		catch ( Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public String newGaspLocation( GaspLocation location ) {
		
		DBCollection games = getLocations(); 
		
		DBObject obj = new BasicDBObject();
		obj.put("name", location.getName());
		obj.put("address", location.getFormattedAddress());
		obj.put("lat", location.getLocation().getLat());
		obj.put("lng", location.getLocation().getLng());
		games.insert(obj);
		
		return obj.get("_id").toString();	
	}	
}
