package com.common.place.model;

public class GroupMember{
	
	private String groupId;
	private String name;
	private String phone;
	private Double locationLat;
	private Double locationLon;
	public GroupMember(String groupId, String name, String phone, Double locationLat, Double locationLon) {
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
	public Double getLocationLat() {
		return locationLat;
	}
	public void setLocationLat(Double locationLat) {
		this.locationLat = locationLat;
	}
	public Double getLocationLon() {
		return locationLon;
	}
	public void setLocationLon(Double locationLon) {
		this.locationLon = locationLon;
	}
	
	@Override
	public String toString() {
		return "["+groupId+"]"+name+" "+phone+" "+locationLat+" "+locationLon;
	}
	
}
