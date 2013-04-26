package com.cloudbees.gasp.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
// JSON format for /geocenter spatial queries
public class SpatialQuery {

    private Location center;
    private double radius;

    public SpatialQuery() {
        super();
    }

    public SpatialQuery(Location theCenter, double theRadius) {
        super();
        this.center = theCenter;
        this.radius = theRadius;
    }

    public Location getCenter() {
        return center;
    }

    public void setCenter(Location center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
