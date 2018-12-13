/**
 * 
 */
package com.trackersurvey.entity;

/**
 * 用户评论信息
 * 
 * @author Eaa
 * @version 2015年12月3日 上午10:49:56
 */
public class CommentData {
	private String CreateTime;    //创建时间
	private double Longitude;  //经度
	private double Latitude;  //纬度
	private double Altitude;  //海拔
	private String PlaceName; //地点名称
	private String Cmt;  //评论文字
	private long TraceNo;  //轨迹编号
	private int PicCount;  //图片数量
	private int VideoCount;  //视频
	private int SoundCount;  //音频
	private String UserId;  //用户ID
	private String CommentId;  //同步云端个人评论时用到的id


	public CommentData(String createTime, double longitude,
			double latitude, double altitude, String placeName, String content,
			long traceNo,int fileNum,int videoName,int audioName,String userId) {
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
	}
	public CommentData(){}
	
	
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
