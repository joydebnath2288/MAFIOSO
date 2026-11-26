package com.mafioso.dao;

import com.mafioso.model.DegreeLevel;
import com.mafioso.model.StudentProfile;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * Parameterized JDBC implementation of {@link StudentProfileDAO}.
 */
public class StudentProfileDAOImpl implements StudentProfileDAO {

    private static final String COLUMNS =
            "id, user_id, date_of_birth, gender, nationality, present_address, permanent_address, "
                    + "passport_number, passport_issue_date, passport_expiry_date, passport_issuing_country, "
                    + "highest_qualification, ssc_result, hsc_result, bachelor_institution, bachelor_subject, "
                    + "bachelor_cgpa, bachelor_graduation_year, ielts_score, pte_score, toefl_score, "
                    + "other_english_qualification, preferred_degree_level, preferred_subject, "
                    + "preferred_university, preferred_intake, preferred_study_location, "
                    + "estimated_budget, budget_currency, funding_source, sponsor_info, created_at, updated_at";

    @Override
    public StudentProfile findByUserId(Connection connection, long userId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM student_profiles WHERE user_id = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public StudentProfile upsert(Connection connection, StudentProfile profile) throws SQLException {
        String sql = "INSERT INTO student_profiles ("
                + "user_id, date_of_birth, gender, nationality, present_address, permanent_address, "
                + "passport_number, passport_issue_date, passport_expiry_date, passport_issuing_country, "
                + "highest_qualification, ssc_result, hsc_result, bachelor_institution, bachelor_subject, "
                + "bachelor_cgpa, bachelor_graduation_year, ielts_score, pte_score, toefl_score, "
                + "other_english_qualification, preferred_degree_level, preferred_subject, "
                + "preferred_university, preferred_intake, preferred_study_location, "
                + "estimated_budget, budget_currency, funding_source, sponsor_info"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "date_of_birth = VALUES(date_of_birth), "
                + "gender = VALUES(gender), "
                + "nationality = VALUES(nationality), "
                + "present_address = VALUES(present_address), "
                + "permanent_address = VALUES(permanent_address), "
                + "passport_number = VALUES(passport_number), "
                + "passport_issue_date = VALUES(passport_issue_date), "
                + "passport_expiry_date = VALUES(passport_expiry_date), "
                + "passport_issuing_country = VALUES(passport_issuing_country), "
                + "highest_qualification = VALUES(highest_qualification), "
                + "ssc_result = VALUES(ssc_result), "
                + "hsc_result = VALUES(hsc_result), "
                + "bachelor_institution = VALUES(bachelor_institution), "
                + "bachelor_subject = VALUES(bachelor_subject), "
                + "bachelor_cgpa = VALUES(bachelor_cgpa), "
                + "bachelor_graduation_year = VALUES(bachelor_graduation_year), "
                + "ielts_score = VALUES(ielts_score), "
                + "pte_score = VALUES(pte_score), "
                + "toefl_score = VALUES(toefl_score), "
                + "other_english_qualification = VALUES(other_english_qualification), "
                + "preferred_degree_level = VALUES(preferred_degree_level), "
                + "preferred_subject = VALUES(preferred_subject), "
                + "preferred_university = VALUES(preferred_university), "
                + "preferred_intake = VALUES(preferred_intake), "
                + "preferred_study_location = VALUES(preferred_study_location), "
                + "estimated_budget = VALUES(estimated_budget), "
                + "budget_currency = VALUES(budget_currency), "
                + "funding_source = VALUES(funding_source), "
                + "sponsor_info = VALUES(sponsor_info)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, profile.getUserId());
            setNullableDate(stmt, 2, profile.getDateOfBirth());
            setNullableString(stmt, 3, profile.getGender());
            setNullableString(stmt, 4, profile.getNationality());
            setNullableString(stmt, 5, profile.getPresentAddress());
            setNullableString(stmt, 6, profile.getPermanentAddress());
            setNullableString(stmt, 7, profile.getPassportNumber());
            setNullableDate(stmt, 8, profile.getPassportIssueDate());
            setNullableDate(stmt, 9, profile.getPassportExpiryDate());
            setNullableString(stmt, 10, profile.getPassportIssuingCountry());
            setNullableString(stmt, 11, profile.getHighestQualification());
            setNullableString(stmt, 12, profile.getSscResult());
            setNullableString(stmt, 13, profile.getHscResult());
            setNullableString(stmt, 14, profile.getBachelorInstitution());
            setNullableString(stmt, 15, profile.getBachelorSubject());
            setNullableString(stmt, 16, profile.getBachelorCgpa());
            if (profile.getBachelorGraduationYear() == null) {
                stmt.setNull(17, Types.SMALLINT);
            } else {
                stmt.setInt(17, profile.getBachelorGraduationYear());
            }
            setNullableString(stmt, 18, profile.getIeltsScore());
            setNullableString(stmt, 19, profile.getPteScore());
            setNullableString(stmt, 20, profile.getToeflScore());
            setNullableString(stmt, 21, profile.getOtherEnglishQualification());
            setNullableString(stmt, 22, profile.getPreferredDegreeLevel() != null ? profile.getPreferredDegreeLevel().getDbValue() : null);
            setNullableString(stmt, 23, profile.getPreferredSubject());
            setNullableString(stmt, 24, profile.getPreferredUniversity());
            setNullableString(stmt, 25, profile.getPreferredIntake());
            setNullableString(stmt, 26, profile.getPreferredStudyLocation());
            if (profile.getEstimatedBudget() == null) {
                stmt.setNull(27, Types.DECIMAL);
            } else {
                stmt.setBigDecimal(27, profile.getEstimatedBudget());
            }
            stmt.setString(28, profile.getBudgetCurrency() != null ? profile.getBudgetCurrency() : "GBP");
            setNullableString(stmt, 29, profile.getFundingSource());
            setNullableString(stmt, 30, profile.getSponsorInfo());
            stmt.executeUpdate();
        }

        return findByUserId(connection, profile.getUserId());
    }

