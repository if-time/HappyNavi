package com.trackersurvey.entity;

public class TimeValue {
	private int UploadTime;
	private int RecTime;
	private int NoRecTime;
	private String LastTime;
	
	public TimeValue(String UploadTime,String RecTime,String NoRecTime,String LastTime){
		try{
			this.UploadTime=Integer.parseInt(UploadTime);
			this.RecTime=Integer.parseInt(RecTime);
			this.NoRecTime=Integer.parseInt(NoRecTime);
		}catch(NumberFormatException e){
			
		}
		this.LastTime=LastTime;
	}
	
	public TimeValue(){}
	
	public void setUploadTime(String UploadTime){
		this.UploadTime=Integer.parseInt(UploadTime);
	}
	public void setRecTime(String RecTime){
		this.RecTime=Integer.parseInt(RecTime);
	}
	public void setNoRecTime(String NoRecTime){
		this.NoRecTime=Integer.parseInt(NoRecTime);
	}
	public void setLastTime(String LastTime){
		this.LastTime=LastTime;
	}
	
	public int  getUploadTime(){
		return UploadTime;
	}
	public int  getRecTime(){
		return RecTime;
	}
	public int  getNoRecTime(){
		return NoRecTime;
	}
	public String  getLastTime(){
		return LastTime;
	}
	
}
