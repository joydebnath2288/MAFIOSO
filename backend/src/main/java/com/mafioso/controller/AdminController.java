package com.mafioso.controller;

import com.mafioso.dto.ApiResponse;
import com.mafioso.dto.ApplicationDocumentResponse;
import com.mafioso.dto.ApplicationResponse;
import com.mafioso.dto.AssignConsultantRequest;
import com.mafioso.dto.ConsultantSummaryResponse;
import com.mafioso.dto.CreateConsultantRequest;
import com.mafioso.dto.PagedResponse;
import com.mafioso.dto.ReviewDocumentRequest;
import com.mafioso.dto.UpdateApplicationStatusRequest;
import com.mafioso.dto.UpdateUserStatusRequest;
import com.mafioso.dto.UserResponse;
import com.mafioso.model.AuthenticatedUser;
import com.mafioso.service.AdminUserService;
import com.mafioso.service.AdminUserServiceImpl;
import com.mafioso.service.ApplicationService;
import com.mafioso.service.ApplicationServiceImpl;
import com.mafioso.service.AuthenticationException;
import com.mafioso.service.DocumentService;
import com.mafioso.service.DocumentServiceImpl;
import com.mafioso.service.DuplicateEmailException;
import com.mafioso.service.ForbiddenException;
import com.mafioso.service.ResourceNotFoundException;
import com.mafioso.service.ValidationException;
import com.mafioso.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Protected administrative endpoints under {@code /api/v1/admin/*}.
 * Strictly restricted to {@code ADMIN} role by {@link com.mafioso.filter.AuthorizationFilter}.
 */
@WebServlet(name = "AdminController", urlPatterns = {"/api/v1/admin/*"})
public class AdminController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AdminController.class.getName());

    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^/students/(\\d+)$");
    private static final Pattern STUDENT_STATUS_PATTERN = Pattern.compile("^/students/(\\d+)/status$");
    private static final Pattern CONSULTANT_ID_PATTERN = Pattern.compile("^/consultants/(\\d+)$");
    private static final Pattern CONSULTANT_STATUS_PATTERN = Pattern.compile("^/consultants/(\\d+)/status$");
    private static final Pattern APPLICATION_ID_PATTERN = Pattern.compile("^/applications/(\\d+)$");
    private static final Pattern APPLICATION_STATUS_PATTERN = Pattern.compile("^/applications/(\\d+)/status$");
    private static final Pattern APPLICATION_ASSIGN_PATTERN = Pattern.compile("^/applications/(\\d+)/assign-consultant$");
    private static final Pattern DOCUMENT_REVIEW_PATTERN = Pattern.compile("^/documents/(\\d+)/review$");

    private final transient AdminUserService adminUserService;
    private final transient ApplicationService applicationService;
    private final transient DocumentService documentService;

    public AdminController() {
        this(new AdminUserServiceImpl(), new ApplicationServiceImpl(), new DocumentServiceImpl());
    }

    public AdminController(AdminUserService adminUserService) {
        this(adminUserService, new ApplicationServiceImpl(), new DocumentServiceImpl());
    }

    public AdminController(AdminUserService adminUserService,
                           ApplicationService applicationService,
                           DocumentService documentService) {
        this.adminUserService = adminUserService;
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
            if (subPath.isEmpty() || "/dashboard".equals(subPath)) {
                Map<String, Object> dashboard = adminUserService.getAdminDashboard(principal.getId());
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(dashboard));
                return;
            }

            if ("/students".equals(subPath)) {
                int page = ControllerSupport.parsePositiveInt(req.getParameter("page"), 1, "page");
                int pageSize = ControllerSupport.parsePositiveInt(req.getParameter("pageSize"), 10, "pageSize");
                String search = req.getParameter("search");
                String status = req.getParameter("status");
                PagedResponse<UserResponse> result = adminUserService.listStudents(search, status, page, pageSize);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(result));
                return;
            }

            Matcher studentMatcher = STUDENT_ID_PATTERN.matcher(subPath);
            if (studentMatcher.matches()) {
                long studentId = ControllerSupport.parsePathId(studentMatcher.group(1));
                Map<String, Object> detail = adminUserService.getStudentDetails(studentId);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(detail));
                return;
            }

            if ("/consultants".equals(subPath)) {
                int page = ControllerSupport.parsePositiveInt(req.getParameter("page"), 1, "page");
                int pageSize = ControllerSupport.parsePositiveInt(req.getParameter("pageSize"), 10, "pageSize");
                String search = req.getParameter("search");
                String status = req.getParameter("status");
                PagedResponse<ConsultantSummaryResponse> result =
                        adminUserService.listConsultants(search, status, page, pageSize);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(result));
                return;
            }

            Matcher consultantMatcher = CONSULTANT_ID_PATTERN.matcher(subPath);
            if (consultantMatcher.matches()) {
                long consultantId = ControllerSupport.parsePathId(consultantMatcher.group(1));
                Map<String, Object> detail = adminUserService.getConsultantDetails(consultantId);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(detail));
                return;
            }

            if ("/applications".equals(subPath)) {
                int page = ControllerSupport.parsePositiveInt(req.getParameter("page"), 1, "page");
                int pageSize = ControllerSupport.parsePositiveInt(req.getParameter("pageSize"), 10, "pageSize");
                String search = req.getParameter("search");
                String status = req.getParameter("status");
                String degreeLevel = req.getParameter("degreeLevel");
                Long consultantId = ControllerSupport.parseOptionalLongParam(req.getParameter("consultantId"), "consultantId");
                boolean unassignedOnly = "true".equalsIgnoreCase(req.getParameter("unassignedOnly"));
                PagedResponse<ApplicationResponse> result = applicationService.listAdminApplications(
                        search, status, degreeLevel, consultantId, unassignedOnly, page, pageSize
                );
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(result));
                return;
            }

            Matcher appMatcher = APPLICATION_ID_PATTERN.matcher(subPath);
            if (appMatcher.matches()) {
                long applicationId = ControllerSupport.parsePathId(appMatcher.group(1));
                Map<String, Object> detail = applicationService.getAdminApplicationDetail(applicationId);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(detail));
                return;
            }

            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", "Admin endpoint not found."));
        } catch (ValidationException | ForbiddenException | ResourceNotFoundException | AuthenticationException ex) {
            ControllerSupport.writeDomainError(resp, ex);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error handling admin GET request", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to complete administration request."));
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
            if ("/consultants".equals(subPath)) {
                CreateConsultantRequest request = readJsonOrBadRequest(req, resp, CreateConsultantRequest.class);
                if (request == null) {
                    return;
                }
                ConsultantSummaryResponse created = adminUserService.createConsultant(request);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_CREATED,
                        ApiResponse.ok("Consultant account created.", created));
                return;
            }

            Matcher assignMatcher = APPLICATION_ASSIGN_PATTERN.matcher(subPath);
            if (assignMatcher.matches()) {
                long applicationId = ControllerSupport.parsePathId(assignMatcher.group(1));
                AssignConsultantRequest request = readJsonOrBadRequest(req, resp, AssignConsultantRequest.class);
                if (request == null) {
                    return;
                }
                Map<String, Object> result =
                        applicationService.assignConsultant(principal.getId(), applicationId, request);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK,
                        ApiResponse.ok("Consultant assigned to application.", result));
                return;
            }

            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", "Admin endpoint not found."));
        } catch (ValidationException | ForbiddenException | ResourceNotFoundException | AuthenticationException ex) {
            ControllerSupport.writeDomainError(resp, ex);
        } catch (DuplicateEmailException ex) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_CONFLICT,
                    ApiResponse.error("EMAIL_EXISTS", ex.getMessage()));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error handling admin POST request", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to complete administrative action."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthenticatedUser principal = ControllerSupport.requirePrincipal(req, resp);
        if (principal == null) {
            return;
        }

        String subPath = ControllerSupport.normalizeSubPath(req.getPathInfo());
        Matcher studentStatusMatcher = STUDENT_STATUS_PATTERN.matcher(subPath);
        Matcher consultantStatusMatcher = CONSULTANT_STATUS_PATTERN.matcher(subPath);
        Matcher appStatusMatcher = APPLICATION_STATUS_PATTERN.matcher(subPath);
        Matcher docReviewMatcher = DOCUMENT_REVIEW_PATTERN.matcher(subPath);

        if (!studentStatusMatcher.matches()
                && !consultantStatusMatcher.matches()
                && !appStatusMatcher.matches()
                && !docReviewMatcher.matches()) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", "Admin endpoint not found."));
            return;
        }

        try {
            if (studentStatusMatcher.matches()) {
                UpdateUserStatusRequest body = readJsonOrBadRequest(req, resp, UpdateUserStatusRequest.class);
                if (body == null) return;
                long studentId = ControllerSupport.parsePathId(studentStatusMatcher.group(1));
                UserResponse updated = adminUserService.updateStudentStatus(studentId, body.getStatus(), principal.getId());
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK,
                        ApiResponse.ok("Student account status updated.", updated));
                return;
            }

            if (consultantStatusMatcher.matches()) {
                UpdateUserStatusRequest body = readJsonOrBadRequest(req, resp, UpdateUserStatusRequest.class);
                if (body == null) return;
                long consultantId = ControllerSupport.parsePathId(consultantStatusMatcher.group(1));
                ConsultantSummaryResponse updated =
                        adminUserService.updateConsultantStatus(consultantId, body.getStatus(), principal.getId());
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK,
                        ApiResponse.ok("Consultant account status updated.", updated));
                return;
            }

            if (appStatusMatcher.matches()) {
                UpdateApplicationStatusRequest body = readJsonOrBadRequest(req, resp, UpdateApplicationStatusRequest.class);
                if (body == null) return;
                long applicationId = ControllerSupport.parsePathId(appStatusMatcher.group(1));
                Map<String, Object> result =
                        applicationService.updateAdminApplicationStatus(principal.getId(), applicationId, body);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK,
                        ApiResponse.ok("Application status updated.", result));
                return;
            }

            ReviewDocumentRequest body = readJsonOrBadRequest(req, resp, ReviewDocumentRequest.class);
            if (body == null) return;
            long documentId = ControllerSupport.parsePathId(docReviewMatcher.group(1));
            ApplicationDocumentResponse reviewed =
                    documentService.reviewDocumentAsAdmin(principal.getId(), documentId, body);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok("Document review updated.", reviewed));
        } catch (ValidationException | ForbiddenException | ResourceNotFoundException | AuthenticationException ex) {
            ControllerSupport.writeDomainError(resp, ex);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error handling admin PUT request", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to update record."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthenticatedUser principal = ControllerSupport.requirePrincipal(req, resp);
        if (principal == null) {
            return;
        }

        String subPath = ControllerSupport.normalizeSubPath(req.getPathInfo());
        Matcher assignMatcher = APPLICATION_ASSIGN_PATTERN.matcher(subPath);
        if (!assignMatcher.matches()) {
            ControllerSupport.writeMethodNotAllowed(resp);
            return;
        }

        try {
            long applicationId = ControllerSupport.parsePathId(assignMatcher.group(1));
            String note = req.getParameter("note");
            Map<String, Object> result =
                    applicationService.removeConsultantAssignment(principal.getId(), applicationId, note);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok("Consultant assignment removed.", result));
        } catch (ValidationException | ForbiddenException | ResourceNotFoundException | AuthenticationException ex) {
            ControllerSupport.writeDomainError(resp, ex);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error removing consultant assignment", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to remove consultant assignment."));
        }
    }

    private static <T> T readJsonOrBadRequest(HttpServletRequest req,
                                              HttpServletResponse resp,
                                              Class<T> type) throws IOException {
        try {
            return JsonUtil.readJsonBody(req, type);
        } catch (IllegalArgumentException ex) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("INVALID_JSON", ex.getMessage()));
            return null;
        }
    }
}
