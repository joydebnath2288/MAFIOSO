package com.mafioso.service;

import com.mafioso.dao.StudentProfileDAO;
import com.mafioso.dao.StudentProfileDAOImpl;
import com.mafioso.dao.UserDAO;
import com.mafioso.dao.UserDAOImpl;
import com.mafioso.dto.StudentProfileRequest;
import com.mafioso.dto.StudentProfileResponse;
import com.mafioso.model.DegreeLevel;
import com.mafioso.model.Role;
import com.mafioso.model.StudentProfile;
import com.mafioso.model.User;
import com.mafioso.util.ConsultancyValidationUtil;
import com.mafioso.util.DatabaseUtil;
import com.mafioso.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Business logic for student profile retrieval, validation, and transactional updates.
 */
public class StudentProfileServiceImpl implements StudentProfileService {

    private final UserDAO userDao;
    private final StudentProfileDAO profileDao;
    private final AuthServiceImpl.ConnectionSupplier connectionSupplier;

    public StudentProfileServiceImpl() {
        this(new UserDAOImpl(), new StudentProfileDAOImpl(), DatabaseUtil::getConnection);
    }

    public StudentProfileServiceImpl(UserDAO userDao,
                                     StudentProfileDAO profileDao,
                                     AuthServiceImpl.ConnectionSupplier connectionSupplier) {
        this.userDao = userDao;
        this.profileDao = profileDao;
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public StudentProfileResponse getOwnProfile(long studentId) throws SQLException {
        try (Connection connection = connectionSupplier.get()) {
            User student = userDao.findByIdAndRole(connection, studentId, Role.STUDENT);
            if (student == null || student.getStatus() == null || !student.getStatus().isActive()) {
                throw new AuthenticationException("Authenticated student account is not active.");
            }
            StudentProfile profile = profileDao.findByUserId(connection, studentId);
            return StudentProfileResponse.from(student, profile);
        }
    }

    @Override
    public StudentProfileResponse updateOwnProfile(long studentId, StudentProfileRequest request) throws SQLException {
        Map<String, String> fieldErrors = ConsultancyValidationUtil.validateStudentProfile(request);
        if (!fieldErrors.isEmpty()) {
            throw new ValidationException("Please correct the highlighted profile fields.", fieldErrors);
        }

        String normalizedName = ValidationUtil.normalizeFullName(request.getFullName());
        String normalizedPhone = ValidationUtil.normalizePhoneNumber(request.getPhoneNumber());

        StudentProfile profile = buildProfileFromRequest(studentId, request);

        try (Connection connection = connectionSupplier.get()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                User student = userDao.findByIdAndRole(connection, studentId, Role.STUDENT);
                if (student == null || student.getStatus() == null || !student.getStatus().isActive()) {
                    throw new AuthenticationException("Authenticated student account is not active.");
                }
                profileDao.updateUserIdentity(connection, studentId, normalizedName, normalizedPhone);
                StudentProfile savedProfile = profileDao.upsert(connection, profile);
                User refreshedStudent = userDao.findByIdAndRole(connection, studentId, Role.STUDENT);
                connection.commit();
                return StudentProfileResponse.from(refreshedStudent, savedProfile);
            } catch (SQLException ex) {
                rollbackQuietly(connection);
                throw ex;
            } catch (RuntimeException ex) {
                rollbackQuietly(connection);
                throw ex;
            } finally {
                restoreAutoCommitQuietly(connection, originalAutoCommit);
            }
        }
    }

