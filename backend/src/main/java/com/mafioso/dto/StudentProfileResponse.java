package com.mafioso.dto;

import com.mafioso.model.StudentProfile;
import com.mafioso.model.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Authorized student profile DTO combining account fields and extended consultancy dossier fields.
 */
public class StudentProfileResponse {

    private Long userId;
    private String fullName;
    private String email;
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

    private BigDecimal estimatedBudget;
    private String budgetCurrency;
    private String fundingSource;
    private String sponsorInfo;

    private int completionPercent;
    private List<String> missingFields;
    private String updatedAt;

    public static StudentProfileResponse from(User user, StudentProfile profile) {
        StudentProfileResponse dto = new StudentProfileResponse();
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setPhoneNumber(user.getPhoneNumber());
        }
        if (profile != null) {
            dto.setDateOfBirth(profile.getDateOfBirth() != null ? profile.getDateOfBirth().toString() : null);
            dto.setGender(profile.getGender());
            dto.setNationality(profile.getNationality());
            dto.setPresentAddress(profile.getPresentAddress());
            dto.setPermanentAddress(profile.getPermanentAddress());

            dto.setPassportNumber(profile.getPassportNumber());
            dto.setPassportIssueDate(profile.getPassportIssueDate() != null ? profile.getPassportIssueDate().toString() : null);
            dto.setPassportExpiryDate(profile.getPassportExpiryDate() != null ? profile.getPassportExpiryDate().toString() : null);
            dto.setPassportIssuingCountry(profile.getPassportIssuingCountry());

            dto.setHighestQualification(profile.getHighestQualification());
            dto.setSscResult(profile.getSscResult());
            dto.setHscResult(profile.getHscResult());
            dto.setBachelorInstitution(profile.getBachelorInstitution());
            dto.setBachelorSubject(profile.getBachelorSubject());
            dto.setBachelorCgpa(profile.getBachelorCgpa());
            dto.setBachelorGraduationYear(profile.getBachelorGraduationYear());

            dto.setIeltsScore(profile.getIeltsScore());
            dto.setPteScore(profile.getPteScore());
            dto.setToeflScore(profile.getToeflScore());
            dto.setOtherEnglishQualification(profile.getOtherEnglishQualification());

            dto.setPreferredDegreeLevel(profile.getPreferredDegreeLevel() != null ? profile.getPreferredDegreeLevel().getDbValue() : null);
            dto.setPreferredSubject(profile.getPreferredSubject());
            dto.setPreferredUniversity(profile.getPreferredUniversity());
            dto.setPreferredIntake(profile.getPreferredIntake());
            dto.setPreferredStudyLocation(profile.getPreferredStudyLocation());

            dto.setEstimatedBudget(profile.getEstimatedBudget());
            dto.setBudgetCurrency(profile.getBudgetCurrency() != null ? profile.getBudgetCurrency() : "GBP");
            dto.setFundingSource(profile.getFundingSource());
            dto.setSponsorInfo(profile.getSponsorInfo());
            dto.setUpdatedAt(profile.getUpdatedAt() != null ? profile.getUpdatedAt().toString() : null);
        } else {
            dto.setBudgetCurrency("GBP");
        }

        List<String> missing = computeMissingFields(dto);
        int totalRequiredItems = 11;
        int completedItems = Math.max(0, totalRequiredItems - missing.size());
        dto.setCompletionPercent((int) Math.round((completedItems * 100.0) / totalRequiredItems));
        dto.setMissingFields(Collections.unmodifiableList(missing));
        return dto;
    }

    private static List<String> computeMissingFields(StudentProfileResponse dto) {
        List<String> missing = new ArrayList<String>();
        if (isBlank(dto.getPhoneNumber())) missing.add("Phone number");
        if (isBlank(dto.getDateOfBirth())) missing.add("Date of birth");
        if (isBlank(dto.getNationality())) missing.add("Nationality");
        if (isBlank(dto.getPresentAddress())) missing.add("Present address");
        if (isBlank(dto.getPassportNumber())) missing.add("Passport number");
        if (isBlank(dto.getPassportExpiryDate())) missing.add("Passport expiry date");
        if (isBlank(dto.getHighestQualification())) missing.add("Highest academic qualification");
        if (isBlank(dto.getIeltsScore()) && isBlank(dto.getPteScore())
                && isBlank(dto.getToeflScore()) && isBlank(dto.getOtherEnglishQualification())) {
            missing.add("English language proficiency");
        }
        if (isBlank(dto.getPreferredDegreeLevel())) missing.add("Preferred degree level");
        if (isBlank(dto.getPreferredSubject())) missing.add("Preferred subject");
        if (dto.getEstimatedBudget() == null || isBlank(dto.getFundingSource())) {
            missing.add("Study budget and funding source");
        }
        return missing;
    }

    private static boolean isBlank(String val) {
        return val == null || val.trim().isEmpty();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public int getCompletionPercent() {
        return completionPercent;
    }

    public void setCompletionPercent(int completionPercent) {
        this.completionPercent = completionPercent;
    }

    public List<String> getMissingFields() {
        return missingFields;
    }

    public void setMissingFields(List<String> missingFields) {
        this.missingFields = missingFields;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
