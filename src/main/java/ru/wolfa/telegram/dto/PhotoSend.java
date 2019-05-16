package ru.wolfa.telegram.dto;

public class PhotoSend {
	private boolean ok;
	private PhotoSendResult result;

	public boolean isOk() {
		return ok;
	}
	public void setOk(boolean ok) {
		this.ok = ok;
	}
	public PhotoSendResult getResult() {
		return result;
	}
	public void setResult(PhotoSendResult result) {
		this.result = result;
	}
}
