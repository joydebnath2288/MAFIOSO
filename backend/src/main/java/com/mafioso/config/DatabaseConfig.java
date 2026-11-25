package com.mafioso.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolves MySQL JDBC connection settings strictly from environment variables.
 * Never hardcodes credentials or defaults host to localhost.
 */
public final class DatabaseConfig {

    private final String host;
    private final int port;
    private final String databaseName;
    private final String username;
    private final String password;
    private final boolean useSsl;

    private DatabaseConfig(String host,
                           int port,
                           String databaseName,
                           String username,
                           String password,
                           boolean useSsl) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.useSsl = useSsl;
    }

    /**
     * Loads database configuration from container environment variables.
     *
     * @return validated {@link DatabaseConfig}
     */
    public static DatabaseConfig fromEnvironment() {
        return fromMap(System.getenv());
    }

    /**
     * Builds configuration from a supplied key-value map (useful for tests).
     *
     * @param env environment variable map
     * @return validated {@link DatabaseConfig}
     */
    public static DatabaseConfig fromMap(Map<String, String> env) {
        Map<String, String> safeEnv = env == null ? Collections.<String, String>emptyMap() : new HashMap<String, String>(env);
        String host = requireNonEmpty(safeEnv, "DB_HOST");
        String portRaw = safeEnv.get("DB_PORT");
        int port = (portRaw == null || portRaw.trim().isEmpty()) ? 3306 : parsePort(portRaw.trim());
        String dbName = requireNonEmpty(safeEnv, "DB_NAME");
        String user = requireNonEmpty(safeEnv, "DB_USER");
        String password = requireNonEmpty(safeEnv, "DB_PASSWORD");
        boolean useSsl = Boolean.parseBoolean(safeEnv.get("DB_USE_SSL"));

        return new DatabaseConfig(host, port, dbName, user, password, useSsl);
    }

    private static String requireNonEmpty(Map<String, String> env, String key) {
        String value = env.get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value.trim();
    }

    private static int parsePort(String raw) {
        try {
            int port = Integer.parseInt(raw);
            if (port < 1 || port > 65535) {
                throw new IllegalStateException("DB_PORT out of range: " + port);
            }
            return port;
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Invalid DB_PORT value: " + raw, ex);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public String buildJdbcUrl() {
        return String.format(
                "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&useSSL=%s&allowPublicKeyRetrieval=true&connectTimeout=%d&socketTimeout=%d",
                host,
                port,
                databaseName,
                Boolean.toString(useSsl),
                AppConfig.getDbConnectTimeoutMs(),
                AppConfig.getDbSocketTimeoutMs()
        );
    }
}
