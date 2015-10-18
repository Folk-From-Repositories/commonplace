package com.common.place.model;

import java.util.ArrayList;

public class Group{
	private String id;
	private String title;
	private String owner;
	private String dateTime;
	private String locationName;
	private String locationImageUrl;
	private String locationLat;
	private String locationLon;
	private String locationPhone;
	private String locationDesc;
	private ArrayList<String> member = new ArrayList<String>();
	
	public Group(String id, String title, String owner, String time, String locationName, String locationImageUrl,String locationLat,
			String locationLon, String locationPhone, String locationDesc, ArrayList<String> member) {
		super();
		this.id = id;
		this.title = title;
		this.owner = owner;
		this.dateTime = time;
		this.locationName = locationName;
		this.locationImageUrl = locationImageUrl;
		this.locationLat = locationLat;
		this.locationLon = locationLon;
		this.locationPhone = locationPhone;
		this.locationDesc = locationDesc;
		this.member = member;
	}


	
	public String getLocationImageUrl() {
		return locationImageUrl;
	}

	public void setLocationImageUrl(String locationImageUrl) {
		this.locationImageUrl = locationImageUrl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getTime() {
		return dateTime;
	}

	public void setTime(String time) {
		this.dateTime = time;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
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

	public String getLocationPhone() {
		return locationPhone;
	}

	public void setLocationPhone(String locationPhone) {
		this.locationPhone = locationPhone;
	}

	public String getLocationDesc() {
		return locationDesc;
	}

	public void setLocationDesc(String locationDesc) {
		this.locationDesc = locationDesc;
	}

	public ArrayList<String> getMemeber() {
		return member;
	}

	public void setMemeber(ArrayList<String> member) {
		this.member = member;
	}



	@Override
	public String toString() {
		String memberList = "[";
		for(int i = 0 ; i < member.size() ; i++){
			memberList += member.get(i);
			if(i < member.size() - 1){
				memberList += ", ";
			}
		}
		memberList += "]";
		return "id="+id+"/title="+title+"/owner="+owner+"/dateTime="+dateTime
				+"/locationName="+locationName+"/locationImageUrl="+locationImageUrl
				+"/locationLat="+locationLat+"/locationLon="+locationLon
				+"/locationPhone="+locationPhone+"/locationDesc="+locationDesc
				+"/memeber="+memberList;
	}
	
	

}
