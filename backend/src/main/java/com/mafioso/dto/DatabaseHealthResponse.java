package com.mafioso.dto;

/**
 * Response payload for {@code GET /api/v1/health/database}.
 */
public class DatabaseHealthResponse {

    private boolean success;
    private String application;
    private String database;
    private String status;
    private long latencyMs;
    private String schemaVersion;
    private String message;
    private String timestamp;

    public DatabaseHealthResponse() {
    }

    public DatabaseHealthResponse(boolean success,
                                  String application,
                                  String database,
                                  String status,
                                  long latencyMs,
                                  String schemaVersion,
                                  String message,
                                  String timestamp) {
        this.success = success;
        this.application = application;
        this.database = database;
        this.status = status;
        this.latencyMs = latencyMs;
        this.schemaVersion = schemaVersion;
        this.message = message;
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
