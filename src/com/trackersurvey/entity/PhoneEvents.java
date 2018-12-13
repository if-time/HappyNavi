package com.trackersurvey.entity;

public class PhoneEvents {
	private String userID;
	private String CreateTime;
	private int EventType;
	private double Longitude;
	private double Latitude;
	private double Altitude;
	public PhoneEvents(){
		
	}
	public PhoneEvents(String userId,String createTime,int EventType,double Longitude,
			double Latitude,double Altitude){
		this.userID=userId;
		this.CreateTime=createTime;
		this.EventType=EventType;
		this.Longitude=Longitude;
		this.Latitude=Latitude;
		this.Altitude=Altitude;
	}
	public void setuserId(String userId){
		this.userID=userId;
	}
	public void setcreateTime(String createTime){
		this.CreateTime=createTime;
	}
	public void setEventType(int EventType){
		this.EventType=EventType;
	}
	public void setLongitude(double Longitude){
		this.Longitude=Longitude;
	}
	public void setLatitude(double Latitude){
		this.Latitude=Latitude;
	}
	public void setAltitude(double Altitude){
		this.Altitude=Altitude;
	}
	public String getuserId(){
		return userID;
	}
	public String getcreateTime(){
		return CreateTime;
	}
	public int getEventType(){
		return EventType;
	}
	public double getLongitude(){
		return Longitude;
	}
	public double getLatitude(){
		return Latitude;
	}
	public double getAltitude(){
		return Altitude;
	}
}
