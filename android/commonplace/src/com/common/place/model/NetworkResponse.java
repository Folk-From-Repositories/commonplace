package com.common.place.model;

public class NetworkResponse {

	private int responseCode;
	private String reponseString;
	public NetworkResponse(int responseCode, String reponseString) {
		super();
		this.responseCode = responseCode;
		this.reponseString = reponseString;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public String getReponseString() {
		return reponseString;
	}
	public void setReponseString(String reponseString) {
		this.reponseString = reponseString;
	}
	@Override
	public String toString() {
		return "["+responseCode+"]"+reponseString;
	}
	
	
}
