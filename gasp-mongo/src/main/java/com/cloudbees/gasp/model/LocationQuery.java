package com.cloudbees.gasp.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
// JSON format for POST data (/new, /lookup and /latlng)
public class LocationQuery {
	private String name = null;
	private String addressString = null;
	
	public LocationQuery() {
		super();
	}

	public LocationQuery(String name, String addressString) {
		super();
		this.name = name;
		this.addressString = addressString;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddressString() {
		return addressString;
	}

	public void setAddressString(String addressString) {
		this.addressString = addressString;
	}
}
