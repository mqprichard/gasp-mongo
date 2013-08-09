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

package com.cloudbees.gasp.services;

import com.cloudbees.gasp.model.*;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import java.util.Arrays;

@Path("/locations")
public class LocationService extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(LocationService.class);
    private final MongoConnection mongoConnection = new MongoConnection();

    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addLocation(LocationQuery location) {
        try {
            logger.debug("addLocation: Name = " + location.getName());
            logger.debug("addLocation: AddressString = "
                         + location.getAddressString());

            GeocoderService geocoder = new GeocoderService();

            if (geocoder.callGeocoder(location)) {

                // Create a Location object from GeocoderResponse
                Gson gson = new Gson();
                String json = gson.toJson(geocoder.getGeocoderResponse()
                                  .getResults()
                                  .get(0)
                                  .getGeometry()
                                  .getLocation());
                Location theLocation = gson.fromJson(json, Location.class);

                // Get formatted address string from GeocoderResponse
                String formattedAddress = geocoder.getGeocoderResponse()
                        .getResults().get(0).getFormattedAddress();

                // GeoLocation is stored in Mongo and returned to the client
                GeoLocation geoLocation = new GeoLocation(location.getName(),
                                                          formattedAddress, 
                                                          theLocation);
                mongoConnection.connect();
                mongoConnection.newLocation(geoLocation);

                // Return 200 OK plus GeoLocation data
                return Response.status(Response.Status.OK)
                        .entity(new Gson().toJson(geoLocation)).build();
            }
            else {
                return Response.status(geocoder.getErrorCode()).build();
            }
        }
        catch (Exception e) {
            logger.error("addLocation()", e.getStackTrace());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        finally {
            mongoConnection.getMongo().close();
        }
    }

    @POST
    @Path("/remove")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeLocation(LocationQuery location) {
        try {
            logger.debug("removeLocation: Name = " + location.getName());

            mongoConnection.connect();
            mongoConnection.deleteLocationByName(location.getName());

            return Response.status(Response.Status.OK).build();
        }
        catch (Exception e) {
            logger.error("removeLocation()", e.getStackTrace());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        finally {
            mongoConnection.getMongo().close();
        }
    }

    @POST
    @Path("/lookup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response checkLocation(LocationQuery location) {
        try {
            logger.debug("checkLocation: Name = " + location.getName());
            logger.debug("checkLocation: AddressString = "
                         + location.getAddressString());

            GeocoderService geocoder = new GeocoderService();

            if (geocoder.callGeocoder(location)) {
                String geoResult = geocoder.getGeocoderResponse()
                                           .getResults()
                                           .get(0)
                                           .toString();
                logger.debug("Geocoder Result: " + geoResult);

                return Response.status(Response.Status.OK)
                        .entity(geoResult).build();
            }
            else {
                return Response.status(geocoder.getErrorCode()).build();
            }
        }
        catch (Exception e) {
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
            logger.error("getLocations()" + Arrays.toString(e.getStackTrace()));
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
    public Response getGeoSpatialCenter(SpatialQuery query) {
        StatusType statusCode = null;
        String result = null;

        try {
            mongoConnection.connect();
            result = mongoConnection.getLocationsByGeoCenter(query.getCenter(),
                                                             query.getRadius());
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
