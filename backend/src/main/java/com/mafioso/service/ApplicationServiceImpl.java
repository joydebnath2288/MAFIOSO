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
import com.mafioso.dto.ApplicationStatusHistoryResponse;
import com.mafioso.dto.AssignConsultantRequest;
import com.mafioso.dto.AssignedStudentSummaryResponse;
import com.mafioso.dto.ConsultantAssignmentResponse;
import com.mafioso.dto.ConsultantSummaryResponse;
import com.mafioso.dto.CreateApplicationRequest;
import com.mafioso.dto.PagedResponse;
import com.mafioso.dto.StudentProfileResponse;
import com.mafioso.dto.UpdateApplicationStatusRequest;
import com.mafioso.dto.UserResponse;
import com.mafioso.model.AccountStatus;
import com.mafioso.model.Application;
import com.mafioso.model.ApplicationDocument;
import com.mafioso.model.ApplicationStatus;
import com.mafioso.model.ApplicationStatusHistory;
import com.mafioso.model.AssignmentAction;
import com.mafioso.model.ConsultantAssignment;
import com.mafioso.model.DegreeLevel;
import com.mafioso.model.Role;
import com.mafioso.model.StudentProfile;
import com.mafioso.model.User;
import com.mafioso.util.ConsultancyValidationUtil;
import com.mafioso.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Core consultancy workflow business logic for applications, status transitions,
 * transactional audit histories, and administrator-controlled consultant assignments.
 */
