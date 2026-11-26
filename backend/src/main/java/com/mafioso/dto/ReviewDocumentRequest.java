package com.mafioso.dto;

/**
 * Request payload for reviewing an uploaded document.
 */
public class ReviewDocumentRequest {

    private String reviewStatus;
    private String reviewNote;

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
}
