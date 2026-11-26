package com.mafioso.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mafioso.model.ApplicationDocument;

/**
 * Safe metadata representation for uploaded documents.
 * Never exposes internal server file paths or random stored filenames.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationDocumentResponse {

    private Long id;
    private Long applicationId;
    private String applicationReference;
    private Long studentId;
    private String category;
    private String originalFilename;
    private String detectedContentType;
    private long fileSizeBytes;
    private String reviewStatus;
    private String reviewNote;
    private String uploadedByName;
    private String reviewedByName;
    private String reviewedAt;
    private String createdAt;
    private String updatedAt;
    private String downloadUrl;

    public static ApplicationDocumentResponse from(ApplicationDocument doc) {
        if (doc == null) {
            return null;
        }
        ApplicationDocumentResponse dto = new ApplicationDocumentResponse();
        dto.setId(doc.getId());
        dto.setApplicationId(doc.getApplicationId());
        dto.setApplicationReference(doc.getApplicationReference());
        dto.setStudentId(doc.getStudentId());
        dto.setCategory(doc.getCategory() != null ? doc.getCategory().name() : null);
        dto.setOriginalFilename(doc.getOriginalFilename());
        dto.setDetectedContentType(doc.getDetectedContentType());
        dto.setFileSizeBytes(doc.getFileSizeBytes());
        dto.setReviewStatus(doc.getReviewStatus() != null ? doc.getReviewStatus().name() : null);
        dto.setReviewNote(doc.getReviewNote());
        dto.setUploadedByName(doc.getUploadedByName());
        dto.setReviewedByName(doc.getReviewedByName());
        dto.setReviewedAt(doc.getReviewedAt() != null ? doc.getReviewedAt().toString() : null);
        dto.setCreatedAt(doc.getCreatedAt() != null ? doc.getCreatedAt().toString() : null);
        dto.setUpdatedAt(doc.getUpdatedAt() != null ? doc.getUpdatedAt().toString() : null);
        dto.setDownloadUrl("/api/v1/documents/" + doc.getId() + "/download");
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationReference() {
        return applicationReference;
    }

    public void setApplicationReference(String applicationReference) {
        this.applicationReference = applicationReference;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getDetectedContentType() {
        return detectedContentType;
    }

    public void setDetectedContentType(String detectedContentType) {
        this.detectedContentType = detectedContentType;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public String getUploadedByName() {
        return uploadedByName;
    }

    public void setUploadedByName(String uploadedByName) {
        this.uploadedByName = uploadedByName;
    }

    public String getReviewedByName() {
        return reviewedByName;
    }

    public void setReviewedByName(String reviewedByName) {
        this.reviewedByName = reviewedByName;
    }

    public String getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(String reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
