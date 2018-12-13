package com.trackersurvey.entity;

public class GpsData {
	private String userID;
	private String CreateTime;
	private double Longitude;
	private double Latitude;
	private double Altitude;
	private double Speed;
	private long TraceNo;
	public GpsData(String userId,String createTime,double Longitude,
			double Latitude,double Altitude,double speed,long traceNo){
		this.userID=userId;
		this.CreateTime=createTime;
		this.Longitude=Longitude;
		this.Latitude=Latitude;
		this.Altitude=Altitude;
		this.Speed=speed;
		this.TraceNo=traceNo;
	}
	public GpsData(){}
	public void setuserId(String userId){
		this.userID=userId;
	}
	public void setcreateTime(String createTime){
		this.CreateTime=createTime;
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
	public void setspeed(double speed){
		this.Speed=speed;
	}
	public void settraceNo(long traceNo){
		this.TraceNo=traceNo;
	}
	public String getuserId(){
		return userID;
	}
	public String getcreateTime(){
		return CreateTime;
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
	public double getspeed(){
		return Speed;
	}
	public long gettraceNo(){
		return TraceNo;
	}
}
