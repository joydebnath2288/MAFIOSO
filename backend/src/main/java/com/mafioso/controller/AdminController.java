package com.mafioso.controller;

import com.mafioso.dto.ApiResponse;
import com.mafioso.dto.ConsultantSummaryResponse;
import com.mafioso.dto.CreateConsultantRequest;
import com.mafioso.dto.PagedResponse;
import com.mafioso.dto.UpdateUserStatusRequest;
import com.mafioso.dto.UserResponse;
import com.mafioso.model.AuthenticatedUser;
import com.mafioso.service.AdminUserService;
import com.mafioso.service.AdminUserServiceImpl;
import com.mafioso.service.AuthenticationException;
import com.mafioso.service.DuplicateEmailException;
import com.mafioso.service.ResourceNotFoundException;
import com.mafioso.service.ValidationException;
import com.mafioso.util.JsonUtil;
import com.mafioso.util.SessionUtil;

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

    private final transient AdminUserService adminUserService;

    public AdminController() {
        this(new AdminUserServiceImpl());
    }

    public AdminController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            writeMethodNotAllowed(resp);
            return;
        }
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthenticatedUser principal = requirePrincipal(req, resp);
        if (principal == null) {
            return;
        }

        String subPath = normalizeSubPath(req.getPathInfo());
        try {
            if (subPath.isEmpty() || "/dashboard".equals(subPath)) {
                Map<String, Object> dashboard = adminUserService.getAdminDashboard(principal.getId());
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(dashboard));
                return;
            }

            if ("/students".equals(subPath)) {
                int page = parsePositiveInt(req.getParameter("page"), 1, "page");
                int pageSize = parsePositiveInt(req.getParameter("pageSize"), 10, "pageSize");
                String search = req.getParameter("search");
                String status = req.getParameter("status");
                PagedResponse<UserResponse> result = adminUserService.listStudents(search, status, page, pageSize);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(result));
                return;
            }

            Matcher studentMatcher = STUDENT_ID_PATTERN.matcher(subPath);
            if (studentMatcher.matches()) {
                long studentId = parsePathId(studentMatcher.group(1));
                Map<String, Object> detail = adminUserService.getStudentDetails(studentId);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(detail));
                return;
            }

            if ("/consultants".equals(subPath)) {
                int page = parsePositiveInt(req.getParameter("page"), 1, "page");
                int pageSize = parsePositiveInt(req.getParameter("pageSize"), 10, "pageSize");
                String search = req.getParameter("search");
                String status = req.getParameter("status");
                PagedResponse<ConsultantSummaryResponse> result =
                        adminUserService.listConsultants(search, status, page, pageSize);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(result));
                return;
            }

            Matcher consultantMatcher = CONSULTANT_ID_PATTERN.matcher(subPath);
            if (consultantMatcher.matches()) {
                long consultantId = parsePathId(consultantMatcher.group(1));
                Map<String, Object> detail = adminUserService.getConsultantDetails(consultantId);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(detail));
                return;
            }

            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", "Admin endpoint not found."));
        } catch (ValidationException ex) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.validationError(ex.getMessage(), ex.getFieldErrors()));
        } catch (ResourceNotFoundException ex) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", ex.getMessage()));
        } catch (AuthenticationException ex) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("UNAUTHORIZED", ex.getMessage()));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error handling admin GET request", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to complete administration request."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthenticatedUser principal = requirePrincipal(req, resp);
        if (principal == null) {
            return;
        }

        String subPath = normalizeSubPath(req.getPathInfo());
        if (!"/consultants".equals(subPath)) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", "Admin endpoint not found."));
            return;
        }

        CreateConsultantRequest request;
        try {
            request = JsonUtil.readJsonBody(req, CreateConsultantRequest.class);
        } catch (IllegalArgumentException ex) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("INVALID_JSON", ex.getMessage()));
            return;
        }

        try {
            ConsultantSummaryResponse created = adminUserService.createConsultant(request);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_CREATED,
                    ApiResponse.ok("Consultant account created.", created));
        } catch (ValidationException ex) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.validationError(ex.getMessage(), ex.getFieldErrors()));
        } catch (DuplicateEmailException ex) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_CONFLICT,
                    ApiResponse.error("EMAIL_EXISTS", ex.getMessage()));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error creating consultant account", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to create consultant account."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthenticatedUser principal = requirePrincipal(req, resp);
        if (principal == null) {
            return;
        }

        String subPath = normalizeSubPath(req.getPathInfo());
        Matcher studentStatusMatcher = STUDENT_STATUS_PATTERN.matcher(subPath);
        Matcher consultantStatusMatcher = CONSULTANT_STATUS_PATTERN.matcher(subPath);

        if (!studentStatusMatcher.matches() && !consultantStatusMatcher.matches()) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", "Admin endpoint not found."));
            return;
        }

        UpdateUserStatusRequest body;
        try {
            body = JsonUtil.readJsonBody(req, UpdateUserStatusRequest.class);
        } catch (IllegalArgumentException ex) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("INVALID_JSON", ex.getMessage()));
            return;
        }

        try {
            if (studentStatusMatcher.matches()) {
                long studentId = parsePathId(studentStatusMatcher.group(1));
                UserResponse updated = adminUserService.updateStudentStatus(
                        studentId,
                        body.getStatus(),
                        principal.getId()
                );
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK,
                        ApiResponse.ok("Student account status updated.", updated));
                return;
            }

            long consultantId = parsePathId(consultantStatusMatcher.group(1));
            ConsultantSummaryResponse updated = adminUserService.updateConsultantStatus(
                    consultantId,
                    body.getStatus(),
                    principal.getId()
            );
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok("Consultant account status updated.", updated));
        } catch (ValidationException ex) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.validationError(ex.getMessage(), ex.getFieldErrors()));
        } catch (ResourceNotFoundException ex) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", ex.getMessage()));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error updating account status", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to update account status."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        writeMethodNotAllowed(resp);
    }

    private static AuthenticatedUser requirePrincipal(HttpServletRequest req,
                                                      HttpServletResponse resp) throws IOException {
        AuthenticatedUser principal = SessionUtil.getAuthenticatedUser(req);
        if (principal == null) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("UNAUTHORIZED", "Authentication required."));
            return null;
        }
        return principal;
    }

    private static void writeMethodNotAllowed(HttpServletResponse resp) throws IOException {
        JsonUtil.writeJson(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                ApiResponse.error("METHOD_NOT_ALLOWED", "HTTP method is not allowed for this endpoint."));
    }

    private static String normalizeSubPath(String pathInfo) {
        if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
            return "";
        }
        if (pathInfo.length() > 1 && pathInfo.endsWith("/")) {
            return pathInfo.substring(0, pathInfo.length() - 1);
        }
        return pathInfo;
    }

    private static int parsePositiveInt(String raw, int defaultValue, String fieldName) {
        if (raw == null || raw.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            int val = Integer.parseInt(raw.trim());
            if (val < 1) {
                throw new ValidationException(
                        "Invalid query parameter.",
                        java.util.Collections.singletonMap(fieldName, fieldName + " must be at least 1.")
                );
            }
            return val;
        } catch (NumberFormatException ex) {
            throw new ValidationException(
                    "Invalid query parameter.",
                    java.util.Collections.singletonMap(fieldName, fieldName + " must be a positive integer.")
            );
        }
    }

    private static long parsePathId(String rawId) {
        try {
            long id = Long.parseLong(rawId);
            if (id <= 0L) {
                throw new ResourceNotFoundException("Invalid resource ID.");
            }
            return id;
        } catch (NumberFormatException ex) {
            throw new ResourceNotFoundException("Invalid resource ID.");
        }
    }
}
