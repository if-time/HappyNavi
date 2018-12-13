/**
 * 
 */
package com.trackersurvey.entity;

/**
 * �û���Ȥ����Ϣ
 * 
 * @author Eaa
 * @version 2015��12��3�� ����10:49:56
 */
public class InterestMarkerData {
	private String CreateTime;    //����ʱ��
	private double Longitude;  //����
	private double Latitude;  //γ��
	private double Altitude;  //����
	private String PlaceName; //�ص�����
	private String Cmt;  //��������
	private long TraceNo;  //�켣���
	private int PicCount;  //ͼƬ����
	private int VideoCount;  //��Ƶ
	private int SoundCount;  //��Ƶ
	private int MotionType;	//����
	private int ActivityType;	//�����
	private int RetentionTime;	//ʱ��
	private int CompanionCount;	//ͬ������
	private int Relationship;//��ϵ
	private String UserId;  //�û�ID
	private String CommentId;  //ͬ���ƶ˸�������ʱ�õ���id
	

	public InterestMarkerData(String createTime, double longitude,
			double latitude, double altitude, String placeName, String content,
			long traceNo,int fileNum,int videoName,int audioName,String userId,
			int feeling,int behaviour,int duration,int companion,int relationship) {
		this.setCreatetime(createTime);
		this.Longitude = longitude;
		this.Latitude = latitude;
		this.Altitude = altitude;
		this.PlaceName = placeName;
		this.Cmt = content;
		this.TraceNo = traceNo;
		this.PicCount = fileNum;
		this.VideoCount = videoName;
		this.SoundCount = audioName;
		this.UserId = userId;
		this.MotionType = feeling;
		this.ActivityType = behaviour;
		this.RetentionTime = duration;
		this.CompanionCount = companion;
		this.Relationship = relationship;
	}
	public InterestMarkerData(){}
	
	
	public String getCreatetime() {
		return CreateTime;
	}
	public void setCreatetime(String createtime) {
		this.CreateTime = createtime;
	}
	public double getLongitude() {
		return Longitude;
	}
	public void setLongitude(double longitude) {
		this.Longitude = longitude;
	}
	public double getLatitude() {
		return Latitude;
	}
	public void setLatitude(double latitude) {
		this.Latitude = latitude;
	}
	public double getAltitude() {
		return Altitude;
	}
	public void setAltitude(double altitude) {
		this.Altitude = altitude;
	}
	public String getPlaceName() {
		return PlaceName;
	}
	public void setPlaceName(String placeName) {
		this.PlaceName = placeName;
	}
	public String getContent() {
		return Cmt;
	}
	public void setContent(String content) {
		this.Cmt = content;
	}
	public long getTraceNo() {
		return TraceNo;
	}
	public void setTraceNo(long traceNo) {
		this.TraceNo = traceNo;
	}
	public int getFileNum() {
		return PicCount;
	}
	public void setFileNum(int fileNum) {
		this.PicCount = fileNum;
	}
	public int getVideoCount() {
		return VideoCount;
	}
	public void setVideoCount(int i) {
		this.VideoCount = i;
	}
	public int getAudioCount() {
		return SoundCount;
	}
	public void setAudioCount(int audioName) {
		this.SoundCount = audioName;
	}
	public int getFeeling() {
		return MotionType;
	}
	public void setFeeling(int feeling) {
		this.MotionType = feeling;
	}
	public int getBehaviour() {
		return ActivityType;
	}
	public void setBehaviour(int behaviour) {
		this.ActivityType = behaviour;
	}
	public int getDuration() {
		return RetentionTime;
	}
	public void setDuration(int Duration) {
		this.RetentionTime = Duration;
	}
	public int getCompanion() {
		return CompanionCount;
	}
	public void setCompanion(int Companion) {
		this.CompanionCount = Companion;
	}
	public int getRelationship() {
		return Relationship;
	}
	public void setRelationship(int Relationship) {
		this.Relationship = Relationship;
	}
	/**
	 * @return the userId
	 */
	public String getUserId() {
		return UserId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.UserId = userId;
	}
	public String getCommentId() {
		return CommentId;
	}
	public void setCommentId(String commentId) {
		this.CommentId = commentId;
	}
}
