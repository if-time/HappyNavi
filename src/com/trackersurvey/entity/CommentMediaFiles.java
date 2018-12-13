/**
 * 
 */
package com.trackersurvey.entity;

/**
 * �¼���Ӧ���ļ����ֶ�
 * @author Eaa
 * @version 2015��12��9��  ����9:46:17
 */
public class CommentMediaFiles {
	private int fileNo;    //�ļ����
	private String fileName;   //�ļ�·����
	private String dateTime;  //��Ӧ���۵�����
	private int type;   //�ļ�����  ͼƬ1����Ƶ2����Ƶ3
	
	private String thumbnailName;//����ͼ·��
	
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
