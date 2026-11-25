package com.mafioso.security;

import com.mafioso.model.Role;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for centralized {@link AuthorizationPolicy} rules.
 */
public class AuthorizationPolicyTest {

    @Test
    public void identifiesPublicEndpointsCorrectly() {
        assertTrue(AuthorizationPolicy.isPublicEndpoint("/api/v1/health", "GET"));
        assertTrue(AuthorizationPolicy.isPublicEndpoint("/api/v1/health/database", "GET"));
        assertTrue(AuthorizationPolicy.isPublicEndpoint("/api/v1/auth/csrf", "GET"));
        assertTrue(AuthorizationPolicy.isPublicEndpoint("/api/v1/auth/register", "POST"));
        assertTrue(AuthorizationPolicy.isPublicEndpoint("/api/v1/auth/login", "POST"));
        assertTrue(AuthorizationPolicy.isPublicEndpoint("/api/v1/auth/logout", "POST"));

        assertFalse(AuthorizationPolicy.isPublicEndpoint("/api/v1/auth/me", "GET"));
        assertFalse(AuthorizationPolicy.isPublicEndpoint("/api/v1/student/dashboard", "GET"));
        assertFalse(AuthorizationPolicy.isPublicEndpoint("/api/v1/consultant/dashboard", "GET"));
        assertFalse(AuthorizationPolicy.isPublicEndpoint("/api/v1/admin/dashboard", "GET"));
    }

    @Test
    public void enforcesRoleHierarchyBoundaries() {
        assertTrue(AuthorizationPolicy.isAuthorized("/api/v1/student/dashboard", Role.STUDENT));
        assertFalse(AuthorizationPolicy.isAuthorized("/api/v1/student/dashboard", Role.CONSULTANT));
        assertFalse(AuthorizationPolicy.isAuthorized("/api/v1/student/dashboard", Role.ADMIN));

        assertFalse(AuthorizationPolicy.isAuthorized("/api/v1/consultant/dashboard", Role.STUDENT));
        assertTrue(AuthorizationPolicy.isAuthorized("/api/v1/consultant/dashboard", Role.CONSULTANT));
        assertTrue(AuthorizationPolicy.isAuthorized("/api/v1/consultant/dashboard", Role.ADMIN));

        assertFalse(AuthorizationPolicy.isAuthorized("/api/v1/admin/dashboard", Role.STUDENT));
        assertFalse(AuthorizationPolicy.isAuthorized("/api/v1/admin/dashboard", Role.CONSULTANT));
        assertTrue(AuthorizationPolicy.isAuthorized("/api/v1/admin/dashboard", Role.ADMIN));
    }
}
