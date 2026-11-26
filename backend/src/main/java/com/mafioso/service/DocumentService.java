package com.mafioso.service;

import com.mafioso.dto.ApplicationDocumentResponse;
import com.mafioso.dto.ReviewDocumentRequest;
import com.mafioso.model.ApplicationDocument;
import com.mafioso.model.AuthenticatedUser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

/**
 * Service contract for secure document upload, listing, review, and authorized download.
 */
public interface DocumentService {

    ApplicationDocumentResponse uploadStudentDocument(long studentId,
                                                      Long applicationId,
                                                      String rawCategory,
                                                      String originalFilename,
                                                      InputStream contentStream) throws SQLException, IOException;

    List<ApplicationDocumentResponse> listStudentDocuments(long studentId,
                                                           String rawCategory,
                                                           String rawReviewStatus,
                                                           Long applicationId) throws SQLException;

    List<ApplicationDocumentResponse> listApplicationDocumentsForStudent(long studentId,
                                                                         long applicationId) throws SQLException;

    ApplicationDocumentResponse reviewDocumentAsConsultant(long consultantId,
                                                           long documentId,
                                                           ReviewDocumentRequest request) throws SQLException;

    ApplicationDocumentResponse reviewDocumentAsAdmin(long adminId,
                                                      long documentId,
                                                      ReviewDocumentRequest request) throws SQLException;

    AuthorizedDownload authorizeDownload(AuthenticatedUser principal, long documentId) throws SQLException;

    final class AuthorizedDownload {
        private final ApplicationDocument document;
        private final Path filePath;

        public AuthorizedDownload(ApplicationDocument document, Path filePath) {
            this.document = document;
            this.filePath = filePath;
        }

        public ApplicationDocument getDocument() {
            return document;
        }

        public Path getFilePath() {
            return filePath;
        }
    }
}
