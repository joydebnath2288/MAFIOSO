package com.mafioso.dao;

import com.mafioso.model.ApplicationDocument;
import com.mafioso.model.DocumentCategory;
import com.mafioso.model.DocumentReviewStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Parameterized JDBC implementation of {@link ApplicationDocumentDAO}.
 */
public class ApplicationDocumentDAOImpl implements ApplicationDocumentDAO {

    private static final String BASE_SELECT =
            "SELECT d.id, d.application_id, a.reference_number AS application_reference, d.student_id, "
                    + "d.uploaded_by_user_id, ub.full_name AS uploaded_by_name, d.category, "
                    + "d.original_filename, d.stored_filename, d.storage_relative_path, d.detected_content_type, "
                    + "d.file_size_bytes, d.sha256_checksum, d.review_status, d.review_note, "
                    + "d.reviewed_by_user_id, rb.full_name AS reviewed_by_name, d.reviewed_at, "
                    + "d.created_at, d.updated_at "
                    + "FROM application_documents d "
                    + "LEFT JOIN applications a ON a.id = d.application_id "
                    + "INNER JOIN users ub ON ub.id = d.uploaded_by_user_id "
                    + "LEFT JOIN users rb ON rb.id = d.reviewed_by_user_id ";

    @Override
    public ApplicationDocument create(Connection connection, ApplicationDocument doc) throws SQLException {
        String sql = "INSERT INTO application_documents ("
                + "application_id, student_id, uploaded_by_user_id, category, original_filename, "
                + "stored_filename, storage_relative_path, detected_content_type, file_size_bytes, "
                + "sha256_checksum, review_status"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (doc.getApplicationId() == null) {
                stmt.setNull(1, Types.BIGINT);
            } else {
                stmt.setLong(1, doc.getApplicationId());
            }
            stmt.setLong(2, doc.getStudentId());
            stmt.setLong(3, doc.getUploadedByUserId());
            stmt.setString(4, doc.getCategory().name());
            stmt.setString(5, doc.getOriginalFilename());
            stmt.setString(6, doc.getStoredFilename());
            stmt.setString(7, doc.getStorageRelativePath());
            stmt.setString(8, doc.getDetectedContentType());
            stmt.setLong(9, doc.getFileSizeBytes());
            stmt.setString(10, doc.getSha256Checksum());
            stmt.setString(11, doc.getReviewStatus().name());

            int affected = stmt.executeUpdate();
            if (affected != 1) {
                throw new SQLException("Expected 1 inserted document row, got " + affected);
            }
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to retrieve generated document ID");
                }
                return findById(connection, keys.getLong(1));
            }
        }
    }

    @Override
    public ApplicationDocument findById(Connection connection, long documentId) throws SQLException {
        String sql = BASE_SELECT + "WHERE d.id = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, documentId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<ApplicationDocument> findByStudentId(Connection connection,
                                                     long studentId,
                                                     DocumentCategory categoryFilter,
                                                     DocumentReviewStatus reviewStatusFilter,
                                                     Long applicationIdFilter) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE d.student_id = ? ");
        List<Object> params = new ArrayList<Object>();
        params.add(Long.valueOf(studentId));
        if (categoryFilter != null) {
            sql.append("AND d.category = ? ");
            params.add(categoryFilter.name());
        }
        if (reviewStatusFilter != null) {
            sql.append("AND d.review_status = ? ");
            params.add(reviewStatusFilter.name());
        }
        if (applicationIdFilter != null) {
            sql.append("AND d.application_id = ? ");
            params.add(applicationIdFilter);
        }
        sql.append("ORDER BY d.created_at DESC, d.id DESC");

        List<ApplicationDocument> items = new ArrayList<ApplicationDocument>();
        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object val = params.get(i);
                if (val instanceof Long) {
                    stmt.setLong(i + 1, (Long) val);
                } else {
                    stmt.setString(i + 1, String.valueOf(val));
                }
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        }
        return items;
    }

    @Override
    public List<ApplicationDocument> findByApplicationId(Connection connection, long applicationId) throws SQLException {
        String sql = BASE_SELECT + "WHERE d.application_id = ? ORDER BY d.created_at DESC, d.id DESC";
        List<ApplicationDocument> items = new ArrayList<ApplicationDocument>();
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
    public boolean updateReviewStatus(Connection connection,
                                      long documentId,
                                      DocumentReviewStatus status,
                                      String note,
                                      long reviewedByUserId) throws SQLException {
        String sql = "UPDATE application_documents SET review_status = ?, review_note = ?, "
                + "reviewed_by_user_id = ?, reviewed_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            if (note == null) {
                stmt.setNull(2, Types.VARCHAR);
            } else {
                stmt.setString(2, note);
            }
            stmt.setLong(3, reviewedByUserId);
            stmt.setLong(4, documentId);
            return stmt.executeUpdate() == 1;
        }
    }

    @Override
    public long countByStudentId(Connection connection, long studentId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM application_documents WHERE student_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong("total") : 0L;
            }
        }
    }

    @Override
    public long countPendingReviewForConsultant(Connection connection, long consultantId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT d.id) AS total "
                + "FROM application_documents d "
                + "INNER JOIN applications a ON (a.id = d.application_id OR (d.application_id IS NULL AND a.student_id = d.student_id)) "
                + "WHERE a.assigned_consultant_id = ? AND d.review_status IN ('PENDING', 'UNDER_REVIEW')";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, consultantId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong("total") : 0L;
            }
        }
    }

    @Override
    public long countPendingReviewAll(Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM application_documents WHERE review_status IN ('PENDING', 'UNDER_REVIEW')";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong("total") : 0L;
        }
    }

    private static ApplicationDocument mapRow(ResultSet rs) throws SQLException {
        ApplicationDocument doc = new ApplicationDocument();
        doc.setId(rs.getLong("id"));
        long appId = rs.getLong("application_id");
        if (!rs.wasNull()) {
            doc.setApplicationId(appId);
        }
        doc.setApplicationReference(rs.getString("application_reference"));
        doc.setStudentId(rs.getLong("student_id"));
        doc.setUploadedByUserId(rs.getLong("uploaded_by_user_id"));
        doc.setUploadedByName(rs.getString("uploaded_by_name"));
        doc.setCategory(DocumentCategory.fromString(rs.getString("category")));
        doc.setOriginalFilename(rs.getString("original_filename"));
        doc.setStoredFilename(rs.getString("stored_filename"));
        doc.setStorageRelativePath(rs.getString("storage_relative_path"));
        doc.setDetectedContentType(rs.getString("detected_content_type"));
        doc.setFileSizeBytes(rs.getLong("file_size_bytes"));
        doc.setSha256Checksum(rs.getString("sha256_checksum"));
        doc.setReviewStatus(DocumentReviewStatus.fromString(rs.getString("review_status")));
        doc.setReviewNote(rs.getString("review_note"));
        long reviewedBy = rs.getLong("reviewed_by_user_id");
        if (!rs.wasNull()) {
            doc.setReviewedByUserId(reviewedBy);
        }
        doc.setReviewedByName(rs.getString("reviewed_by_name"));
        Timestamp reviewedTs = rs.getTimestamp("reviewed_at");
        if (reviewedTs != null) doc.setReviewedAt(reviewedTs.toInstant());
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) doc.setCreatedAt(createdTs.toInstant());
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) doc.setUpdatedAt(updatedTs.toInstant());
        return doc;
    }
}
