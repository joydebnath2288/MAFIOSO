package com.mafioso.model;

import java.time.Instant;

/**
 * Domain entity representing {@code consultant_assignments}.
 */
public class ConsultantAssignment {

    private Long id;
    private Long applicationId;
    private String applicationReference;
    private Long studentId;
    private Long consultantId;
    private String consultantName;
    private String consultantEmail;
    private Long assignedByUserId;
    private String assignedByName;
    private AssignmentAction action;
    private boolean active;
    private String note;
    private Instant assignedAt;
    private Instant unassignedAt;
    private Long unassignedByUserId;
    private String unassignedByName;

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

    public AssignmentAction getAction() {
        return action;
    }

    public void setAction(AssignmentAction action) {
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

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Instant getUnassignedAt() {
        return unassignedAt;
    }

    public void setUnassignedAt(Instant unassignedAt) {
        this.unassignedAt = unassignedAt;
    }

    public Long getUnassignedByUserId() {
        return unassignedByUserId;
    }

    public void setUnassignedByUserId(Long unassignedByUserId) {
        this.unassignedByUserId = unassignedByUserId;
    }

    public String getUnassignedByName() {
        return unassignedByName;
    }

    public void setUnassignedByName(String unassignedByName) {
        this.unassignedByName = unassignedByName;
    }
}