    private static StudentProfile buildProfileFromRequest(long studentId, StudentProfileRequest req) {
        Map<String, String> unusedErrors = new LinkedHashMap<String, String>();
        StudentProfile profile = new StudentProfile();
        profile.setUserId(studentId);
        profile.setDateOfBirth(ConsultancyValidationUtil.parseOptionalDate(req.getDateOfBirth(), "dateOfBirth", unusedErrors));
        profile.setGender(ConsultancyValidationUtil.normalizeText(req.getGender()));
        profile.setNationality(ConsultancyValidationUtil.normalizeText(req.getNationality()));
        profile.setPresentAddress(ConsultancyValidationUtil.normalizeText(req.getPresentAddress()));
        profile.setPermanentAddress(ConsultancyValidationUtil.normalizeText(req.getPermanentAddress()));

        String passportNum = ConsultancyValidationUtil.normalizeText(req.getPassportNumber());
        profile.setPassportNumber(passportNum != null ? passportNum.toUpperCase(Locale.ROOT) : null);
        profile.setPassportIssueDate(ConsultancyValidationUtil.parseOptionalDate(req.getPassportIssueDate(), "passportIssueDate", unusedErrors));
        profile.setPassportExpiryDate(ConsultancyValidationUtil.parseOptionalDate(req.getPassportExpiryDate(), "passportExpiryDate", unusedErrors));
        profile.setPassportIssuingCountry(ConsultancyValidationUtil.normalizeText(req.getPassportIssuingCountry()));

        profile.setHighestQualification(ConsultancyValidationUtil.normalizeText(req.getHighestQualification()));
        profile.setSscResult(ConsultancyValidationUtil.normalizeText(req.getSscResult()));
        profile.setHscResult(ConsultancyValidationUtil.normalizeText(req.getHscResult()));
        profile.setBachelorInstitution(ConsultancyValidationUtil.normalizeText(req.getBachelorInstitution()));
        profile.setBachelorSubject(ConsultancyValidationUtil.normalizeText(req.getBachelorSubject()));
        profile.setBachelorCgpa(ConsultancyValidationUtil.normalizeText(req.getBachelorCgpa()));
        profile.setBachelorGraduationYear(req.getBachelorGraduationYear());

        profile.setIeltsScore(ConsultancyValidationUtil.normalizeText(req.getIeltsScore()));
        profile.setPteScore(ConsultancyValidationUtil.normalizeText(req.getPteScore()));
        profile.setToeflScore(ConsultancyValidationUtil.normalizeText(req.getToeflScore()));
        profile.setOtherEnglishQualification(ConsultancyValidationUtil.normalizeText(req.getOtherEnglishQualification()));

        String degreeRaw = ConsultancyValidationUtil.normalizeText(req.getPreferredDegreeLevel());
        if (degreeRaw != null) {
            profile.setPreferredDegreeLevel(DegreeLevel.fromString(degreeRaw));
        }
        profile.setPreferredSubject(ConsultancyValidationUtil.normalizeText(req.getPreferredSubject()));
        profile.setPreferredUniversity(ConsultancyValidationUtil.normalizeText(req.getPreferredUniversity()));
        profile.setPreferredIntake(ConsultancyValidationUtil.normalizeText(req.getPreferredIntake()));
        profile.setPreferredStudyLocation(ConsultancyValidationUtil.normalizeText(req.getPreferredStudyLocation()));

        String budgetRaw = ConsultancyValidationUtil.normalizeText(req.getEstimatedBudget());
        if (budgetRaw != null) {
            profile.setEstimatedBudget(new BigDecimal(budgetRaw.replace(",", "")));
        }
        String currencyRaw = ConsultancyValidationUtil.normalizeText(req.getBudgetCurrency());
        profile.setBudgetCurrency(currencyRaw != null ? currencyRaw.toUpperCase(Locale.ROOT) : "GBP");
        profile.setFundingSource(ConsultancyValidationUtil.normalizeText(req.getFundingSource()));
        profile.setSponsorInfo(ConsultancyValidationUtil.normalizeText(req.getSponsorInfo()));
        return profile;
    }

    private static void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Preserve primary exception
        }
    }

    private static void restoreAutoCommitQuietly(Connection connection, boolean originalAutoCommit) {
        try {
            connection.setAutoCommit(originalAutoCommit);
        } catch (SQLException ignored) {
            // Closing connection next
        }
    }
}
