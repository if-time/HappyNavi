package com.trackersurvey.entity;

public class StepData {
	private String userID;
	private long TraceNo;
	private int Steps;
	public StepData(){
		this.Steps=0;
	}
	public StepData(String userId,long traceNo,int Steps){
		this.userID=userId;
		this.TraceNo=traceNo;
		this.Steps=Steps;
	}
	
	public void setuserId(String userId){
		this.userID=userId;
	}
	public void settraceNo(long traceNo){
		this.TraceNo=traceNo;
	}
	public void setsteps(int Steps){
		this.Steps=Steps;
	}
	public String getuserId(){
		return userID;
	}
	public long gettraceNo(){
		return TraceNo;
	}
	public int getsteps(){
		return Steps;
	}
}
