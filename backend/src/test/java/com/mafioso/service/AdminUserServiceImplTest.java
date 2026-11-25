package com.mafioso.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafioso.dao.UserDAO;
import com.mafioso.dto.ConsultantSummaryResponse;
import com.mafioso.dto.CreateConsultantRequest;
import com.mafioso.dto.DashboardMetricsResponse;
import com.mafioso.dto.PagedResponse;
import com.mafioso.dto.UserResponse;
import com.mafioso.model.AccountStatus;
import com.mafioso.model.Role;
import com.mafioso.model.User;
import com.mafioso.util.JsonUtil;
import com.mafioso.util.PasswordUtil;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link AdminUserServiceImpl} covering dashboard metrics,
 * student/consultant filtering & pagination, role isolation, consultant creation, and status toggles.
 */
public class AdminUserServiceImplTest {

    private StubUserDao userDao;
    private AdminUserServiceImpl service;
    private User adminUser;
    private User studentActive;
    private User studentDisabled;
    private User consultantActive;

    @Before
    public void setUp() throws SQLException {
        userDao = new StubUserDao();
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
        service = new AdminUserServiceImpl(userDao, () -> dummyConn);

        adminUser = seed("Admin One", "admin@example.com", Role.ADMIN, AccountStatus.ACTIVE);
        studentActive = seed("Amina Rahman", "amina@example.com", Role.STUDENT, AccountStatus.ACTIVE);
        studentDisabled = seed("Bilal Chowdhury", "bilal@example.com", Role.STUDENT, AccountStatus.DISABLED);
        consultantActive = seed("Karim Uddin", "karim@example.com", Role.CONSULTANT, AccountStatus.ACTIVE);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void returnsRealMetricsAndListsWithoutInventedAssignmentCounts() throws Exception {
        Map<String, Object> dashboard = service.getAdminDashboard(adminUser.getId());
        DashboardMetricsResponse metrics = (DashboardMetricsResponse) dashboard.get("metrics");

        assertEquals(2L, metrics.getTotalStudents());
        assertEquals(1L, metrics.getActiveStudents());
        assertEquals(1L, metrics.getTotalConsultants());
        assertEquals(1L, metrics.getActiveConsultants());
        assertEquals(1L, metrics.getTotalAdministrators());

        List<UserResponse> recentStudents = (List<UserResponse>) dashboard.get("recentStudents");
        assertEquals(2, recentStudents.size());

        List<ConsultantSummaryResponse> consultants = (List<ConsultantSummaryResponse>) dashboard.get("consultants");
        assertEquals(1, consultants.size());
        assertNull(consultants.get(0).getAssignedStudentCount());
        assertFalse(consultants.get(0).isAssignmentTrackingAvailable());
    }

    @Test
    public void searchesFiltersAndPaginatesStudents() throws Exception {
        PagedResponse<UserResponse> filtered = service.listStudents("amina", "ACTIVE", 1, 10);
        assertEquals(1L, filtered.getTotalItems());
        assertEquals("amina@example.com", filtered.getItems().get(0).getEmail());

        PagedResponse<UserResponse> paged = service.listStudents(null, "ALL", 2, 1);
        assertEquals(2L, paged.getTotalItems());
        assertEquals(2, paged.getTotalPages());
        assertEquals(1, paged.getItems().size());
        assertTrue(paged.isHasPrevious());
        assertFalse(paged.isHasNext());
    }

    @Test
    public void createsConsultantEnforcingConsultantRoleAndRejectingSpoofedRole() throws Exception {
        String payload = "{"
                + "\"fullName\":\"  Farhana   Akter \","
                + "\"email\":\"  Farhana.Akter@Example.COM \","
                + "\"phoneNumber\":\"+880 1711 555666\","
                + "\"password\":\"ConsultPass#2026\","
                + "\"confirmPassword\":\"ConsultPass#2026\","
                + "\"role\":\"ADMIN\""
                + "}";
        ObjectMapper mapper = JsonUtil.getObjectMapper();
        CreateConsultantRequest req = mapper.readValue(payload, CreateConsultantRequest.class);

        ConsultantSummaryResponse created = service.createConsultant(req);
        assertEquals("CONSULTANT", created.getRole());
        assertEquals("ACTIVE", created.getStatus());
        assertEquals("farhana.akter@example.com", created.getEmail());
        assertNull(created.getAssignedStudentCount());

        User persisted = userDao.findByEmail("farhana.akter@example.com");
        assertNotNull(persisted);
        assertEquals(Role.CONSULTANT, persisted.getRole());
        assertTrue(PasswordUtil.verify("ConsultPass#2026", persisted.getPasswordHash()));
    }

    @Test(expected = DuplicateEmailException.class)
    public void rejectsDuplicateEmailWhenCreatingConsultant() throws Exception {
        CreateConsultantRequest req = new CreateConsultantRequest();
        req.setFullName("Duplicate Staff");
        req.setEmail("amina@example.com");
        req.setPassword("ConsultPass#2026");
        req.setConfirmPassword("ConsultPass#2026");
        service.createConsultant(req);
    }

    @Test
    public void togglesStudentAndConsultantStatus() throws Exception {
        UserResponse deactivated = service.updateStudentStatus(studentActive.getId(), "DISABLED", adminUser.getId());
        assertEquals("DISABLED", deactivated.getStatus());

        UserResponse reactivated = service.updateStudentStatus(studentDisabled.getId(), "ACTIVE", adminUser.getId());
        assertEquals("ACTIVE", reactivated.getStatus());

        ConsultantSummaryResponse disabledStaff = service.updateConsultantStatus(
                consultantActive.getId(), "DISABLED", adminUser.getId());
        assertEquals("DISABLED", disabledStaff.getStatus());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void enforcesRoleIsolationOnStudentStatusUpdate() throws Exception {
        // Attempting to target an ADMIN or CONSULTANT id via the student endpoint must fail with 404
        service.updateStudentStatus(consultantActive.getId(), "DISABLED", adminUser.getId());
    }

    @Test(expected = ValidationException.class)
    public void preventsSelfStatusMutation() throws Exception {
        service.updateConsultantStatus(adminUser.getId(), "DISABLED", adminUser.getId());
    }

    private User seed(String name, String email, Role role, AccountStatus status) throws SQLException {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPasswordHash(PasswordUtil.hash("SeedPass#2026", 10));
        u.setRole(role);
        u.setStatus(status);
        return userDao.createUser(null, u);
    }

    private static final class StubUserDao implements UserDAO {
        private final AtomicLong seq = new AtomicLong(1L);
        private final Map<Long, User> byId = new ConcurrentHashMap<Long, User>();

        @Override
        public User findByEmail(String normalizedEmail) {
            for (User u : byId.values()) {
                if (u.getEmail().equals(normalizedEmail)) {
                    return u;
                }
            }
            return null;
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
            return byId.get(userId);
        }

        @Override
        public User findByIdAndRole(Connection connection, long userId, Role expectedRole) {
            User u = byId.get(userId);
            return (u != null && u.getRole() == expectedRole) ? u : null;
        }

        @Override
        public boolean existsByEmail(Connection connection, String normalizedEmail) {
            return findByEmail(normalizedEmail) != null;
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
        public boolean updateAccountStatus(Connection connection, long userId, Role expectedRole, AccountStatus status) {
            User u = findByIdAndRole(connection, userId, expectedRole);
            if (u == null) {
                return false;
            }
            u.setStatus(status);
            u.setUpdatedAt(Instant.now());
            return true;
        }

        @Override
        public Map<Role, Long> countUsersByRole() {
            Map<Role, Long> counts = new EnumMap<Role, Long>(Role.class);
            for (Role r : Role.values()) {
                counts.put(r, 0L);
            }
            for (User u : byId.values()) {
                counts.put(u.getRole(), counts.get(u.getRole()) + 1L);
            }
            return counts;
        }

        @Override
        public DashboardMetricsResponse getDashboardMetrics() {
            long ts = 0L, as = 0L, tc = 0L, ac = 0L, ta = 0L;
            for (User u : byId.values()) {
                if (u.getRole() == Role.STUDENT) {
                    ts++;
                    if (u.getStatus() == AccountStatus.ACTIVE) {
                        as++;
                    }
                } else if (u.getRole() == Role.CONSULTANT) {
                    tc++;
                    if (u.getStatus() == AccountStatus.ACTIVE) {
                        ac++;
                    }
                } else if (u.getRole() == Role.ADMIN) {
                    ta++;
                }
            }
            return new DashboardMetricsResponse(ts, as, tc, ac, ta);
        }

        @Override
        public List<User> findUsersByRole(Role role, String search, AccountStatus statusFilter, int offset, int limit) {
            List<User> matching = filterUsers(role, search, statusFilter);
            int from = Math.min(offset, matching.size());
            int to = Math.min(offset + limit, matching.size());
            return new ArrayList<User>(matching.subList(from, to));
        }

        @Override
        public long countUsersByRoleAndFilter(Role role, String search, AccountStatus statusFilter) {
            return filterUsers(role, search, statusFilter).size();
        }

        private List<User> filterUsers(Role role, String search, AccountStatus statusFilter) {
            String term = (search == null || search.trim().isEmpty())
                    ? null
                    : search.trim().toLowerCase(Locale.ROOT);
            List<User> list = new ArrayList<User>();
            for (User u : byId.values()) {
                if (u.getRole() != role) {
                    continue;
                }
                if (statusFilter != null && u.getStatus() != statusFilter) {
                    continue;
                }
                if (term != null) {
                    boolean matchName = u.getFullName().toLowerCase(Locale.ROOT).contains(term);
                    boolean matchEmail = u.getEmail().toLowerCase(Locale.ROOT).contains(term);
                    if (!matchName && !matchEmail) {
                        continue;
                    }
                }
                list.add(u);
            }
            Collections.sort(list, Comparator.comparing(User::getId).reversed());
            return list;
        }
    }
}
