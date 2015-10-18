package com.common.place.model;

import java.io.Serializable;

public class Restaurant implements Serializable{
	 
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String photo_reference;
    private String name;
    private String rating;
    private String imageUrl;
    private String phone;
    private String locationLat;
    private String locationLon;
    private String vicinity;
 
    public String getVicinity() {
		return vicinity;
	}

	public void setVicinity(String vicinity) {
		this.vicinity = vicinity;
	}
	private boolean isGroupHeader = false;
 
    public Restaurant(String title) {
        isGroupHeader = true;
    }
    
	public Restaurant(String photo_reference, String name, String rating, String imageUrl, String phone, String locationLat,
			String locationLon, boolean isGroupHeader, String vicinity) {
		super();
		this.photo_reference = photo_reference;
		this.name = name;
		this.rating = rating;
		this.imageUrl = imageUrl;
		this.phone = phone;
		this.locationLat = locationLat;
		this.locationLon = locationLon;
		this.isGroupHeader = isGroupHeader;
		this.vicinity = vicinity;
	}

	public String getPhotoReference() {
		return photo_reference;
	}

	public void setPhotoReference(String photo_reference) {
		this.photo_reference = photo_reference;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
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

	public boolean isGroupHeader() {
		return isGroupHeader;
	}
	public void setGroupHeader(boolean isGroupHeader) {
		this.isGroupHeader = isGroupHeader;
	}
}
