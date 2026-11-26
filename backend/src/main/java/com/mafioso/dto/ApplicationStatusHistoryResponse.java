package com.mafioso.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mafioso.model.ApplicationStatusHistory;

/**
 * Audit trail item for application status transitions.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationStatusHistoryResponse {

    private Long id;
    private Long applicationId;
    private String previousStatus;
    private String newStatus;
    private Long changedByUserId;
    private String changedByName;
    private String changedByRole;
    private String note;
    private String changedAt;

    public static ApplicationStatusHistoryResponse from(ApplicationStatusHistory item) {
        if (item == null) {
            return null;
        }
        ApplicationStatusHistoryResponse dto = new ApplicationStatusHistoryResponse();
        dto.setId(item.getId());
        dto.setApplicationId(item.getApplicationId());
        dto.setPreviousStatus(item.getPreviousStatus() != null ? item.getPreviousStatus().name() : null);
        dto.setNewStatus(item.getNewStatus() != null ? item.getNewStatus().name() : null);
        dto.setChangedByUserId(item.getChangedByUserId());
        dto.setChangedByName(item.getChangedByName());
        dto.setChangedByRole(item.getChangedByRole());
        dto.setNote(item.getNote());
        dto.setChangedAt(item.getChangedAt() != null ? item.getChangedAt().toString() : null);
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

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public Long getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public String getChangedByRole() {
        return changedByRole;
    }

    public void setChangedByRole(String changedByRole) {
        this.changedByRole = changedByRole;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(String changedAt) {
        this.changedAt = changedAt;
    }
}
