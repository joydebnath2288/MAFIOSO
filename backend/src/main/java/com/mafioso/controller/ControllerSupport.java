package com.mafioso.controller;

import com.mafioso.dto.ApiResponse;
import com.mafioso.model.AuthenticatedUser;
import com.mafioso.service.AuthenticationException;
import com.mafioso.service.ForbiddenException;
import com.mafioso.service.ResourceNotFoundException;
import com.mafioso.service.ValidationException;
import com.mafioso.util.JsonUtil;
import com.mafioso.util.SessionUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Shared HTTP servlet request/response helpers across MAFIOSO API controllers.
 */
final class ControllerSupport {

    private ControllerSupport() {
    }

    static AuthenticatedUser requirePrincipal(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthenticatedUser principal = SessionUtil.getAuthenticatedUser(req);
        if (principal == null) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("UNAUTHORIZED", "Authentication required."));
            return null;
        }
        return principal;
    }

    static void writeMethodNotAllowed(HttpServletResponse resp) throws IOException {
        JsonUtil.writeJson(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                ApiResponse.error("METHOD_NOT_ALLOWED", "HTTP method is not allowed for this endpoint."));
    }

    static String normalizeSubPath(String pathInfo) {
        if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
            return "";
        }
        if (pathInfo.length() > 1 && pathInfo.endsWith("/")) {
            return pathInfo.substring(0, pathInfo.length() - 1);
        }
        return pathInfo;
    }

    static int parsePositiveInt(String raw, int defaultValue, String fieldName) {
        if (raw == null || raw.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            int val = Integer.parseInt(raw.trim());
            if (val < 1) {
                throw new ValidationException(
                        "Invalid query parameter.",
                        Collections.singletonMap(fieldName, fieldName + " must be at least 1.")
                );
            }
            return val;
        } catch (NumberFormatException ex) {
            throw new ValidationException(
                    "Invalid query parameter.",
                    Collections.singletonMap(fieldName, fieldName + " must be a positive integer.")
            );
        }
    }

    static Long parseOptionalLongParam(String raw, String fieldName) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            long val = Long.parseLong(raw.trim());
            if (val <= 0L) {
                throw new ValidationException(
                        "Invalid query parameter.",
                        Collections.singletonMap(fieldName, fieldName + " must be positive.")
                );
            }
            return Long.valueOf(val);
        } catch (NumberFormatException ex) {
            throw new ValidationException(
                    "Invalid query parameter.",
                    Collections.singletonMap(fieldName, fieldName + " must be a valid integer.")
            );
        }
    }

    static long parsePathId(String rawId) {
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

    static String readPartAsString(Part part, int maxBytes) throws IOException {
        if (part == null) {
            return null;
        }
        byte[] buffer = new byte[512];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int total = 0;
        try (InputStream in = part.getInputStream()) {
            int n;
            while ((n = in.read(buffer)) != -1) {
                total += n;
                if (total > maxBytes) {
                    throw new ValidationException("Form field exceeds maximum length.",
                            Collections.singletonMap("request", "Form parameter is too large."));
                }
                out.write(buffer, 0, n);
            }
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    static void writeDomainError(HttpServletResponse resp, RuntimeException ex) throws IOException {
        if (ex instanceof ValidationException) {
            ValidationException vex = (ValidationException) ex;
            JsonUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.validationError(vex.getMessage(), vex.getFieldErrors()));
        } else if (ex instanceof ForbiddenException) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_FORBIDDEN,
                    ApiResponse.error("FORBIDDEN", ex.getMessage()));
        } else if (ex instanceof ResourceNotFoundException) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_NOT_FOUND,
                    ApiResponse.error("NOT_FOUND", ex.getMessage()));
        } else if (ex instanceof AuthenticationException) {
            JsonUtil.writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("UNAUTHORIZED", ex.getMessage()));
        } else {
            throw ex;
        }
    }
}
