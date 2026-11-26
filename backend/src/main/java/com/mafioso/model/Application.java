package com.mafioso.model;

import java.time.Instant;

/**
 * Domain entity representing a student study application in {@code applications}.
 */
public class Application {

    private Long id;
    private String referenceNumber;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentPhoneNumber;
    private DegreeLevel degreeLevel;
    private String preferredSubject;
    private String intendedIntake;
    private String preferredUniversity;
    private String preferredLocation;
    private String personalStatement;
    private ApplicationStatus status;
    private Long assignedConsultantId;
    private String assignedConsultantName;
    private String assignedConsultantEmail;
    private String assignedConsultantPhone;
    private String assignedConsultantStatus;
    private Instant submittedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private long documentCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentPhoneNumber() {
        return studentPhoneNumber;
    }

    public void setStudentPhoneNumber(String studentPhoneNumber) {
        this.studentPhoneNumber = studentPhoneNumber;
    }

    public DegreeLevel getDegreeLevel() {
        return degreeLevel;
    }

    public void setDegreeLevel(DegreeLevel degreeLevel) {
        this.degreeLevel = degreeLevel;
    }

    public String getPreferredSubject() {
        return preferredSubject;
    }

    public void setPreferredSubject(String preferredSubject) {
        this.preferredSubject = preferredSubject;
    }

    public String getIntendedIntake() {
        return intendedIntake;
    }

    public void setIntendedIntake(String intendedIntake) {
        this.intendedIntake = intendedIntake;
    }

    public String getPreferredUniversity() {
        return preferredUniversity;
    }

    public void setPreferredUniversity(String preferredUniversity) {
        this.preferredUniversity = preferredUniversity;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public String getPersonalStatement() {
        return personalStatement;
    }

    public void setPersonalStatement(String personalStatement) {
        this.personalStatement = personalStatement;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Long getAssignedConsultantId() {
        return assignedConsultantId;
    }

    public void setAssignedConsultantId(Long assignedConsultantId) {
        this.assignedConsultantId = assignedConsultantId;
    }

    public String getAssignedConsultantName() {
        return assignedConsultantName;
    }

    public void setAssignedConsultantName(String assignedConsultantName) {
        this.assignedConsultantName = assignedConsultantName;
    }

    public String getAssignedConsultantEmail() {
        return assignedConsultantEmail;
    }

    public void setAssignedConsultantEmail(String assignedConsultantEmail) {
        this.assignedConsultantEmail = assignedConsultantEmail;
    }

    public String getAssignedConsultantPhone() {
        return assignedConsultantPhone;
    }

    public void setAssignedConsultantPhone(String assignedConsultantPhone) {
        this.assignedConsultantPhone = assignedConsultantPhone;
    }

    public String getAssignedConsultantStatus() {
        return assignedConsultantStatus;
    }

    public void setAssignedConsultantStatus(String assignedConsultantStatus) {
        this.assignedConsultantStatus = assignedConsultantStatus;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(long documentCount) {
        this.documentCount = documentCount;
    }
}