public class ApplicationServiceImpl implements ApplicationService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final ApplicationDAO applicationDao;
    private final ConsultantAssignmentDAO assignmentDao;
    private final ApplicationDocumentDAO documentDao;
    private final StudentProfileDAO profileDao;
    private final UserDAO userDao;
    private final AuthServiceImpl.ConnectionSupplier connectionSupplier;

    public ApplicationServiceImpl() {
        this(
                new ApplicationDAOImpl(),
                new ConsultantAssignmentDAOImpl(),
                new ApplicationDocumentDAOImpl(),
                new StudentProfileDAOImpl(),
                new UserDAOImpl(),
                DatabaseUtil::getConnection
        );
    }

    public ApplicationServiceImpl(ApplicationDAO applicationDao,
                                  ConsultantAssignmentDAO assignmentDao,
                                  ApplicationDocumentDAO documentDao,
                                  StudentProfileDAO profileDao,
                                  UserDAO userDao,
                                  AuthServiceImpl.ConnectionSupplier connectionSupplier) {
        this.applicationDao = applicationDao;
        this.assignmentDao = assignmentDao;
        this.documentDao = documentDao;
        this.profileDao = profileDao;
        this.userDao = userDao;
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public ApplicationResponse createApplication(long studentId, CreateApplicationRequest request) throws SQLException {
        Map<String, String> fieldErrors = ConsultancyValidationUtil.validateCreateApplication(request);
        if (!fieldErrors.isEmpty()) {
            throw new ValidationException("Please correct the highlighted application fields.", fieldErrors);
        }

        try (Connection connection = connectionSupplier.get()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                User student = userDao.findByIdAndRole(connection, studentId, Role.STUDENT);
                if (student == null || student.getStatus() == null || !student.getStatus().isActive()) {
                    throw new AuthenticationException("Authenticated student account is not active.");
                }

                int currentYear = Year.now().getValue();
                String refNumber = applicationDao.nextReferenceNumber(connection, currentYear);

                Application app = new Application();
                app.setReferenceNumber(refNumber);
                app.setStudentId(studentId);
                app.setDegreeLevel(DegreeLevel.fromString(request.getDegreeLevel()));
                app.setPreferredSubject(ConsultancyValidationUtil.normalizeText(request.getPreferredSubject()));
                app.setIntendedIntake(ConsultancyValidationUtil.normalizeText(request.getIntendedIntake()));
                app.setPreferredUniversity(ConsultancyValidationUtil.normalizeText(request.getPreferredUniversity()));
                app.setPreferredLocation(ConsultancyValidationUtil.normalizeText(request.getPreferredLocation()));
                app.setPersonalStatement(ConsultancyValidationUtil.normalizeText(request.getPersonalStatement()));
                app.setStatus(ApplicationStatus.SUBMITTED);
                app.setAssignedConsultantId(null);

                Application created = applicationDao.create(connection, app);

                ApplicationStatusHistory initialHistory = new ApplicationStatusHistory();
                initialHistory.setApplicationId(created.getId());
                initialHistory.setPreviousStatus(null);
                initialHistory.setNewStatus(ApplicationStatus.SUBMITTED);
                initialHistory.setChangedByUserId(studentId);
                initialHistory.setNote("Application submitted by student.");
                applicationDao.addStatusHistory(connection, initialHistory);

                connection.commit();
                return ApplicationResponse.from(created);
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

    @Override
    public PagedResponse<ApplicationResponse> listStudentApplications(long studentId,
                                                                      String status,
                                                                      int page,
                                                                      int pageSize) throws SQLException {
        ApplicationStatus statusFilter = parseOptionalStatus(status);
        int safePage = Math.max(1, page);
        int safeSize = normalizePageSize(pageSize);
        int offset = (safePage - 1) * safeSize;

        try (Connection connection = connectionSupplier.get()) {
            long total = applicationDao.countByStudentId(connection, studentId, statusFilter);
            List<Application> rows = total == 0
                    ? Collections.<Application>emptyList()
                    : applicationDao.findByStudentId(connection, studentId, statusFilter, offset, safeSize);
            return new PagedResponse<ApplicationResponse>(toAppResponses(rows), safePage, safeSize, total);
        }
    }

    @Override
    public Map<String, Object> getStudentApplicationDetail(long studentId, long applicationId) throws SQLException {
        try (Connection connection = connectionSupplier.get()) {
            Application app = applicationDao.findById(connection, applicationId);
            if (app == null) {
                throw new ResourceNotFoundException("Application not found.");
            }
            if (app.getStudentId() == null || app.getStudentId().longValue() != studentId) {
                throw new ForbiddenException("You do not have permission to view another student's application.");
            }

            List<ApplicationStatusHistory> history = applicationDao.findStatusHistoryByApplicationId(connection, applicationId);
            List<ApplicationDocument> docs = documentDao.findByApplicationId(connection, applicationId);

            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("application", ApplicationResponse.from(app));
            payload.put("statusHistory", toHistoryResponses(history));
            payload.put("documents", toDocumentResponses(docs));
            return payload;
        }
    }

    @Override
    public Map<String, Object> getStudentDashboardSummary(long studentId) throws SQLException {
        try (Connection connection = connectionSupplier.get()) {
            User student = userDao.findByIdAndRole(connection, studentId, Role.STUDENT);
            if (student == null || student.getStatus() == null || !student.getStatus().isActive()) {
                throw new AuthenticationException("Authenticated student session is no longer valid.");
            }

            StudentProfile profile = profileDao.findByUserId(connection, studentId);
            StudentProfileResponse profileDto = StudentProfileResponse.from(student, profile);

            long totalApps = applicationDao.countByStudentId(connection, studentId, null);
            List<Application> recentAppsRaw = totalApps == 0
                    ? Collections.<Application>emptyList()
                    : applicationDao.findByStudentId(connection, studentId, null, 0, 5);
            long totalDocs = documentDao.countByStudentId(connection, studentId);

            ApplicationResponse.AssignedConsultantSummary assignedConsultant = null;
            for (Application app : recentAppsRaw) {
                if (app.getAssignedConsultantId() != null) {
                    assignedConsultant = ApplicationResponse.from(app).getAssignedConsultant();
                    break;
                }
            }

            Map<String, Object> summary = new LinkedHashMap<String, Object>();
            summary.put("account", UserResponse.fromUser(student));
            summary.put("profile", profileDto);
            summary.put("totalApplications", Long.valueOf(totalApps));
            summary.put("totalDocuments", Long.valueOf(totalDocs));
            summary.put("recentApplications", toAppResponses(recentAppsRaw));
            summary.put("assignedConsultant", assignedConsultant);
            return summary;
        }
    }

    @Override
    public Map<String, Object> getConsultantDashboard(long consultantId) throws SQLException {
        try (Connection connection = connectionSupplier.get()) {
            User staff = userDao.findById(connection, consultantId);
            if (staff == null || staff.getStatus() == null || !staff.getStatus().isActive()) {
                throw new AuthenticationException("Authenticated staff session is no longer valid.");
            }

            Map<String, Long> metrics = applicationDao.getConsultantApplicationMetrics(connection, consultantId);
            long pendingReviews = documentDao.countPendingReviewForConsultant(connection, consultantId);
            metrics.put("pendingDocumentReviews", Long.valueOf(pendingReviews));

            List<Application> recentApps = applicationDao.findByConsultantId(connection, consultantId, null, null, 0, 5);
            List<AssignedStudentSummaryResponse> recentStudents =
                    assignmentDao.findAssignedStudentsByConsultant(connection, consultantId, null, 0, 5);

            boolean isAdmin = staff.getRole() == Role.ADMIN;
            Map<String, Object> permissions = new LinkedHashMap<String, Object>();
            permissions.put("canAccessConsultantWorkspace", Boolean.TRUE);
            permissions.put("canAccessAdminWorkspace", Boolean.valueOf(isAdmin));
            permissions.put("canManageStudents", Boolean.valueOf(isAdmin));
            permissions.put("canManageConsultants", Boolean.valueOf(isAdmin));

            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("workspace", "STAFF_OPERATIONS");
            payload.put("account", UserResponse.fromUser(staff));
            payload.put("permissions", permissions);
            payload.put("metrics", metrics);
            payload.put("recentApplications", toAppResponses(recentApps));
            payload.put("recentAssignedStudents", recentStudents);
            payload.put("authorizedCapabilities", Arrays.asList(
                    "Assigned Student Profile Inspection",
                    "Application Progress & Status Updates",
                    "Document Review & Verification"
            ));
            return payload;
        }
    }

    @Override
    public PagedResponse<AssignedStudentSummaryResponse> listConsultantAssignedStudents(long consultantId,
                                                                                      String search,
                                                                                      int page,
                                                                                      int pageSize) throws SQLException {
        int safePage = Math.max(1, page);
        int safeSize = normalizePageSize(pageSize);
        int offset = (safePage - 1) * safeSize;

        try (Connection connection = connectionSupplier.get()) {
            long total = assignmentDao.countAssignedStudentsByConsultant(connection, consultantId, search);
            List<AssignedStudentSummaryResponse> rows = total == 0
                    ? Collections.<AssignedStudentSummaryResponse>emptyList()
                    : assignmentDao.findAssignedStudentsByConsultant(connection, consultantId, search, offset, safeSize);
            return new PagedResponse<AssignedStudentSummaryResponse>(rows, safePage, safeSize, total);
        }
    }

    @Override
    public Map<String, Object> getConsultantAssignedStudentDetail(long consultantId, long studentId) throws SQLException {
        try (Connection connection = connectionSupplier.get()) {
            User student = userDao.findByIdAndRole(connection, studentId, Role.STUDENT);
            if (student == null) {
                throw new ResourceNotFoundException("Student account not found.");
            }
            if (!assignmentDao.isConsultantAssignedToStudent(connection, consultantId, studentId)) {
                throw new ForbiddenException("You do not have permission to view an unassigned student.");
            }

            StudentProfile profile = profileDao.findByUserId(connection, studentId);
            List<Application> allApps = applicationDao.findByStudentId(connection, studentId, null, 0, 50);
            List<Application> assignedApps = new ArrayList<Application>();
            for (Application app : allApps) {
                if (app.getAssignedConsultantId() != null && app.getAssignedConsultantId().longValue() == consultantId) {
                    assignedApps.add(app);
                }
            }

            List<ApplicationDocument> allStudentDocs = documentDao.findByStudentId(connection, studentId, null, null, null);
            List<ApplicationDocument> permittedDocs = filterDocumentsForConsultant(allStudentDocs, assignedApps);

            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("student", UserResponse.fromUser(student));
            payload.put("profile", StudentProfileResponse.from(student, profile));
            payload.put("applications", toAppResponses(assignedApps));
            payload.put("documents", toDocumentResponses(permittedDocs));
            return payload;
        }
    }

    @Override
    public PagedResponse<ApplicationResponse> listConsultantApplications(long consultantId,
                                                                         String search,
                                                                         String status,
                                                                         int page,
                                                                         int pageSize) throws SQLException {
        ApplicationStatus statusFilter = parseOptionalStatus(status);
        int safePage = Math.max(1, page);
        int safeSize = normalizePageSize(pageSize);
        int offset = (safePage - 1) * safeSize;

        try (Connection connection = connectionSupplier.get()) {
            long total = applicationDao.countByConsultantId(connection, consultantId, search, statusFilter);
            List<Application> rows = total == 0
                    ? Collections.<Application>emptyList()
                    : applicationDao.findByConsultantId(connection, consultantId, search, statusFilter, offset, safeSize);
            return new PagedResponse<ApplicationResponse>(toAppResponses(rows), safePage, safeSize, total);
        }
    }

    @Override
    public Map<String, Object> getConsultantApplicationDetail(long consultantId, long applicationId) throws SQLException {
        try (Connection connection = connectionSupplier.get()) {
            Application app = applicationDao.findById(connection, applicationId);
            if (app == null) {
                throw new ResourceNotFoundException("Application not found.");
            }
            if (app.getAssignedConsultantId() == null || app.getAssignedConsultantId().longValue() != consultantId) {
                throw new ForbiddenException("You do not have permission to view an application that is not assigned to you.");
            }

            User student = userDao.findByIdAndRole(connection, app.getStudentId(), Role.STUDENT);
            StudentProfile profile = profileDao.findByUserId(connection, app.getStudentId());
            List<ApplicationStatusHistory> history = applicationDao.findStatusHistoryByApplicationId(connection, applicationId);
            List<ConsultantAssignment> assignments = assignmentDao.findHistoryByApplicationId(connection, applicationId);
            List<ApplicationDocument> docs = documentDao.findByApplicationId(connection, applicationId);

            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("application", ApplicationResponse.from(app));
            payload.put("studentProfile", StudentProfileResponse.from(student, profile));
            payload.put("statusHistory", toHistoryResponses(history));
            payload.put("assignmentHistory", toAssignmentResponses(assignments));
            payload.put("documents", toDocumentResponses(docs));
            return payload;
        }
    }

    @Override
    public Map<String, Object> updateConsultantApplicationStatus(long consultantId,
                                                                 long applicationId,
                                                                 UpdateApplicationStatusRequest request) throws SQLException {
        Map<String, String> fieldErrors = ConsultancyValidationUtil.validateStatusUpdate(request, Role.CONSULTANT);
        if (!fieldErrors.isEmpty()) {
            throw new ValidationException("Please correct the highlighted status fields.", fieldErrors);
        }

        ApplicationStatus nextStatus = ApplicationStatus.fromString(request.getStatus());
        String note = ConsultancyValidationUtil.normalizeText(request.getNote());

        try (Connection connection = connectionSupplier.get()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Application existing = applicationDao.findById(connection, applicationId);
                if (existing == null) {
                    throw new ResourceNotFoundException("Application not found.");
                }
                if (existing.getAssignedConsultantId() == null
                        || existing.getAssignedConsultantId().longValue() != consultantId) {
                    throw new ForbiddenException("You do not have permission to update an unassigned application.");
                }
                if (existing.getStatus() == ApplicationStatus.CANCELLED) {
                    throw new ValidationException("Cancelled applications cannot be updated by consultants.",
                            Collections.singletonMap("status", "Cancelled applications can only be reopened by an administrator."));
                }

                applyStatusTransition(connection, existing, nextStatus, consultantId, note);
                Application updated = applicationDao.findById(connection, applicationId);
                List<ApplicationStatusHistory> history = applicationDao.findStatusHistoryByApplicationId(connection, applicationId);
                connection.commit();

                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("application", ApplicationResponse.from(updated));
                result.put("statusHistory", toHistoryResponses(history));
                return result;
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

    @Override
    public PagedResponse<ApplicationResponse> listAdminApplications(String search,
                                                                    String status,
                                                                    String degreeLevel,
                                                                    Long consultantId,
                                                                    boolean unassignedOnly,
                                                                    int page,
                                                                    int pageSize) throws SQLException {
        ApplicationStatus statusFilter = parseOptionalStatus(status);
        DegreeLevel degreeFilter = parseOptionalDegree(degreeLevel);
        int safePage = Math.max(1, page);
        int safeSize = normalizePageSize(pageSize);
        int offset = (safePage - 1) * safeSize;

        try (Connection connection = connectionSupplier.get()) {
            long total = applicationDao.countAll(connection, search, statusFilter, degreeFilter, consultantId, unassignedOnly);
            List<Application> rows = total == 0
                    ? Collections.<Application>emptyList()
                    : applicationDao.findAll(connection, search, statusFilter, degreeFilter, consultantId, unassignedOnly, offset, safeSize);
            return new PagedResponse<ApplicationResponse>(toAppResponses(rows), safePage, safeSize, total);
        }
    }

    @Override
    public Map<String, Object> getAdminApplicationDetail(long applicationId) throws SQLException {
        try (Connection connection = connectionSupplier.get()) {
            Application app = applicationDao.findById(connection, applicationId);
            if (app == null) {
                throw new ResourceNotFoundException("Application not found.");
            }

            User student = userDao.findByIdAndRole(connection, app.getStudentId(), Role.STUDENT);
            StudentProfile profile = profileDao.findByUserId(connection, app.getStudentId());
            List<ApplicationStatusHistory> history = applicationDao.findStatusHistoryByApplicationId(connection, applicationId);
            List<ConsultantAssignment> assignments = assignmentDao.findHistoryByApplicationId(connection, applicationId);
            List<ApplicationDocument> docs = documentDao.findByApplicationId(connection, applicationId);

            List<User> activeConsultants = userDao.findUsersByRole(Role.CONSULTANT, null, AccountStatus.ACTIVE, 0, 100);
            List<ConsultantSummaryResponse> consultantOptions = new ArrayList<ConsultantSummaryResponse>(activeConsultants.size());
            for (User c : activeConsultants) {
                ConsultantSummaryResponse dto = ConsultantSummaryResponse.fromUser(c);
                dto.setAssignmentTrackingAvailable(true);
                consultantOptions.add(dto);
            }

            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("application", ApplicationResponse.from(app));
            payload.put("student", UserResponse.fromUser(student));
            payload.put("studentProfile", StudentProfileResponse.from(student, profile));
            payload.put("statusHistory", toHistoryResponses(history));
            payload.put("assignmentHistory", toAssignmentResponses(assignments));
            payload.put("documents", toDocumentResponses(docs));
            payload.put("availableConsultants", consultantOptions);
            return payload;
        }
    }

    @Override
    public Map<String, Object> updateAdminApplicationStatus(long adminId,
                                                            long applicationId,
                                                            UpdateApplicationStatusRequest request) throws SQLException {
        Map<String, String> fieldErrors = ConsultancyValidationUtil.validateStatusUpdate(request, Role.ADMIN);
        if (!fieldErrors.isEmpty()) {
            throw new ValidationException("Please correct the highlighted status fields.", fieldErrors);
        }

        ApplicationStatus nextStatus = ApplicationStatus.fromString(request.getStatus());
        String note = ConsultancyValidationUtil.normalizeText(request.getNote());

        try (Connection connection = connectionSupplier.get()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Application existing = applicationDao.findById(connection, applicationId);
                if (existing == null) {
                    throw new ResourceNotFoundException("Application not found.");
                }

                applyStatusTransition(connection, existing, nextStatus, adminId, note);
                Application updated = applicationDao.findById(connection, applicationId);
                List<ApplicationStatusHistory> history = applicationDao.findStatusHistoryByApplicationId(connection, applicationId);
                connection.commit();

                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("application", ApplicationResponse.from(updated));
                result.put("statusHistory", toHistoryResponses(history));
                return result;
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

    @Override
    public Map<String, Object> assignConsultant(long adminId,
                                                long applicationId,
                                                AssignConsultantRequest request) throws SQLException {
        Map<String, String> fieldErrors = ConsultancyValidationUtil.validateAssignConsultant(request);
        if (!fieldErrors.isEmpty()) {
            throw new ValidationException("Please correct the highlighted assignment fields.", fieldErrors);
        }

        long targetConsultantId = request.getConsultantId().longValue();
        String note = ConsultancyValidationUtil.normalizeText(request.getNote());

        try (Connection connection = connectionSupplier.get()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Application existing = applicationDao.findById(connection, applicationId);
                if (existing == null) {
                    throw new ResourceNotFoundException("Application not found.");
                }

                User consultant = userDao.findByIdAndRole(connection, targetConsultantId, Role.CONSULTANT);
                if (consultant == null) {
                    throw new ResourceNotFoundException("Target consultant account not found.");
                }
                if (consultant.getStatus() == null || !consultant.getStatus().isActive()) {
                    throw new ValidationException("Only active consultants can be assigned to applications.",
                            Collections.singletonMap("consultantId", "Selected consultant account is not active."));
                }

                boolean isReassignment = existing.getAssignedConsultantId() != null;
                if (isReassignment && existing.getAssignedConsultantId().longValue() == targetConsultantId) {
                    throw new ValidationException("This consultant is already assigned to the application.",
                            Collections.singletonMap("consultantId", "Consultant is already assigned."));
                }

                if (isReassignment) {
                    assignmentDao.deactivateActiveForApplication(connection, applicationId, adminId);
                }

                ConsultantAssignment assignment = new ConsultantAssignment();
                assignment.setApplicationId(applicationId);
                assignment.setStudentId(existing.getStudentId());
                assignment.setConsultantId(targetConsultantId);
                assignment.setAssignedByUserId(adminId);
                assignment.setAction(isReassignment ? AssignmentAction.REASSIGNED : AssignmentAction.ASSIGNED);
                assignment.setActive(true);
                assignment.setNote(note);
                assignmentDao.insert(connection, assignment);

                applicationDao.updateAssignedConsultant(connection, applicationId, Long.valueOf(targetConsultantId));

                if (existing.getStatus() == ApplicationStatus.SUBMITTED || existing.getStatus() == ApplicationStatus.UNDER_REVIEW) {
                    String statusNote = note != null ? note : "Assigned to consultant " + consultant.getFullName();
                    applyStatusTransition(connection, existing, ApplicationStatus.ASSIGNED, adminId, statusNote);
                }

                Application updated = applicationDao.findById(connection, applicationId);
                List<ApplicationStatusHistory> statusHistory = applicationDao.findStatusHistoryByApplicationId(connection, applicationId);
                List<ConsultantAssignment> assignmentHistory = assignmentDao.findHistoryByApplicationId(connection, applicationId);
                connection.commit();

                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("application", ApplicationResponse.from(updated));
                result.put("statusHistory", toHistoryResponses(statusHistory));
                result.put("assignmentHistory", toAssignmentResponses(assignmentHistory));
                return result;
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

    @Override
    public Map<String, Object> removeConsultantAssignment(long adminId,
                                                          long applicationId,
                                                          String rawNote) throws SQLException {
        String note = ConsultancyValidationUtil.normalizeText(rawNote);
        if (ConsultancyValidationUtil.hasUnsafeContent(note)) {
            throw new ValidationException("Invalid note characters.",
                    Collections.singletonMap("note", "Note contains invalid characters."));
        }

        try (Connection connection = connectionSupplier.get()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Application existing = applicationDao.findById(connection, applicationId);
                if (existing == null) {
                    throw new ResourceNotFoundException("Application not found.");
                }
                if (existing.getAssignedConsultantId() == null) {
                    throw new ValidationException("No consultant is currently assigned to this application.",
                            Collections.singletonMap("consultantId", "Application does not have an assigned consultant."));
                }

                Long previousConsultantId = existing.getAssignedConsultantId();
                assignmentDao.deactivateActiveForApplication(connection, applicationId, adminId);

                ConsultantAssignment removalRecord = new ConsultantAssignment();
                removalRecord.setApplicationId(applicationId);
                removalRecord.setStudentId(existing.getStudentId());
                removalRecord.setConsultantId(previousConsultantId);
                removalRecord.setAssignedByUserId(adminId);
                removalRecord.setAction(AssignmentAction.REMOVED);
                removalRecord.setActive(false);
                removalRecord.setNote(note != null ? note : "Consultant assignment removed by administrator.");
                removalRecord.setUnassignedByUserId(Long.valueOf(adminId));
                removalRecord.setUnassignedAt(java.time.Instant.now());
                assignmentDao.insert(connection, removalRecord);

                applicationDao.updateAssignedConsultant(connection, applicationId, null);

                if (existing.getStatus() == ApplicationStatus.ASSIGNED) {
                    String statusNote = note != null ? note : "Consultant assignment removed; returned to under review.";
                    applyStatusTransition(connection, existing, ApplicationStatus.UNDER_REVIEW, adminId, statusNote);
                }

                Application updated = applicationDao.findById(connection, applicationId);
                List<ApplicationStatusHistory> statusHistory = applicationDao.findStatusHistoryByApplicationId(connection, applicationId);
                List<ConsultantAssignment> assignmentHistory = assignmentDao.findHistoryByApplicationId(connection, applicationId);
                connection.commit();

                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("application", ApplicationResponse.from(updated));
                result.put("statusHistory", toHistoryResponses(statusHistory));
                result.put("assignmentHistory", toAssignmentResponses(assignmentHistory));
                return result;
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

    private void applyStatusTransition(Connection connection,
                                       Application existing,
                                       ApplicationStatus nextStatus,
                                       long actorId,
                                       String note) throws SQLException {
        if (existing.getStatus() == nextStatus && note == null) {
            return;
        }
        if (existing.getStatus() != nextStatus) {
            applicationDao.updateStatus(connection, existing.getId(), nextStatus);
        }
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplicationId(existing.getId());
        history.setPreviousStatus(existing.getStatus());
        history.setNewStatus(nextStatus);
        history.setChangedByUserId(actorId);
        history.setNote(note);
        applicationDao.addStatusHistory(connection, history);
    }

    private static List<ApplicationDocument> filterDocumentsForConsultant(List<ApplicationDocument> docs,
                                                                           List<Application> assignedApps) {
        List<Long> assignedAppIds = new ArrayList<Long>(assignedApps.size());
        for (Application app : assignedApps) {
            assignedAppIds.add(app.getId());
        }
        List<ApplicationDocument> permitted = new ArrayList<ApplicationDocument>();
        for (ApplicationDocument doc : docs) {
            if (doc.getApplicationId() == null || assignedAppIds.contains(doc.getApplicationId())) {
                permitted.add(doc);
            }
        }
        return permitted;
    }

    private static List<ApplicationResponse> toAppResponses(List<Application> apps) {
        List<ApplicationResponse> out = new ArrayList<ApplicationResponse>(apps.size());
        for (Application app : apps) {
            out.add(ApplicationResponse.from(app));
        }
        return out;
    }

    private static List<ApplicationStatusHistoryResponse> toHistoryResponses(List<ApplicationStatusHistory> items) {
        List<ApplicationStatusHistoryResponse> out = new ArrayList<ApplicationStatusHistoryResponse>(items.size());
        for (ApplicationStatusHistory item : items) {
            out.add(ApplicationStatusHistoryResponse.from(item));
        }
        return out;
    }

    private static List<ConsultantAssignmentResponse> toAssignmentResponses(List<ConsultantAssignment> items) {
        List<ConsultantAssignmentResponse> out = new ArrayList<ConsultantAssignmentResponse>(items.size());
        for (ConsultantAssignment item : items) {
            out.add(ConsultantAssignmentResponse.from(item));
        }
        return out;
    }

    private static List<ApplicationDocumentResponse> toDocumentResponses(List<ApplicationDocument> docs) {
        List<ApplicationDocumentResponse> out = new ArrayList<ApplicationDocumentResponse>(docs.size());
        for (ApplicationDocument doc : docs) {
            out.add(ApplicationDocumentResponse.from(doc));
        }
        return out;
    }

    private static ApplicationStatus parseOptionalStatus(String raw) {
        String clean = ConsultancyValidationUtil.normalizeText(raw);
        if (clean == null || "ALL".equalsIgnoreCase(clean)) {
            return null;
        }
        try {
            return ApplicationStatus.fromString(clean);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Invalid application status filter.",
                    Collections.singletonMap("status", "Unsupported application status filter."));
        }
    }

    private static DegreeLevel parseOptionalDegree(String raw) {
        String clean = ConsultancyValidationUtil.normalizeText(raw);
        if (clean == null || "ALL".equalsIgnoreCase(clean)) {
            return null;
        }
        try {
            return DegreeLevel.fromString(clean);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Invalid degree level filter.",
                    Collections.singletonMap("degreeLevel", "Degree level must be BSc, MSc, or ALL."));
        }
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
