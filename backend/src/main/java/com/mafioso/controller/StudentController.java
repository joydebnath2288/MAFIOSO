package com.mafioso.controller;

import com.mafioso.dto.ApiResponse;
import com.mafioso.dto.ApplicationDocumentResponse;
import com.mafioso.dto.ApplicationResponse;
import com.mafioso.dto.CreateApplicationRequest;
import com.mafioso.dto.PagedResponse;
import com.mafioso.dto.StudentProfileRequest;
import com.mafioso.dto.StudentProfileResponse;
import com.mafioso.dto.UserResponse;
import com.mafioso.model.AuthenticatedUser;
import com.mafioso.service.ApplicationService;
import com.mafioso.service.ApplicationServiceImpl;
import com.mafioso.service.AuthService;
import com.mafioso.service.AuthServiceImpl;
import com.mafioso.service.AuthenticationException;
import com.mafioso.service.DocumentService;
import com.mafioso.service.DocumentServiceImpl;
import com.mafioso.service.ForbiddenException;
import com.mafioso.service.ResourceNotFoundException;
import com.mafioso.service.StudentProfileService;
import com.mafioso.service.StudentProfileServiceImpl;
import com.mafioso.service.ValidationException;
import com.mafioso.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Protected student endpoints under {@code /api/v1/student/*}.
 * Enforces self-only access scoped strictly to the authenticated session user ID.
 */
@WebServlet(name = "StudentController", urlPatterns = {"/api/v1/student/*"})
@MultipartConfig(fileSizeThreshold = 65536, maxFileSize = 15728640L, maxRequestSize = 16777216L)
public class StudentController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(StudentController.class.getName());

    private static final Pattern APPLICATION_ID_PATTERN = Pattern.compile("^/applications/(\\d+)$");
    private static final Pattern APPLICATION_DOCS_PATTERN = Pattern.compile("^/applications/(\\d+)/documents$");

    private final transient AuthService authService;
    private final transient StudentProfileService profileService;
    private final transient ApplicationService applicationService;
    private final transient DocumentService documentService;

    public StudentController() {
        this(
                new AuthServiceImpl(),
                new StudentProfileServiceImpl(),
                new ApplicationServiceImpl(),
                new DocumentServiceImpl()
        );
    }

    public StudentController(AuthService authService,
                             StudentProfileService profileService,
                             ApplicationService applicationService,
                             DocumentService documentService) {
        this.authService = authService;
        this.profileService = profileService;
        this.applicationService = applicationService;
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
        try {
            if (subPath.isEmpty() || "/dashboard".equals(subPath) || "/me".equals(subPath)) {
                Map<String, Object> summary = applicationService.getStudentDashboardSummary(principal.getId());
                Map<String, Object> payload = new LinkedHashMap<String, Object>(summary);
                payload.put("workspace", "STUDENT_PORTAL");
                payload.put("accessScope", "SELF_ONLY");
                payload.put("academicProfileAvailable", Boolean.TRUE);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(payload));
                return;
            }

            if ("/profile".equals(subPath)) {
                UserResponse account = authService.getActiveUserProfile(principal.getId());
                StudentProfileResponse profile = profileService.getOwnProfile(principal.getId());
                Map<String, Object> payload = new LinkedHashMap<String, Object>();
                payload.put("workspace", "STUDENT_PROFILE");
                payload.put("account", account);
                payload.put("profile", profile);
                payload.put("accessScope", "SELF_ONLY");
                payload.put("academicProfileAvailable", Boolean.TRUE);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(payload));
                return;
            }

            if ("/applications".equals(subPath)) {
                int page = ControllerSupport.parsePositiveInt(req.getParameter("page"), 1, "page");
                int pageSize = ControllerSupport.parsePositiveInt(req.getParameter("pageSize"), 10, "pageSize");
                String status = req.getParameter("status");
                PagedResponse<ApplicationResponse> result =
                        applicationService.listStudentApplications(principal.getId(), status, page, pageSize);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(result));
                return;
            }

            Matcher appMatcher = APPLICATION_ID_PATTERN.matcher(subPath);
            if (appMatcher.matches()) {
                long applicationId = ControllerSupport.parsePathId(appMatcher.group(1));
                Map<String, Object> detail =
                        applicationService.getStudentApplicationDetail(principal.getId(), applicationId);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(detail));
                return;
            }

            Matcher appDocsMatcher = APPLICATION_DOCS_PATTERN.matcher(subPath);
            if (appDocsMatcher.matches()) {
                long applicationId = ControllerSupport.parsePathId(appDocsMatcher.group(1));
                List<ApplicationDocumentResponse> docs =
                        documentService.listApplicationDocumentsForStudent(principal.getId(), applicationId);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(Collections.singletonMap("items", docs)));
                return;
            }

            if ("/documents".equals(subPath)) {
                String category = req.getParameter("category");
                String reviewStatus = req.getParameter("reviewStatus");
                Long applicationId = ControllerSupport.parseOptionalLongParam(req.getParameter("applicationId"), "applicationId");
                List<ApplicationDocumentResponse> docs =
                        documentService.listStudentDocuments(principal.getId(), category, reviewStatus, applicationId);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(Collections.singletonMap("items", docs)));
                return;
            }

            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", "Student endpoint not found."));
        } catch (ValidationException | ForbiddenException | ResourceNotFoundException | AuthenticationException ex) {
            ControllerSupport.writeDomainError(resp, ex);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error handling student GET request", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to load student workspace."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthenticatedUser principal = ControllerSupport.requirePrincipal(req, resp);
        if (principal == null) {
            return;
        }

        String subPath = ControllerSupport.normalizeSubPath(req.getPathInfo());
        try {
            if ("/applications".equals(subPath)) {
                CreateApplicationRequest request;
                try {
                    request = JsonUtil.readJsonBody(req, CreateApplicationRequest.class);
                } catch (IllegalArgumentException ex) {
                    JsonUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                            ApiResponse.error("INVALID_JSON", ex.getMessage()));
                    return;
                }
                ApplicationResponse created = applicationService.createApplication(principal.getId(), request);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_CREATED,
                        ApiResponse.ok("Application submitted successfully.", created));
                return;
            }

            Matcher appDocsMatcher = APPLICATION_DOCS_PATTERN.matcher(subPath);
            if (appDocsMatcher.matches()) {
                long applicationId = ControllerSupport.parsePathId(appDocsMatcher.group(1));
                handleMultipartUpload(req, resp, principal.getId(), Long.valueOf(applicationId));
                return;
            }

            if ("/documents".equals(subPath)) {
                handleMultipartUpload(req, resp, principal.getId(), null);
                return;
            }

            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", "Student endpoint not found."));
        } catch (ValidationException | ForbiddenException | ResourceNotFoundException | AuthenticationException ex) {
            ControllerSupport.writeDomainError(resp, ex);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error handling student POST request", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to complete student request."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthenticatedUser principal = ControllerSupport.requirePrincipal(req, resp);
        if (principal == null) {
            return;
        }

        String subPath = ControllerSupport.normalizeSubPath(req.getPathInfo());
        if (!"/profile".equals(subPath)) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", "Student endpoint not found."));
            return;
        }

        StudentProfileRequest request;
        try {
            request = JsonUtil.readJsonBody(req, StudentProfileRequest.class);
        } catch (IllegalArgumentException ex) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("INVALID_JSON", ex.getMessage()));
            return;
        }

        try {
            StudentProfileResponse updated = profileService.updateOwnProfile(principal.getId(), request);
            UserResponse account = authService.getActiveUserProfile(principal.getId());
            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("account", account);
            payload.put("profile", updated);
            payload.put("academicProfileAvailable", Boolean.TRUE);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok("Student profile updated.", payload));
        } catch (ValidationException | ForbiddenException | ResourceNotFoundException | AuthenticationException ex) {
            ControllerSupport.writeDomainError(resp, ex);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error updating student profile", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to update student profile."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ControllerSupport.writeMethodNotAllowed(resp);
    }

    private void handleMultipartUpload(HttpServletRequest req,
                                       HttpServletResponse resp,
                                       long studentId,
                                       Long fixedApplicationId) throws IOException, SQLException {
        String contentType = req.getContentType();
        if (contentType == null || !contentType.toLowerCase(java.util.Locale.ROOT).startsWith("multipart/form-data")) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("INVALID_CONTENT_TYPE", "Document upload requires multipart/form-data."));
            return;
        }

        Part filePart;
        String category;
        Long applicationId = fixedApplicationId;
        try {
            filePart = req.getPart("file");
            category = ControllerSupport.readPartAsString(req.getPart("category"), 128);
            if (category == null) {
                category = req.getParameter("category");
            }
            if (applicationId == null) {
                String appIdStr = ControllerSupport.readPartAsString(req.getPart("applicationId"), 32);
                if (appIdStr == null) {
                    appIdStr = req.getParameter("applicationId");
                }
                applicationId = ControllerSupport.parseOptionalLongParam(appIdStr, "applicationId");
            }
        } catch (IllegalStateException ex) {
            throw new ValidationException("Uploaded file exceeds the maximum allowed request size.",
                    Collections.singletonMap("file", "Uploaded file exceeds size limit."));
        } catch (ServletException ex) {
            throw new ValidationException("Invalid multipart upload payload.",
                    Collections.singletonMap("file", "Unable to parse uploaded file."));
        }

        if (filePart == null || filePart.getSize() <= 0L) {
            throw new ValidationException("Document file is required.",
                    Collections.singletonMap("file", "Please select a document file to upload."));
        }

        String submittedName = filePart.getSubmittedFileName();
        try (InputStream stream = filePart.getInputStream()) {
            ApplicationDocumentResponse saved = documentService.uploadStudentDocument(
                    studentId,
                    applicationId,
                    category,
                    submittedName,
                    stream
            );
            JsonUtil.writeJson(resp, HttpServletResponse.SC_CREATED,
                    ApiResponse.ok("Document uploaded successfully.", saved));
        }
    }
}
