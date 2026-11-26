package com.mafioso.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mafioso.model.ConsultantAssignment;

/**
 * Audit trail item for consultant assignments on an application.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsultantAssignmentResponse {

    private Long id;
    private Long applicationId;
    private String applicationReference;
    private Long studentId;
    private Long consultantId;
    private String consultantName;
    private String consultantEmail;
    private Long assignedByUserId;
    private String assignedByName;
    private String action;
    private boolean active;
    private String note;
    private String assignedAt;
    private String unassignedAt;
    private String unassignedByName;

    public static ConsultantAssignmentResponse from(ConsultantAssignment item) {
        if (item == null) {
            return null;
        }
        ConsultantAssignmentResponse dto = new ConsultantAssignmentResponse();
        dto.setId(item.getId());
        dto.setApplicationId(item.getApplicationId());
        dto.setApplicationReference(item.getApplicationReference());
        dto.setStudentId(item.getStudentId());
        dto.setConsultantId(item.getConsultantId());
        dto.setConsultantName(item.getConsultantName());
        dto.setConsultantEmail(item.getConsultantEmail());
        dto.setAssignedByUserId(item.getAssignedByUserId());
        dto.setAssignedByName(item.getAssignedByName());
        dto.setAction(item.getAction() != null ? item.getAction().name() : null);
        dto.setActive(item.isActive());
        dto.setNote(item.getNote());
        dto.setAssignedAt(item.getAssignedAt() != null ? item.getAssignedAt().toString() : null);
        dto.setUnassignedAt(item.getUnassignedAt() != null ? item.getUnassignedAt().toString() : null);
        dto.setUnassignedByName(item.getUnassignedByName());
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

    public Long getConsultantId() {
        return consultantId;
    }

    public void setConsultantId(Long consultantId) {
        this.consultantId = consultantId;
    }

    public String getConsultantName() {
        return consultantName;
    }

    public void setConsultantName(String consultantName) {
        this.consultantName = consultantName;
    }

    public String getConsultantEmail() {
        return consultantEmail;
    }

    public void setConsultantEmail(String consultantEmail) {
        this.consultantEmail = consultantEmail;
    }

    public Long getAssignedByUserId() {
        return assignedByUserId;
    }

    public void setAssignedByUserId(Long assignedByUserId) {
        this.assignedByUserId = assignedByUserId;
    }

    public String getAssignedByName() {
        return assignedByName;
    }

    public void setAssignedByName(String assignedByName) {
        this.assignedByName = assignedByName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(String assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getUnassignedAt() {
        return unassignedAt;
    }

    public void setUnassignedAt(String unassignedAt) {
        this.unassignedAt = unassignedAt;
    }

    public String getUnassignedByName() {
        return unassignedByName;
    }

    public void setUnassignedByName(String unassignedByName) {
        this.unassignedByName = unassignedByName;
    }
}
