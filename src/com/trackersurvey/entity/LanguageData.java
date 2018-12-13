package com.trackersurvey.entity;

public class LanguageData {
	private String language;
	private String code;
	public LanguageData(String language, String code) {
		super();
		this.language = language;
		this.code = code;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
}
