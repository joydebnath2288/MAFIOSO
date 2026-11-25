package com.mafioso.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mafioso.config.AppConfig;
import com.mafioso.dto.ApiResponse;
import com.mafioso.dto.AuthResponse;
import com.mafioso.dto.CsrfResponse;
import com.mafioso.dto.LoginRequest;
import com.mafioso.dto.RegisterRequest;
import com.mafioso.dto.UserResponse;
import com.mafioso.model.AuthenticatedUser;
import com.mafioso.model.User;
import com.mafioso.service.AccountDisabledException;
import com.mafioso.service.AuthService;
import com.mafioso.service.AuthServiceImpl;
import com.mafioso.service.AuthenticationException;
import com.mafioso.service.DuplicateEmailException;
import com.mafioso.service.RateLimitException;
import com.mafioso.service.ValidationException;
import com.mafioso.util.CsrfTokenUtil;
import com.mafioso.util.JsonUtil;
import com.mafioso.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP Servlet controller exposing authentication, session, and CSRF endpoints under {@code /api/v1/auth/*}.
 */
@WebServlet(name = "AuthController", urlPatterns = {"/api/v1/auth/*"})
public class AuthController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());
    private static final int MAX_BODY_BYTES = 65536;

    private final transient AuthService authService;

    public AuthController() {
        this(new AuthServiceImpl());
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
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
        String subPath = normalizeSubPath(req.getPathInfo());
        if ("/csrf".equals(subPath)) {
            handleCsrf(req, resp);
            return;
        }
        if ("/me".equals(subPath)) {
            handleMe(req, resp);
            return;
        }
        if ("/register".equals(subPath) || "/login".equals(subPath) || "/logout".equals(subPath)) {
            writeMethodNotAllowed(resp);
            return;
        }
        JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                ApiResponse.error("NOT_FOUND", "Authentication endpoint not found."));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String subPath = normalizeSubPath(req.getPathInfo());
        if ("/register".equals(subPath)) {
            handleRegister(req, resp);
            return;
        }
        if ("/login".equals(subPath)) {
            handleLogin(req, resp);
            return;
        }
        if ("/logout".equals(subPath)) {
            handleLogout(req, resp);
            return;
        }
        if ("/csrf".equals(subPath) || "/me".equals(subPath)) {
            writeMethodNotAllowed(resp);
            return;
        }
        JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                ApiResponse.error("NOT_FOUND", "Authentication endpoint not found."));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        writeMethodNotAllowed(resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        writeMethodNotAllowed(resp);
    }

    private void handleCsrf(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(true);
        session.setMaxInactiveInterval(AppConfig.getSessionTimeoutSeconds());
        String token = CsrfTokenUtil.getOrCreateToken(session);
        JsonUtil.writeJson(resp, HttpServletResponse.SC_OK,
                ApiResponse.ok(new CsrfResponse(token, CsrfTokenUtil.HEADER_NAME)));
    }

    private void handleMe(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthenticatedUser principal = SessionUtil.getAuthenticatedUser(req);
        if (principal == null) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("UNAUTHORIZED", "Authentication required."));
            return;
        }

        try {
            UserResponse profile = authService.getActiveUserProfile(principal.getId());
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(profile));
        } catch (AuthenticationException ex) {
            SessionUtil.invalidateSession(req, resp);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("UNAUTHORIZED", ex.getMessage()));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve authenticated user profile", ex);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to retrieve user profile."));
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RegisterRequest registerRequest;
        try {
            registerRequest = readJsonBody(req, RegisterRequest.class);
        } catch (IllegalArgumentException ex) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("INVALID_JSON", ex.getMessage()));
            return;
        }

        try {
            UserResponse createdUser = authService.registerStudent(registerRequest);
            HttpSession session = req.getSession(false);
            String freshCsrf = session != null ? CsrfTokenUtil.rotateToken(session) : null;
            AuthResponse payload = new AuthResponse(createdUser, freshCsrf, "/login");
            JsonUtil.writeJson(
                    resp,
                    HttpServletResponse.SC_CREATED,
                    ApiResponse.ok("Student account created. You can now sign in.", payload)
            );
        } catch (ValidationException ex) {
            JsonUtil.writeJson(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.validationError(ex.getMessage(), ex.getFieldErrors())
            );
        } catch (DuplicateEmailException ex) {
            JsonUtil.writeJson(
                    resp,
                    HttpServletResponse.SC_CONFLICT,
                    ApiResponse.error("EMAIL_EXISTS", ex.getMessage())
            );
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error during student registration", ex);
            JsonUtil.writeJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to complete registration at this time.")
            );
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginRequest loginRequest;
        try {
            loginRequest = readJsonBody(req, LoginRequest.class);
        } catch (IllegalArgumentException ex) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error("INVALID_JSON", ex.getMessage()));
            return;
        }

        try {
            String clientIp = req.getRemoteAddr();
            User user = authService.authenticate(loginRequest, clientIp);
            String freshCsrfToken = SessionUtil.establishAuthenticatedSession(req, user);
            String redirectTo = authService.resolveDashboardRoute(user.getRole());

            AuthResponse payload = new AuthResponse(
                    UserResponse.fromUser(user),
                    freshCsrfToken,
                    redirectTo
            );
            JsonUtil.writeJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    ApiResponse.ok("Sign in successful.", payload)
            );
        } catch (ValidationException ex) {
            JsonUtil.writeJson(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.validationError(ex.getMessage(), ex.getFieldErrors())
            );
        } catch (RateLimitException ex) {
            resp.setHeader("Retry-After", Long.toString(ex.getRetryAfterSeconds()));
            JsonUtil.writeJson(
                    resp,
                    429,
                    ApiResponse.error("RATE_LIMITED", ex.getMessage())
            );
        } catch (AuthenticationException ex) {
            JsonUtil.writeJson(
                    resp,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("INVALID_CREDENTIALS", ex.getMessage())
            );
        } catch (AccountDisabledException ex) {
            JsonUtil.writeJson(
                    resp,
                    HttpServletResponse.SC_FORBIDDEN,
                    ApiResponse.error("ACCOUNT_DISABLED", ex.getMessage())
            );
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error during authentication", ex);
            JsonUtil.writeJson(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("INTERNAL_ERROR", "Unable to complete sign in at this time.")
            );
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUtil.invalidateSession(req, resp);
        JsonUtil.writeJson(resp, HttpServletResponse.SC_OK,
                ApiResponse.ok("Signed out successfully.", null));
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

    private static <T> T readJsonBody(HttpServletRequest req, Class<T> type) throws IOException {
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int totalRead = 0;
        try (InputStream in = req.getInputStream()) {
            int n;
            while ((n = in.read(buffer)) != -1) {
                totalRead += n;
                if (totalRead > MAX_BODY_BYTES) {
                    throw new IllegalArgumentException("Request payload is too large.");
                }
                out.write(buffer, 0, n);
            }
        }
        String raw = new String(out.toByteArray(), StandardCharsets.UTF_8).trim();
        if (raw.isEmpty()) {
            throw new IllegalArgumentException("Request body must be valid JSON.");
        }
        try {
            return JsonUtil.getObjectMapper().readValue(raw, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Malformed JSON request body.");
        }
    }
}
