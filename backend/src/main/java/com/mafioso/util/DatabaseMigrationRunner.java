package com.mafioso.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Forward-only, idempotent SQL migration runner executed on application startup.
 * Ensures existing persistent MySQL volumes (e.g., Milestone 1) receive Milestone 2+
 * schema changes safely without resetting or erasing data.
 */
public final class DatabaseMigrationRunner {

    private static final Logger LOGGER = Logger.getLogger(DatabaseMigrationRunner.class.getName());
    private static final String MIGRATION_LOCK_NAME = "mafioso_schema_migration_lock";
    private static final int LOCK_TIMEOUT_SECONDS = 30;

    private static final List<MigrationSpec> MIGRATIONS = Collections.unmodifiableList(Arrays.asList(
            new MigrationSpec(
                    "V001",
                    "Initial Milestone 1 foundation tables",
                    "V001__initial_foundation.sql",
                    "/db/migrations/V001__initial_foundation.sql"
            ),
            new MigrationSpec(
                    "V002",
                    "Milestone 2 users, roles, and authentication schema",
                    "V002__users_and_auth_schema.sql",
                    "/db/migrations/V002__users_and_auth_schema.sql"
            ),
            new MigrationSpec(
                    "V003",
                    "Milestone 4 consultancy workflow and student application management",
                    "V003__consultancy_workflow_schema.sql",
                    "/db/migrations/V003__consultancy_workflow_schema.sql"
            )
    ));

    private DatabaseMigrationRunner() {
    }

    /**
     * Applies any pending database migrations under a MySQL advisory lock.
     *
     * @throws SQLException if database connectivity or migration execution fails
     */
    public static void migrate() throws SQLException {
        try (Connection connection = DatabaseUtil.getConnection()) {
            migrate(connection);
        }
    }

    /**
     * Applies pending migrations using an existing JDBC connection.
     *
     * @param connection open JDBC connection
     * @throws SQLException if SQL execution fails
     */
    public static void migrate(Connection connection) throws SQLException {
        acquireLock(connection);
        try {
            ensureMigrationsTable(connection);
            Set<String> appliedVersions = loadAppliedVersions(connection);

            for (MigrationSpec spec : MIGRATIONS) {
                if (appliedVersions.contains(spec.version)) {
                    LOGGER.log(Level.FINE, "Migration {0} already applied; skipping.", spec.version);
                    continue;
                }
                applyMigration(connection, spec);
            }
        } finally {
            releaseLock(connection);
        }
    }

    private static void acquireLock(Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT GET_LOCK(?, ?)")) {
            stmt.setString(1, MIGRATION_LOCK_NAME);
            stmt.setInt(2, LOCK_TIMEOUT_SECONDS);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next() || rs.getInt(1) != 1) {
                    throw new SQLException("Could not acquire MySQL migration advisory lock");
                }
            }
        }
    }

    private static void releaseLock(Connection connection) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            stmt.setString(1, MIGRATION_LOCK_NAME);
            stmt.executeQuery();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Failed to release migration advisory lock cleanly", ex);
        }
    }

    private static void ensureMigrationsTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS schema_migrations ("
                + "id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                + "version VARCHAR(32) NOT NULL, "
                + "description VARCHAR(255) NOT NULL, "
                + "script_name VARCHAR(255) NOT NULL, "
                + "checksum VARCHAR(64) NULL, "
                + "applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "PRIMARY KEY (id), "
                + "UNIQUE KEY uk_schema_migrations_version (version)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static Set<String> loadAppliedVersions(Connection connection) throws SQLException {
        Set<String> versions = new HashSet<String>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT version FROM schema_migrations");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                versions.add(rs.getString("version"));
            }
        }
        return versions;
    }

    private static void applyMigration(Connection connection, MigrationSpec spec) throws SQLException {
        String scriptContent = loadClasspathResource(spec.resourcePath);
        String checksum = sha256Hex(scriptContent);
        List<String> statements = splitSqlStatements(scriptContent);

        LOGGER.log(Level.INFO, "Applying database migration {0} ({1})",
                new Object[]{spec.version, spec.scriptName});

        for (String sql : statements) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
            }
        }

        String recordSql = "INSERT INTO schema_migrations (version, description, script_name, checksum) "
                + "VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE description = VALUES(description), "
                + "script_name = VALUES(script_name), checksum = VALUES(checksum)";
        try (PreparedStatement stmt = connection.prepareStatement(recordSql)) {
            stmt.setString(1, spec.version);
            stmt.setString(2, spec.description);
            stmt.setString(3, spec.scriptName);
            stmt.setString(4, checksum);
            stmt.executeUpdate();
        }

        LOGGER.log(Level.INFO, "Database migration {0} applied successfully.", spec.version);
    }

    static String loadClasspathResource(String resourcePath) {
        try (InputStream in = DatabaseMigrationRunner.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Missing migration script on classpath: " + resourcePath);
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            return sb.toString();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read migration script: " + resourcePath, ex);
        }
    }

    /**
     * Splits SQL script text into executable statements while ignoring {@code --} line comments.
     *
     * @param sqlScript full SQL migration text
     * @return non-empty SQL statements
     */
    public static List<String> splitSqlStatements(String sqlScript) {
        List<String> statements = new ArrayList<String>();
        if (sqlScript == null || sqlScript.trim().isEmpty()) {
            return statements;
        }

        StringBuilder current = new StringBuilder();
        String[] lines = sqlScript.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            current.append(line).append('\n');
            if (trimmed.endsWith(";")) {
                String full = current.toString().trim();
                if (full.endsWith(";")) {
                    full = full.substring(0, full.length() - 1).trim();
                }
                if (!full.isEmpty()) {
                    statements.add(full);
                }
                current.setLength(0);
            }
        }
        String trailing = current.toString().trim();
        if (!trailing.isEmpty()) {
            statements.add(trailing);
        }
        return statements;
    }

    public static String sha256Hex(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                String part = Integer.toHexString(0xff & b);
                if (part.length() == 1) {
                    hex.append('0');
                }
                hex.append(part);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", ex);
        }
    }

    private static final class MigrationSpec {
        private final String version;
        private final String description;
        private final String scriptName;
        private final String resourcePath;

        private MigrationSpec(String version, String description, String scriptName, String resourcePath) {
            this.version = version;
            this.description = description;
            this.scriptName = scriptName;
            this.resourcePath = resourcePath;
        }
    }
}
