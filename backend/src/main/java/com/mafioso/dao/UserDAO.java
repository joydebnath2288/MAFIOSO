package com.mafioso.dao;

import com.mafioso.model.Role;
import com.mafioso.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

/**
 * JDBC persistence contract for user accounts and role counts.
 */
public interface UserDAO {

    User findByEmail(String normalizedEmail) throws SQLException;

    User findByEmail(Connection connection, String normalizedEmail) throws SQLException;

    User findById(long userId) throws SQLException;

    User findById(Connection connection, long userId) throws SQLException;

    boolean existsByEmail(Connection connection, String normalizedEmail) throws SQLException;

    boolean existsAnyByRole(Connection connection, Role role) throws SQLException;

    User createUser(Connection connection, User user) throws SQLException;

    void updateLastLoginAt(long userId, Instant loginTime) throws SQLException;

    void updateLastLoginAt(Connection connection, long userId, Instant loginTime) throws SQLException;

    Map<Role, Long> countUsersByRole() throws SQLException;
}
