package com.trackersurvey.entity;

public class GroupInfo {
	private String GroupID;//群组ID
	private String GroupName;//群组名
	private String GroupDetail;//群组描述
	private String MemberNums;//群成员数
	private String CreateTime;//群创建时间
	private String CreateMan;//群创建人
	private String PhotoUrl;//群头像url
	private String PhotoName;//群头像名字，便于查找缓存
	private String[] ManagerIDs;//管理员
	private String[] UserIDs;//用户
	
	public GroupInfo(){
		
	}
	public GroupInfo(String GroupID,String GroupName,String GroupDetail,String MemberNums,String CreateTime,
			String CreateMan,String PhotoUrl,String PhotoName,String[] ManagerIDs,String[] UserIDs){
		this.GroupID = GroupID;
		this.GroupName=GroupName;
		this.GroupDetail=GroupDetail;
		this.MemberNums=MemberNums;
		this.CreateTime=CreateTime;
		this.CreateMan=CreateMan;
		this.PhotoUrl=PhotoUrl;
		this.PhotoName=PhotoName;
		this.ManagerIDs=new String[ManagerIDs.length];
		for(int i=0;i<ManagerIDs.length;i++){
			this.ManagerIDs[i]=ManagerIDs[i];
		}
		
		this.UserIDs=new String[UserIDs.length];
		for(int i=0;i<UserIDs.length;i++){
			this.UserIDs[i]=UserIDs[i];
		}
	}
	public void setGroupID(String GroupID){
		this.GroupID=GroupID;
	}
	public void setGroupName(String GroupName){
		this.GroupName=GroupName;
	}
	public void setGroupDetail(String GroupDetail){
		this.GroupDetail=GroupDetail;
	}
	public void setMemberNums(String MemberNums){
		this.MemberNums=MemberNums;
	}
	public void setCreateTime(String CreateTime){
		this.CreateTime=CreateTime;
	}
	public void setCreateMan(String CreateMan){
		this.CreateMan=CreateMan;
	}
	public void setPhotoUrl(String PhotoUrl){
		this.PhotoUrl=PhotoUrl;
	}
	public void setPhotoName(String PhotoName){
		this.PhotoName=PhotoName;
	}
	public void setManagerIDs(String []ManagerIDs){
		this.ManagerIDs=new String[ManagerIDs.length];
		for(int i=0;i<ManagerIDs.length;i++){
			this.ManagerIDs[i]=ManagerIDs[i];
		}
	}
	public void setUserIDs(String []UserIDs){
		this.UserIDs=new String[UserIDs.length];
		for(int i=0;i<UserIDs.length;i++){
			this.UserIDs[i]=UserIDs[i];
		}
	}
	public String getGroupID(){
		return this.GroupID;
	}
	public String getGroupName(){
		return this.GroupName;
	}
	public String getGroupDetail(){
		return this.GroupDetail;
	}
	public String getMemberNums(){
		return this.MemberNums;
	}
	public String getCreateTime(){
		return this.CreateTime;
	}
	public String getCreateMan(){
		return this.CreateMan;
	}
	public String getPhotoUrl(){
		return this.PhotoUrl;
	}
	public String getPhotoName(){
		return this.PhotoName;
	}
	public String[] getManagerIDs(){
		return this.ManagerIDs;
	}
	public String[] getUserIDs(){
		return this.UserIDs;
	}
	
}
