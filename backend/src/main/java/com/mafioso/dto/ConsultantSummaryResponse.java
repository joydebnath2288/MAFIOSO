package com.mafioso.dto;

import com.mafioso.model.User;

/**
 * Safe consultant summary/detail representation.
 * Explicitly sets {@code assignedStudentCount} to {@code null} and {@code assignmentTrackingAvailable}
 * to {@code false} until consultant-student assignment tables are implemented.
 */
public class ConsultantSummaryResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String lastLoginAt;
    private Long assignedStudentCount;
    private boolean assignmentTrackingAvailable;

    public ConsultantSummaryResponse() {
    }

    public static ConsultantSummaryResponse fromUser(User user) {
        if (user == null) {
            return null;
        }
        ConsultantSummaryResponse dto = new ConsultantSummaryResponse();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        dto.setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
        dto.setLastLoginAt(user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null);
        dto.setAssignedStudentCount(null);
        dto.setAssignmentTrackingAvailable(false);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(String lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Long getAssignedStudentCount() {
        return assignedStudentCount;
    }

    public void setAssignedStudentCount(Long assignedStudentCount) {
        this.assignedStudentCount = assignedStudentCount;
    }

    public boolean isAssignmentTrackingAvailable() {
        return assignmentTrackingAvailable;
    }

    public void setAssignmentTrackingAvailable(boolean assignmentTrackingAvailable) {
        this.assignmentTrackingAvailable = assignmentTrackingAvailable;
    }
}
