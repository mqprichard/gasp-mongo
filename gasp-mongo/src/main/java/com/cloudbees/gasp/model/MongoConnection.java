package com.cloudbees.gasp.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

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
	
	public DBCollection getCollection() {
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
	
	public String newLocation( GeoLocation location ) {
		
		DBCollection locations = getCollection(); 

		// Search by "name" for existing record
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("name", location.getName());
		
		// Upsert from GaspLocation object
		DBObject updateExpression = new BasicDBObject();
		updateExpression.put("name", location.getName());
		updateExpression.put("formattedAddress", location.getFormattedAddress());
		
		// Include Location lat/lng data
		BasicDBObject locObj = new BasicDBObject();
		locObj.put("lng", location.getLocation().getLng());
		locObj.put("lat", location.getLocation().getLat());
		updateExpression.put("location", locObj);
		
		locations.update(searchQuery, updateExpression, true, false);
		String upsertId = locations.findOne(searchQuery).get("_id").toString();
		
		if (logger.isDebugEnabled())
			logger.debug("addLocation(): " + upsertId);

		// Return the _id field for the upserted record
		return upsertId;	
	}	
	
	public String getLocations()  {
		
		DBCollection locations = getCollection(); 
		 
		// Omit object id from result
		BasicDBObject omits = new BasicDBObject();
		omits.put("_id",0);
		
		// Search
		BasicDBObject findLocations = new BasicDBObject();			
		List<DBObject> listLocations = locations.find(findLocations, omits).limit(200).toArray();

		if (logger.isDebugEnabled())
			logger.debug("getLocations(): " + listLocations.toString());

		// Return the JSON string with the locations collection
		return( listLocations.toString() );		
	}

	// db.locations.find( { location : { $within : { $center: [ [-122.1139858, 37.3774655] , 0.005 ] } } } )
	// http://docs.mongodb.org/manual/reference/operator/center/#op._S_center
	public String getLocationsByGeoCenter(Location center,
										  double radius )  {
		if (logger.isDebugEnabled()) {
			logger.debug("getLocationsByGeoCenter(): lng = " + center.getLng());
			logger.debug("getLocationsByGeoCenter(): lat = " + center.getLat());
			logger.debug("getLocationsByGeoCenter(): radius = " + radius);
		}
		
		DBCollection locations = getCollection(); 
		locations.ensureIndex(new BasicDBObject("location", "2d"));
		 
		// Omit object id from result
		BasicDBObject omits = new BasicDBObject();
		omits.put("_id",0);
		
		// Perform geo-search
		List<Object> circle = new ArrayList<Object>();
		circle.add(new double[] { center.getLng(), center.getLat() });
		circle.add(radius);
		BasicDBObject query = new BasicDBObject("location", 
				new BasicDBObject("$within", 
						new BasicDBObject("$center", circle)));
		List<DBObject> listLocations = locations.find(query, omits).limit(200).toArray();

		if (logger.isDebugEnabled())
			logger.debug("getLocationsByGeoCenter(): " + listLocations.toString());

		// Return the JSON string with the locations collection
		return( listLocations.toString() );		
	}	
	
	public WriteResult deleteLocationByName(String name) {
		
		DBCollection locations = getCollection();
		
		BasicDBObject document = new BasicDBObject();
		document.put("name", name);
		return(locations.remove(document));
	}
	
	public WriteResult deleteLocationByAddress(String formattedAddress) {
		
		DBCollection locations = getCollection();
		
		BasicDBObject document = new BasicDBObject();
		document.put("formattedAddress", formattedAddress);
		return(locations.remove(document));
	}
	
	public WriteResult deleteLocations() {
		return(locations.remove(new BasicDBObject()));
	}
}
