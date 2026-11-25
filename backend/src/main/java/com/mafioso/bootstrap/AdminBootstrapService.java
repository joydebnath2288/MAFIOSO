package com.mafioso.bootstrap;

import com.mafioso.dao.UserDAO;
import com.mafioso.dao.UserDAOImpl;
import com.mafioso.dto.UserResponse;
import com.mafioso.model.AccountStatus;
import com.mafioso.model.Role;
import com.mafioso.model.User;
import com.mafioso.util.DatabaseUtil;
import com.mafioso.util.PasswordUtil;
import com.mafioso.util.ValidationUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Secure one-time bootstrap service for provisioning the initial {@code ADMIN} account
 * (and operator-invoked CLI provisioning of {@code CONSULTANT} staff accounts).
 * Never hardcodes credentials and refuses to create a duplicate initial administrator.
 */
public class AdminBootstrapService {

    private static final Logger LOGGER = Logger.getLogger(AdminBootstrapService.class.getName());

    private final UserDAO userDao;

    public AdminBootstrapService() {
        this(new UserDAOImpl());
    }

    public AdminBootstrapService(UserDAO userDao) {
        this.userDao = userDao;
    }

    /**
     * Creates the very first {@code ADMIN} account if and only if no {@code ADMIN} user exists yet.
     *
     * @throws IllegalStateException if an administrator already exists or email is taken
     * @throws IllegalArgumentException if validation fails
     */
    public UserResponse bootstrapInitialAdmin(String fullName,
                                              String email,
                                              String password) throws SQLException {
        return createPrivilegedUser(fullName, email, password, Role.ADMIN, true);
    }

    /**
     * Operator-invoked staff provisioning helper for creating {@code CONSULTANT} accounts securely.
     */
    public UserResponse provisionConsultant(String fullName,
                                            String email,
                                            String password) throws SQLException {
        return createPrivilegedUser(fullName, email, password, Role.CONSULTANT, false);
    }

    /**
     * Runs one-time initial administrator creation at application startup only when
     * {@code MAFIOSO_BOOTSTRAP_ADMIN_ENABLED=true} and required credentials are supplied
     * via environment variables or a secret file. Skips cleanly if an admin already exists.
     */
    public void bootstrapFromEnvironmentIfEnabled() {
        String enabled = System.getenv("MAFIOSO_BOOTSTRAP_ADMIN_ENABLED");
        if (!"true".equalsIgnoreCase(enabled == null ? "" : enabled.trim())) {
            return;
        }

        String fullName = System.getenv("MAFIOSO_BOOTSTRAP_ADMIN_NAME");
        String email = System.getenv("MAFIOSO_BOOTSTRAP_ADMIN_EMAIL");
        String password = resolvePasswordFromEnvOrFile(
                "MAFIOSO_BOOTSTRAP_ADMIN_PASSWORD",
                "MAFIOSO_BOOTSTRAP_ADMIN_PASSWORD_FILE"
        );

        if (isBlank(fullName) || isBlank(email) || isBlank(password)) {
            LOGGER.warning("MAFIOSO_BOOTSTRAP_ADMIN_ENABLED is true, but bootstrap admin name, email, "
                    + "or password secret is missing. Skipping initial admin bootstrap.");
            return;
        }

        try (Connection connection = DatabaseUtil.getConnection()) {
            if (userDao.existsAnyByRole(connection, Role.ADMIN)) {
                LOGGER.info("Initial administrator account already exists; skipping environment bootstrap. "
                        + "Remove MAFIOSO_BOOTSTRAP_ADMIN_* variables from your environment.");
                return;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Unable to inspect existing administrator accounts during bootstrap", ex);
            return;
        }

        try {
            UserResponse created = bootstrapInitialAdmin(fullName, email, password);
            LOGGER.log(Level.INFO,
                    "Initial ADMIN account provisioned (id={0}). Disable MAFIOSO_BOOTSTRAP_ADMIN_ENABLED "
                            + "and remove bootstrap password secrets from .env.",
                    created.getId());
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.INFO, "Initial admin bootstrap skipped: {0}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.SEVERE, "Initial admin bootstrap rejected invalid configuration: {0}", ex.getMessage());
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error during initial admin bootstrap", ex);
        }
    }

    private UserResponse createPrivilegedUser(String fullName,
                                              String email,
                                              String password,
                                              Role role,
                                              boolean failIfRoleAlreadyExists) throws SQLException {
        Map<String, String> errors = ValidationUtil.validatePrivilegedAccount(fullName, email, password);
        if (!errors.isEmpty()) {
            String firstError = errors.values().iterator().next();
            throw new IllegalArgumentException(firstError);
        }

        String normalizedEmail = ValidationUtil.normalizeEmail(email);
        String normalizedName = ValidationUtil.normalizeFullName(fullName);
        String passwordHash = PasswordUtil.hash(password);

        User user = new User();
        user.setFullName(normalizedName);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        user.setStatus(AccountStatus.ACTIVE);

        try (Connection connection = DatabaseUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                if (failIfRoleAlreadyExists && userDao.existsAnyByRole(connection, role)) {
                    throw new IllegalStateException("An initial " + role.name() + " account already exists.");
                }
                if (userDao.existsByEmail(connection, normalizedEmail)) {
                    throw new IllegalStateException("An account with this email address already exists.");
                }
                User saved = userDao.createUser(connection, user);
                connection.commit();
                return UserResponse.fromUser(saved);
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

    static String resolvePasswordFromEnvOrFile(String envKey, String fileEnvKey) {
        String filePath = System.getenv(fileEnvKey);
        if (!isBlank(filePath)) {
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(filePath.trim()));
                return new String(bytes, StandardCharsets.UTF_8).replaceAll("[\\r\\n]+$", "");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to read secret file configured in " + fileEnvKey, ex);
                return null;
            }
        }
        return System.getenv(envKey);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Preserve primary failure
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
