package com.mafioso.util;

import com.mafioso.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Reusable JDBC connection helper for MySQL 8.
 */
public final class DatabaseUtil {

    private static final String MYSQL_DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(MYSQL_DRIVER_CLASS);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("MySQL JDBC driver not found on classpath: " + MYSQL_DRIVER_CLASS, ex);
        }
    }

    private DatabaseUtil() {
    }

    /**
     * Opens a new JDBC connection using environment-driven {@link DatabaseConfig}.
     *
     * @return open SQL connection (caller is responsible for closing via try-with-resources)
     * @throws SQLException if connecting to MySQL fails
     */
    public static Connection getConnection() throws SQLException {
        return getConnection(DatabaseConfig.fromEnvironment());
    }

    /**
     * Opens a new JDBC connection using the supplied {@link DatabaseConfig}.
     *
     * @param config resolved database configuration
     * @return open SQL connection
     * @throws SQLException if connecting to MySQL fails
     */
    public static Connection getConnection(DatabaseConfig config) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", config.getUsername());
        props.setProperty("password", config.getPassword());
        return DriverManager.getConnection(config.buildJdbcUrl(), props);
    }

    /**
     * Closes an {@link AutoCloseable} resource without throwing checked exceptions.
     *
     * @param closeable resource to close (nullable)
     */
    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // Intentionally suppressed during cleanup
            }
        }
    }
}
