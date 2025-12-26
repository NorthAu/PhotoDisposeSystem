package com.photodisposesystem.dto;

import java.time.LocalDateTime;

public class ImageUploadResponse {

    private Long imageId;
    private String filename;
    private String contentType;
    private Long size;
    private LocalDateTime uploadedAt;

    public ImageUploadResponse() {
    }

    public ImageUploadResponse(Long imageId, String filename, String contentType, Long size, LocalDateTime uploadedAt) {
        this.imageId = imageId;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
        this.uploadedAt = uploadedAt;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
