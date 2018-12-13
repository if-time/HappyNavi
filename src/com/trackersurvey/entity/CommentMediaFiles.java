/**
 * 
 */
package com.trackersurvey.entity;

/**
 * 事件对应的文件表字段
 * @author Eaa
 * @version 2015年12月9日  上午9:46:17
 */
public class CommentMediaFiles {
	private int fileNo;    //文件编号
	private String fileName;   //文件路径名
	private String dateTime;  //对应评论的主键
	private int type;   //文件类型  图片1，视频2，音频3
	
	private String thumbnailName;//缩略图路径
	
	public static final int TYPE_PIC = 1;
	public static final int TYPE_VIDEO = 2;
	public static final int TYPE_AUDIO =3;
	
	public CommentMediaFiles(int fileNo,String picName,String dateTime,int fileType,String thumbnailName){
		this.fileNo = fileNo;
		this.fileName = picName;
		this.dateTime = dateTime;
		this.type = fileType;
		this.thumbnailName = thumbnailName;
	}
	
	public CommentMediaFiles(){}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String picName) {
		this.fileName = picName;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	public int getFileNo() {
		return fileNo;
	}
	public void setFileNo(int fileNo){
		this.fileNo = fileNo;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the thumbnailName
	 */
	public String getThumbnailName() {
		return thumbnailName;
	}

	/**
	 * @param thumbnailName the thumbnailName to set
	 */
	public void setThumbnailName(String thumbnailName) {
		this.thumbnailName = thumbnailName;
	}
}
