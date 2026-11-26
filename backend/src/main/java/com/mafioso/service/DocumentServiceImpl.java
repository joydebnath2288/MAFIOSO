package com.mafioso.service;

import com.mafioso.dao.ApplicationDAO;
import com.mafioso.dao.ApplicationDAOImpl;
import com.mafioso.dao.ApplicationDocumentDAO;
import com.mafioso.dao.ApplicationDocumentDAOImpl;
import com.mafioso.dao.ConsultantAssignmentDAO;
import com.mafioso.dao.ConsultantAssignmentDAOImpl;
import com.mafioso.dto.ApplicationDocumentResponse;
import com.mafioso.dto.ReviewDocumentRequest;
import com.mafioso.model.Application;
import com.mafioso.model.ApplicationDocument;
import com.mafioso.model.AuthenticatedUser;
import com.mafioso.model.DocumentCategory;
import com.mafioso.model.DocumentReviewStatus;
import com.mafioso.model.Role;
import com.mafioso.util.ConsultancyValidationUtil;
import com.mafioso.util.DatabaseUtil;
import com.mafioso.util.DocumentStorageUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Secure document vault service enforcing ownership, consultant assignment boundaries,
 * magic-byte validation, and streaming downloads.
 */
public class DocumentServiceImpl implements DocumentService {

    private final ApplicationDocumentDAO documentDao;
    private final ApplicationDAO applicationDao;
    private final ConsultantAssignmentDAO assignmentDao;
    private final AuthServiceImpl.ConnectionSupplier connectionSupplier;
    private final Path customStorageRoot;
    private final long maxFileBytes;

    public DocumentServiceImpl() {
        this(
                new ApplicationDocumentDAOImpl(),
                new ApplicationDAOImpl(),
                new ConsultantAssignmentDAOImpl(),
                DatabaseUtil::getConnection,
                null,
                com.mafioso.config.AppConfig.getMaxDocumentSizeBytes()
        );
    }

    public DocumentServiceImpl(ApplicationDocumentDAO documentDao,
                               ApplicationDAO applicationDao,
                               ConsultantAssignmentDAO assignmentDao,
                               AuthServiceImpl.ConnectionSupplier connectionSupplier,
                               Path customStorageRoot,
                               long maxFileBytes) {
        this.documentDao = documentDao;
        this.applicationDao = applicationDao;
        this.assignmentDao = assignmentDao;
        this.connectionSupplier = connectionSupplier;
        this.customStorageRoot = customStorageRoot;
        this.maxFileBytes = maxFileBytes;
    }

