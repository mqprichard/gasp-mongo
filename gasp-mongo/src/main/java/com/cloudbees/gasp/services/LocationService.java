package com.cloudbees.gasp.services;

import javax.servlet.http.HttpServlet;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudbees.gasp.model.GeoLocation;
import com.cloudbees.gasp.model.Location;
import com.cloudbees.gasp.model.LocationQuery;
import com.cloudbees.gasp.model.MongoConnection;
import com.cloudbees.gasp.model.SpatialQuery;

import com.google.gson.Gson;

@Path("/locations")
public class LocationService extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(LocationService.class);
	private MongoConnection mongoConnection = new MongoConnection();

	@POST
    @Path("/new")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addLocation( LocationQuery location ) {
		try {
			logger.debug("Name = " + location.getName());
			logger.debug("AddressString = " + location.getAddressString());	
			
			GeocoderService geocoder = new GeocoderService();
			
			if (geocoder.callGeocoder(location)) {
			
				// Create a Location object from GeocoderResponse
				Gson gson = new Gson();
				String json = gson.toJson(geocoder.getGeocoderResponse()
													.getResults().get(0)
													.getGeometry()
													.getLocation());
				Location theLocation = gson.fromJson(json, Location.class);
			
				// Get formatted address string from GeocoderResponse
				String formattedAddress = geocoder.getGeocoderResponse()
													.getResults()
													.get(0)
													.getFormattedAddress();
			
				// GaspLocation is stored in Mongo and returned to the client
				GeoLocation gaspLocation = new GeoLocation(location.getName(),
									 					   formattedAddress,
									 					   theLocation);
				mongoConnection.connect();
				mongoConnection.newLocation(gaspLocation);
			
				//Return 200 OK plus GaspLocation data
				return Response
						.status(Response.Status.OK)
						.entity(new Gson().toJson(gaspLocation))
						.build();
			}
			else {
				return Response.status(geocoder.getErrorCode()).build();
			}
		}
		catch (Exception e){
			logger.error("addLocation()", e.getStackTrace());
	    	return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();		
		}
	}
	
	@POST
    @Path("/lookup")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkLocation( LocationQuery location ) {
		try {
			logger.debug("Name = " + location.getName());
			logger.debug("AddressString = " + location.getAddressString());
			
			GeocoderService geocoder = new GeocoderService();
			
			if (geocoder.callGeocoder(location)) {
				String geoResult = geocoder.getGeocoderResponse()
						   				.getResults()
						   				.get(0)
						   				.toString();
				logger.debug("Geocoder Result: " + geoResult);
				
				return Response
						.status(Response.Status.OK)
						.entity(geoResult.toString())
						.build();
			}
			else {
				return Response.status(geocoder.getErrorCode()).build();
			}
		}
		catch (Exception e){
			logger.error("getLatLng()", e.getStackTrace());
	    	return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();		
		}
	}
	
	@POST
    @Path("/latlng")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON) 
	public Response getLatLng( LocationQuery location ) {
		try {
			logger.debug("Name = " + location.getName());
			logger.debug("AddressString = " + location.getAddressString());
			
			GeocoderService geocoder = new GeocoderService();
			if (geocoder.callGeocoder(location)) {
				String geoResult = geocoder.getGeocoderResponse()
						   				.getResults()
						   				.get(0)
						   				.getGeometry()
						   				.getLocation()
						   				.toString();
				
				logger.debug("Geocoded LatLng: " + geoResult);
				
				//We have a match: return 200 OK plus location data
				return Response
						.status(Response.Status.OK)
						.entity(geoResult.toString())
						.build();						
			}
			else {
				return Response.status(geocoder.getErrorCode()).build();
			}
		}
		catch (Exception e){
			logger.error("getLatLng()", e.getStackTrace());
	    	return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();		
		}
	}	
	
	@GET
    @Path("/get")	
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLocations() {
		StatusType statusCode = null;
		String result = null;
		
		try {			
			mongoConnection.connect();
			result = mongoConnection.getLocations();
			statusCode = Response.Status.OK;
		} 
		catch (Exception e) {
			logger.error("getLocations()" + e.getStackTrace());
    		statusCode = Response.Status.INTERNAL_SERVER_ERROR;
		}
		finally {
			mongoConnection.getMongo().close();
		}
		
		if (statusCode != Response.Status.OK)
			return Response.status(statusCode).build();
		else
			return Response.status(statusCode).entity(result).build();
	}	

	@POST
    @Path("/geocenter")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGeoSpatialCenter( SpatialQuery query ) {
		StatusType statusCode = null;
		String result = null;
		
		try {			
			mongoConnection.connect();
			result = mongoConnection.getLocationsByGeoCenter(
										query.getCenter(), query.getRadius());
			statusCode = Response.Status.OK;
		} 
		catch (Exception e) {
			logger.error("getGeoSpatialCenter", e.getStackTrace());
    		statusCode = Response.Status.INTERNAL_SERVER_ERROR;
		}
		finally {
			mongoConnection.getMongo().close();
		}
		
		if (statusCode != Response.Status.OK)
			return Response.status(statusCode).build();
		else
			return Response.status(statusCode).entity(result).build();
	}
	
}
