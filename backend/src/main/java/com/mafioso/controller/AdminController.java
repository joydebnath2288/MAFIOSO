package com.mafioso.controller;

import com.mafioso.dao.UserDAO;
import com.mafioso.dao.UserDAOImpl;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Protected administrative endpoints under {@code /api/v1/admin/*}.
 * Strictly restricted to {@code ADMIN} role by {@link com.mafioso.filter.AuthorizationFilter}.
 */
@WebServlet(name = "AdminController", urlPatterns = {"/api/v1/admin/*"})
public class AdminController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AdminController.class.getName());

    private final transient AuthService authService = new AuthServiceImpl();
    private final transient UserDAO userDao = new UserDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String subPath = req.getPathInfo();
        if (subPath != null && !"/".equals(subPath) && !"/dashboard".equals(subPath)) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", "Admin endpoint not found."));
            return;
        }

        AuthenticatedUser principal = SessionUtil.getAuthenticatedUser(req);
        if (principal == null) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("UNAUTHORIZED", "Authentication required."));
            return;
        }

        try {
            UserResponse adminProfile = authService.getActiveUserProfile(principal.getId());
            Map<Role, Long> roleCountsRaw = userDao.countUsersByRole();
            Map<String, Long> roleCounts = new LinkedHashMap<String, Long>();
            for (Map.Entry<Role, Long> entry : roleCountsRaw.entrySet()) {
                roleCounts.put(entry.getKey().name(), entry.getValue());
            }

            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("workspace", "ADMINISTRATION");
            payload.put("account", adminProfile);
            payload.put("roleCounts", roleCounts);
            payload.put("staffProvisioningNotice",
                    "Staff account provisioning UI will be enabled in upcoming milestones.");

            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(payload));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error loading admin workspace", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to load administration workspace."));
        }
    }
}
