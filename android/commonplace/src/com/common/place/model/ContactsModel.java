package com.common.place.model;

public class ContactsModel{
	
	private String groupId;
	private String name;
	private String phone;
	private String locationLat;
	private String locationLon;
	public ContactsModel(String groupId, String name, String phone, String locationLat, String locationLon) {
		super();
		this.groupId = groupId;
		this.name = name;
		this.phone = phone;
		this.locationLat = locationLat;
		this.locationLon = locationLon;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
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
	
	
	
}
