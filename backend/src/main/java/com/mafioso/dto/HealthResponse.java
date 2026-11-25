package com.mafioso.dto;

/**
 * Response payload for {@code GET /api/v1/health}.
 */
public class HealthResponse {

    private boolean success;
    private String application;
    private String status;
    private String environment;
    private String version;
    private String timestamp;

    public HealthResponse() {
    }

    public HealthResponse(boolean success,
                          String application,
                          String status,
                          String environment,
                          String version,
                          String timestamp) {
        this.success = success;
        this.application = application;
        this.status = status;
        this.environment = environment;
        this.version = version;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
