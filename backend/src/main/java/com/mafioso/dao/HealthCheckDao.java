package com.mafioso.dao;

import com.mafioso.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Verifies live MySQL database connectivity and reads the applied schema migration version.
 */
public class HealthCheckDao {

    /**
     * Immutable probe result returned by {@link #checkConnectivity()}.
     */
    public static final class DatabaseProbeResult {
        private final boolean reachable;
        private final long latencyMs;
        private final String schemaVersion;

        public DatabaseProbeResult(boolean reachable, long latencyMs, String schemaVersion) {
            this.reachable = reachable;
            this.latencyMs = latencyMs;
            this.schemaVersion = schemaVersion;
        }

        public boolean isReachable() {
            return reachable;
        }

        public long getLatencyMs() {
            return latencyMs;
        }

        public String getSchemaVersion() {
            return schemaVersion;
        }
    }

    /**
     * Executes a lightweight {@code SELECT 1} check against MySQL and inspects
     * {@code schema_migrations} if present.
     *
     * @return probe result containing reachability, latency in milliseconds, and schema version
     * @throws SQLException if connecting or executing the validation query fails
     */
    public DatabaseProbeResult checkConnectivity() throws SQLException {
        long startNanos = System.nanoTime();
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement pingStmt = connection.prepareStatement("SELECT 1");
             ResultSet pingRs = pingStmt.executeQuery()) {

            if (!pingRs.next() || pingRs.getInt(1) != 1) {
                throw new SQLException("Unexpected validation query result from MySQL");
            }

            String schemaVersion = readLatestSchemaVersion(connection);
            long latencyMs = Math.max(1L, (System.nanoTime() - startNanos) / 1_000_000L);
            return new DatabaseProbeResult(true, latencyMs, schemaVersion);
        }
    }

    private String readLatestSchemaVersion(Connection connection) {
        String sql = "SELECT version FROM schema_migrations ORDER BY applied_at DESC, id DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("version");
            }
            return "uninitialized";
        } catch (SQLException ex) {
            // Table may not exist yet if migrations have not run; SELECT 1 already succeeded
            return "not-applied";
        }
    }
}
