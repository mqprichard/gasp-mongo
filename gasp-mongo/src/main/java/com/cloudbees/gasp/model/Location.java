package com.cloudbees.gasp.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Location {
	private String name;
	private String formattedAddress;
	private GeoLocation location;
	
	public Location() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Location(String name, String formattedAddress, GeoLocation location) {
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
	public GeoLocation getLocation() {
		return location;
	}
	public void setLocation(GeoLocation location) {
		this.location = location;
	}
}
