package com.mafioso.dao;

import com.mafioso.model.StudentProfile;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * JDBC persistence contract for {@code student_profiles}.
 */
public interface StudentProfileDAO {

    StudentProfile findByUserId(Connection connection, long userId) throws SQLException;

    StudentProfile upsert(Connection connection, StudentProfile profile) throws SQLException;

    boolean updateUserIdentity(Connection connection, long userId, String fullName, String phoneNumber) throws SQLException;
}
