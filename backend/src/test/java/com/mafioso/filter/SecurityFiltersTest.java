package com.mafioso.filter;

import com.mafioso.dao.UserDAO;
import com.mafioso.model.AccountStatus;
import com.mafioso.model.AuthenticatedUser;
import com.mafioso.model.Role;
import com.mafioso.model.User;
import com.mafioso.util.CsrfTokenUtil;
import com.mafioso.util.SessionUtil;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link AuthenticationFilter}, {@link CsrfFilter}, and {@link AuthorizationFilter}.
 */
public class SecurityFiltersTest {

    @Test
    public void csrfFilterRejectsMissingOrInvalidTokenOnPostAndAllowsValidToken() throws Exception {
        CsrfFilter filter = new CsrfFilter();
        MockSession session = new MockSession();
        String token = CsrfTokenUtil.rotateToken(session.asHttpSession());

        // Missing token -> 403
        MockResponse missingResp = new MockResponse();
        AtomicBoolean passed = new AtomicBoolean(false);
        FilterChain chain = (req, resp) -> passed.set(true);

        filter.doFilter(mockRequest("POST", "/api/v1/auth/login", session, null), missingResp.asHttpResponse(), chain);
        assertFalse(passed.get());
        assertEquals(403, missingResp.status.get());
        assertTrue(missingResp.body.toString().contains("CSRF_INVALID"));

        // Invalid token -> 403
        MockResponse wrongResp = new MockResponse();
        filter.doFilter(mockRequest("POST", "/api/v1/auth/login", session, "wrong-token"), wrongResp.asHttpResponse(), chain);
        assertFalse(passed.get());
        assertEquals(403, wrongResp.status.get());

        // Valid token -> passes
        MockResponse okResp = new MockResponse();
        filter.doFilter(mockRequest("POST", "/api/v1/auth/login", session, token), okResp.asHttpResponse(), chain);
        assertTrue(passed.get());
    }

    @Test
    public void authenticationFilterReturns401ForUnauthenticatedProtectedRoute() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter(new StubUserDao(null));
        MockResponse resp = new MockResponse();
        AtomicBoolean passed = new AtomicBoolean(false);

