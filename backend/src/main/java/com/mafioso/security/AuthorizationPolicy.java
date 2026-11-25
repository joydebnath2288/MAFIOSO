package com.mafioso.security;

import com.mafioso.model.Role;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Centralized server-side authorization rules mapping API path prefixes to permitted roles.
 */
public final class AuthorizationPolicy {

    private static final Map<String, Set<Role>> ROLE_RULES = createRoleRules();

    private AuthorizationPolicy() {
    }

    private static Map<String, Set<Role>> createRoleRules() {
        Map<String, Set<Role>> rules = new LinkedHashMap<String, Set<Role>>();
        rules.put("/api/v1/student", Collections.unmodifiableSet(EnumSet.of(Role.STUDENT)));
        rules.put("/api/v1/consultant", Collections.unmodifiableSet(EnumSet.of(Role.CONSULTANT, Role.ADMIN)));
        rules.put("/api/v1/admin", Collections.unmodifiableSet(EnumSet.of(Role.ADMIN)));
        return Collections.unmodifiableMap(rules);
    }

    /**
     * Checks whether the requested API path and HTTP method is publicly accessible without an authenticated session.
     * Note: public state-changing routes ({@code register}, {@code login}, {@code logout}) remain protected by CSRF.
     */
    public static boolean isPublicEndpoint(String path, String method) {
        if (path == null) {
            return false;
        }
        String normalizedPath = normalizePath(path);
        String upperMethod = method == null ? "" : method.toUpperCase();

        if ("GET".equals(upperMethod) && (
                "/api/v1/health".equals(normalizedPath)
                        || "/api/v1/health/database".equals(normalizedPath)
                        || "/api/v1/auth/csrf".equals(normalizedPath))) {
            return true;
        }

        if ("POST".equals(upperMethod) && (
                "/api/v1/auth/register".equals(normalizedPath)
                        || "/api/v1/auth/login".equals(normalizedPath)
                        || "/api/v1/auth/logout".equals(normalizedPath))) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether the given role is authorized for the requested path.
     * Paths without an explicit role restriction (e.g., {@code /api/v1/auth/me} or public routes)
     * return {@code true} after authentication has already been verified.
     */
    public static boolean isAuthorized(String path, Role role) {
        Set<Role> requiredRoles = getRequiredRoles(path);
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            return true;
        }
        return role != null && requiredRoles.contains(role);
    }

    public static Set<Role> getRequiredRoles(String path) {
        if (path == null) {
            return null;
        }
        String normalizedPath = normalizePath(path);
        for (Map.Entry<String, Set<Role>> entry : ROLE_RULES.entrySet()) {
            String prefix = entry.getKey();
            if (normalizedPath.equals(prefix) || normalizedPath.startsWith(prefix + "/")) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String normalizePath(String path) {
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}
