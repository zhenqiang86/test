package com.s;

public class EntityBean {

	private int id;
	
	private String viewUrl;
	
	private String fileTempName;
	
	private String filePath;
	
	private String fileId;
	
	private String status;
	
	private String msg;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getViewUrl() {
		return viewUrl;
	}
	
	public void setViewUrl(String viewUrl) {
		this.viewUrl = viewUrl;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getFileTempName() {
		return fileTempName;
	}

	public void setFileTempName(String fileTempName) {
		this.fileTempName = fileTempName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String toString(){
		return " [id="+this.id+"; viewUrl="+this.viewUrl+"; fileId="+fileId+"; status="+this.status+"]";
	}
	
}
