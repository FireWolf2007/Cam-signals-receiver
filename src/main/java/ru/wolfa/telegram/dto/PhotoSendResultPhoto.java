package ru.wolfa.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PhotoSendResultPhoto {
    @JsonProperty("file_id")
    private String fileId;
    @JsonProperty("file_size")
    private int fileSize;
    private int width;
    private int height;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
