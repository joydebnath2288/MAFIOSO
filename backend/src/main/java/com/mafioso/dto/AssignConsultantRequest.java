package com.mafioso.dto;

/**
 * Request payload for {@code POST /api/v1/admin/applications/{id}/assign-consultant}.
 */
public class AssignConsultantRequest {

    private Long consultantId;
    private String note;

    public Long getConsultantId() {
        return consultantId;
    }

    public void setConsultantId(Long consultantId) {
        this.consultantId = consultantId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
