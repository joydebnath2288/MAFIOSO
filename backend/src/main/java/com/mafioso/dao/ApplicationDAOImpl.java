package com.mafioso.dao;

import com.mafioso.model.Application;
import com.mafioso.model.ApplicationStatus;
import com.mafioso.model.ApplicationStatusHistory;
import com.mafioso.model.DegreeLevel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parameterized JDBC implementation of {@link ApplicationDAO}.
 */
public class ApplicationDAOImpl implements ApplicationDAO {

    private static final String BASE_SELECT =
            "SELECT a.id, a.reference_number, a.student_id, "
                    + "s.full_name AS student_name, s.email AS student_email, s.phone_number AS student_phone, "
                    + "a.degree_level, a.preferred_subject, a.intended_intake, a.preferred_university, "
                    + "a.preferred_location, a.personal_statement, a.status, a.assigned_consultant_id, "
                    + "c.full_name AS consultant_name, c.email AS consultant_email, c.phone_number AS consultant_phone, "
                    + "c.status AS consultant_status, "
                    + "(SELECT COUNT(*) FROM application_documents d WHERE d.application_id = a.id) AS doc_count, "
                    + "a.submitted_at, a.created_at, a.updated_at "
                    + "FROM applications a "
                    + "INNER JOIN users s ON s.id = a.student_id "
                    + "LEFT JOIN users c ON c.id = a.assigned_consultant_id ";

