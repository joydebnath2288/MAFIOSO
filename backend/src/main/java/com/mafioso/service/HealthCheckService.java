package com.mafioso.service;

import com.mafioso.config.AppConfig;
import com.mafioso.config.DatabaseConfig;
import com.mafioso.dao.HealthCheckDao;
import com.mafioso.dto.DatabaseHealthResponse;
import com.mafioso.dto.HealthResponse;

import java.sql.SQLException;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides application and database health diagnostics for Milestone 1 verification.
 */
public class HealthCheckService {

    private static final Logger LOGGER = Logger.getLogger(HealthCheckService.class.getName());

    private final HealthCheckDao healthCheckDao;

    public HealthCheckService() {
        this(new HealthCheckDao());
    }

    public HealthCheckService(HealthCheckDao healthCheckDao) {
        this.healthCheckDao = healthCheckDao;
    }

    public HealthResponse getApplicationHealth() {
        return new HealthResponse(
                true,
                AppConfig.getAppName(),
                "UP",
                AppConfig.getEnvironment(),
                AppConfig.getAppVersion(),
                Instant.now().toString()
        );
    }

    public DatabaseHealthResponse getDatabaseHealth() {
        String dbName = resolveDatabaseNameSafely();
        try {
            HealthCheckDao.DatabaseProbeResult probe = healthCheckDao.checkConnectivity();
            return new DatabaseHealthResponse(
                    probe.isReachable(),
                    AppConfig.getAppName(),
                    dbName,
                    probe.isReachable() ? "UP" : "DOWN",
                    probe.getLatencyMs(),
                    probe.getSchemaVersion(),
                    "MySQL connection verified",
                    Instant.now().toString()
            );
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database health check failed", ex);
            return new DatabaseHealthResponse(
                    false,
                    AppConfig.getAppName(),
                    dbName,
                    "DOWN",
                    0L,
                    "unknown",
                    "Database connectivity check failed",
                    Instant.now().toString()
            );
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.SEVERE, "Database configuration error", ex);
            return new DatabaseHealthResponse(
                    false,
                    AppConfig.getAppName(),
                    dbName,
                    "DOWN",
                    0L,
                    "unconfigured",
                    "Database configuration is incomplete",
                    Instant.now().toString()
            );
        }
    }

    private String resolveDatabaseNameSafely() {
        try {
            return DatabaseConfig.fromEnvironment().getDatabaseName();
        } catch (IllegalStateException ex) {
            String fallback = System.getenv("DB_NAME");
            return (fallback != null && !fallback.trim().isEmpty()) ? fallback.trim() : "mafioso_db";
        }
    }
}
