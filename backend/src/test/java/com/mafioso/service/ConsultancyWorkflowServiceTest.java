package com.mafioso.service;

import com.mafioso.dao.ApplicationDAO;
import com.mafioso.dao.ApplicationDocumentDAO;
import com.mafioso.dao.ConsultantAssignmentDAO;
import com.mafioso.dao.StudentProfileDAO;
import com.mafioso.dao.UserDAO;
import com.mafioso.dto.ApplicationDocumentResponse;
import com.mafioso.dto.ApplicationResponse;
import com.mafioso.dto.AssignConsultantRequest;
import com.mafioso.dto.AssignedStudentSummaryResponse;
import com.mafioso.dto.CreateApplicationRequest;
import com.mafioso.dto.DashboardMetricsResponse;
import com.mafioso.dto.ReviewDocumentRequest;
import com.mafioso.dto.StudentProfileRequest;
import com.mafioso.dto.StudentProfileResponse;
import com.mafioso.dto.UpdateApplicationStatusRequest;
import com.mafioso.model.AccountStatus;
import com.mafioso.model.Application;
import com.mafioso.model.ApplicationDocument;
import com.mafioso.model.ApplicationStatus;
import com.mafioso.model.ApplicationStatusHistory;
import com.mafioso.model.AuthenticatedUser;
import com.mafioso.model.ConsultantAssignment;
import com.mafioso.model.DegreeLevel;
import com.mafioso.model.DocumentCategory;
import com.mafioso.model.DocumentReviewStatus;
import com.mafioso.model.Role;
import com.mafioso.model.StudentProfile;
import com.mafioso.model.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests covering Milestone 4 consultancy workflow: student profile, application reference generation,
 * status history, consultant assignment/reassignment/removal, ownership/assignment access boundaries,
 * and document upload/review/download authorization.
 */
public class ConsultancyWorkflowServiceTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private InMemoryUserDao userDao;
    private InMemoryProfileDao profileDao;
    private InMemoryApplicationDao applicationDao;
    private InMemoryAssignmentDao assignmentDao;
    private InMemoryDocumentDao documentDao;

    private StudentProfileServiceImpl profileService;
    private ApplicationServiceImpl applicationService;
    private DocumentServiceImpl documentService;

    private User admin;
    private User studentA;
    private User studentB;
    private User consultant1;
    private User consultant2;

    @Before
    public void setUp() throws Exception {
        userDao = new InMemoryUserDao();
        profileDao = new InMemoryProfileDao();
        applicationDao = new InMemoryApplicationDao();
        assignmentDao = new InMemoryAssignmentDao(applicationDao);
        documentDao = new InMemoryDocumentDao();

        Connection dummyConn = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getAutoCommit".equals(name)) return Boolean.TRUE;
                    if ("isClosed".equals(name)) return Boolean.FALSE;
                    return null;
                }
        );
        AuthServiceImpl.ConnectionSupplier connSupplier = () -> dummyConn;

        Path storageRoot = tempFolder.newFolder("vault").toPath();
        profileService = new StudentProfileServiceImpl(userDao, profileDao, connSupplier);
        applicationService = new ApplicationServiceImpl(
                applicationDao, assignmentDao, documentDao, profileDao, userDao, connSupplier);
        documentService = new DocumentServiceImpl(
                documentDao, applicationDao, assignmentDao, connSupplier, storageRoot, 5L * 1024L * 1024L);

        admin = seedUser("Admin User", "admin@mafioso.test", Role.ADMIN);
        studentA = seedUser("Ayesha Khan", "ayesha@mafioso.test", Role.STUDENT);
        studentB = seedUser("Tariq Hasan", "tariq@mafioso.test", Role.STUDENT);
        consultant1 = seedUser("Advisor One", "advisor1@mafioso.test", Role.CONSULTANT);
        consultant2 = seedUser("Advisor Two", "advisor2@mafioso.test", Role.CONSULTANT);
    }

    @Test
    public void createsAndUpdatesStudentProfileWithCompletionSummary() throws Exception {
        StudentProfileRequest req = new StudentProfileRequest();
        req.setFullName("Ayesha Rahman Khan");
        req.setPhoneNumber("+8801711000111");
        req.setDateOfBirth("2002-05-14");
        req.setNationality("Bangladeshi");
        req.setPresentAddress("Dhanmondi, Dhaka");
        req.setPassportNumber("BD9876543");
        req.setPassportIssueDate("2022-01-10");
        req.setPassportExpiryDate("2032-01-09");
        req.setHighestQualification("Bachelor of Science");
        req.setIeltsScore("7.0");
        req.setPreferredDegreeLevel("MSc");
        req.setPreferredSubject("Computer Science");
        req.setEstimatedBudget("22000");
        req.setBudgetCurrency("GBP");
        req.setFundingSource("Family Sponsor");

        StudentProfileResponse saved = profileService.updateOwnProfile(studentA.getId(), req);
        assertEquals("Ayesha Rahman Khan", saved.getFullName());
        assertEquals("MSc", saved.getPreferredDegreeLevel());
        assertEquals(100, saved.getCompletionPercent());
        assertTrue(saved.getMissingFields().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createsApplicationGeneratesUniqueReferenceAndTracksWorkflow() throws Exception {
        CreateApplicationRequest createReq = new CreateApplicationRequest();
        createReq.setDegreeLevel("MSc");
        createReq.setPreferredSubject("Data Science");
        createReq.setIntendedIntake("September 2026");
        createReq.setPreferredUniversity("University of Manchester");

        ApplicationResponse app1 = applicationService.createApplication(studentA.getId(), createReq);
        ApplicationResponse app2 = applicationService.createApplication(studentB.getId(), createReq);

        assertTrue(app1.getReferenceNumber().matches("MAF-\\d{4}-000001"));
        assertTrue(app2.getReferenceNumber().matches("MAF-\\d{4}-000002"));
        assertEquals("SUBMITTED", app1.getStatus());

        // Admin assigns consultant1 -> automatically moves SUBMITTED -> ASSIGNED
        AssignConsultantRequest assignReq = new AssignConsultantRequest();
        assignReq.setConsultantId(consultant1.getId());
        assignReq.setNote("Assigned for UK MSc CS caseload.");
        Map<String, Object> assignedResult = applicationService.assignConsultant(admin.getId(), app1.getId(), assignReq);
        ApplicationResponse assignedApp = (ApplicationResponse) assignedResult.get("application");
        assertEquals("ASSIGNED", assignedApp.getStatus());
        assertEquals(consultant1.getId(), assignedApp.getAssignedConsultant().getId());

        // Consultant1 updates status -> IN_PROGRESS
        UpdateApplicationStatusRequest statusReq = new UpdateApplicationStatusRequest();
        statusReq.setStatus("IN_PROGRESS");
        statusReq.setNote("Statement of purpose verified.");
        Map<String, Object> updatedResult =
                applicationService.updateConsultantApplicationStatus(consultant1.getId(), app1.getId(), statusReq);
        ApplicationResponse updatedApp = (ApplicationResponse) updatedResult.get("application");
        assertEquals("IN_PROGRESS", updatedApp.getStatus());

        List<Object> history = (List<Object>) updatedResult.get("statusHistory");
        assertEquals(3, history.size());

        // Admin removes assignment
        Map<String, Object> removedResult =
                applicationService.removeConsultantAssignment(admin.getId(), app1.getId(), "Rebalancing caseload.");
        ApplicationResponse unassignedApp = (ApplicationResponse) removedResult.get("application");
        assertNull(unassignedApp.getAssignedConsultant());
    }

    @Test(expected = ForbiddenException.class)
    public void preventsStudentFromViewingAnotherStudentsApplication() throws Exception {
        CreateApplicationRequest createReq = new CreateApplicationRequest();
        createReq.setDegreeLevel("BSc");
        createReq.setPreferredSubject("Accounting and Finance");
        createReq.setIntendedIntake("January 2027");
        createReq.setPreferredUniversity("University of Leeds");

        ApplicationResponse created = applicationService.createApplication(studentA.getId(), createReq);
        applicationService.getStudentApplicationDetail(studentB.getId(), created.getId());
    }

    @Test(expected = ForbiddenException.class)
    public void preventsConsultantFromAccessingUnassignedApplication() throws Exception {
        CreateApplicationRequest createReq = new CreateApplicationRequest();
        createReq.setDegreeLevel("MSc");
        createReq.setPreferredSubject("Cyber Security");
        createReq.setIntendedIntake("September 2026");
        createReq.setPreferredUniversity("University of Southampton");

        ApplicationResponse created = applicationService.createApplication(studentA.getId(), createReq);
        AssignConsultantRequest assignReq = new AssignConsultantRequest();
        assignReq.setConsultantId(consultant1.getId());
        applicationService.assignConsultant(admin.getId(), created.getId(), assignReq);

        // consultant2 must be denied
        applicationService.getConsultantApplicationDetail(consultant2.getId(), created.getId());
    }

    @Test(expected = ValidationException.class)
    public void consultantCannotSetAdministrativeCancelledStatus() throws Exception {
        CreateApplicationRequest createReq = new CreateApplicationRequest();
        createReq.setDegreeLevel("MSc");
        createReq.setPreferredSubject("Software Engineering");
        createReq.setIntendedIntake("September 2026");
        createReq.setPreferredUniversity("University of Glasgow");

        ApplicationResponse created = applicationService.createApplication(studentA.getId(), createReq);
        AssignConsultantRequest assignReq = new AssignConsultantRequest();
        assignReq.setConsultantId(consultant1.getId());
        applicationService.assignConsultant(admin.getId(), created.getId(), assignReq);

        UpdateApplicationStatusRequest badReq = new UpdateApplicationStatusRequest();
        badReq.setStatus("CANCELLED");
        applicationService.updateConsultantApplicationStatus(consultant1.getId(), created.getId(), badReq);
    }

    @Test
    public void uploadsReviewsAndAuthorizesDocumentDownloads() throws Exception {
        CreateApplicationRequest createReq = new CreateApplicationRequest();
        createReq.setDegreeLevel("MSc");
        createReq.setPreferredSubject("AI");
        createReq.setIntendedIntake("September 2026");
        createReq.setPreferredUniversity("University of Edinburgh");

        ApplicationResponse app = applicationService.createApplication(studentA.getId(), createReq);
        AssignConsultantRequest assignReq = new AssignConsultantRequest();
        assignReq.setConsultantId(consultant1.getId());
        applicationService.assignConsultant(admin.getId(), app.getId(), assignReq);

        byte[] pdfBytes = "%PDF-1.4\n1 0 obj\n<<>>\nendobj\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        ApplicationDocumentResponse uploaded = documentService.uploadStudentDocument(
                studentA.getId(),
                app.getId(),
                "PASSPORT",
                "passport_scan.pdf",
                new ByteArrayInputStream(pdfBytes)
        );
        assertEquals("PENDING", uploaded.getReviewStatus());

        // Reject requires note
        ReviewDocumentRequest missingNoteReject = new ReviewDocumentRequest();
        missingNoteReject.setReviewStatus("REJECTED");
        boolean threw = false;
        try {
            documentService.reviewDocumentAsConsultant(consultant1.getId(), uploaded.getId(), missingNoteReject);
        } catch (ValidationException ex) {
            threw = true;
        }
        assertTrue(threw);

        // Approve as assigned consultant
        ReviewDocumentRequest approveReq = new ReviewDocumentRequest();
        approveReq.setReviewStatus("APPROVED");
        approveReq.setReviewNote("Clear scan verified.");
        ApplicationDocumentResponse approved =
                documentService.reviewDocumentAsConsultant(consultant1.getId(), uploaded.getId(), approveReq);
        assertEquals("APPROVED", approved.getReviewStatus());

        // Download authorized for owner & assigned consultant, denied for unrelated student/consultant
        assertNotNull(documentService.authorizeDownload(
                new AuthenticatedUser(studentA.getId(), studentA.getFullName(), studentA.getEmail(), Role.STUDENT),
                uploaded.getId()));
        assertNotNull(documentService.authorizeDownload(
                new AuthenticatedUser(consultant1.getId(), consultant1.getFullName(), consultant1.getEmail(), Role.CONSULTANT),
                uploaded.getId()));

        boolean deniedOtherStudent = false;
        try {
            documentService.authorizeDownload(
                    new AuthenticatedUser(studentB.getId(), studentB.getFullName(), studentB.getEmail(), Role.STUDENT),
                    uploaded.getId());
        } catch (ForbiddenException ex) {
            deniedOtherStudent = true;
        }
        assertTrue(deniedOtherStudent);

        boolean deniedOtherConsultant = false;
        try {
            documentService.authorizeDownload(
                    new AuthenticatedUser(consultant2.getId(), consultant2.getFullName(), consultant2.getEmail(), Role.CONSULTANT),
                    uploaded.getId());
        } catch (ForbiddenException ex) {
            deniedOtherConsultant = true;
        }
        assertTrue(deniedOtherConsultant);
    }

    private User seedUser(String name, String email, Role role) {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPasswordHash("hashed");
        u.setRole(role);
        u.setStatus(AccountStatus.ACTIVE);
        return userDao.createUser(null, u);
    }

    // -------------------------------------------------------------------------
    // Lightweight in-memory DAOs for deterministic unit testing
    // -------------------------------------------------------------------------

    private static final class InMemoryUserDao implements UserDAO {
        private final AtomicLong seq = new AtomicLong(1L);
        private final Map<Long, User> byId = new ConcurrentHashMap<Long, User>();

        @Override public User findByEmail(String e) { return null; }
        @Override public User findByEmail(Connection c, String e) { return null; }
        @Override public User findById(long id) { return byId.get(id); }
        @Override public User findById(Connection c, long id) { return byId.get(id); }
        @Override public User findByIdAndRole(Connection c, long id, Role r) {
            User u = byId.get(id);
            return (u != null && u.getRole() == r) ? u : null;
        }
        @Override public boolean existsByEmail(Connection c, String e) { return false; }
        @Override public boolean existsAnyByRole(Connection c, Role r) { return false; }
        @Override public User createUser(Connection c, User u) {
            long id = seq.getAndIncrement();
            u.setId(id);
            u.setCreatedAt(Instant.now());
            byId.put(id, u);
            return u;
        }
        @Override public void updateLastLoginAt(long id, Instant t) {}
        @Override public void updateLastLoginAt(Connection c, long id, Instant t) {}
        @Override public boolean updateAccountStatus(Connection c, long id, Role r, AccountStatus s) { return false; }
        @Override public Map<Role, Long> countUsersByRole() { return new EnumMap<Role, Long>(Role.class); }
        @Override public DashboardMetricsResponse getDashboardMetrics() { return new DashboardMetricsResponse(); }
        @Override public List<User> findUsersByRole(Role r, String s, AccountStatus st, int off, int lim) {
            List<User> out = new ArrayList<User>();
            for (User u : byId.values()) {
                if (u.getRole() == r && (st == null || u.getStatus() == st)) out.add(u);
            }
            return out;
        }
        @Override public long countUsersByRoleAndFilter(Role r, String s, AccountStatus st) { return 0L; }
    }

    private final class InMemoryProfileDao implements StudentProfileDAO {
        private final Map<Long, StudentProfile> byUser = new ConcurrentHashMap<Long, StudentProfile>();

        @Override public StudentProfile findByUserId(Connection c, long userId) { return byUser.get(userId); }
        @Override public StudentProfile upsert(Connection c, StudentProfile p) {
            p.setUpdatedAt(Instant.now());
            byUser.put(p.getUserId(), p);
            return p;
        }
        @Override public boolean updateUserIdentity(Connection c, long userId, String fullName, String phone) {
            User u = userDao.findById(userId);
            if (u == null) return false;
            u.setFullName(fullName);
            u.setPhoneNumber(phone);
            return true;
        }
    }

    private final class InMemoryApplicationDao implements ApplicationDAO {
        private final AtomicLong seq = new AtomicLong(1L);
        private final AtomicLong refSeq = new AtomicLong(1L);
        private final Map<Long, Application> byId = new ConcurrentHashMap<Long, Application>();
        private final List<ApplicationStatusHistory> history = new ArrayList<ApplicationStatusHistory>();

        @Override public String nextReferenceNumber(Connection c, int year) {
            return String.format("MAF-%04d-%06d", year, refSeq.getAndIncrement());
        }
        @Override public Application create(Connection c, Application app) {
            long id = seq.getAndIncrement();
            app.setId(id);
            app.setSubmittedAt(Instant.now());
            app.setCreatedAt(Instant.now());
            app.setUpdatedAt(Instant.now());
            User stu = userDao.findById(app.getStudentId());
            if (stu != null) {
                app.setStudentName(stu.getFullName());
                app.setStudentEmail(stu.getEmail());
            }
            byId.put(id, app);
            return app;
        }
        @Override public Application findById(Connection c, long id) { return byId.get(id); }
        @Override public List<Application> findByStudentId(Connection c, long sid, ApplicationStatus st, int off, int lim) {
            List<Application> out = new ArrayList<Application>();
            for (Application a : byId.values()) if (a.getStudentId().equals(sid)) out.add(a);
            return out;
        }
        @Override public long countByStudentId(Connection c, long sid, ApplicationStatus st) {
            return findByStudentId(c, sid, st, 0, 100).size();
        }
        @Override public List<Application> findByConsultantId(Connection c, long cid, String q, ApplicationStatus st, int off, int lim) {
            List<Application> out = new ArrayList<Application>();
            for (Application a : byId.values()) if (cid == (a.getAssignedConsultantId() != null ? a.getAssignedConsultantId() : -1L)) out.add(a);
            return out;
        }
        @Override public long countByConsultantId(Connection c, long cid, String q, ApplicationStatus st) {
            return findByConsultantId(c, cid, q, st, 0, 100).size();
        }
        @Override public List<Application> findAll(Connection c, String q, ApplicationStatus st, DegreeLevel d, Long cid, boolean un, int off, int lim) {
            return new ArrayList<Application>(byId.values());
        }
        @Override public long countAll(Connection c, String q, ApplicationStatus st, DegreeLevel d, Long cid, boolean un) {
            return byId.size();
        }
        @Override public boolean updateStatus(Connection c, long id, ApplicationStatus s) {
            Application a = byId.get(id);
            if (a == null) return false;
            a.setStatus(s);
            return true;
        }
        @Override public boolean updateAssignedConsultant(Connection c, long id, Long cid) {
            Application a = byId.get(id);
            if (a == null) return false;
            a.setAssignedConsultantId(cid);
            if (cid != null) {
                User con = userDao.findById(cid);
                a.setAssignedConsultantName(con.getFullName());
                a.setAssignedConsultantEmail(con.getEmail());
                a.setAssignedConsultantStatus(con.getStatus().name());
            } else {
                a.setAssignedConsultantName(null);
                a.setAssignedConsultantEmail(null);
                a.setAssignedConsultantStatus(null);
            }
            return true;
        }
        @Override public void addStatusHistory(Connection c, ApplicationStatusHistory h) {
            h.setId((long) (history.size() + 1));
            h.setChangedAt(Instant.now());
            User actor = userDao.findById(h.getChangedByUserId());
            if (actor != null) {
                h.setChangedByName(actor.getFullName());
                h.setChangedByRole(actor.getRole().name());
            }
            history.add(h);
        }
        @Override public List<ApplicationStatusHistory> findStatusHistoryByApplicationId(Connection c, long appId) {
            List<ApplicationStatusHistory> out = new ArrayList<ApplicationStatusHistory>();
            for (ApplicationStatusHistory h : history) if (h.getApplicationId().equals(appId)) out.add(h);
            return out;
        }
        @Override public Map<String, Long> getApplicationMetrics(Connection c) { return new HashMap<String, Long>(); }
        @Override public Map<String, Long> getConsultantApplicationMetrics(Connection c, long cid) { return new HashMap<String, Long>(); }
    }

    private static final class InMemoryAssignmentDao implements ConsultantAssignmentDAO {
        private final InMemoryApplicationDao appDao;
        private final List<ConsultantAssignment> items = new ArrayList<ConsultantAssignment>();

        private InMemoryAssignmentDao(InMemoryApplicationDao appDao) {
            this.appDao = appDao;
        }
        @Override public ConsultantAssignment findActiveByApplicationId(Connection c, long appId) { return null; }
        @Override public List<ConsultantAssignment> findHistoryByApplicationId(Connection c, long appId) {
            List<ConsultantAssignment> out = new ArrayList<ConsultantAssignment>();
            for (ConsultantAssignment ca : items) if (ca.getApplicationId().equals(appId)) out.add(ca);
            return out;
        }
        @Override public List<ConsultantAssignment> findActiveByConsultantId(Connection c, long cid) { return Collections.emptyList(); }
        @Override public void deactivateActiveForApplication(Connection c, long appId, long byId) {
            for (ConsultantAssignment ca : items) if (ca.getApplicationId().equals(appId)) ca.setActive(false);
        }
        @Override public void insert(Connection c, ConsultantAssignment ca) {
            ca.setId((long) (items.size() + 1));
            ca.setAssignedAt(Instant.now());
            items.add(ca);
        }
        @Override public boolean isConsultantAssignedToStudent(Connection c, long cid, long sid) {
            for (Application a : appDao.byId.values()) {
                if (a.getStudentId().equals(sid) && a.getAssignedConsultantId() != null && a.getAssignedConsultantId().equals(cid)) {
                    return true;
                }
            }
            return false;
        }
        @Override public List<AssignedStudentSummaryResponse> findAssignedStudentsByConsultant(Connection c, long cid, String s, int off, int lim) {
            return Collections.emptyList();
        }
        @Override public long countAssignedStudentsByConsultant(Connection c, long cid, String s) { return 0L; }
        @Override public Map<Long, Long> countActiveStudentsByConsultantIds(Connection c, List<Long> ids) { return Collections.emptyMap(); }
    }

    private static final class InMemoryDocumentDao implements ApplicationDocumentDAO {
        private final AtomicLong seq = new AtomicLong(1L);
        private final Map<Long, ApplicationDocument> byId = new ConcurrentHashMap<Long, ApplicationDocument>();

        @Override public ApplicationDocument create(Connection c, ApplicationDocument d) {
            long id = seq.getAndIncrement();
            d.setId(id);
            d.setCreatedAt(Instant.now());
            byId.put(id, d);
            return d;
        }
        @Override public ApplicationDocument findById(Connection c, long id) { return byId.get(id); }
        @Override public List<ApplicationDocument> findByStudentId(Connection c, long sid, DocumentCategory cat, DocumentReviewStatus rs, Long appId) {
            return new ArrayList<ApplicationDocument>(byId.values());
        }
        @Override public List<ApplicationDocument> findByApplicationId(Connection c, long appId) {
            List<ApplicationDocument> out = new ArrayList<ApplicationDocument>();
            for (ApplicationDocument d : byId.values()) if (appId == (d.getApplicationId() != null ? d.getApplicationId() : -1L)) out.add(d);
            return out;
        }
        @Override public boolean updateReviewStatus(Connection c, long id, DocumentReviewStatus rs, String note, long by) {
            ApplicationDocument d = byId.get(id);
            if (d == null) return false;
            d.setReviewStatus(rs);
            d.setReviewNote(note);
            d.setReviewedByUserId(by);
            d.setReviewedAt(Instant.now());
            return true;
        }
        @Override public long countByStudentId(Connection c, long sid) { return byId.size(); }
        @Override public long countPendingReviewForConsultant(Connection c, long cid) { return 0L; }
        @Override public long countPendingReviewAll(Connection c) { return 0L; }
    }
}
