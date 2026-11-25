package com.mafioso.service;

/**
 * Thrown when too many failed login attempts trigger temporary rate limiting.
 */
public class RateLimitException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final long retryAfterSeconds;

    public RateLimitException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
