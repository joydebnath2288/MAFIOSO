package com.mafioso.service;

import com.mafioso.dao.UserDAO;
import com.mafioso.dao.UserDAOImpl;
import com.mafioso.dto.LoginRequest;
import com.mafioso.dto.RegisterRequest;
import com.mafioso.dto.UserResponse;
import com.mafioso.model.AccountStatus;
import com.mafioso.model.Role;
import com.mafioso.model.User;
import com.mafioso.util.DatabaseUtil;
import com.mafioso.util.LoginRateLimiter;
import com.mafioso.util.PasswordUtil;
import com.mafioso.util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Instant;
import java.util.Map;

/**
 * Core authentication business logic: registration, BCrypt credential verification,
 * account status enforcement, rate limiting, and role-to-dashboard resolution.
 */
public class AuthServiceImpl implements AuthService {

    private static final String GENERIC_INVALID_CREDENTIALS = "Invalid email or password.";
    private static final LoginRateLimiter SHARED_RATE_LIMITER = new LoginRateLimiter();

    @FunctionalInterface
    public interface ConnectionSupplier {
        Connection get() throws SQLException;
    }

    private final UserDAO userDao;
    private final LoginRateLimiter rateLimiter;
    private final ConnectionSupplier connectionSupplier;

    public AuthServiceImpl() {
        this(new UserDAOImpl(), SHARED_RATE_LIMITER, DatabaseUtil::getConnection);
    }

    public AuthServiceImpl(UserDAO userDao, LoginRateLimiter rateLimiter) {
        this(userDao, rateLimiter, DatabaseUtil::getConnection);
    }

    public AuthServiceImpl(UserDAO userDao,
                           LoginRateLimiter rateLimiter,
                           ConnectionSupplier connectionSupplier) {
        this.userDao = userDao;
        this.rateLimiter = rateLimiter;
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public UserResponse registerStudent(RegisterRequest request) throws SQLException {
        Map<String, String> fieldErrors = ValidationUtil.validateRegistration(request);
        if (!fieldErrors.isEmpty()) {
            throw new ValidationException("Please correct the highlighted fields.", fieldErrors);
        }

        String normalizedEmail = ValidationUtil.normalizeEmail(request.getEmail());
        String normalizedName = ValidationUtil.normalizeFullName(request.getFullName());
        String normalizedPhone = ValidationUtil.normalizePhoneNumber(request.getPhoneNumber());
        String passwordHash = PasswordUtil.hash(request.getPassword());

        // Public registration always assigns STUDENT role regardless of client payload.
        User newUser = new User();
        newUser.setFullName(normalizedName);
        newUser.setEmail(normalizedEmail);
        newUser.setPhoneNumber(normalizedPhone);
        newUser.setPasswordHash(passwordHash);
        newUser.setRole(Role.STUDENT);
        newUser.setStatus(AccountStatus.ACTIVE);

        try (Connection connection = connectionSupplier.get()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                if (userDao.existsByEmail(connection, normalizedEmail)) {
                    throw new DuplicateEmailException("An account with this email address already exists.");
                }
                User created = userDao.createUser(connection, newUser);
                connection.commit();
                return UserResponse.fromUser(created);
            } catch (SQLIntegrityConstraintViolationException ex) {
                rollbackQuietly(connection);
                throw new DuplicateEmailException("An account with this email address already exists.");
            } catch (SQLException ex) {
                rollbackQuietly(connection);
                if (isUniqueConstraintViolation(ex)) {
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
    public User authenticate(LoginRequest request, String clientIp) throws SQLException {
        Map<String, String> fieldErrors = ValidationUtil.validateLogin(request);
        if (!fieldErrors.isEmpty()) {
            throw new ValidationException("Please enter your email address and password.", fieldErrors);
        }

        String normalizedEmail = ValidationUtil.normalizeEmail(request.getEmail());
        long retryAfterSeconds = rateLimiter.checkAllowed(clientIp, normalizedEmail);
        if (retryAfterSeconds > 0L) {
            throw new RateLimitException(
                    "Too many failed login attempts. Please try again later.",
                    retryAfterSeconds
            );
        }

        User user = userDao.findByEmail(normalizedEmail);
        if (user == null) {
            PasswordUtil.dummyVerify();
            rateLimiter.recordFailure(clientIp, normalizedEmail);
            throw new AuthenticationException(GENERIC_INVALID_CREDENTIALS);
        }

        boolean passwordMatches = PasswordUtil.verify(request.getPassword(), user.getPasswordHash());
        if (!passwordMatches) {
            rateLimiter.recordFailure(clientIp, normalizedEmail);
            throw new AuthenticationException(GENERIC_INVALID_CREDENTIALS);
        }

        if (user.getStatus() == null || !user.getStatus().isActive()) {
            throw new AccountDisabledException(
                    "This account is currently disabled. Please contact an administrator."
            );
        }

        rateLimiter.recordSuccess(clientIp, normalizedEmail);
        Instant now = Instant.now();
        userDao.updateLastLoginAt(user.getId(), now);
        user.setLastLoginAt(now);
        return user;
    }

    @Override
    public UserResponse getActiveUserProfile(long userId) throws SQLException {
        User user = userDao.findById(userId);
        if (user == null || user.getStatus() == null || !user.getStatus().isActive()) {
            throw new AuthenticationException("Authenticated session is no longer valid.");
        }
        return UserResponse.fromUser(user);
    }

    @Override
    public String resolveDashboardRoute(Role role) {
        if (role == null) {
            return "/login";
        }
        switch (role) {
            case STUDENT:
                return "/student/dashboard";
            case CONSULTANT:
            case ADMIN:
                return "/staff/dashboard";
            default:
                return "/unauthorized";
        }
    }

    private static boolean isUniqueConstraintViolation(SQLException ex) {
        return "23000".equals(ex.getSQLState()) || ex.getErrorCode() == 1062;
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
