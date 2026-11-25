package com.mafioso.dao;

import com.mafioso.dto.DashboardMetricsResponse;
import com.mafioso.model.AccountStatus;
import com.mafioso.model.Role;
import com.mafioso.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * JDBC persistence contract for user accounts, dashboard metrics, search, and management.
 */
public interface UserDAO {

    User findByEmail(String normalizedEmail) throws SQLException;

    User findByEmail(Connection connection, String normalizedEmail) throws SQLException;

    User findById(long userId) throws SQLException;

    User findById(Connection connection, long userId) throws SQLException;

    User findByIdAndRole(Connection connection, long userId, Role expectedRole) throws SQLException;

    boolean existsByEmail(Connection connection, String normalizedEmail) throws SQLException;

    boolean existsAnyByRole(Connection connection, Role role) throws SQLException;

    User createUser(Connection connection, User user) throws SQLException;

    void updateLastLoginAt(long userId, Instant loginTime) throws SQLException;

    void updateLastLoginAt(Connection connection, long userId, Instant loginTime) throws SQLException;

    boolean updateAccountStatus(Connection connection, long userId, Role expectedRole, AccountStatus status) throws SQLException;

    Map<Role, Long> countUsersByRole() throws SQLException;

    DashboardMetricsResponse getDashboardMetrics() throws SQLException;

    List<User> findUsersByRole(Role role, String search, AccountStatus statusFilter, int offset, int limit) throws SQLException;

    long countUsersByRoleAndFilter(Role role, String search, AccountStatus statusFilter) throws SQLException;
}
