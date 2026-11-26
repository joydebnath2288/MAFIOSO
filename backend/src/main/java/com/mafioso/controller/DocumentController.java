package com.mafioso.controller;

import com.mafioso.dto.ApiResponse;
import com.mafioso.model.ApplicationDocument;
import com.mafioso.model.AuthenticatedUser;
import com.mafioso.service.AuthenticationException;
import com.mafioso.service.DocumentService;
import com.mafioso.service.DocumentServiceImpl;
import com.mafioso.service.ForbiddenException;
import com.mafioso.service.ResourceNotFoundException;
import com.mafioso.service.ValidationException;
import com.mafioso.util.DocumentStorageUtil;
import com.mafioso.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authorized streaming document download endpoints under {@code /api/v1/documents/*}.
 */
@WebServlet(name = "DocumentController", urlPatterns = {"/api/v1/documents/*"})
public class DocumentController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DocumentController.class.getName());
    private static final Pattern DOWNLOAD_PATTERN = Pattern.compile("^/(\\d+)/download$");

    private final transient DocumentService documentService;

    public DocumentController() {
        this(new DocumentServiceImpl());
    }

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            ControllerSupport.writeMethodNotAllowed(resp);
            return;
        }
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthenticatedUser principal = ControllerSupport.requirePrincipal(req, resp);
        if (principal == null) {
            return;
        }

        String subPath = ControllerSupport.normalizeSubPath(req.getPathInfo());
        Matcher matcher = DOWNLOAD_PATTERN.matcher(subPath);
        if (!matcher.matches()) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", "Document endpoint not found."));
            return;
        }

        try {
            long documentId = ControllerSupport.parsePathId(matcher.group(1));
            DocumentService.AuthorizedDownload download = documentService.authorizeDownload(principal, documentId);
            ApplicationDocument doc = download.getDocument();

            String safeHeaderFilename = DocumentStorageUtil.sanitizeDownloadHeaderFilename(doc.getOriginalFilename());
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(doc.getDetectedContentType());
            resp.setHeader("Content-Length", Long.toString(doc.getFileSizeBytes()));
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + safeHeaderFilename + "\"");
            resp.setHeader("X-Content-Type-Options", "nosniff");
            resp.setHeader("Cache-Control", "no-store");

            byte[] buffer = new byte[8192];
            try (InputStream in = Files.newInputStream(download.getFilePath());
                 OutputStream out = resp.getOutputStream()) {
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            }
        } catch (ValidationException | ForbiddenException | ResourceNotFoundException | AuthenticationException ex) {
            ControllerSupport.writeDomainError(resp, ex);
        } catch (SecurityException ex) {
            LOGGER.log(Level.WARNING, "Security error resolving document download", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_FORBIDDEN,
                    ApiResponse.error("FORBIDDEN", "Access to requested document path is denied."));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error preparing document download", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to download document at this time."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ControllerSupport.writeMethodNotAllowed(resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ControllerSupport.writeMethodNotAllowed(resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ControllerSupport.writeMethodNotAllowed(resp);
    }
}
