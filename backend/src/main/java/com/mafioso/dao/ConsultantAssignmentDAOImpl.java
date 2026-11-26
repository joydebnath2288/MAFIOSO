package com.mafioso.dao;

import com.mafioso.dto.AssignedStudentSummaryResponse;
import com.mafioso.model.AssignmentAction;
import com.mafioso.model.ConsultantAssignment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parameterized JDBC implementation of {@link ConsultantAssignmentDAO}.
 */
public class ConsultantAssignmentDAOImpl implements ConsultantAssignmentDAO {

    private static final String SELECT_COLUMNS =
            "ca.id, ca.application_id, a.reference_number AS application_reference, ca.student_id, "
                    + "ca.consultant_id, c.full_name AS consultant_name, c.email AS consultant_email, "
                    + "ca.assigned_by_user_id, ab.full_name AS assigned_by_name, ca.action, ca.is_active, "
                    + "ca.note, ca.assigned_at, ca.unassigned_at, ca.unassigned_by_user_id, ub.full_name AS unassigned_by_name "
                    + "FROM consultant_assignments ca "
                    + "INNER JOIN applications a ON a.id = ca.application_id "
                    + "INNER JOIN users c ON c.id = ca.consultant_id "
                    + "INNER JOIN users ab ON ab.id = ca.assigned_by_user_id "
                    + "LEFT JOIN users ub ON ub.id = ca.unassigned_by_user_id ";

