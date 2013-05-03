GASP-MONGO
==========

Geo-location search and storage for Gasp! project: mongoDB version.  Provides a short set of REST services that are useful for displaying locations on mobile devices using the Google Maps API.
An example of the service is running on CloudBees at gasp-mongo.mqprichard.cloudbees.net using the integrated MongoDB service from MongoHQ.  It can also be run using a local MongoDB.
This is a Maven project, written in Java using the Google Geocoding API v3, MongoDB Geospatial Indexing and JAX-RS/JAX-B for the REST service implementation.  Please note the [usage limits and conditions of use](https://developers.google.com/maps/documentation/geocoding/#Limits) for the Google Geocoding API.  The project currently uses the MongoDB legacy 2D co-ordinate system: this is adequate for its intended use, but I will be updating to use the Mongo 2.4 geospatial query operators. 

For more information, please see:
---------------------------------
- [Google Geocoding API v3](https://developers.google.com/maps/documentation/geocoding/)
- [MongoDB Geospatial Indexes and Queries](http://docs.mongodb.org/manual/reference/operator/query-geospatial/)

General Notes
-------------

The /new and /remove operations are idempotent: for a given name, only a single record will be upserted into Mongo. For all geocoder queries, only non-ambiguous query results will return 200 OK plus data (ambiguous or no matches will return 204 No Content). 

For geospatial queries, the units for the search radius are the same as those for the lat/lng co-ordinates (degrees): At the latitude of the San Francisco Bay area, a value of 0.005 will give a radius of about 0.3 miles, as shown in the example.


MongoURL Settings
-----------------

The application will try to read the MongoURL setting the first time the MongoConnection() constructor is called.  It does this in the following order:

1.  System property (MONGOHQ_URL_GASP) loaded from ${MONGO_GASP_TEST}/gasp-mongo.env (for use with the Jenkins Build Secret plugin)
2.  System property (MONGOHQ_URL_GASP)
3.  System environment variable (MONGOHQ_URL_GASP)

You should use a different MongoURL for continuous integration builds, or you will lose data as the unit tests will remove all locations before running. I suggest using the [Jenkins Build Secret plugin](https://wiki.jenkins-ci.org/display/JENKINS/Build+Secret+Plugin) for this.

If you are using the integrated MongoHQ service on CloudBees, note that this assumes that your MongoDB service reource (i.e. the database instance) is bound to the app using the alias GASP.  If you are using a different alias, change MONGOHQ\_URL\_GASP to MONGOHQ\_URL\_\<ALIAS\>


Running with CloudBees RUN@cloud
--------------------------------

To configure RUN@cloud to run the service, subscribe to the (free) base service from MongoHQ from GrandCentral and use the management console to create a new Mongo database resource to use.  You can then run the following commands using the Bees SDK to have the Mongo URL automatically available to your application as a system property as described above (assuming that your app is called "gasp-mongo" and your MongoDB resource is called "gasp"):

    bees app:bind -a gasp-mongo -r mongohq:gasp -as GASP

The following commands are useful to verify your MongoHQ resources and app bindings:

    bees mongohq:list
    bees app:bindings -a gasp-mongo
    

Android Client Notes
--------------------

The model classes are all in gasp-mongo/src/main/java/com/cloudbees/gasp/model.  Use LocationQuery/GeoSpatialQuery for the request body and GeoLocation/GeoLocation[] for the response.  Import these into your Android project and call the REST services as per the following example:

    final String requestURI = "http://gasp-mongo.mqprichard.cloudbees.net/locations/geocenter";
    final String requestBody = "{\"center\" : {\"lng\" : -122.1139858 , \"lat\" : 37.3774655 }, \"radius\" : 0.005}";
    HttpClient httpClient = new DefaultHttpClient();
    HttpContext localContext = new BasicHttpContext();
    HttpPost httpPost = new HttpPost(requestURI);
    httpPost.addHeader("Accept", "application/json");
    StringEntity input = new StringEntity(requestBody);
    input.setContentType("application/json");
    httpPost.setEntity(input);
    HttpResponse response = httpClient.execute(httpPost,localContext);
    HttpEntity entity = response.getEntity();
            
            ...
            
    Gson gson = new Gson();
    Type type = new TypeToken<List<GeoLocation>>(){}.getType();
    List<GeoLocation> list = gson.fromJson(text, type);
    Iterator<GeoLocation> iterator = list.iterator();
    while(iterator.hasNext()){
    GeoLocation geoLocation = iterator.next();
        Log.i(TAG, geoLocation.getName());
        Log.i(TAG, " " + geoLocation.getFormattedAddress());
        Log.i(TAG, " " + String.valueOf(geoLocation.getLocation().getLng()));
        Log.i(TAG, " " + String.valueOf(geoLocation.getLocation().getLat()));
    }


Add new geocoded location
-------------------------


curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST http://gasp-mongo.mqprichard.cloudbees.net/locations/new -d '{"name":"Cliff House","addressString":"1090 Point Lobos San Francisco CA 94121"}'

{"name":"Cliff House","formattedAddress":"1090 Point Lobos, San Francisco, CA 94121, USA","location":{"lat":37.7768388,"lng":-122.5120706}}


Remove a geocoded location
--------------------------

curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST http://gasp-mongo.mqprichard.cloudbees.net/locations/remove -d '{"name":"Cliff House","addressString":"1090 Point Lobos San Francisco CA 94121"}'


Get all locations
-----------------

curl -H "Accept: application/json" -X GET http://gasp-mongo.mqprichard.cloudbees.net/locations/get

[{ "name" : "Cliff House" , "formattedAddress" : "1090 Point Lobos, San Francisco, CA 94121, USA" , "location" : { "lng" : -122.5120706 , "lat" : 37.7768388}}, { "name" : "Alices Restaurant" , "formattedAddress" : "17288 Skyline Boulevard, Woodside, CA 94062, USA" , "location" : { "lng" : -122.2649424 , "lat" : 37.3867203}}, { "name" : "Flea Street Cafe" , "formattedAddress" : "3607 Alameda De Las Pulgas, Menlo Park, CA 94025, USA" , "location" : { "lng" : -122.2011702 , "lat" : 37.4317999}}, { "name" : "The Dutch Goose" , "formattedAddress" : "3567 Alameda De Las Pulgas, Menlo Park, CA 94025, USA" , "location" : { "lng" : -122.2016498 , "lat" : 37.431867}}, { "name" : "Mikado Restaurant" , "formattedAddress" : "161 Main Street, Los Altos, CA 94022, USA" , "location" : { "lng" : -122.114929 , "lat" : 37.3793043}}, { "name" : "Sumika Grill" , "formattedAddress" : "236 Plaza Central, Los Altos, CA 94022, USA" , "location" : { "lng" : -122.1166286 , "lat" : 37.3791531}}, { "name" : "Peets Coffee" , "formattedAddress" : "367 State Street, Los Altos, CA 94022, USA" , "location" : { "lng" : -122.1179248 , "lat" : 37.3787929}}]


Geospatial query (centred search, radius in degrees)
----------------------------------------------------

curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST http://gasp-mongo.mqprichard.cloudbees.net/locations/geocenter -d '{"center" : {"lng" : -122.1139858 , "lat" : 37.3774655 }, "radius" : 0.005}'

[{ "name" : "Sumika Grill" , "formattedAddress" : "236 Plaza Central, Los Altos, CA 94022, USA" , "location" : { "lng" : -122.1166286 , "lat" : 37.3791531}}, { "name" : "Mikado Restaurant" , "formattedAddress" : "161 Main Street, Los Altos, CA 94022, USA" , "location" : { "lng" : -122.114929 , "lat" : 37.3793043}}, { "name" : "Peets Coffee" , "formattedAddress" : "367 State Street, Los Altos, CA 94022, USA" , "location" : { "lng" : -122.1179248 , "lat" : 37.3787929}}]


Lookup an address (returns text/plain - for development only)
--------------------------------------------------------------------------------------

curl -H "Accept: text/plain" -H "Content-Type: application/json" -X POST http://gasp-mongo.mqprichard.cloudbees.net/locations/lookup -d '{"name":"Cliff House","addressString":"1090 Point Lobos San Francisco CA 94121"}'

GeocoderResult{types=[street_address], formattedAddress='1090 Point Lobos, San Francisco, CA 94121, USA', addressComponents=[GeocoderAddressComponent{longName='1090', shortName='1090', types=[street_number]}, GeocoderAddressComponent{longName='Point Lobos', shortName='Point Lobos', types=[route]}, GeocoderAddressComponent{longName='San Francisco', shortName='SF', types=[locality, political]}, GeocoderAddressComponent{longName='San Francisco', shortName='San Francisco', types=[administrative_area_level_2, political]}, GeocoderAddressComponent{longName='California', shortName='CA', types=[administrative_area_level_1, political]}, GeocoderAddressComponent{longName='United States', shortName='US', types=[country, political]}, GeocoderAddressComponent{longName='94121', shortName='94121', types=[postal_code]}], geometry=GeocoderGeometry{location=LatLng{lat=37.77683880, lng=-122.51207060}, locationType=RANGE_INTERPOLATED, viewport=LatLngBounds{southwest=LatLng{lat=37.77549521970850, lng=-122.5134133802915}, northeast=LatLng{lat=37.77819318029150, lng=-122.5107154197085}}, bounds=LatLngBounds{southwest=LatLng{lat=37.77683880, lng=-122.51207060}, northeast=LatLng{lat=37.77684960, lng=-122.51205820}}}, partialMatch=false}


Some sample data
----------------

curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST http://gasp-mongo.mqprichard.cloudbees.net/locations/new -d '{"name":"Cliff House","addressString":"1090 Point Lobos San Francisco CA 94121"}';

curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST http://gasp-mongo.mqprichard.cloudbees.net/locations/new -d '{"name":"Alices Restaurant","addressString":"17288 Skyline Boulevard Woodside, CA"}';

curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST http://gasp-mongo.mqprichard.cloudbees.net/locations/new -d '{"name":"Flea Street Cafe","addressString":"3607 Alameda de las Pulgas Menlo Park, CA 94025"}';

curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST http://gasp-mongo.mqprichard.cloudbees.net/locations/new -d '{"name":"The Dutch Goose","addressString":"3567 Alameda De Las Pulgas  Menlo Park, CA 94025"}';

curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST http://gasp-mongo.mqprichard.cloudbees.net/locations/new -d '{"name":"Mikado Restaurant","addressString":"161 Main St  Los Altos, CA"}';

curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST http://gasp-mongo.mqprichard.cloudbees.net/locations/new -d '{"name":"Sumika Grill","addressString":"236 Central Plaza Los Altos, CA"}';

curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST http://gasp-mongo.mqprichard.cloudbees.net/locations/new -d '{"name":"Peets Coffee","addressString":"367 State Street, Los Altos"}';

curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST http://gasp-mongo.mqprichard.cloudbees.net/locations/new -d '{"name":"Work","addressString":"289 S San Antonio, Los Altos 94022"}';

curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST http://gasp-mongo.mqprichard.cloudbees.net/locations/new -d '{"name":"Home","addressString":"1285 Altschul Ave, Menlo Park, CA 94025"}';
