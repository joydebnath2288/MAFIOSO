package com.mafioso.service;

import com.mafioso.dao.ApplicationDAO;
import com.mafioso.dao.ApplicationDAOImpl;
import com.mafioso.dao.ApplicationDocumentDAO;
import com.mafioso.dao.ApplicationDocumentDAOImpl;
import com.mafioso.dao.ConsultantAssignmentDAO;
import com.mafioso.dao.ConsultantAssignmentDAOImpl;
import com.mafioso.dao.StudentProfileDAO;
import com.mafioso.dao.StudentProfileDAOImpl;
import com.mafioso.dao.UserDAO;
import com.mafioso.dao.UserDAOImpl;
import com.mafioso.dto.ApplicationDocumentResponse;
import com.mafioso.dto.ApplicationResponse;
import com.mafioso.dto.ConsultantSummaryResponse;
import com.mafioso.dto.CreateConsultantRequest;
import com.mafioso.dto.DashboardMetricsResponse;
import com.mafioso.dto.PagedResponse;
import com.mafioso.dto.StudentProfileResponse;
import com.mafioso.dto.UserResponse;
import com.mafioso.model.AccountStatus;
import com.mafioso.model.Application;
import com.mafioso.model.ApplicationDocument;
import com.mafioso.model.Role;
import com.mafioso.model.StudentProfile;
import com.mafioso.model.User;
import com.mafioso.util.DatabaseUtil;
import com.mafioso.util.PasswordUtil;
import com.mafioso.util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Administrative operations for inspecting students, managing account activation status,
 * securely provisioning {@code CONSULTANT} accounts, and surfacing application/assignment metrics.
 */
