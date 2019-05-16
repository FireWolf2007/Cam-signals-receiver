package ru.wolfa.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PhotoSendResultFrom {
    private long id;
    @JsonProperty("is_bot")
    private boolean isBot;
    @JsonProperty("first_name")
    private String firstName;
    private String username;

    public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public boolean isBot() {
		return isBot;
	}
	public void setBot(boolean isBot) {
		this.isBot = isBot;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
}