    @Override
    public ApplicationDocumentResponse uploadStudentDocument(long studentId,
                                                             Long applicationId,
                                                             String rawCategory,
                                                             String originalFilename,
                                                             InputStream contentStream) throws SQLException, IOException {
        DocumentCategory category = parseRequiredCategory(rawCategory);

        if (applicationId != null) {
            try (Connection connection = connectionSupplier.get()) {
                Application app = applicationDao.findById(connection, applicationId);
                if (app == null) {
                    throw new ResourceNotFoundException("Target application not found.");
                }
                if (app.getStudentId() == null || app.getStudentId().longValue() != studentId) {
                    throw new ForbiddenException("You cannot upload documents to another student's application.");
                }
            }
        }

        DocumentStorageUtil.StoredFileResult stored = customStorageRoot != null
                ? DocumentStorageUtil.storeValidatedDocument(studentId, originalFilename, contentStream, customStorageRoot, maxFileBytes)
                : DocumentStorageUtil.storeValidatedDocument(studentId, originalFilename, contentStream);

        ApplicationDocument doc = new ApplicationDocument();
        doc.setApplicationId(applicationId);
        doc.setStudentId(studentId);
        doc.setUploadedByUserId(studentId);
        doc.setCategory(category);
        doc.setOriginalFilename(stored.getSanitizedOriginalFilename());
        doc.setStoredFilename(stored.getStoredFilename());
        doc.setStorageRelativePath(stored.getStorageRelativePath());
        doc.setDetectedContentType(stored.getDetectedContentType());
        doc.setFileSizeBytes(stored.getFileSizeBytes());
        doc.setSha256Checksum(stored.getSha256Checksum());
        doc.setReviewStatus(DocumentReviewStatus.PENDING);

        try (Connection connection = connectionSupplier.get()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                if (applicationId != null) {
                    Application app = applicationDao.findById(connection, applicationId);
                    if (app == null) {
                        throw new ResourceNotFoundException("Target application not found.");
                    }
                    if (app.getStudentId() == null || app.getStudentId().longValue() != studentId) {
                        throw new ForbiddenException("You cannot upload documents to another student's application.");
                    }
                }
                ApplicationDocument created = documentDao.create(connection, doc);
                connection.commit();
                return ApplicationDocumentResponse.from(created);
            } catch (SQLException ex) {
                rollbackQuietly(connection);
                DocumentStorageUtil.deleteQuietly(stored.getAbsolutePath());
                throw ex;
            } catch (RuntimeException ex) {
                rollbackQuietly(connection);
                DocumentStorageUtil.deleteQuietly(stored.getAbsolutePath());
                throw ex;
            } finally {
                restoreAutoCommitQuietly(connection, originalAutoCommit);
            }
        }
    }

    @Override
    public List<ApplicationDocumentResponse> listStudentDocuments(long studentId,
                                                                  String rawCategory,
                                                                  String rawReviewStatus,
                                                                  Long applicationId) throws SQLException {
        DocumentCategory category = parseOptionalCategory(rawCategory);
        DocumentReviewStatus reviewStatus = parseOptionalReviewStatus(rawReviewStatus);

        try (Connection connection = connectionSupplier.get()) {
            if (applicationId != null) {
                Application app = applicationDao.findById(connection, applicationId);
                if (app == null) {
                    throw new ResourceNotFoundException("Application not found.");
                }
                if (app.getStudentId() == null || app.getStudentId().longValue() != studentId) {
                    throw new ForbiddenException("You do not have permission to view documents for this application.");
                }
            }
            List<ApplicationDocument> docs = documentDao.findByStudentId(connection, studentId, category, reviewStatus, applicationId);
            return toResponses(docs);
        }
    }

    @Override
    public List<ApplicationDocumentResponse> listApplicationDocumentsForStudent(long studentId, long applicationId) throws SQLException {
        try (Connection connection = connectionSupplier.get()) {
            Application app = applicationDao.findById(connection, applicationId);
            if (app == null) {
                throw new ResourceNotFoundException("Application not found.");
            }
            if (app.getStudentId() == null || app.getStudentId().longValue() != studentId) {
                throw new ForbiddenException("You do not have permission to view documents for this application.");
            }
            return toResponses(documentDao.findByApplicationId(connection, applicationId));
        }
    }

    @Override
    public ApplicationDocumentResponse reviewDocumentAsConsultant(long consultantId,
                                                                  long documentId,
                                                                  ReviewDocumentRequest request) throws SQLException {
        return reviewDocument(consultantId, Role.CONSULTANT, documentId, request);
    }

    @Override
    public ApplicationDocumentResponse reviewDocumentAsAdmin(long adminId,
                                                             long documentId,
                                                             ReviewDocumentRequest request) throws SQLException {
        return reviewDocument(adminId, Role.ADMIN, documentId, request);
    }

    private ApplicationDocumentResponse reviewDocument(long actorId,
                                                       Role actorRole,
                                                       long documentId,
                                                       ReviewDocumentRequest request) throws SQLException {
        Map<String, String> fieldErrors = ConsultancyValidationUtil.validateDocumentReview(request);
        if (!fieldErrors.isEmpty()) {
            throw new ValidationException("Please correct the highlighted review fields.", fieldErrors);
        }

        DocumentReviewStatus targetStatus = DocumentReviewStatus.fromString(request.getReviewStatus());
        String cleanNote = ConsultancyValidationUtil.normalizeText(request.getReviewNote());

        try (Connection connection = connectionSupplier.get()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                ApplicationDocument existing = documentDao.findById(connection, documentId);
                if (existing == null) {
                    throw new ResourceNotFoundException("Document not found.");
                }
                if (actorRole == Role.CONSULTANT) {
                    verifyConsultantDocumentAccess(connection, actorId, existing);
                }
                documentDao.updateReviewStatus(connection, documentId, targetStatus, cleanNote, actorId);
                ApplicationDocument updated = documentDao.findById(connection, documentId);
                connection.commit();
                return ApplicationDocumentResponse.from(updated);
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

    @Override
    public AuthorizedDownload authorizeDownload(AuthenticatedUser principal, long documentId) throws SQLException {
        if (principal == null) {
            throw new AuthenticationException("Authentication required.");
        }
        try (Connection connection = connectionSupplier.get()) {
            ApplicationDocument doc = documentDao.findById(connection, documentId);
            if (doc == null) {
                throw new ResourceNotFoundException("Document not found.");
            }
            if (principal.getRole() == Role.STUDENT) {
                if (doc.getStudentId() == null || doc.getStudentId().longValue() != principal.getId()) {
                    throw new ForbiddenException("You do not have permission to download another student's document.");
                }
            } else if (principal.getRole() == Role.CONSULTANT) {
                verifyConsultantDocumentAccess(connection, principal.getId(), doc);
            } else if (principal.getRole() != Role.ADMIN) {
                throw new ForbiddenException("You do not have permission to download this document.");
            }

            Path resolvedPath = customStorageRoot != null
                    ? DocumentStorageUtil.resolveExistingDocumentPath(doc.getStorageRelativePath(), customStorageRoot)
                    : DocumentStorageUtil.resolveExistingDocumentPath(doc.getStorageRelativePath());

            if (!Files.exists(resolvedPath) || !Files.isRegularFile(resolvedPath)) {
                throw new ResourceNotFoundException("Stored document file could not be located.");
            }
            return new AuthorizedDownload(doc, resolvedPath);
        }
    }

    private void verifyConsultantDocumentAccess(Connection connection, long consultantId, ApplicationDocument doc) throws SQLException {
        if (doc.getApplicationId() != null) {
            Application app = applicationDao.findById(connection, doc.getApplicationId());
            if (app != null && app.getAssignedConsultantId() != null && app.getAssignedConsultantId().longValue() == consultantId) {
                return;
            }
            throw new ForbiddenException("You do not have permission to access documents for an unassigned application.");
        }
        if (!assignmentDao.isConsultantAssignedToStudent(connection, consultantId, doc.getStudentId())) {
            throw new ForbiddenException("You do not have permission to access documents for an unassigned student.");
        }
    }

    private static List<ApplicationDocumentResponse> toResponses(List<ApplicationDocument> docs) {
        List<ApplicationDocumentResponse> out = new ArrayList<ApplicationDocumentResponse>(docs.size());
        for (ApplicationDocument doc : docs) {
            out.add(ApplicationDocumentResponse.from(doc));
        }
        return out;
    }

    private static DocumentCategory parseRequiredCategory(String rawCategory) {
        String clean = ConsultancyValidationUtil.normalizeText(rawCategory);
        if (clean == null) {
            throw new ValidationException("Document category is required.",
                    Collections.singletonMap("category", "Please select a document category."));
        }
        try {
            return DocumentCategory.fromString(clean);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Invalid document category.",
                    Collections.singletonMap("category", "Unsupported document category."));
        }
    }

    private static DocumentCategory parseOptionalCategory(String rawCategory) {
        String clean = ConsultancyValidationUtil.normalizeText(rawCategory);
        if (clean == null || "ALL".equalsIgnoreCase(clean)) {
            return null;
        }
        try {
            return DocumentCategory.fromString(clean);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Invalid category filter.",
                    Collections.singletonMap("category", "Unsupported document category filter."));
        }
    }

    private static DocumentReviewStatus parseOptionalReviewStatus(String rawStatus) {
        String clean = ConsultancyValidationUtil.normalizeText(rawStatus);
        if (clean == null || "ALL".equalsIgnoreCase(clean)) {
            return null;
        }
        try {
            return DocumentReviewStatus.fromString(clean);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Invalid review status filter.",
                    Collections.singletonMap("reviewStatus", "Unsupported review status filter."));
        }
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
