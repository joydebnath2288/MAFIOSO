package com.mafioso.controller;

import com.mafioso.dto.ApiResponse;
import com.mafioso.dto.UserResponse;
import com.mafioso.model.AuthenticatedUser;
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
 * Protected student endpoints under {@code /api/v1/student/*}.
 * Enforces self-only access scoped strictly to the session user ID.
 */
@WebServlet(name = "StudentController", urlPatterns = {"/api/v1/student/*"})
public class StudentController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(StudentController.class.getName());

    private final transient AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String subPath = req.getPathInfo();
        boolean isDashboard = subPath == null || "/".equals(subPath) || "/dashboard".equals(subPath) || "/me".equals(subPath);
        boolean isProfile = "/profile".equals(subPath);

        if (!isDashboard && !isProfile) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", "Student endpoint not found."));
            return;
        }

        AuthenticatedUser principal = SessionUtil.getAuthenticatedUser(req);
        if (principal == null) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("UNAUTHORIZED", "Authentication required."));
            return;
        }

        try {
            UserResponse selfProfile = authService.getActiveUserProfile(principal.getId());
            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("workspace", isProfile ? "STUDENT_PROFILE" : "STUDENT_PORTAL");
            payload.put("account", selfProfile);
            payload.put("accessScope", "SELF_ONLY");
            if (isProfile) {
                payload.put("academicProfileAvailable", Boolean.FALSE);
                payload.put("academicProfileNote",
                        "Academic credential uploads, IELTS scores, and UK university shortlisting will be enabled in upcoming milestones.");
            } else {
                payload.put("plannedModules", Arrays.asList(
                        "Academic Profile & Transcript Review",
                        "UK University & Scholarship Shortlist",
                        "Private Document Vault",
                        "Consultancy Fee & CAS Readiness"
                ));
            }
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(payload));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error loading student workspace", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to load student workspace."));
        }
    }
}
