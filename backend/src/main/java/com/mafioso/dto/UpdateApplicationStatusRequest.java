package com.mafioso.dto;

/**
 * Request payload for updating application workflow status.
 */
public class UpdateApplicationStatusRequest {

    private String status;
    private String note;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
