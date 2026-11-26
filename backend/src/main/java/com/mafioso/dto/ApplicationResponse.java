package com.mafioso.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mafioso.model.Application;

/**
 * Safe application representation returned by student, consultant, and admin endpoints.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationResponse {

    private Long id;
    private String referenceNumber;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentPhoneNumber;
    private String degreeLevel;
    private String preferredSubject;
    private String intendedIntake;
    private String preferredUniversity;
    private String preferredLocation;
    private String personalStatement;
    private String status;
    private AssignedConsultantSummary assignedConsultant;
    private long documentCount;
    private String submittedAt;
    private String createdAt;
    private String updatedAt;

    public static ApplicationResponse from(Application app) {
        if (app == null) {
            return null;
        }
        ApplicationResponse dto = new ApplicationResponse();
        dto.setId(app.getId());
        dto.setReferenceNumber(app.getReferenceNumber());
        dto.setStudentId(app.getStudentId());
        dto.setStudentName(app.getStudentName());
        dto.setStudentEmail(app.getStudentEmail());
        dto.setStudentPhoneNumber(app.getStudentPhoneNumber());
        dto.setDegreeLevel(app.getDegreeLevel() != null ? app.getDegreeLevel().getDbValue() : null);
        dto.setPreferredSubject(app.getPreferredSubject());
        dto.setIntendedIntake(app.getIntendedIntake());
        dto.setPreferredUniversity(app.getPreferredUniversity());
        dto.setPreferredLocation(app.getPreferredLocation());
        dto.setPersonalStatement(app.getPersonalStatement());
        dto.setStatus(app.getStatus() != null ? app.getStatus().name() : null);
        dto.setDocumentCount(app.getDocumentCount());
        dto.setSubmittedAt(app.getSubmittedAt() != null ? app.getSubmittedAt().toString() : null);
        dto.setCreatedAt(app.getCreatedAt() != null ? app.getCreatedAt().toString() : null);
        dto.setUpdatedAt(app.getUpdatedAt() != null ? app.getUpdatedAt().toString() : null);

        if (app.getAssignedConsultantId() != null) {
            AssignedConsultantSummary consultant = new AssignedConsultantSummary();
            consultant.setId(app.getAssignedConsultantId());
            consultant.setFullName(app.getAssignedConsultantName());
            consultant.setEmail(app.getAssignedConsultantEmail());
            // Share phone contact only when consultant account is active
            if ("ACTIVE".equalsIgnoreCase(app.getAssignedConsultantStatus())) {
                consultant.setPhoneNumber(app.getAssignedConsultantPhone());
            }
            consultant.setStatus(app.getAssignedConsultantStatus());
            dto.setAssignedConsultant(consultant);
        }
        return dto;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AssignedConsultantSummary {
        private Long id;
        private String fullName;
        private String email;
        private String phoneNumber;
        private String status;

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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

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

    public String getDegreeLevel() {
        return degreeLevel;
    }

    public void setDegreeLevel(String degreeLevel) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AssignedConsultantSummary getAssignedConsultant() {
        return assignedConsultant;
    }

    public void setAssignedConsultant(AssignedConsultantSummary assignedConsultant) {
        this.assignedConsultant = assignedConsultant;
    }

    public long getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(long documentCount) {
        this.documentCount = documentCount;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
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
}
