package com.common.place.model;

public class Contact {

	private String phoneNumber;
	private String name;
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Contact(String phoneNumber, String name) {
		super();
		this.phoneNumber = phoneNumber;
		this.name = name;
	}
	
	
}
