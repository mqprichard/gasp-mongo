package com.cloudbees.gasp.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GaspLocation {
	private String name;
	private String formattedAddress;
	private Location location;
	
	public GaspLocation() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public GaspLocation(String name, String formattedAddress, Location location) {
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
