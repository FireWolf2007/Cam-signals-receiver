package ru.wolfa.telegram.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PhotoSendResult {
	@JsonProperty("message_id")
	private int messageId;
	private PhotoSendResultFrom from;
	private PhotoSendResultChat chat;
	private long date;
	private List<PhotoSendResultPhoto> photo;
    private String caption;

	public int getMessageId() {
		return messageId;
	}
	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}
	public PhotoSendResultFrom getFrom() {
		return from;
	}
	public void setFrom(PhotoSendResultFrom from) {
		this.from = from;
	}
	public PhotoSendResultChat getChat() {
		return chat;
	}
	public void setChat(PhotoSendResultChat chat) {
		this.chat = chat;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public List<PhotoSendResultPhoto> getPhoto() {
		return photo;
	}
	public void setPhoto(List<PhotoSendResultPhoto> photo) {
		this.photo = photo;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
}
