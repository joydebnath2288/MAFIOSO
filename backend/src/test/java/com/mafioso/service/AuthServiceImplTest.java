package com.mafioso.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafioso.dao.UserDAO;
import com.mafioso.dto.LoginRequest;
import com.mafioso.dto.RegisterRequest;
import com.mafioso.dto.UserResponse;
import com.mafioso.model.AccountStatus;
import com.mafioso.model.Role;
import com.mafioso.model.User;
import com.mafioso.util.JsonUtil;
import com.mafioso.util.LoginRateLimiter;
import com.mafioso.util.PasswordUtil;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link AuthServiceImpl} covering student registration,
 * role-escalation resistance, BCrypt verification, disabled accounts, and rate limiting.
 */
public class AuthServiceImplTest {

    private InMemoryUserDao userDao;
    private AuthServiceImpl authService;
    private LoginRateLimiter rateLimiter;

    @Before
    public void setUp() {
        userDao = new InMemoryUserDao();
        rateLimiter = new LoginRateLimiter(true, 3, 20, 300, 300, System::currentTimeMillis);
        Connection dummyConn = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getAutoCommit".equals(name)) {
                        return Boolean.TRUE;
                    }
                    if ("isClosed".equals(name)) {
                        return Boolean.FALSE;
                    }
                    return null;
                }
        );
        authService = new AuthServiceImpl(userDao, rateLimiter, () -> dummyConn);
    }

    @Test
    public void registersStudentWithNormalizedEmailAndAlwaysForcesStudentRole() throws Exception {
        String maliciousJson = "{"
                + "\"fullName\":\"Nadia   Karim\","
                + "\"email\":\"  Nadia.Karim@Example.COM \","
                + "\"password\":\"LondonMSc#2026\","
                + "\"confirmPassword\":\"LondonMSc#2026\","
                + "\"phoneNumber\":\"+880 1711 000111\","
                + "\"role\":\"ADMIN\""
                + "}";
        ObjectMapper mapper = JsonUtil.getObjectMapper();
        RegisterRequest req = mapper.readValue(maliciousJson, RegisterRequest.class);

        UserResponse created = authService.registerStudent(req);
        assertEquals("STUDENT", created.getRole());
        assertEquals("nadia.karim@example.com", created.getEmail());
        assertEquals("Nadia Karim", created.getFullName());

        User stored = userDao.findByEmail("nadia.karim@example.com");
        assertNotNull(stored);
        assertEquals(Role.STUDENT, stored.getRole());
        assertTrue(PasswordUtil.verify("LondonMSc#2026", stored.getPasswordHash()));
    }

    @Test(expected = DuplicateEmailException.class)
    public void rejectsDuplicateEmailRegistration() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Student One");
        req.setEmail("dup@example.com");
        req.setPassword("LondonMSc#2026");
        req.setConfirmPassword("LondonMSc#2026");

        authService.registerStudent(req);
        authService.registerStudent(req);
    }

    @Test(expected = AuthenticationException.class)
    public void rejectsWrongPasswordWithGenericError() throws Exception {
        seedUser("user@example.com", "RightPass#2026", Role.STUDENT, AccountStatus.ACTIVE);

        LoginRequest login = new LoginRequest();
        login.setEmail("USER@example.com");
        login.setPassword("WrongPass#2026");
        authService.authenticate(login, "127.0.0.1");
    }

    @Test(expected = AccountDisabledException.class)
    public void rejectsDisabledAccountAfterValidPassword() throws Exception {
        seedUser("disabled@example.com", "RightPass#2026", Role.CONSULTANT, AccountStatus.DISABLED);

        LoginRequest login = new LoginRequest();
        login.setEmail("disabled@example.com");
        login.setPassword("RightPass#2026");
        authService.authenticate(login, "127.0.0.1");
    }

    @Test(expected = RateLimitException.class)
    public void enforcesRateLimitAfterRepeatedFailedLogins() throws Exception {
        seedUser("locked@example.com", "RightPass#2026", Role.STUDENT, AccountStatus.ACTIVE);

        LoginRequest login = new LoginRequest();
        login.setEmail("locked@example.com");
        login.setPassword("BadPass#2026");

        for (int i = 0; i < 3; i++) {
            try {
                authService.authenticate(login, "198.51.100.5");
            } catch (AuthenticationException ignored) {
                // Expected until lockout threshold reached
            }
        }
        authService.authenticate(login, "198.51.100.5");
    }

    @Test
    public void resolvesRoleSpecificDashboards() {
        assertEquals("/student/dashboard", authService.resolveDashboardRoute(Role.STUDENT));
        assertEquals("/staff/dashboard", authService.resolveDashboardRoute(Role.CONSULTANT));
        assertEquals("/staff/dashboard", authService.resolveDashboardRoute(Role.ADMIN));
    }

    private void seedUser(String email, String rawPassword, Role role, AccountStatus status) throws SQLException {
        User u = new User();
        u.setFullName("Seed User");
        u.setEmail(email);
        u.setPasswordHash(PasswordUtil.hash(rawPassword, 10));
        u.setRole(role);
        u.setStatus(status);
        userDao.createUser(null, u);
    }

    private static final class InMemoryUserDao implements UserDAO {
        private final AtomicLong seq = new AtomicLong(1L);
        private final Map<Long, User> byId = new ConcurrentHashMap<Long, User>();
        private final Map<String, User> byEmail = new ConcurrentHashMap<String, User>();

        @Override
        public User findByEmail(String normalizedEmail) {
            return byEmail.get(normalizedEmail);
        }

        @Override
        public User findByEmail(Connection connection, String normalizedEmail) {
            return findByEmail(normalizedEmail);
        }

        @Override
        public User findById(long userId) {
            return byId.get(userId);
        }

        @Override
        public User findById(Connection connection, long userId) {
            return findById(userId);
        }

        @Override
        public boolean existsByEmail(Connection connection, String normalizedEmail) {
            return byEmail.containsKey(normalizedEmail);
        }

        @Override
        public boolean existsAnyByRole(Connection connection, Role role) {
            for (User u : byId.values()) {
                if (u.getRole() == role) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public User createUser(Connection connection, User user) {
            long id = seq.getAndIncrement();
            user.setId(id);
            user.setCreatedAt(Instant.now());
            byId.put(id, user);
            byEmail.put(user.getEmail(), user);
            return user;
        }

        @Override
        public void updateLastLoginAt(long userId, Instant loginTime) {
            User u = byId.get(userId);
            if (u != null) {
                u.setLastLoginAt(loginTime);
            }
        }

        @Override
        public void updateLastLoginAt(Connection connection, long userId, Instant loginTime) {
            updateLastLoginAt(userId, loginTime);
        }

        @Override
        public Map<Role, Long> countUsersByRole() {
            Map<Role, Long> counts = new EnumMap<Role, Long>(Role.class);
            for (Role role : Role.values()) {
                counts.put(role, 0L);
            }
            for (User u : byId.values()) {
                counts.put(u.getRole(), counts.get(u.getRole()) + 1L);
            }
            return counts;
        }
    }
}
