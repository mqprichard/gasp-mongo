package com.cloudbees.gasp.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
// JSON format for location-based /new and /geocenter results
// Includes formattedAddress and Location information from GeocoderService
public class GeoLocation {
    private String name;
    private String formattedAddress;
    private Location location;

    public GeoLocation() {
        super();
    }

    public GeoLocation(String name, String formattedAddress, Location location) {
        super();
        this.name = name;
        this.formattedAddress = formattedAddress;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
