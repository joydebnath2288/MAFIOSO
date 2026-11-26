package com.mafioso.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Safe summary of an assigned student returned in consultant caseload listings.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignedStudentSummaryResponse {

    private Long studentId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String accountStatus;
    private long activeApplicationCount;
    private String latestApplicationReference;
    private String latestApplicationStatus;
    private String preferredDegreeLevel;
    private String preferredUniversity;
    private String assignedSince;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
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

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public long getActiveApplicationCount() {
        return activeApplicationCount;
    }

    public void setActiveApplicationCount(long activeApplicationCount) {
        this.activeApplicationCount = activeApplicationCount;
    }

    public String getLatestApplicationReference() {
        return latestApplicationReference;
    }

    public void setLatestApplicationReference(String latestApplicationReference) {
        this.latestApplicationReference = latestApplicationReference;
    }

    public String getLatestApplicationStatus() {
        return latestApplicationStatus;
    }

    public void setLatestApplicationStatus(String latestApplicationStatus) {
        this.latestApplicationStatus = latestApplicationStatus;
    }

    public String getPreferredDegreeLevel() {
        return preferredDegreeLevel;
    }

    public void setPreferredDegreeLevel(String preferredDegreeLevel) {
        this.preferredDegreeLevel = preferredDegreeLevel;
    }

    public String getPreferredUniversity() {
        return preferredUniversity;
    }

    public void setPreferredUniversity(String preferredUniversity) {
        this.preferredUniversity = preferredUniversity;
    }

    public String getAssignedSince() {
        return assignedSince;
    }

    public void setAssignedSince(String assignedSince) {
        this.assignedSince = assignedSince;
    }
}
