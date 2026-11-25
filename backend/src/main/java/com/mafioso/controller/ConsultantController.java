package com.mafioso.controller;

import com.mafioso.dto.ApiResponse;
import com.mafioso.dto.UserResponse;
import com.mafioso.model.AuthenticatedUser;
import com.mafioso.model.Role;
import com.mafioso.service.AuthService;
import com.mafioso.service.AuthServiceImpl;
import com.mafioso.util.JsonUtil;
import com.mafioso.util.SessionUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Protected staff endpoints under {@code /api/v1/consultant/*}.
 * Accessible by {@code CONSULTANT} and explicitly authorized {@code ADMIN} accounts.
 */
@WebServlet(name = "ConsultantController", urlPatterns = {"/api/v1/consultant/*"})
public class ConsultantController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ConsultantController.class.getName());

    private final transient AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String subPath = req.getPathInfo();
        if (subPath != null && !"/".equals(subPath) && !"/dashboard".equals(subPath)) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", "Consultant endpoint not found."));
            return;
        }

        AuthenticatedUser principal = SessionUtil.getAuthenticatedUser(req);
        if (principal == null) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("UNAUTHORIZED", "Authentication required."));
            return;
        }

        try {
            UserResponse staffProfile = authService.getActiveUserProfile(principal.getId());
            boolean isAdmin = principal.getRole() == Role.ADMIN;

            Map<String, Object> permissions = new LinkedHashMap<String, Object>();
            permissions.put("canAccessConsultantWorkspace", Boolean.TRUE);
            permissions.put("canAccessAdminWorkspace", Boolean.valueOf(isAdmin));
            permissions.put("canManageStaffInFutureMilestones", Boolean.valueOf(isAdmin));

            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("workspace", "STAFF_OPERATIONS");
            payload.put("account", staffProfile);
            payload.put("permissions", permissions);
            payload.put("authorizedCapabilities", Arrays.asList(
                    "Applicant Dossier Review (Planned)",
                    "UK University Shortlist Guidance (Planned)",
                    "Student Live Advisory Queue (Planned)"
            ));

            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(payload));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error loading consultant workspace", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to load staff workspace."));
        }
    }
}
