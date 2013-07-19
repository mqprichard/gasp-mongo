package com.cloudbees.gasp.services;

import static org.junit.Assert.*;
import org.junit.Test;

import com.cloudbees.gasp.model.LocationQuery;

public class GeocoderServiceTest {
    private final String testName = "Home";
    private final String testAddress = "1285 Altschul Ave, Menlo Park CA";
    private final String testFormattedAddress = "1285 Altschul Avenue, Menlo Park, CA 94025, USA";
    private final String testLat = "37.4315230";
    private final String testLng = "-122.2064280";

    @Test
    public void geoLocationTest() {
        LocationQuery location = new LocationQuery(testName, testAddress);
        GeocoderService geocoder = new GeocoderService();

        assertEquals(geocoder.callGeocoder(location), true);
        assertEquals(geocoder.getGeocoderResponse()
                             .getResults()
                             .get(0)
                             .getFormattedAddress(), testFormattedAddress);
        assertEquals(geocoder.getGeocoderResponse()
                             .getResults()
                             .get(0)
                             .getGeometry()
                             .getLocation()
                             .getLat()
                             .toString(), testLat);
        assertEquals(geocoder.getGeocoderResponse()
                             .getResults()
                             .get(0)
                             .getGeometry()
                             .getLocation()
                             .getLng()
                             .toString(), testLng);
    }
}