    @Override
    public String nextReferenceNumber(Connection connection, int year) throws SQLException {
        String initSql = "INSERT INTO application_reference_sequences (ref_year, last_seq) VALUES (?, 0) "
                + "ON DUPLICATE KEY UPDATE ref_year = ref_year";
        try (PreparedStatement stmt = connection.prepareStatement(initSql)) {
            stmt.setInt(1, year);
            stmt.executeUpdate();
        }

        long nextSeq;
        String lockSql = "SELECT last_seq FROM application_reference_sequences WHERE ref_year = ? FOR UPDATE";
        try (PreparedStatement stmt = connection.prepareStatement(lockSql)) {
            stmt.setInt(1, year);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Failed to lock sequence row for year " + year);
                }
                nextSeq = rs.getLong("last_seq") + 1L;
            }
        }

        String updateSql = "UPDATE application_reference_sequences SET last_seq = ? WHERE ref_year = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setLong(1, nextSeq);
            stmt.setInt(2, year);
            stmt.executeUpdate();
        }

        return String.format(Locale.ROOT, "MAF-%04d-%06d", year, nextSeq);
    }

    @Override
    public Application create(Connection connection, Application app) throws SQLException {
        String sql = "INSERT INTO applications ("
                + "reference_number, student_id, degree_level, preferred_subject, intended_intake, "
                + "preferred_university, preferred_location, personal_statement, status, assigned_consultant_id"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, app.getReferenceNumber());
            stmt.setLong(2, app.getStudentId());
            stmt.setString(3, app.getDegreeLevel().getDbValue());
            stmt.setString(4, app.getPreferredSubject());
            stmt.setString(5, app.getIntendedIntake());
            stmt.setString(6, app.getPreferredUniversity());
            if (app.getPreferredLocation() == null) {
                stmt.setNull(7, Types.VARCHAR);
            } else {
                stmt.setString(7, app.getPreferredLocation());
            }
            if (app.getPersonalStatement() == null) {
                stmt.setNull(8, Types.VARCHAR);
            } else {
                stmt.setString(8, app.getPersonalStatement());
            }
            stmt.setString(9, app.getStatus().name());
            if (app.getAssignedConsultantId() == null) {
                stmt.setNull(10, Types.BIGINT);
            } else {
                stmt.setLong(10, app.getAssignedConsultantId());
            }

            int affected = stmt.executeUpdate();
            if (affected != 1) {
                throw new SQLException("Expected 1 inserted application row, got " + affected);
            }
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to retrieve generated application ID");
                }
                long id = keys.getLong(1);
                return findById(connection, id);
            }
        }
    }

    @Override
    public Application findById(Connection connection, long applicationId) throws SQLException {
        String sql = BASE_SELECT + "WHERE a.id = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, applicationId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapApplication(rs) : null;
            }
        }
    }

    @Override
    public List<Application> findByStudentId(Connection connection,
                                             long studentId,
                                             ApplicationStatus statusFilter,
                                             int offset,
                                             int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE a.student_id = ? ");
        List<Object> params = new ArrayList<Object>();
        params.add(Long.valueOf(studentId));
        if (statusFilter != null) {
            sql.append("AND a.status = ? ");
            params.add(statusFilter.name());
        }
        sql.append("ORDER BY a.created_at DESC, a.id DESC LIMIT ? OFFSET ?");
        params.add(Integer.valueOf(limit));
        params.add(Integer.valueOf(offset));

        return queryList(connection, sql.toString(), params);
    }

    @Override
    public long countByStudentId(Connection connection, long studentId, ApplicationStatus statusFilter) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM applications a WHERE a.student_id = ? ");
        List<Object> params = new ArrayList<Object>();
        params.add(Long.valueOf(studentId));
        if (statusFilter != null) {
            sql.append("AND a.status = ? ");
            params.add(statusFilter.name());
        }
        return queryCount(connection, sql.toString(), params);
    }

    @Override
    public List<Application> findByConsultantId(Connection connection,
                                                long consultantId,
                                                String search,
                                                ApplicationStatus statusFilter,
                                                int offset,
                                                int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE a.assigned_consultant_id = ? ");
        List<Object> params = new ArrayList<Object>();
        params.add(Long.valueOf(consultantId));
        appendSearchAndStatus(sql, params, search, statusFilter, null);
        sql.append("ORDER BY a.updated_at DESC, a.id DESC LIMIT ? OFFSET ?");
        params.add(Integer.valueOf(limit));
        params.add(Integer.valueOf(offset));
        return queryList(connection, sql.toString(), params);
    }

    @Override
    public long countByConsultantId(Connection connection,
                                    long consultantId,
                                    String search,
                                    ApplicationStatus statusFilter) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM applications a INNER JOIN users s ON s.id = a.student_id WHERE a.assigned_consultant_id = ? ");
        List<Object> params = new ArrayList<Object>();
        params.add(Long.valueOf(consultantId));
        appendSearchAndStatus(sql, params, search, statusFilter, null);
        return queryCount(connection, sql.toString(), params);
    }

    @Override
    public List<Application> findAll(Connection connection,
                                     String search,
                                     ApplicationStatus statusFilter,
                                     DegreeLevel degreeFilter,
                                     Long consultantId,
                                     boolean unassignedOnly,
                                     int offset,
                                     int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<Object>();
        appendSearchAndStatus(sql, params, search, statusFilter, degreeFilter);
        if (unassignedOnly) {
            sql.append("AND a.assigned_consultant_id IS NULL ");
        } else if (consultantId != null) {
            sql.append("AND a.assigned_consultant_id = ? ");
            params.add(consultantId);
        }
        sql.append("ORDER BY a.created_at DESC, a.id DESC LIMIT ? OFFSET ?");
        params.add(Integer.valueOf(limit));
        params.add(Integer.valueOf(offset));
        return queryList(connection, sql.toString(), params);
    }

    @Override
    public long countAll(Connection connection,
                         String search,
                         ApplicationStatus statusFilter,
                         DegreeLevel degreeFilter,
                         Long consultantId,
                         boolean unassignedOnly) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM applications a INNER JOIN users s ON s.id = a.student_id WHERE 1=1 ");
        List<Object> params = new ArrayList<Object>();
        appendSearchAndStatus(sql, params, search, statusFilter, degreeFilter);
        if (unassignedOnly) {
            sql.append("AND a.assigned_consultant_id IS NULL ");
        } else if (consultantId != null) {
            sql.append("AND a.assigned_consultant_id = ? ");
            params.add(consultantId);
        }
        return queryCount(connection, sql.toString(), params);
    }

    @Override
    public boolean updateStatus(Connection connection, long applicationId, ApplicationStatus newStatus) throws SQLException {
        String sql = "UPDATE applications SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setLong(2, applicationId);
            return stmt.executeUpdate() == 1;
        }
    }

    @Override
    public boolean updateAssignedConsultant(Connection connection, long applicationId, Long consultantId) throws SQLException {
        String sql = "UPDATE applications SET assigned_consultant_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (consultantId == null) {
                stmt.setNull(1, Types.BIGINT);
            } else {
                stmt.setLong(1, consultantId);
            }
            stmt.setLong(2, applicationId);
            return stmt.executeUpdate() == 1;
        }
    }

    @Override
    public void addStatusHistory(Connection connection, ApplicationStatusHistory history) throws SQLException {
        String sql = "INSERT INTO application_status_history ("
                + "application_id, previous_status, new_status, changed_by_user_id, note"
                + ") VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, history.getApplicationId());
            if (history.getPreviousStatus() == null) {
                stmt.setNull(2, Types.VARCHAR);
            } else {
                stmt.setString(2, history.getPreviousStatus().name());
            }
            stmt.setString(3, history.getNewStatus().name());
            stmt.setLong(4, history.getChangedByUserId());
            if (history.getNote() == null) {
                stmt.setNull(5, Types.VARCHAR);
            } else {
                stmt.setString(5, history.getNote());
            }
            stmt.executeUpdate();
        }
    }

    @Override
    public List<ApplicationStatusHistory> findStatusHistoryByApplicationId(Connection connection, long applicationId) throws SQLException {
        String sql = "SELECT h.id, h.application_id, h.previous_status, h.new_status, h.changed_by_user_id, "
                + "u.full_name AS changed_by_name, u.role AS changed_by_role, h.note, h.changed_at "
                + "FROM application_status_history h "
                + "INNER JOIN users u ON u.id = h.changed_by_user_id "
                + "WHERE h.application_id = ? "
                + "ORDER BY h.changed_at ASC, h.id ASC";
        List<ApplicationStatusHistory> items = new ArrayList<ApplicationStatusHistory>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, applicationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ApplicationStatusHistory item = new ApplicationStatusHistory();
                    item.setId(rs.getLong("id"));
                    item.setApplicationId(rs.getLong("application_id"));
                    String prev = rs.getString("previous_status");
                    if (prev != null) {
                        item.setPreviousStatus(ApplicationStatus.fromString(prev));
                    }
                    item.setNewStatus(ApplicationStatus.fromString(rs.getString("new_status")));
                    item.setChangedByUserId(rs.getLong("changed_by_user_id"));
                    item.setChangedByName(rs.getString("changed_by_name"));
                    item.setChangedByRole(rs.getString("changed_by_role"));
                    item.setNote(rs.getString("note"));
                    Timestamp ts = rs.getTimestamp("changed_at");
                    if (ts != null) {
                        item.setChangedAt(ts.toInstant());
                    }
                    items.add(item);
                }
            }
        }
        return items;
    }

    @Override
    public Map<String, Long> getApplicationMetrics(Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) AS total, "
                + "SUM(CASE WHEN assigned_consultant_id IS NULL AND status <> 'CANCELLED' THEN 1 ELSE 0 END) AS unassigned, "
                + "SUM(CASE WHEN status = 'SUBMITTED' THEN 1 ELSE 0 END) AS submitted, "
                + "SUM(CASE WHEN status IN ('UNDER_REVIEW','ASSIGNED','DOCUMENTS_PENDING','DOCUMENTS_REVIEW','IN_PROGRESS') THEN 1 ELSE 0 END) AS active_workflow, "
                + "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed "
                + "FROM applications";
        Map<String, Long> metrics = new LinkedHashMap<String, Long>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                metrics.put("totalApplications", rs.getLong("total"));
                metrics.put("unassignedApplications", rs.getLong("unassigned"));
                metrics.put("submittedApplications", rs.getLong("submitted"));
                metrics.put("activeWorkflowApplications", rs.getLong("active_workflow"));
                metrics.put("completedApplications", rs.getLong("completed"));
            }
        }
        return metrics;
    }

    @Override
    public Map<String, Long> getConsultantApplicationMetrics(Connection connection, long consultantId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total_apps, "
                + "COUNT(DISTINCT student_id) AS assigned_students, "
                + "SUM(CASE WHEN status IN ('DOCUMENTS_PENDING','DOCUMENTS_REVIEW') THEN 1 ELSE 0 END) AS docs_attention, "
                + "SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS in_progress, "
                + "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed "
                + "FROM applications WHERE assigned_consultant_id = ?";
        Map<String, Long> metrics = new LinkedHashMap<String, Long>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, consultantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    metrics.put("totalApplications", rs.getLong("total_apps"));
                    metrics.put("assignedStudents", rs.getLong("assigned_students"));
                    metrics.put("documentsAttention", rs.getLong("docs_attention"));
                    metrics.put("inProgress", rs.getLong("in_progress"));
                    metrics.put("completed", rs.getLong("completed"));
                }
            }
        }
        return metrics;
    }

    private static void appendSearchAndStatus(StringBuilder sql,
                                              List<Object> params,
                                              String search,
                                              ApplicationStatus statusFilter,
                                              DegreeLevel degreeFilter) {
        if (statusFilter != null) {
            sql.append("AND a.status = ? ");
            params.add(statusFilter.name());
        }
        if (degreeFilter != null) {
            sql.append("AND a.degree_level = ? ");
            params.add(degreeFilter.getDbValue());
        }
        if (search != null && !search.trim().isEmpty()) {
            String escaped = escapeLikePattern(search.trim().toLowerCase(Locale.ROOT));
            String like = "%" + escaped + "%";
            sql.append("AND (LOWER(a.reference_number) LIKE ? ESCAPE '\\\\' "
                    + "OR LOWER(s.full_name) LIKE ? ESCAPE '\\\\' "
                    + "OR LOWER(s.email) LIKE ? ESCAPE '\\\\' "
                    + "OR LOWER(a.preferred_subject) LIKE ? ESCAPE '\\\\' "
                    + "OR LOWER(a.preferred_university) LIKE ? ESCAPE '\\\\') ");
            for (int i = 0; i < 5; i++) {
                params.add(like);
            }
        }
    }

    private List<Application> queryList(Connection connection, String sql, List<Object> params) throws SQLException {
        List<Application> items = new ArrayList<Application>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            bindParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapApplication(rs));
                }
            }
        }
        return items;
    }

    private long queryCount(Connection connection, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            bindParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong("total") : 0L;
            }
        }
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

    private static Application mapApplication(ResultSet rs) throws SQLException {
        Application app = new Application();
        app.setId(rs.getLong("id"));
        app.setReferenceNumber(rs.getString("reference_number"));
        app.setStudentId(rs.getLong("student_id"));
        app.setStudentName(rs.getString("student_name"));
        app.setStudentEmail(rs.getString("student_email"));
        app.setStudentPhoneNumber(rs.getString("student_phone"));
        app.setDegreeLevel(DegreeLevel.fromString(rs.getString("degree_level")));
        app.setPreferredSubject(rs.getString("preferred_subject"));
        app.setIntendedIntake(rs.getString("intended_intake"));
        app.setPreferredUniversity(rs.getString("preferred_university"));
        app.setPreferredLocation(rs.getString("preferred_location"));
        app.setPersonalStatement(rs.getString("personal_statement"));
        app.setStatus(ApplicationStatus.fromString(rs.getString("status")));

        long consultantId = rs.getLong("assigned_consultant_id");
        if (!rs.wasNull()) {
            app.setAssignedConsultantId(consultantId);
            app.setAssignedConsultantName(rs.getString("consultant_name"));
            app.setAssignedConsultantEmail(rs.getString("consultant_email"));
            app.setAssignedConsultantPhone(rs.getString("consultant_phone"));
            app.setAssignedConsultantStatus(rs.getString("consultant_status"));
        }

        app.setDocumentCount(rs.getLong("doc_count"));

        Timestamp submittedTs = rs.getTimestamp("submitted_at");
        if (submittedTs != null) app.setSubmittedAt(submittedTs.toInstant());
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) app.setCreatedAt(createdTs.toInstant());
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) app.setUpdatedAt(updatedTs.toInstant());
        return app;
    }
}
