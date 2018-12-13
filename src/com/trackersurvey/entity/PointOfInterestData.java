package com.trackersurvey.entity;

public class PointOfInterestData {
	private int key;
	private String value;
	public PointOfInterestData(int key, String value) {
		super();
		this.key = key;
		this.value = value;
	}
	public PointOfInterestData() {
		super();
		// TODO Auto-generated constructor stub
	}
	public int getKey() {
		return key;
	}
	public void setKey(int key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
