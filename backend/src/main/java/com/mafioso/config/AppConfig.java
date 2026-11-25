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
        return PROPERTIES.getProperty("app.version", "0.1.0-SNAPSHOT");
    }

    public static int getMilestone() {
        return Integer.parseInt(PROPERTIES.getProperty("app.milestone", "1"));
    }

    public static String getEnvironment() {
        String env = System.getenv("APP_ENV");
        if (env == null || env.trim().isEmpty()) {
            return "development";
        }
        return env.trim();
    }

    public static String getDocumentsDirectory() {
        String envDir = System.getenv("STORAGE_UPLOAD_DIR");
        if (envDir != null && !envDir.trim().isEmpty()) {
            return envDir.trim();
        }
        return PROPERTIES.getProperty("storage.documents.dir", "/var/mafioso/storage/documents");
    }

    public static int getDbConnectTimeoutMs() {
        return Integer.parseInt(PROPERTIES.getProperty("db.connect.timeout.ms", "5000"));
    }

    public static int getDbSocketTimeoutMs() {
        return Integer.parseInt(PROPERTIES.getProperty("db.socket.timeout.ms", "10000"));
    }
}
