package com.cloudbees.gasp.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GeoLocation {
    private double lat;
    private double lng;

    public GeoLocation() {
    }

    public GeoLocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
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