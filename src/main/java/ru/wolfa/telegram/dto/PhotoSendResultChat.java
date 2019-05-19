package ru.wolfa.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PhotoSendResultChat {
    private long id;
    @JsonProperty("first_name")
    private String firstName;
    private String title;
    private String username;
    private String type;
    @JsonProperty("all_members_are_administrators")
    private Boolean allMembersAreAdministrators;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
