package com.trackersurvey.entity;

public class GroupInfo {
	private String GroupID;//Ⱥ��ID
	private String GroupName;//Ⱥ����
	private String GroupDetail;//Ⱥ������
	private String MemberNums;//Ⱥ��Ա��
	private String CreateTime;//Ⱥ����ʱ��
	private String CreateMan;//Ⱥ������
	private String PhotoUrl;//Ⱥͷ��url
	private String PhotoName;//Ⱥͷ�����֣����ڲ��һ���
	private String[] ManagerIDs;//����Ա
	private String[] UserIDs;//�û�
	
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
