package com.trackersurvey.entity;

public class TraceData {
	private String userID;
	private String TraceName;
	private long TraceNo;
	private String StartTime;
	private String EndTime;
	private long Duration;
	private double Distance;
	private int SportType;
	private int ShareType;
	private int Calorie;
	private int PoiCount;
	public TraceData(){
		
	}
	public TraceData(String userId,String traceName,long traceNo,String startTime,
			String endTime,long duration,Double distance,int sportType,int shareType,int Calorie){
		this.userID=userId;
		this.TraceName=traceName;
		this.TraceNo=traceNo;
		this.StartTime=startTime;
		this.EndTime=endTime;
		this.Duration=duration;
		this.Distance=distance;
		this.SportType=sportType;
		this.ShareType=shareType;
		this.Calorie=Calorie;
		this.PoiCount=PoiCount;
	}
	public void setUserID(String userId){
		this.userID=userId;
	}
	public void setTraceName(String traceName){
		this.TraceName=traceName;
	}
	public void setTraceNo(long traceNo){
		this.TraceNo=traceNo;
	}
	public void setStartTime(String startTime){
		this.StartTime=startTime;
	}
	public void setEndTime(String endTime){
		this.EndTime=endTime;
	}
	public void setDuration(long duration){
		this.Duration=duration;
	}
	public void setDistance(double distance){
		this.Distance=distance;
	}
	public void setSportType(int sportType){
		this.SportType=sportType;
	}
	public void setShareType(int shareType){
		this.ShareType=shareType;
	}
	public void setCalorie(int Calorie){
		this.Calorie=Calorie;
	}
	public void setPoiCount(int PoiCount){
		this.PoiCount=PoiCount;
	}
	public String getUserID(){
		return userID;
	}
	public String getTraceName(){
		return TraceName;
	}
	public long getTraceNo(){
		return TraceNo;
	}
	public String getStartTime(){
		return StartTime;
	}
	public String getEndTime(){
		return EndTime;
	}
	public long getDuration(){
		return Duration;
	}
	public double getDistance(){
		return Distance;
	}
	public int getSportType(){
		return SportType;
	}
	public int getShareType(){
		return ShareType;
	}
	public int getCalorie(){
		return Calorie;
	}
	public int getPoiCount(){
		return PoiCount;
	}
}
