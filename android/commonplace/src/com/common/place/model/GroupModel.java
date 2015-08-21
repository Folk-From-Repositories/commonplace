package com.common.place.model;

import java.io.Serializable;
import java.util.ArrayList;

public class GroupModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String title;
	private String owner;
	private String time;
	private String locationName;
	private String locationImageUrl;
	private String locationLat;
	private String locationLon;
	private String locationPhone;
	private String locationDesc;

	private ArrayList<ContactsModel> memeber = new ArrayList<ContactsModel>();
	
	public GroupModel(){
		
	}
	
	public GroupModel(String id, String title, String owner, String time, String locationName, String locationImageUrl,String locationLat,
			String locationLon, String locationPhone, String locationDesc, ArrayList<ContactsModel> memeber) {
		super();
		this.id = id;
		this.title = title;
		this.owner = owner;
		this.time = time;
		this.locationName = locationName;
		this.locationImageUrl = locationImageUrl;
		this.locationLat = locationLat;
		this.locationLon = locationLon;
		this.locationPhone = locationPhone;
		this.locationDesc = locationDesc;
		this.memeber = memeber;
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
		return time;
	}

	public void setTime(String time) {
		this.time = time;
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

	public ArrayList<ContactsModel> getMemeber() {
		return memeber;
	}

	public void setMemeber(ArrayList<ContactsModel> memeber) {
		this.memeber = memeber;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

//	public class Member{
//		
//		private String phone;
//
//		public Member(String phone) {
//			super();
//			this.phone = phone;
//		}
//
//		public String getPhone() {
//			return phone;
//		}
//
//		public void setPhone(String phone) {
//			this.phone = phone;
//		}
//	}
}
