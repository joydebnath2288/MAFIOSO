package com.mafioso.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Domain entity representing {@code student_profiles}.
 */
public class StudentProfile {

    private Long id;
    private Long userId;

    // Personal information (fullName and phoneNumber come from users table)
    private LocalDate dateOfBirth;
    private String gender;
    private String nationality;
    private String presentAddress;
    private String permanentAddress;

    // Passport information (sensitive PII)
    private String passportNumber;
    private LocalDate passportIssueDate;
    private LocalDate passportExpiryDate;
    private String passportIssuingCountry;

    // Academic information
    private String highestQualification;
    private String sscResult;
    private String hscResult;
    private String bachelorInstitution;
    private String bachelorSubject;
    private String bachelorCgpa;
    private Integer bachelorGraduationYear;

    // English proficiency
    private String ieltsScore;
    private String pteScore;
    private String toeflScore;
    private String otherEnglishQualification;

    // Study preferences
    private DegreeLevel preferredDegreeLevel;
    private String preferredSubject;
    private String preferredUniversity;
    private String preferredIntake;
    private String preferredStudyLocation;

    // Financial information
    private BigDecimal estimatedBudget;
    private String budgetCurrency;
    private String fundingSource;
    private String sponsorInfo;

    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getPresentAddress() {
        return presentAddress;
    }

    public void setPresentAddress(String presentAddress) {
        this.presentAddress = presentAddress;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public LocalDate getPassportIssueDate() {
        return passportIssueDate;
    }

    public void setPassportIssueDate(LocalDate passportIssueDate) {
        this.passportIssueDate = passportIssueDate;
    }

    public LocalDate getPassportExpiryDate() {
        return passportExpiryDate;
    }

    public void setPassportExpiryDate(LocalDate passportExpiryDate) {
        this.passportExpiryDate = passportExpiryDate;
    }

    public String getPassportIssuingCountry() {
        return passportIssuingCountry;
    }

    public void setPassportIssuingCountry(String passportIssuingCountry) {
        this.passportIssuingCountry = passportIssuingCountry;
    }

    public String getHighestQualification() {
        return highestQualification;
    }

    public void setHighestQualification(String highestQualification) {
        this.highestQualification = highestQualification;
    }

    public String getSscResult() {
        return sscResult;
    }

    public void setSscResult(String sscResult) {
        this.sscResult = sscResult;
    }

    public String getHscResult() {
        return hscResult;
    }

    public void setHscResult(String hscResult) {
        this.hscResult = hscResult;
    }

    public String getBachelorInstitution() {
        return bachelorInstitution;
    }

    public void setBachelorInstitution(String bachelorInstitution) {
        this.bachelorInstitution = bachelorInstitution;
    }

    public String getBachelorSubject() {
        return bachelorSubject;
    }

    public void setBachelorSubject(String bachelorSubject) {
        this.bachelorSubject = bachelorSubject;
    }

    public String getBachelorCgpa() {
        return bachelorCgpa;
    }

    public void setBachelorCgpa(String bachelorCgpa) {
        this.bachelorCgpa = bachelorCgpa;
    }

    public Integer getBachelorGraduationYear() {
        return bachelorGraduationYear;
    }

    public void setBachelorGraduationYear(Integer bachelorGraduationYear) {
        this.bachelorGraduationYear = bachelorGraduationYear;
    }

    public String getIeltsScore() {
        return ieltsScore;
    }

    public void setIeltsScore(String ieltsScore) {
        this.ieltsScore = ieltsScore;
    }

    public String getPteScore() {
        return pteScore;
    }

    public void setPteScore(String pteScore) {
        this.pteScore = pteScore;
    }

    public String getToeflScore() {
        return toeflScore;
    }

    public void setToeflScore(String toeflScore) {
        this.toeflScore = toeflScore;
    }

    public String getOtherEnglishQualification() {
        return otherEnglishQualification;
    }

    public void setOtherEnglishQualification(String otherEnglishQualification) {
        this.otherEnglishQualification = otherEnglishQualification;
    }

    public DegreeLevel getPreferredDegreeLevel() {
        return preferredDegreeLevel;
    }

    public void setPreferredDegreeLevel(DegreeLevel preferredDegreeLevel) {
        this.preferredDegreeLevel = preferredDegreeLevel;
    }

    public String getPreferredSubject() {
        return preferredSubject;
    }

    public void setPreferredSubject(String preferredSubject) {
        this.preferredSubject = preferredSubject;
    }

    public String getPreferredUniversity() {
        return preferredUniversity;
    }

    public void setPreferredUniversity(String preferredUniversity) {
        this.preferredUniversity = preferredUniversity;
    }

    public String getPreferredIntake() {
        return preferredIntake;
    }

    public void setPreferredIntake(String preferredIntake) {
        this.preferredIntake = preferredIntake;
    }

    public String getPreferredStudyLocation() {
        return preferredStudyLocation;
    }

    public void setPreferredStudyLocation(String preferredStudyLocation) {
        this.preferredStudyLocation = preferredStudyLocation;
    }

    public BigDecimal getEstimatedBudget() {
        return estimatedBudget;
    }

    public void setEstimatedBudget(BigDecimal estimatedBudget) {
        this.estimatedBudget = estimatedBudget;
    }

    public String getBudgetCurrency() {
        return budgetCurrency;
    }

    public void setBudgetCurrency(String budgetCurrency) {
        this.budgetCurrency = budgetCurrency;
    }

    public String getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
    }

    public String getSponsorInfo() {
        return sponsorInfo;
    }

    public void setSponsorInfo(String sponsorInfo) {
        this.sponsorInfo = sponsorInfo;
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

    @Override
    public String toString() {
        return "StudentProfile{id=" + id + ", userId=" + userId + '}';
    }
}
