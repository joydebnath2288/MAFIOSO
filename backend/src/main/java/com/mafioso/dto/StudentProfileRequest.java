package com.mafioso.dto;

/**
 * Request payload for {@code PUT /api/v1/student/profile}.
 */
public class StudentProfileRequest {

    private String fullName;
    private String phoneNumber;
    private String dateOfBirth;
    private String gender;
    private String nationality;
    private String presentAddress;
    private String permanentAddress;

    private String passportNumber;
    private String passportIssueDate;
    private String passportExpiryDate;
    private String passportIssuingCountry;

    private String highestQualification;
    private String sscResult;
    private String hscResult;
    private String bachelorInstitution;
    private String bachelorSubject;
    private String bachelorCgpa;
    private Integer bachelorGraduationYear;

    private String ieltsScore;
    private String pteScore;
    private String toeflScore;
    private String otherEnglishQualification;

    private String preferredDegreeLevel;
    private String preferredSubject;
    private String preferredUniversity;
    private String preferredIntake;
    private String preferredStudyLocation;

    private String estimatedBudget;
    private String budgetCurrency;
    private String fundingSource;
    private String sponsorInfo;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
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

    public String getPassportIssueDate() {
        return passportIssueDate;
    }

    public void setPassportIssueDate(String passportIssueDate) {
        this.passportIssueDate = passportIssueDate;
    }

    public String getPassportExpiryDate() {
        return passportExpiryDate;
    }

    public void setPassportExpiryDate(String passportExpiryDate) {
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

    public String getPreferredDegreeLevel() {
        return preferredDegreeLevel;
    }

    public void setPreferredDegreeLevel(String preferredDegreeLevel) {
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

    public String getEstimatedBudget() {
        return estimatedBudget;
    }

    public void setEstimatedBudget(String estimatedBudget) {
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
}