    @Override
    public boolean updateUserIdentity(Connection connection, long userId, String fullName, String phoneNumber) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, phone_number = ? WHERE id = ? AND role = 'STUDENT'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            setNullableString(stmt, 2, phoneNumber);
            stmt.setLong(3, userId);
            return stmt.executeUpdate() == 1;
        }
    }

    private static void setNullableString(PreparedStatement stmt, int idx, String value) throws SQLException {
        if (value == null) {
            stmt.setNull(idx, Types.VARCHAR);
        } else {
            stmt.setString(idx, value);
        }
    }

    private static void setNullableDate(PreparedStatement stmt, int idx, java.time.LocalDate value) throws SQLException {
        if (value == null) {
            stmt.setNull(idx, Types.DATE);
        } else {
            stmt.setDate(idx, Date.valueOf(value));
        }
    }

    private static StudentProfile mapRow(ResultSet rs) throws SQLException {
        StudentProfile p = new StudentProfile();
        p.setId(rs.getLong("id"));
        p.setUserId(rs.getLong("user_id"));

        Date dob = rs.getDate("date_of_birth");
        if (dob != null) p.setDateOfBirth(dob.toLocalDate());
        p.setGender(rs.getString("gender"));
        p.setNationality(rs.getString("nationality"));
        p.setPresentAddress(rs.getString("present_address"));
        p.setPermanentAddress(rs.getString("permanent_address"));

        p.setPassportNumber(rs.getString("passport_number"));
        Date issue = rs.getDate("passport_issue_date");
        if (issue != null) p.setPassportIssueDate(issue.toLocalDate());
        Date expiry = rs.getDate("passport_expiry_date");
        if (expiry != null) p.setPassportExpiryDate(expiry.toLocalDate());
        p.setPassportIssuingCountry(rs.getString("passport_issuing_country"));

        p.setHighestQualification(rs.getString("highest_qualification"));
        p.setSscResult(rs.getString("ssc_result"));
        p.setHscResult(rs.getString("hsc_result"));
        p.setBachelorInstitution(rs.getString("bachelor_institution"));
        p.setBachelorSubject(rs.getString("bachelor_subject"));
        p.setBachelorCgpa(rs.getString("bachelor_cgpa"));
        int gradYear = rs.getInt("bachelor_graduation_year");
        if (!rs.wasNull()) {
            p.setBachelorGraduationYear(gradYear);
        }

        p.setIeltsScore(rs.getString("ielts_score"));
        p.setPteScore(rs.getString("pte_score"));
        p.setToeflScore(rs.getString("toefl_score"));
        p.setOtherEnglishQualification(rs.getString("other_english_qualification"));

        String degreeRaw = rs.getString("preferred_degree_level");
        if (degreeRaw != null) {
            p.setPreferredDegreeLevel(DegreeLevel.fromString(degreeRaw));
        }
        p.setPreferredSubject(rs.getString("preferred_subject"));
        p.setPreferredUniversity(rs.getString("preferred_university"));
        p.setPreferredIntake(rs.getString("preferred_intake"));
        p.setPreferredStudyLocation(rs.getString("preferred_study_location"));

        p.setEstimatedBudget(rs.getBigDecimal("estimated_budget"));
        p.setBudgetCurrency(rs.getString("budget_currency"));
        p.setFundingSource(rs.getString("funding_source"));
        p.setSponsorInfo(rs.getString("sponsor_info"));

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) p.setCreatedAt(createdTs.toInstant());
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) p.setUpdatedAt(updatedTs.toInstant());
        return p;
    }
}