public class AdminUserServiceImpl implements AdminUserService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int OVERVIEW_RECENT_LIMIT = 5;

    private final UserDAO userDao;
    private final StudentProfileDAO profileDao;
    private final ApplicationDAO applicationDao;
    private final ConsultantAssignmentDAO assignmentDao;
    private final ApplicationDocumentDAO documentDao;
    private final AuthServiceImpl.ConnectionSupplier connectionSupplier;

    public AdminUserServiceImpl() {
        this(
                new UserDAOImpl(),
                new StudentProfileDAOImpl(),
                new ApplicationDAOImpl(),
                new ConsultantAssignmentDAOImpl(),
                new ApplicationDocumentDAOImpl(),
                DatabaseUtil::getConnection
        );
    }

    public AdminUserServiceImpl(UserDAO userDao, AuthServiceImpl.ConnectionSupplier connectionSupplier) {
        this(userDao, null, null, null, null, connectionSupplier);
    }

    public AdminUserServiceImpl(UserDAO userDao,
                                StudentProfileDAO profileDao,
                                ApplicationDAO applicationDao,
                                ConsultantAssignmentDAO assignmentDao,
                                ApplicationDocumentDAO documentDao,
                                AuthServiceImpl.ConnectionSupplier connectionSupplier) {
        this.userDao = userDao;
        this.profileDao = profileDao;
        this.applicationDao = applicationDao;
        this.assignmentDao = assignmentDao;
        this.documentDao = documentDao;
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public Map<String, Object> getAdminDashboard(long adminUserId) throws SQLException {
        User adminUser = userDao.findById(adminUserId);
        if (adminUser == null || adminUser.getStatus() == null || !adminUser.getStatus().isActive()) {
            throw new AuthenticationException("Authenticated administrator session is no longer valid.");
        }

        DashboardMetricsResponse metrics = userDao.getDashboardMetrics();
        List<User> recentStudentsRaw = userDao.findUsersByRole(Role.STUDENT, null, null, 0, OVERVIEW_RECENT_LIMIT);
        List<User> consultantsRaw = userDao.findUsersByRole(Role.CONSULTANT, null, null, 0, OVERVIEW_RECENT_LIMIT);

        List<UserResponse> recentStudents = new ArrayList<UserResponse>(recentStudentsRaw.size());
        for (User student : recentStudentsRaw) {
            recentStudents.add(UserResponse.fromUser(student));
        }

        boolean trackingEnabled = assignmentDao != null && applicationDao != null;
        List<ConsultantSummaryResponse> consultants = buildConsultantSummaries(consultantsRaw, trackingEnabled);

        Map<String, Long> roleCounts = new LinkedHashMap<String, Long>();
        roleCounts.put(Role.STUDENT.name(), metrics.getTotalStudents());
        roleCounts.put(Role.CONSULTANT.name(), metrics.getTotalConsultants());
        roleCounts.put(Role.ADMIN.name(), metrics.getTotalAdministrators());

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("workspace", "ADMINISTRATION");
        payload.put("account", UserResponse.fromUser(adminUser));
        payload.put("metrics", metrics);
        payload.put("roleCounts", roleCounts);
        payload.put("recentStudents", recentStudents);
        payload.put("consultants", consultants);
        payload.put("assignmentTrackingAvailable", Boolean.valueOf(trackingEnabled));

        if (trackingEnabled) {
            try (Connection connection = connectionSupplier.get()) {
                Map<String, Long> appMetrics = applicationDao.getApplicationMetrics(connection);
                if (documentDao != null) {
                    appMetrics.put("pendingDocumentReviews", Long.valueOf(documentDao.countPendingReviewAll(connection)));
                }
                List<Application> recentAppsRaw = applicationDao.findAll(connection, null, null, null, null, false, 0, OVERVIEW_RECENT_LIMIT);
                List<ApplicationResponse> recentApps = new ArrayList<ApplicationResponse>(recentAppsRaw.size());
                for (Application app : recentAppsRaw) {
                    recentApps.add(ApplicationResponse.from(app));
                }
                payload.put("applicationMetrics", appMetrics);
                payload.put("recentApplications", recentApps);
            }
        }
        return payload;
    }

    @Override
    public PagedResponse<UserResponse> listStudents(String search,
                                                    String status,
                                                    int page,
                                                    int pageSize) throws SQLException {
        AccountStatus statusFilter = parseOptionalStatusFilter(status);
        int safePage = Math.max(1, page);
        int safeSize = normalizePageSize(pageSize);
        int offset = (safePage - 1) * safeSize;

        long total = userDao.countUsersByRoleAndFilter(Role.STUDENT, search, statusFilter);
        List<User> rows = total == 0
                ? Collections.<User>emptyList()
                : userDao.findUsersByRole(Role.STUDENT, search, statusFilter, offset, safeSize);

        List<UserResponse> items = new ArrayList<UserResponse>(rows.size());
        for (User user : rows) {
            items.add(UserResponse.fromUser(user));
        }
        return new PagedResponse<UserResponse>(items, safePage, safeSize, total);
    }

    @Override
    public Map<String, Object> getStudentDetails(long studentId) throws SQLException {
        try (Connection connection = connectionSupplier.get()) {
            User student = userDao.findByIdAndRole(connection, studentId, Role.STUDENT);
            if (student == null) {
                throw new ResourceNotFoundException("Student account not found.");
            }
            Map<String, Object> detail = new LinkedHashMap<String, Object>();
            detail.put("student", UserResponse.fromUser(student));

            if (profileDao != null && applicationDao != null && documentDao != null) {
                StudentProfile profile = profileDao.findByUserId(connection, studentId);
                List<Application> apps = applicationDao.findByStudentId(connection, studentId, null, 0, 50);
                List<ApplicationDocument> docs = documentDao.findByStudentId(connection, studentId, null, null, null);

                List<ApplicationResponse> appDtos = new ArrayList<ApplicationResponse>(apps.size());
                for (Application app : apps) {
                    appDtos.add(ApplicationResponse.from(app));
                }
                List<ApplicationDocumentResponse> docDtos = new ArrayList<ApplicationDocumentResponse>(docs.size());
                for (ApplicationDocument doc : docs) {
                    docDtos.add(ApplicationDocumentResponse.from(doc));
                }

                detail.put("academicProfileAvailable", Boolean.TRUE);
                detail.put("profile", StudentProfileResponse.from(student, profile));
                detail.put("applications", appDtos);
                detail.put("documents", docDtos);
            } else {
                detail.put("academicProfileAvailable", Boolean.FALSE);
                detail.put("academicProfileNote",
                        "Academic credentials, UK shortlists, and document vault records will appear here in upcoming milestones.");
            }
            return detail;
        }
    }

    @Override
    public UserResponse updateStudentStatus(long studentId,
                                            String rawStatus,
                                            long actingAdminId) throws SQLException {
        AccountStatus targetStatus = parseToggleTargetStatus(rawStatus);
        User updated = updateUserStatusByRole(studentId, Role.STUDENT, targetStatus, actingAdminId);
        return UserResponse.fromUser(updated);
    }

    @Override
    public PagedResponse<ConsultantSummaryResponse> listConsultants(String search,
                                                                  String status,
                                                                  int page,
                                                                  int pageSize) throws SQLException {
        AccountStatus statusFilter = parseOptionalStatusFilter(status);
        int safePage = Math.max(1, page);
        int safeSize = normalizePageSize(pageSize);
        int offset = (safePage - 1) * safeSize;

        long total = userDao.countUsersByRoleAndFilter(Role.CONSULTANT, search, statusFilter);
        List<User> rows = total == 0
                ? Collections.<User>emptyList()
                : userDao.findUsersByRole(Role.CONSULTANT, search, statusFilter, offset, safeSize);

        boolean trackingEnabled = assignmentDao != null;
        List<ConsultantSummaryResponse> items = buildConsultantSummaries(rows, trackingEnabled);
        return new PagedResponse<ConsultantSummaryResponse>(items, safePage, safeSize, total);
    }

    @Override
    public Map<String, Object> getConsultantDetails(long consultantId) throws SQLException {
        try (Connection connection = connectionSupplier.get()) {
            User consultant = userDao.findByIdAndRole(connection, consultantId, Role.CONSULTANT);
            if (consultant == null) {
                throw new ResourceNotFoundException("Consultant account not found.");
            }
            ConsultantSummaryResponse dto = ConsultantSummaryResponse.fromUser(consultant);
            Map<String, Object> detail = new LinkedHashMap<String, Object>();

            if (assignmentDao != null && applicationDao != null) {
                Map<Long, Long> counts = assignmentDao.countActiveStudentsByConsultantIds(
                        connection, Collections.singletonList(Long.valueOf(consultantId)));
                dto.setAssignmentTrackingAvailable(true);
                dto.setAssignedStudentCount(counts.getOrDefault(Long.valueOf(consultantId), 0L));

                List<Application> apps = applicationDao.findByConsultantId(connection, consultantId, null, null, 0, 50);
                List<ApplicationResponse> appDtos = new ArrayList<ApplicationResponse>(apps.size());
                for (Application app : apps) {
                    appDtos.add(ApplicationResponse.from(app));
                }
                detail.put("assignmentTrackingAvailable", Boolean.TRUE);
                detail.put("assignedApplications", appDtos);
                detail.put("assignedStudents", assignmentDao.findAssignedStudentsByConsultant(connection, consultantId, null, 0, 50));
            } else {
                detail.put("assignmentTrackingAvailable", Boolean.FALSE);
                detail.put("assignmentNote",
                        "Student-to-consultant assignment relationships are not yet enabled in this milestone.");
            }

            detail.put("consultant", dto);
            return detail;
        }
    }

    private List<ConsultantSummaryResponse> buildConsultantSummaries(List<User> rows, boolean trackingEnabled) throws SQLException {
        List<ConsultantSummaryResponse> items = new ArrayList<ConsultantSummaryResponse>(rows.size());
        if (rows.isEmpty()) {
            return items;
        }

        Map<Long, Long> counts = Collections.emptyMap();
        if (trackingEnabled) {
            List<Long> ids = new ArrayList<Long>(rows.size());
            for (User u : rows) {
                ids.add(u.getId());
            }
            try (Connection connection = connectionSupplier.get()) {
                counts = assignmentDao.countActiveStudentsByConsultantIds(connection, ids);
            }
        }

        for (User user : rows) {
            ConsultantSummaryResponse dto = ConsultantSummaryResponse.fromUser(user);
            if (trackingEnabled) {
                dto.setAssignmentTrackingAvailable(true);
                dto.setAssignedStudentCount(counts.getOrDefault(user.getId(), 0L));
            }
            items.add(dto);
        }
        return items;
    }

    @Override
    public ConsultantSummaryResponse createConsultant(CreateConsultantRequest request) throws SQLException {
        Map<String, String> fieldErrors = ValidationUtil.validateConsultantCreation(request);
        if (!fieldErrors.isEmpty()) {
            throw new ValidationException("Please correct the highlighted fields.", fieldErrors);
        }

        String normalizedEmail = ValidationUtil.normalizeEmail(request.getEmail());
        String normalizedName = ValidationUtil.normalizeFullName(request.getFullName());
        String normalizedPhone = ValidationUtil.normalizePhoneNumber(request.getPhoneNumber());
        String passwordHash = PasswordUtil.hash(request.getPassword());

        // Backend always enforces CONSULTANT role regardless of client payload.
        User consultant = new User();
        consultant.setFullName(normalizedName);
        consultant.setEmail(normalizedEmail);
        consultant.setPhoneNumber(normalizedPhone);
        consultant.setPasswordHash(passwordHash);
        consultant.setRole(Role.CONSULTANT);
        consultant.setStatus(AccountStatus.ACTIVE);

        try (Connection connection = connectionSupplier.get()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                if (userDao.existsByEmail(connection, normalizedEmail)) {
                    throw new DuplicateEmailException("An account with this email address already exists.");
                }
                User created = userDao.createUser(connection, consultant);
                connection.commit();
                return ConsultantSummaryResponse.fromUser(created);
            } catch (SQLIntegrityConstraintViolationException ex) {
                rollbackQuietly(connection);
                throw new DuplicateEmailException("An account with this email address already exists.");
            } catch (SQLException ex) {
                rollbackQuietly(connection);
                if ("23000".equals(ex.getSQLState()) || ex.getErrorCode() == 1062) {
                    throw new DuplicateEmailException("An account with this email address already exists.");
                }
                throw ex;
            } catch (RuntimeException ex) {
                rollbackQuietly(connection);
                throw ex;
            } finally {
                restoreAutoCommitQuietly(connection, originalAutoCommit);
            }
        }
    }

    @Override
    public ConsultantSummaryResponse updateConsultantStatus(long consultantId,
                                                            String rawStatus,
                                                            long actingAdminId) throws SQLException {
        AccountStatus targetStatus = parseToggleTargetStatus(rawStatus);
        User updated = updateUserStatusByRole(consultantId, Role.CONSULTANT, targetStatus, actingAdminId);
        return ConsultantSummaryResponse.fromUser(updated);
    }

    private User updateUserStatusByRole(long userId,
                                        Role expectedRole,
                                        AccountStatus targetStatus,
                                        long actingAdminId) throws SQLException {
        if (userId == actingAdminId) {
            throw new ValidationException(
                    "You cannot change the activation status of your own account.",
                    Collections.singletonMap("status", "Self status change is not permitted.")
            );
        }

        try (Connection connection = connectionSupplier.get()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                User existing = userDao.findByIdAndRole(connection, userId, expectedRole);
                if (existing == null) {
                    throw new ResourceNotFoundException(
                            expectedRole == Role.STUDENT ? "Student account not found." : "Consultant account not found."
                    );
                }
                if (existing.getStatus() != targetStatus) {
                    boolean changed = userDao.updateAccountStatus(connection, userId, expectedRole, targetStatus);
                    if (!changed) {
                        throw new ResourceNotFoundException("Target user account no longer exists.");
                    }
                }
                User refreshed = userDao.findByIdAndRole(connection, userId, expectedRole);
                connection.commit();
                return refreshed;
            } catch (SQLException ex) {
                rollbackQuietly(connection);
                throw ex;
            } catch (RuntimeException ex) {
                rollbackQuietly(connection);
                throw ex;
            } finally {
                restoreAutoCommitQuietly(connection, originalAutoCommit);
            }
        }
    }

    private static AccountStatus parseOptionalStatusFilter(String rawStatus) {
        if (rawStatus == null || rawStatus.trim().isEmpty() || "ALL".equalsIgnoreCase(rawStatus.trim())) {
            return null;
        }
        try {
            return AccountStatus.fromString(rawStatus);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(
                    "Invalid status filter value.",
                    Collections.singletonMap("status", "Status must be ACTIVE, DISABLED, SUSPENDED, or ALL.")
            );
        }
    }

    private static AccountStatus parseToggleTargetStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.trim().isEmpty()) {
            throw new ValidationException(
                    "Account status is required.",
                    Collections.singletonMap("status", "Status must be ACTIVE or DISABLED.")
            );
        }
        String normalized = rawStatus.trim().toUpperCase(Locale.ROOT);
        if (!"ACTIVE".equals(normalized) && !"DISABLED".equals(normalized)) {
            throw new ValidationException(
                    "Invalid account status transition.",
                    Collections.singletonMap("status", "Status must be ACTIVE or DISABLED.")
            );
        }
        return AccountStatus.valueOf(normalized);
    }

    private static int normalizePageSize(int requested) {
        if (requested <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(requested, MAX_PAGE_SIZE);
    }

    private static void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Preserve primary exception
        }
    }

    private static void restoreAutoCommitQuietly(Connection connection, boolean originalAutoCommit) {
        try {
            connection.setAutoCommit(originalAutoCommit);
        } catch (SQLException ignored) {
            // Connection closing next
        }
    }
}
