package com.mafioso.controller;

import com.mafioso.dto.ApiResponse;
import com.mafioso.dto.ApplicationDocumentResponse;
import com.mafioso.dto.ApplicationResponse;
import com.mafioso.dto.AssignedStudentSummaryResponse;
import com.mafioso.dto.PagedResponse;
import com.mafioso.dto.ReviewDocumentRequest;
import com.mafioso.dto.UpdateApplicationStatusRequest;
import com.mafioso.model.AuthenticatedUser;
import com.mafioso.service.ApplicationService;
import com.mafioso.service.ApplicationServiceImpl;
import com.mafioso.service.AuthenticationException;
import com.mafioso.service.DocumentService;
import com.mafioso.service.DocumentServiceImpl;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Protected staff endpoints under {@code /api/v1/consultant/*}.
 * Consultants only access students and applications actively assigned to them.
 */
@WebServlet(name = "ConsultantController", urlPatterns = {"/api/v1/consultant/*"})
public class ConsultantController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ConsultantController.class.getName());

    private static final Pattern ASSIGNED_STUDENT_ID_PATTERN = Pattern.compile("^/assigned-students/(\\d+)$");
    private static final Pattern APPLICATION_ID_PATTERN = Pattern.compile("^/applications/(\\d+)$");
    private static final Pattern APPLICATION_STATUS_PATTERN = Pattern.compile("^/applications/(\\d+)/status$");
    private static final Pattern DOCUMENT_REVIEW_PATTERN = Pattern.compile("^/documents/(\\d+)/review$");

    private final transient ApplicationService applicationService;
    private final transient DocumentService documentService;

    public ConsultantController() {
        this(new ApplicationServiceImpl(), new DocumentServiceImpl());
    }

    public ConsultantController(ApplicationService applicationService, DocumentService documentService) {
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
                Map<String, Object> dashboard = applicationService.getConsultantDashboard(principal.getId());
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(dashboard));
                return;
            }

            if ("/assigned-students".equals(subPath)) {
                int page = ControllerSupport.parsePositiveInt(req.getParameter("page"), 1, "page");
                int pageSize = ControllerSupport.parsePositiveInt(req.getParameter("pageSize"), 10, "pageSize");
                String search = req.getParameter("search");
                PagedResponse<AssignedStudentSummaryResponse> paged =
                        applicationService.listConsultantAssignedStudents(principal.getId(), search, page, pageSize);

                Map<String, Object> payload = new LinkedHashMap<String, Object>();
                payload.put("assignmentTrackingAvailable", Boolean.TRUE);
                payload.put("items", paged.getItems());
                payload.put("page", Integer.valueOf(paged.getPage()));
                payload.put("pageSize", Integer.valueOf(paged.getPageSize()));
                payload.put("totalItems", Long.valueOf(paged.getTotalItems()));
                payload.put("totalPages", Integer.valueOf(paged.getTotalPages()));
                payload.put("hasNext", Boolean.valueOf(paged.isHasNext()));
                payload.put("hasPrevious", Boolean.valueOf(paged.isHasPrevious()));
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(payload));
                return;
            }

            Matcher studentMatcher = ASSIGNED_STUDENT_ID_PATTERN.matcher(subPath);
            if (studentMatcher.matches()) {
                long studentId = ControllerSupport.parsePathId(studentMatcher.group(1));
                Map<String, Object> detail =
                        applicationService.getConsultantAssignedStudentDetail(principal.getId(), studentId);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(detail));
                return;
            }

            if ("/applications".equals(subPath)) {
                int page = ControllerSupport.parsePositiveInt(req.getParameter("page"), 1, "page");
                int pageSize = ControllerSupport.parsePositiveInt(req.getParameter("pageSize"), 10, "pageSize");
                String search = req.getParameter("search");
                String status = req.getParameter("status");
                PagedResponse<ApplicationResponse> result =
                        applicationService.listConsultantApplications(principal.getId(), search, status, page, pageSize);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(result));
                return;
            }

            Matcher appMatcher = APPLICATION_ID_PATTERN.matcher(subPath);
            if (appMatcher.matches()) {
                long applicationId = ControllerSupport.parsePathId(appMatcher.group(1));
                Map<String, Object> detail =
                        applicationService.getConsultantApplicationDetail(principal.getId(), applicationId);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(detail));
                return;
            }

            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", "Consultant endpoint not found."));
        } catch (ValidationException | ForbiddenException | ResourceNotFoundException | AuthenticationException ex) {
            ControllerSupport.writeDomainError(resp, ex);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error handling consultant GET request", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to load consultant workspace."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthenticatedUser principal = ControllerSupport.requirePrincipal(req, resp);
        if (principal == null) {
            return;
        }

        String subPath = ControllerSupport.normalizeSubPath(req.getPathInfo());
        Matcher statusMatcher = APPLICATION_STATUS_PATTERN.matcher(subPath);
        Matcher docReviewMatcher = DOCUMENT_REVIEW_PATTERN.matcher(subPath);

        if (!statusMatcher.matches() && !docReviewMatcher.matches()) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", "Consultant endpoint not found."));
            return;
        }

        try {
            if (statusMatcher.matches()) {
                long applicationId = ControllerSupport.parsePathId(statusMatcher.group(1));
                UpdateApplicationStatusRequest body;
                try {
                    body = JsonUtil.readJsonBody(req, UpdateApplicationStatusRequest.class);
                } catch (IllegalArgumentException ex) {
                    JsonUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                            ApiResponse.error("INVALID_JSON", ex.getMessage()));
                    return;
                }
                Map<String, Object> result =
                        applicationService.updateConsultantApplicationStatus(principal.getId(), applicationId, body);
                JsonUtil.writeJson(resp, HttpServletResponse.SC_OK,
                        ApiResponse.ok("Application status updated.", result));
                return;
            }

            long documentId = ControllerSupport.parsePathId(docReviewMatcher.group(1));
            ReviewDocumentRequest body;
            try {
                body = JsonUtil.readJsonBody(req, ReviewDocumentRequest.class);
            } catch (IllegalArgumentException ex) {
                JsonUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                        ApiResponse.error("INVALID_JSON", ex.getMessage()));
                return;
            }
            ApplicationDocumentResponse reviewed =
                    documentService.reviewDocumentAsConsultant(principal.getId(), documentId, body);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK,
                    ApiResponse.ok("Document review updated.", reviewed));
        } catch (ValidationException | ForbiddenException | ResourceNotFoundException | AuthenticationException ex) {
            ControllerSupport.writeDomainError(resp, ex);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error handling consultant PUT request", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to update consultant record."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ControllerSupport.writeMethodNotAllowed(resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ControllerSupport.writeMethodNotAllowed(resp);
    }
}
