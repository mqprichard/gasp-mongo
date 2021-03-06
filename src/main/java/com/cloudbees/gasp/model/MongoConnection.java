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

import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MongoConnection {
    private final Logger logger = LoggerFactory.getLogger(MongoConnection.class);

    private static boolean isInitialized = false;
    private static String mongoURL = "";
    
    private DB mongoDB = null;
    private Mongo mongo = null;
    private DBCollection locations = null;

    public MongoConnection() {
        if (! isInitialized) {
            mongoURL = getMongoURL();
            isInitialized = true;
        }
    }

    private String getMongoURL() {
        String useMongoURL = "";
        String envMongoURL;
        String envBuildSecretDir;
        
        // Get MONGOHQ_URL_GASP from : 
        try {
            // 1. Jenkins build secret plugin
            if ((envBuildSecretDir = System.getenv("MONGO_GASP_TEST")) != null) {
                logger.debug("Getting Build Secret from: " + envBuildSecretDir);
                FileInputStream propFile = new FileInputStream(envBuildSecretDir + "/" + "gasp-mongo.env");
                Properties p = new Properties(System.getProperties());
                p.load(propFile);
                System.setProperties(p);
                useMongoURL = System.getProperty("MONGOHQ_URL_GASP");
                logger.debug("MONGOHQ_URL_GASP (from Build Secret): " + System.getProperty("MONGOHQ_URL_GASP"));
            }
             
            // 2. System property
            else if ((envMongoURL = System.getProperty("MONGOHQ_URL_GASP")) != null) {
                logger.debug("MONGOHQ_URL_GASP (from system property): " + envMongoURL);
                useMongoURL = envMongoURL;
            }
            
            // 3, System environment
            else if ((envMongoURL = System.getenv("MONGOHQ_URL_GASP")) != null){
                logger.debug("MONGOHQ_URL_GASP (from system environment): " + envMongoURL);
                useMongoURL = envMongoURL;
            }
            
            // Error: MONGOHQ_URL_GASP not set
            else {
                logger.error("MONGOHQ_URL_GASP not set");
            }
        }
        catch (Exception e){
            logger.error(Arrays.toString(e.getStackTrace()));
        }
        return useMongoURL;
    }
    
    DB getMongoDB() {
        return mongoDB;
    }

    public Mongo getMongo() {
        return mongo;
    }

    DBCollection getCollection() {
        return locations;
    }

    public void connect() throws Exception {
        try {
            // Connect to Mongo and Authenticate
            MongoURI mongoURI = new MongoURI(mongoURL);
            mongo = new Mongo(mongoURI);
            mongoDB = mongo.getDB(mongoURI.getDatabase());
            mongoDB.authenticate(mongoURI.getUsername(), mongoURI.getPassword());

            // Get Mongo collections and set WriteConcern
            String mongoLocations = "locations";
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