        filter.doFilter(
                mockRequest("GET", "/api/v1/student/dashboard", null, null),
                resp.asHttpResponse(),
                (r, s) -> passed.set(true)
        );
        assertFalse(passed.get());
        assertEquals(401, resp.status.get());
        assertTrue(resp.body.toString().contains("UNAUTHORIZED"));
    }

    @Test
    public void authorizationFilterEnforcesRoleBoundariesWith403() throws Exception {
        AuthorizationFilter filter = new AuthorizationFilter();
        MockSession studentSession = new MockSession();
        studentSession.attrs.put(
                SessionUtil.SESSION_USER_ATTR,
                new AuthenticatedUser(1L, "Student User", "student@example.com", Role.STUDENT)
        );

        MockResponse resp = new MockResponse();
        AtomicBoolean passed = new AtomicBoolean(false);
        filter.doFilter(
                mockRequest("GET", "/api/v1/admin/dashboard", studentSession, null),
                resp.asHttpResponse(),
                (r, s) -> passed.set(true)
        );

        assertFalse(passed.get());
        assertEquals(403, resp.status.get());
        assertTrue(resp.body.toString().contains("FORBIDDEN"));
    }

    @Test
    public void authenticationFilterInvalidatesSessionWhenAccountDisabled() throws Exception {
        User disabled = new User();
        disabled.setId(5L);
        disabled.setFullName("Disabled Student");
        disabled.setEmail("disabled@example.com");
        disabled.setRole(Role.STUDENT);
        disabled.setStatus(AccountStatus.DISABLED);

        AuthenticationFilter filter = new AuthenticationFilter(new StubUserDao(disabled));
        MockSession session = new MockSession();
        session.attrs.put(
                SessionUtil.SESSION_USER_ATTR,
                new AuthenticatedUser(5L, "Disabled Student", "disabled@example.com", Role.STUDENT)
        );

        MockResponse resp = new MockResponse();
        AtomicBoolean passed = new AtomicBoolean(false);
        filter.doFilter(
                mockRequest("GET", "/api/v1/auth/me", session, null),
                resp.asHttpResponse(),
                (r, s) -> passed.set(true)
        );

        assertFalse(passed.get());
        assertEquals(401, resp.status.get());
        assertTrue(session.invalidated);
    }

    private static HttpServletRequest mockRequest(String method,
                                                  String uri,
                                                  MockSession session,
                                                  String csrfHeader) {
        Map<String, Object> requestAttrs = new HashMap<String, Object>();
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("getMethod".equals(name)) {
                        return method;
                    }
                    if ("getRequestURI".equals(name)) {
                        return uri;
                    }
                    if ("getContextPath".equals(name)) {
                        return "";
                    }
                    if ("getHeader".equals(name)) {
                        String headerName = (String) args[0];
                        return CsrfTokenUtil.HEADER_NAME.equalsIgnoreCase(headerName) ? csrfHeader : null;
                    }
                    if ("getSession".equals(name)) {
                        boolean create = args != null && args.length == 1 && Boolean.TRUE.equals(args[0]);
                        return (session != null && !session.invalidated) ? session.asHttpSession() : (create ? new MockSession().asHttpSession() : null);
                    }
                    if ("getAttribute".equals(name)) {
                        return requestAttrs.get(args[0]);
                    }
                    if ("setAttribute".equals(name)) {
                        requestAttrs.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("removeAttribute".equals(name)) {
                        requestAttrs.remove(args[0]);
                        return null;
                    }
                    if ("isSecure".equals(name)) {
                        return Boolean.FALSE;
                    }
                    return null;
                }
        );
    }

    private static final class MockSession {
        private final Map<String, Object> attrs = new HashMap<String, Object>();
        private boolean invalidated = false;

        private HttpSession asHttpSession() {
            return (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class<?>[]{HttpSession.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("getAttribute".equals(name)) {
                            return attrs.get(args[0]);
                        }
                        if ("setAttribute".equals(name)) {
                            attrs.put((String) args[0], args[1]);
                            return null;
                        }
                        if ("invalidate".equals(name)) {
                            invalidated = true;
                            attrs.clear();
                            return null;
                        }
                        return null;
                    }
            );
        }
    }

    private static final class MockResponse {
        private final AtomicInteger status = new AtomicInteger(200);
        private final StringWriter body = new StringWriter();
        private final PrintWriter writer = new PrintWriter(body);

        private HttpServletResponse asHttpResponse() {
            return (HttpServletResponse) Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class<?>[]{HttpServletResponse.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("setStatus".equals(name)) {
                            status.set((Integer) args[0]);
                            return null;
                        }
                        if ("getWriter".equals(name)) {
                            return writer;
                        }
                        return null;
                    }
            );
        }
    }

    private static final class StubUserDao implements UserDAO {
        private final User user;

        private StubUserDao(User user) {
            this.user = user;
        }

        @Override
        public User findByEmail(String normalizedEmail) {
            return user;
        }

        @Override
        public User findByEmail(Connection connection, String normalizedEmail) {
            return user;
        }

        @Override
        public User findById(long userId) {
            return user;
        }

        @Override
        public User findById(Connection connection, long userId) {
            return user;
        }

        @Override
        public boolean existsByEmail(Connection connection, String normalizedEmail) {
            return user != null;
        }

        @Override
        public boolean existsAnyByRole(Connection connection, Role role) {
            return false;
        }

        @Override
        public User createUser(Connection connection, User u) {
            return u;
        }

        @Override
        public void updateLastLoginAt(long userId, Instant loginTime) {
        }

        @Override
        public void updateLastLoginAt(Connection connection, long userId, Instant loginTime) {
        }

        @Override
        public Map<Role, Long> countUsersByRole() {
            return new HashMap<Role, Long>();
        }
    }
}
