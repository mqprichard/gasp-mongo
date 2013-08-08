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

import com.cloudbees.gasp.model.LocationQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
