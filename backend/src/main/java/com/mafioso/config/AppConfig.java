package com.mafioso.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads non-secret application settings from {@code application.properties}
 * and runtime environment variables.
 */
public final class AppConfig {

    private static final String PROPERTIES_RESOURCE = "/application.properties";
    private static final Properties PROPERTIES = loadProperties();

    private AppConfig() {
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getResourceAsStream(PROPERTIES_RESOURCE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load " + PROPERTIES_RESOURCE, ex);
        }
        return props;
    }

    public static String getAppName() {
        return PROPERTIES.getProperty("app.name", "MAFIOSO");
    }

    public static String getAppVersion() {
        return PROPERTIES.getProperty("app.version", "0.2.0-SNAPSHOT");
    }

    public static int getMilestone() {
        return Integer.parseInt(PROPERTIES.getProperty("app.milestone", "2"));
    }

    public static String getEnvironment() {
        String env = System.getenv("APP_ENV");
        if (env == null || env.trim().isEmpty()) {
            return "development";
        }
        return env.trim();
    }

    public static boolean isProduction() {
        return "production".equalsIgnoreCase(getEnvironment());
    }

    public static String getDocumentsDirectory() {
        String envDir = System.getenv("STORAGE_UPLOAD_DIR");
        if (envDir != null && !envDir.trim().isEmpty()) {
            return envDir.trim();
        }
        return PROPERTIES.getProperty("storage.documents.dir", "/var/mafioso/storage/documents");
    }

    public static int getDbConnectTimeoutMs() {
        return getIntSetting("DB_CONNECT_TIMEOUT_MS", "db.connect.timeout.ms", 5000, 500, 60000);
    }

    public static int getDbSocketTimeoutMs() {
        return getIntSetting("DB_SOCKET_TIMEOUT_MS", "db.socket.timeout.ms", 10000, 500, 120000);
    }

    public static int getSessionTimeoutMinutes() {
        return getIntSetting("SESSION_TIMEOUT_MINUTES", "session.timeout.minutes", 30, 1, 1440);
    }

    public static int getSessionTimeoutSeconds() {
        String envSeconds = System.getenv("SESSION_TIMEOUT_SECONDS");
        if (envSeconds != null && !envSeconds.trim().isEmpty()) {
            try {
                int parsed = Integer.parseInt(envSeconds.trim());
                if (parsed >= 5 && parsed <= 86400) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
                // Fall back to minutes setting
            }
        }
        return getSessionTimeoutMinutes() * 60;
    }

    public static boolean isSessionCookieSecure() {
        String raw = System.getenv("SESSION_COOKIE_SECURE");
        if (raw != null && !raw.trim().isEmpty()) {
            String normalized = raw.trim();
            if ("true".equalsIgnoreCase(normalized)) {
                return true;
            }
            if ("false".equalsIgnoreCase(normalized)) {
                return false;
            }
        }
        return isProduction();
    }

    public static int getBcryptCost() {
        return getIntSetting("AUTH_BCRYPT_COST", "auth.bcrypt.cost", 12, 10, 14);
    }

    public static boolean isRateLimitEnabled() {
        String env = System.getenv("AUTH_RATE_LIMIT_ENABLED");
        if (env != null && !env.trim().isEmpty()) {
            return Boolean.parseBoolean(env.trim());
        }
        return Boolean.parseBoolean(PROPERTIES.getProperty("auth.rate.limit.enabled", "true"));
    }

    public static int getRateLimitMaxAttempts() {
        return getIntSetting("AUTH_RATE_LIMIT_MAX_ATTEMPTS", "auth.rate.limit.max.attempts", 5, 1, 100);
    }

    public static int getRateLimitIpMaxAttempts() {
        return getIntSetting("AUTH_RATE_LIMIT_IP_MAX_ATTEMPTS", "auth.rate.limit.ip.max.attempts", 25, 5, 1000);
    }

    public static int getRateLimitWindowSeconds() {
        return getIntSetting("AUTH_RATE_LIMIT_WINDOW_SECONDS", "auth.rate.limit.window.seconds", 300, 10, 86400);
    }

    public static int getRateLimitLockoutSeconds() {
        return getIntSetting("AUTH_RATE_LIMIT_LOCKOUT_SECONDS", "auth.rate.limit.lockout.seconds", 300, 10, 86400);
    }

    private static int getIntSetting(String envKey,
                                     String propKey,
                                     int defaultValue,
                                     int minValue,
                                     int maxValue) {
        String raw = System.getenv(envKey);
        if (raw == null || raw.trim().isEmpty()) {
            raw = PROPERTIES.getProperty(propKey);
        }
        if (raw == null || raw.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            if (parsed < minValue) {
                return minValue;
            }
            if (parsed > maxValue) {
                return maxValue;
            }
            return parsed;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
