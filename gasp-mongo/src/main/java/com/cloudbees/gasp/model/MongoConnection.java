package com.cloudbees.gasp.model;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

    protected String strURI = "mongodb://guest:welcome1@localhost:27017/mydb";
    protected DB mongoDB = null;
    protected Mongo mongo = null;
    protected DBCollection locations = null;
    protected String mongoLocations = "locations";

    public MongoConnection() {
        String envMongoURL = null;
        String envBuildSecretDir = null;

        // Get MONGOHQ_URL_GASP from 
        // 1. Jenkins build secret plugin
        // 2. System property ("MONGOHQ_URL_GASP");
        // 3, System environment ("MONGOHQ_URL_GASP");
        // 4. Default: Local MongoDB
        try {
         // $MONGO_GASP_TEST has the location of the secret properties file
            if ((envBuildSecretDir = System.getenv("MONGO_GASP_TEST")) != null) {
                logger.debug("MONGO_GASP_TEST = " + envBuildSecretDir);
                FileInputStream propFile = new FileInputStream(envBuildSecretDir + "/" + "gasp-mongo.env");
                Properties p = new Properties(System.getProperties());
                p.load(propFile);
                System.setProperties(p);
                logger.debug("MONGOHQ_URL_GASP (from Build Secret): " + System.getProperty("MONGOHQ_URL_GASP"));
            }
            else { 
                // Either: get MongoURL from system property
                if ((envMongoURL = System.getProperty("MONGOHQ_URL_GASP")) != null) {
                    logger.debug("Using MONGOHQ_URL_GASP system property: " + envMongoURL);
                    strURI = envMongoURL;
                }
                // Or: get MongoURL from system environment
                else if ((envMongoURL = System.getenv("MONGOHQ_URL_GASP")) != null){
                    logger.debug("Using MONGOHQ_URL_GASP from system environment: " + envMongoURL);
                    strURI = envMongoURL;
                }
                // Otherwise: default to (hard-coded) local MongoDB
                else {
                    logger.debug("Using default mongoURI: " + strURI);
                }
            }
        }
        catch (Exception e){
            logger.error(e.getStackTrace().toString());
        }
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
            MongoURI mongoURI = new MongoURI(strURI);
            mongo = new Mongo(mongoURI);
            mongoDB = mongo.getDB(mongoURI.getDatabase());
            mongoDB.authenticate(mongoURI.getUsername(), mongoURI.getPassword());

            // Get Mongo collections and set WriteConcern
            locations = getMongoDB().getCollection(mongoLocations);
            mongoDB.setWriteConcern(WriteConcern.SAFE);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String newLocation(GeoLocation location) {

        DBCollection locations = getCollection();

        // Search by "name" for existing record
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("name", location.getName());

        // Upsert from GaspLocation object
        DBObject updateExpression = new BasicDBObject();
        updateExpression.put("name", location.getName());
        updateExpression
                .put("formattedAddress", location.getFormattedAddress());

        // Include Location lat/lng data
        BasicDBObject locObj = new BasicDBObject();
        locObj.put("lng", location.getLocation().getLng());
        locObj.put("lat", location.getLocation().getLat());
        updateExpression.put("location", locObj);

        locations.update(searchQuery, updateExpression, true, false);
        String upsertId = locations.findOne(searchQuery).get("_id").toString();

        if (logger.isDebugEnabled()) logger.debug("addLocation(): " + upsertId);

        // Return the _id field for the upserted record
        return upsertId;
    }

    public String getLocations() {

        DBCollection locations = getCollection();

        // Omit object id from result
        BasicDBObject omits = new BasicDBObject();
        omits.put("_id", 0);

        // Search
        BasicDBObject findLocations = new BasicDBObject();
        List<DBObject> listLocations = locations.find(findLocations, omits)
                                                .limit(200).toArray();

        if (logger.isDebugEnabled()) logger.debug("getLocations(): "
                                                  + listLocations.toString());

        // Return the JSON string with the locations collection
        return (listLocations.toString());
    }

    // db.locations.find( { location : { $within : { $center: [ [-122.1139858, 37.3774655] , 0.005 ] } } } )
    // http://docs.mongodb.org/manual/reference/operator/center/#op._S_center
    public String getLocationsByGeoCenter(Location center, double radius) {
        if (logger.isDebugEnabled()) {
            logger.debug("getLocationsByGeoCenter(): lng = " + center.getLng());
            logger.debug("getLocationsByGeoCenter(): lat = " + center.getLat());
            logger.debug("getLocationsByGeoCenter(): radius = " + radius);
        }

        DBCollection locations = getCollection();
        locations.ensureIndex(new BasicDBObject("location", "2d"));

        // Omit object id from result
        BasicDBObject omits = new BasicDBObject();
        omits.put("_id", 0);

        // Perform geo-search
        List<Object> circle = new ArrayList<Object>();
        circle.add(new double[] { center.getLng(), center.getLat() });
        circle.add(radius);
        BasicDBObject query = new BasicDBObject(
                                   "location", new BasicDBObject(
                                   "$within", new BasicDBObject(
                                   "$center", circle)));
        List<DBObject> listLocations = locations.find(query, omits)
                                                .limit(200)
                                                .toArray();

        if (logger.isDebugEnabled()) logger.debug("getLocationsByGeoCenter(): "
                                                  + listLocations.toString());

        // Return the JSON string with the locations collection
        return (listLocations.toString());
    }

    public WriteResult deleteLocationByName(String name) {

        DBCollection locations = getCollection();

        BasicDBObject document = new BasicDBObject();
        document.put("name", name);
        return (locations.remove(document));
    }

    public WriteResult deleteLocationByAddress(String formattedAddress) {

        DBCollection locations = getCollection();

        BasicDBObject document = new BasicDBObject();
        document.put("formattedAddress", formattedAddress);
        return (locations.remove(document));
    }

    public WriteResult deleteLocations() {
        return (locations.remove(new BasicDBObject()));
    }
}
