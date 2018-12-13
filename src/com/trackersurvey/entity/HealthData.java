package com.trackersurvey.entity;

public class HealthData {
	private String UserID;
	private String YearMonth;//�·� ��ʽ��201604
	private String[] Day;//һ������ÿ��Ĳ���ͳ��
	private String Total;//�����ܲ���
	public HealthData(){
		//Day=new int[31];
	}
	public HealthData(String UserID,String YearMonth,String[] Day,String total){
		this.UserID=UserID;
		this.YearMonth=YearMonth;
		this.Day=new String[31];
		for(int i=0;i<Day.length;i++){
			this.Day[i]=Day[i];
		}
		this.Total=total;
	}
	public void setUserID(String UserID){
		this.UserID=UserID;
	}
	public void setYearMonth(String YearMonth){
		this.YearMonth=YearMonth;
	}
	public void setDayarry(String []Day){
		for(int i=0;i<Day.length;i++){
			this.Day[i]=Day[i];
		}
	}
	public void setTotal(String total){
		this.Total=total;
	}
	public String getUserID(){
		return this.UserID;
	}
	public String getYearMonth(){
		return this.YearMonth;
	}
	public String[] getDayarry(){
		return this.Day;
	}
	public String getTotal(){
		return this.Total;
	}
}
