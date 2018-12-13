/**
 * 
 */
package com.trackersurvey.entity;


/**
 * 
 * @author Eaa
 * @version 2015年12月11日  下午4:19:49
 */
public class ListItemData {
	private String time;  //时间
	private String place;   //地点
	private InterestMarkerData event;
	private CommentMediaFiles  files[];
	private String comment;  //评论
	private int feeling;     //心情状态
	private int behaviour;   //活动类型
 	private int duration;    //停留时长
 	private int companion;   //同伴人数
 	private int relation;    //同伴关系
	public ListItemData(InterestMarkerData event,CommentMediaFiles files[]){
		this.event = event;
		this.files = files;
		this.time = event.getCreatetime();
		this.place = event.getPlaceName();
		this.comment = event.getContent();
		this.feeling = event.getFeeling();
		this.behaviour = event.getBehaviour();
		this.duration = event.getDuration();
		this.companion = event.getCompanion();
		this.relation = event.getRelationship();
	}
	
	public String getTime() {
		return time;
	}

	public String getPlace() {
		return place;
	}
	
	public void setFiles(CommentMediaFiles[] files) {
		this.files = files;
	}
	public CommentMediaFiles[] getFiles() {
		return files;
	}

	public InterestMarkerData getEvent() {
		return event;
	}

	public String getComment() {
		return comment;
	}
	public int getFeeling(){
		return feeling;
	}
	public int getBehaviour(){
		return behaviour;
	}
	public int getDuration(){
		return duration;
	}
	public int getCompanion(){
		return companion;
	}
	public int getRelation(){
		return relation;
	}
	public void setOneFile(int i,CommentMediaFiles file){
		files[i] = file;
	}

	
}
