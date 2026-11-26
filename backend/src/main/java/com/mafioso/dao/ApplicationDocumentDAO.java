package com.mafioso.dao;

import com.mafioso.model.ApplicationDocument;
import com.mafioso.model.DocumentCategory;
import com.mafioso.model.DocumentReviewStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC persistence contract for {@code application_documents}.
 */
public interface ApplicationDocumentDAO {

    ApplicationDocument create(Connection connection, ApplicationDocument document) throws SQLException;

    ApplicationDocument findById(Connection connection, long documentId) throws SQLException;

    List<ApplicationDocument> findByStudentId(Connection connection,
                                              long studentId,
                                              DocumentCategory categoryFilter,
                                              DocumentReviewStatus reviewStatusFilter,
                                              Long applicationIdFilter) throws SQLException;

    List<ApplicationDocument> findByApplicationId(Connection connection, long applicationId) throws SQLException;

    boolean updateReviewStatus(Connection connection,
                               long documentId,
                               DocumentReviewStatus status,
                               String note,
                               long reviewedByUserId) throws SQLException;

    long countByStudentId(Connection connection, long studentId) throws SQLException;

    long countPendingReviewForConsultant(Connection connection, long consultantId) throws SQLException;

    long countPendingReviewAll(Connection connection) throws SQLException;
}
