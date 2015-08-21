package com.common.place.model;

import java.io.Serializable;

public class ContactsModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String phone;
	private String locationLat;
	private String locationLon;
	
	public ContactsModel(String name, String phone, String locationLat, String locationLon) {
		super();
		this.name = name;
		this.phone = phone;
		this.locationLat = locationLat;
		this.locationLon = locationLon;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getLocationLat() {
		return locationLat;
	}
	public void setLocationLat(String locationLat) {
		this.locationLat = locationLat;
	}
	public String getLocationLon() {
		return locationLon;
	}
	public void setLocationLon(String locationLon) {
		this.locationLon = locationLon;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
