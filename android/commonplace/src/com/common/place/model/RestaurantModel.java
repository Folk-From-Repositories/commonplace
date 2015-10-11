package com.common.place.model;

import java.io.Serializable;

public class RestaurantModel implements Serializable{
	 
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String photo_reference;
    private String name;
    private String description;
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
 
    public RestaurantModel(String title) {
        isGroupHeader = true;
    }
    
	public RestaurantModel(String photo_reference, String name, String description, String imageUrl, String phone, String locationLat,
			String locationLon, boolean isGroupHeader, String vicinity) {
		super();
		this.photo_reference = photo_reference;
		this.name = name;
		this.description = description;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
