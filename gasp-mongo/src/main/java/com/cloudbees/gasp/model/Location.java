package com.cloudbees.gasp.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
// JSON format for Location (lat/lng) objects 
// Includes LatLng information from geocoder
public class Location {
    private double lat;
    private double lng;

    public Location() {
    }

    public Location(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}