    @Override
    public ConsultantAssignment findActiveByApplicationId(Connection connection, long applicationId) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS
                + "WHERE ca.application_id = ? AND ca.is_active = 1 ORDER BY ca.assigned_at DESC, ca.id DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, applicationId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<ConsultantAssignment> findHistoryByApplicationId(Connection connection, long applicationId) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS
                + "WHERE ca.application_id = ? ORDER BY ca.assigned_at DESC, ca.id DESC";
        List<ConsultantAssignment> items = new ArrayList<ConsultantAssignment>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, applicationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        }
        return items;
    }

    @Override
    public List<ConsultantAssignment> findActiveByConsultantId(Connection connection, long consultantId) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS
                + "WHERE ca.consultant_id = ? AND ca.is_active = 1 ORDER BY ca.assigned_at DESC";
        List<ConsultantAssignment> items = new ArrayList<ConsultantAssignment>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, consultantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        }
        return items;
    }

    @Override
    public void deactivateActiveForApplication(Connection connection, long applicationId, long unassignedByUserId) throws SQLException {
        String sql = "UPDATE consultant_assignments SET is_active = 0, unassigned_at = CURRENT_TIMESTAMP, unassigned_by_user_id = ? "
                + "WHERE application_id = ? AND is_active = 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, unassignedByUserId);
            stmt.setLong(2, applicationId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void insert(Connection connection, ConsultantAssignment assignment) throws SQLException {
        String sql = "INSERT INTO consultant_assignments ("
                + "application_id, student_id, consultant_id, assigned_by_user_id, action, is_active, note, unassigned_at, unassigned_by_user_id"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, assignment.getApplicationId());
            stmt.setLong(2, assignment.getStudentId());
            stmt.setLong(3, assignment.getConsultantId());
            stmt.setLong(4, assignment.getAssignedByUserId());
            stmt.setString(5, assignment.getAction().name());
            stmt.setInt(6, assignment.isActive() ? 1 : 0);
            if (assignment.getNote() == null) {
                stmt.setNull(7, Types.VARCHAR);
            } else {
                stmt.setString(7, assignment.getNote());
            }
            if (assignment.getUnassignedAt() == null) {
                stmt.setNull(8, Types.TIMESTAMP);
            } else {
                stmt.setTimestamp(8, Timestamp.from(assignment.getUnassignedAt()));
            }
            if (assignment.getUnassignedByUserId() == null) {
                stmt.setNull(9, Types.BIGINT);
            } else {
                stmt.setLong(9, assignment.getUnassignedByUserId());
            }
            stmt.executeUpdate();
        }
    }

    @Override
    public boolean isConsultantAssignedToStudent(Connection connection, long consultantId, long studentId) throws SQLException {
        String sql = "SELECT 1 FROM applications WHERE assigned_consultant_id = ? AND student_id = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, consultantId);
            stmt.setLong(2, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public List<AssignedStudentSummaryResponse> findAssignedStudentsByConsultant(Connection connection,
                                                                               long consultantId,
                                                                               String search,
                                                                               int offset,
                                                                               int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT s.id AS student_id, s.full_name, s.email, s.phone_number, s.status AS account_status, "
                        + "COUNT(a.id) AS active_application_count, "
                        + "MAX(a.updated_at) AS latest_updated_at, "
                        + "MIN(a.created_at) AS assigned_since, "
                        + "(SELECT a2.reference_number FROM applications a2 WHERE a2.student_id = s.id AND a2.assigned_consultant_id = ? ORDER BY a2.updated_at DESC, a2.id DESC LIMIT 1) AS latest_ref, "
                        + "(SELECT a3.status FROM applications a3 WHERE a3.student_id = s.id AND a3.assigned_consultant_id = ? ORDER BY a3.updated_at DESC, a3.id DESC LIMIT 1) AS latest_status, "
                        + "sp.preferred_degree_level, sp.preferred_university "
                        + "FROM applications a "
                        + "INNER JOIN users s ON s.id = a.student_id "
                        + "LEFT JOIN student_profiles sp ON sp.user_id = s.id "
                        + "WHERE a.assigned_consultant_id = ? ");

        List<Object> params = new ArrayList<Object>();
        params.add(Long.valueOf(consultantId));
        params.add(Long.valueOf(consultantId));
        params.add(Long.valueOf(consultantId));

        if (search != null && !search.trim().isEmpty()) {
            String escaped = escapeLikePattern(search.trim().toLowerCase(Locale.ROOT));
            String like = "%" + escaped + "%";
            sql.append("AND (LOWER(s.full_name) LIKE ? ESCAPE '\\\\' OR LOWER(s.email) LIKE ? ESCAPE '\\\\' OR LOWER(a.reference_number) LIKE ? ESCAPE '\\\\') ");
            params.add(like);
            params.add(like);
            params.add(like);
        }

        sql.append("GROUP BY s.id, s.full_name, s.email, s.phone_number, s.status, sp.preferred_degree_level, sp.preferred_university ");
        sql.append("ORDER BY latest_updated_at DESC, s.id DESC LIMIT ? OFFSET ?");
        params.add(Integer.valueOf(limit));
        params.add(Integer.valueOf(offset));

        List<AssignedStudentSummaryResponse> items = new ArrayList<AssignedStudentSummaryResponse>();
        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            bindParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AssignedStudentSummaryResponse dto = new AssignedStudentSummaryResponse();
                    dto.setStudentId(rs.getLong("student_id"));
                    dto.setFullName(rs.getString("full_name"));
                    dto.setEmail(rs.getString("email"));
                    dto.setPhoneNumber(rs.getString("phone_number"));
                    dto.setAccountStatus(rs.getString("account_status"));
                    dto.setActiveApplicationCount(rs.getLong("active_application_count"));
                    dto.setLatestApplicationReference(rs.getString("latest_ref"));
                    dto.setLatestApplicationStatus(rs.getString("latest_status"));
                    dto.setPreferredDegreeLevel(rs.getString("preferred_degree_level"));
                    dto.setPreferredUniversity(rs.getString("preferred_university"));
                    Timestamp since = rs.getTimestamp("assigned_since");
                    if (since != null) {
                        dto.setAssignedSince(since.toInstant().toString());
                    }
                    items.add(dto);
                }
            }
        }
        return items;
    }

    @Override
    public long countAssignedStudentsByConsultant(Connection connection, long consultantId, String search) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT a.student_id) AS total "
                        + "FROM applications a "
                        + "INNER JOIN users s ON s.id = a.student_id "
                        + "WHERE a.assigned_consultant_id = ? ");
        List<Object> params = new ArrayList<Object>();
        params.add(Long.valueOf(consultantId));
        if (search != null && !search.trim().isEmpty()) {
            String escaped = escapeLikePattern(search.trim().toLowerCase(Locale.ROOT));
            String like = "%" + escaped + "%";
            sql.append("AND (LOWER(s.full_name) LIKE ? ESCAPE '\\\\' OR LOWER(s.email) LIKE ? ESCAPE '\\\\' OR LOWER(a.reference_number) LIKE ? ESCAPE '\\\\') ");
            params.add(like);
            params.add(like);
            params.add(like);
        }
        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            bindParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong("total") : 0L;
            }
        }
    }

    @Override
    public Map<Long, Long> countActiveStudentsByConsultantIds(Connection connection, List<Long> consultantIds) throws SQLException {
        if (consultantIds == null || consultantIds.isEmpty()) {
            return Collections.emptyMap();
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < consultantIds.size(); i++) {
            if (i > 0) {
                placeholders.append(", ");
            }
            placeholders.append("?");
        }
        String sql = "SELECT assigned_consultant_id, COUNT(DISTINCT student_id) AS total "
                + "FROM applications "
                + "WHERE assigned_consultant_id IN (" + placeholders + ") "
                + "GROUP BY assigned_consultant_id";

        Map<Long, Long> counts = new HashMap<Long, Long>();
        for (Long id : consultantIds) {
            counts.put(id, 0L);
        }
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < consultantIds.size(); i++) {
                stmt.setLong(i + 1, consultantIds.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getLong("assigned_consultant_id"), rs.getLong("total"));
                }
            }
        }
        return counts;
    }

    private static void bindParameters(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object val = params.get(i);
            if (val instanceof Long) {
                stmt.setLong(i + 1, (Long) val);
            } else if (val instanceof Integer) {
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

    private static ConsultantAssignment mapRow(ResultSet rs) throws SQLException {
        ConsultantAssignment item = new ConsultantAssignment();
        item.setId(rs.getLong("id"));
        item.setApplicationId(rs.getLong("application_id"));
        item.setApplicationReference(rs.getString("application_reference"));
        item.setStudentId(rs.getLong("student_id"));
        item.setConsultantId(rs.getLong("consultant_id"));
        item.setConsultantName(rs.getString("consultant_name"));
        item.setConsultantEmail(rs.getString("consultant_email"));
        item.setAssignedByUserId(rs.getLong("assigned_by_user_id"));
        item.setAssignedByName(rs.getString("assigned_by_name"));
        item.setAction(AssignmentAction.fromString(rs.getString("action")));
        item.setActive(rs.getInt("is_active") == 1);
        item.setNote(rs.getString("note"));
        Timestamp assignedTs = rs.getTimestamp("assigned_at");
        if (assignedTs != null) item.setAssignedAt(assignedTs.toInstant());
        Timestamp unassignedTs = rs.getTimestamp("unassigned_at");
        if (unassignedTs != null) item.setUnassignedAt(unassignedTs.toInstant());
        long unassignedBy = rs.getLong("unassigned_by_user_id");
        if (!rs.wasNull()) {
            item.setUnassignedByUserId(unassignedBy);
            item.setUnassignedByName(rs.getString("unassigned_by_name"));
        }
        return item;
    }
}
