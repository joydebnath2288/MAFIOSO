package com.mafioso.dao;

import com.mafioso.dto.DashboardMetricsResponse;
import com.mafioso.model.AccountStatus;
import com.mafioso.model.Role;
import com.mafioso.model.User;
import com.mafioso.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parameterized JDBC implementation of {@link UserDAO} for MySQL 8.
 */
public class UserDAOImpl implements UserDAO {

    private static final String USER_COLUMNS =
            "id, full_name, email, phone_number, password_hash, role, status, created_at, updated_at, last_login_at";

    @Override
    public User findByEmail(String normalizedEmail) throws SQLException {
        try (Connection connection = DatabaseUtil.getConnection()) {
            return findByEmail(connection, normalizedEmail);
        }
    }

    @Override
    public User findByEmail(Connection connection, String normalizedEmail) throws SQLException {
        String sql = "SELECT " + USER_COLUMNS + " FROM users WHERE email = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, normalizedEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public User findById(long userId) throws SQLException {
        try (Connection connection = DatabaseUtil.getConnection()) {
            return findById(connection, userId);
        }
    }

    @Override
    public User findById(Connection connection, long userId) throws SQLException {
        String sql = "SELECT " + USER_COLUMNS + " FROM users WHERE id = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public User findByIdAndRole(Connection connection, long userId, Role expectedRole) throws SQLException {
        String sql = "SELECT " + USER_COLUMNS + " FROM users WHERE id = ? AND role = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, expectedRole.name());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public boolean existsByEmail(Connection connection, String normalizedEmail) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, normalizedEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean existsAnyByRole(Connection connection, Role role) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE role = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public User createUser(Connection connection, User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, phone_number, password_hash, role, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            if (user.getPhoneNumber() == null) {
                stmt.setNull(3, Types.VARCHAR);
            } else {
                stmt.setString(3, user.getPhoneNumber());
            }
            stmt.setString(4, user.getPasswordHash());
            stmt.setString(5, user.getRole().name());
            stmt.setString(6, user.getStatus().name());

            int affected = stmt.executeUpdate();
            if (affected != 1) {
                throw new SQLException("Expected 1 inserted user row, got " + affected);
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to retrieve generated user ID");
                }
                long generatedId = keys.getLong(1);
                return findById(connection, generatedId);
            }
        }
    }

    @Override
    public void updateLastLoginAt(long userId, Instant loginTime) throws SQLException {
        try (Connection connection = DatabaseUtil.getConnection()) {
            updateLastLoginAt(connection, userId, loginTime);
        }
    }

    @Override
    public void updateLastLoginAt(Connection connection, long userId, Instant loginTime) throws SQLException {
        String sql = "UPDATE users SET last_login_at = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.from(loginTime));
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        }
    }

    @Override
    public boolean updateAccountStatus(Connection connection,
                                       long userId,
                                       Role expectedRole,
                                       AccountStatus status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE id = ? AND role = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, userId);
            stmt.setString(3, expectedRole.name());
            return stmt.executeUpdate() == 1;
        }
    }

    @Override
    public Map<Role, Long> countUsersByRole() throws SQLException {
        Map<Role, Long> counts = new EnumMap<Role, Long>(Role.class);
        for (Role role : Role.values()) {
            counts.put(role, 0L);
        }
        String sql = "SELECT role, COUNT(*) AS total FROM users GROUP BY role";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Role role = Role.fromString(rs.getString("role"));
                counts.put(role, rs.getLong("total"));
            }
        }
        return counts;
    }

    @Override
    public DashboardMetricsResponse getDashboardMetrics() throws SQLException {
        long totalStudents = 0L;
        long activeStudents = 0L;
        long totalConsultants = 0L;
        long activeConsultants = 0L;
        long totalAdmins = 0L;

        String sql = "SELECT role, status, COUNT(*) AS total FROM users GROUP BY role, status";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Role role = Role.fromString(rs.getString("role"));
                AccountStatus status = AccountStatus.fromString(rs.getString("status"));
                long count = rs.getLong("total");
                if (role == Role.STUDENT) {
                    totalStudents += count;
                    if (status == AccountStatus.ACTIVE) {
                        activeStudents += count;
                    }
                } else if (role == Role.CONSULTANT) {
                    totalConsultants += count;
                    if (status == AccountStatus.ACTIVE) {
                        activeConsultants += count;
                    }
                } else if (role == Role.ADMIN) {
                    totalAdmins += count;
                }
            }
        }
        return new DashboardMetricsResponse(
                totalStudents,
                activeStudents,
                totalConsultants,
                activeConsultants,
                totalAdmins
        );
    }

    @Override
    public List<User> findUsersByRole(Role role,
                                      String search,
                                      AccountStatus statusFilter,
                                      int offset,
                                      int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT ").append(USER_COLUMNS).append(" FROM users");
        List<Object> params = new ArrayList<Object>();
        appendRoleSearchFilter(sql, params, role, search, statusFilter);
        sql.append(" ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?");
        params.add(Integer.valueOf(limit));
        params.add(Integer.valueOf(offset));

        List<User> results = new ArrayList<User>();
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            bindParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    @Override
    public long countUsersByRoleAndFilter(Role role,
                                          String search,
                                          AccountStatus statusFilter) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM users");
        List<Object> params = new ArrayList<Object>();
        appendRoleSearchFilter(sql, params, role, search, statusFilter);

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            bindParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong("total") : 0L;
            }
        }
    }

    private static void appendRoleSearchFilter(StringBuilder sql,
                                               List<Object> params,
                                               Role role,
                                               String search,
                                               AccountStatus statusFilter) {
        sql.append(" WHERE role = ?");
        params.add(role.name());

        if (statusFilter != null) {
            sql.append(" AND status = ?");
            params.add(statusFilter.name());
        }

        if (search != null && !search.trim().isEmpty()) {
            String escaped = escapeLikePattern(search.trim().toLowerCase(Locale.ROOT));
            String likeValue = "%" + escaped + "%";
            sql.append(" AND (LOWER(full_name) LIKE ? ESCAPE '\\\\' OR LOWER(email) LIKE ? ESCAPE '\\\\')");
            params.add(likeValue);
            params.add(likeValue);
        }
    }

    private static void bindParameters(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object val = params.get(i);
            if (val instanceof Integer) {
                stmt.setInt(i + 1, (Integer) val);
            } else {
                stmt.setString(i + 1, String.valueOf(val));
            }
        }
    }

    private static String escapeLikePattern(String raw) {
        return raw.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.fromString(rs.getString("role")));
        user.setStatus(AccountStatus.fromString(rs.getString("status")));

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            user.setCreatedAt(createdTs.toInstant());
        }
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) {
            user.setUpdatedAt(updatedTs.toInstant());
        }
        Timestamp lastLoginTs = rs.getTimestamp("last_login_at");
        if (lastLoginTs != null) {
            user.setLastLoginAt(lastLoginTs.toInstant());
        }
        return user;
    }
}
