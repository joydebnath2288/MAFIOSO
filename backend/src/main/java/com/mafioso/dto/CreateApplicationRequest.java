package com.mafioso.dto;

/**
 * Request payload for {@code POST /api/v1/student/applications}.
 */
public class CreateApplicationRequest {

    private String degreeLevel;
    private String preferredSubject;
    private String intendedIntake;
    private String preferredUniversity;
    private String preferredLocation;
    private String personalStatement;

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
}
