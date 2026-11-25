package com.mafioso.util;

import com.mafioso.config.AppConfig;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Thread-safe, configurable in-memory login rate limiter keyed by client IP + normalized email
 * and overall client IP ceiling to slow brute-force and credential-stuffing attacks.
 */
public class LoginRateLimiter {

    private static final int CLEANUP_INTERVAL_OPERATIONS = 100;

    private final boolean enabled;
    private final int maxAccountAttempts;
    private final int maxIpAttempts;
    private final long windowMillis;
    private final long lockoutMillis;
    private final LongSupplier clockMillis;
    private final ConcurrentHashMap<String, AttemptBucket> buckets = new ConcurrentHashMap<String, AttemptBucket>();
    private int operationCounter = 0;

    public LoginRateLimiter() {
        this(
                AppConfig.isRateLimitEnabled(),
                AppConfig.getRateLimitMaxAttempts(),
                AppConfig.getRateLimitIpMaxAttempts(),
                AppConfig.getRateLimitWindowSeconds(),
                AppConfig.getRateLimitLockoutSeconds(),
                System::currentTimeMillis
        );
    }

    public LoginRateLimiter(boolean enabled,
                            int maxAccountAttempts,
                            int maxIpAttempts,
                            int windowSeconds,
                            int lockoutSeconds,
                            LongSupplier clockMillis) {
        this.enabled = enabled;
        this.maxAccountAttempts = Math.max(1, maxAccountAttempts);
        this.maxIpAttempts = Math.max(this.maxAccountAttempts, maxIpAttempts);
        this.windowMillis = Math.max(1, windowSeconds) * 1000L;
        this.lockoutMillis = Math.max(1, lockoutSeconds) * 1000L;
        this.clockMillis = clockMillis;
    }

    /**
     * Checks whether a login attempt is permitted.
     *
     * @param clientIp resolved client IP address
     * @param normalizedEmail normalized email address
     * @return {@code 0} if allowed, otherwise positive retry-after duration in seconds
     */
    public long checkAllowed(String clientIp, String normalizedEmail) {
        if (!enabled) {
            return 0L;
        }
        long now = clockMillis.getAsLong();
        maybePruneExpired(now);

        long accountRetry = getRemainingLockoutSeconds(accountKey(clientIp, normalizedEmail), now);
        long ipRetry = getRemainingLockoutSeconds(ipKey(clientIp), now);
        return Math.max(accountRetry, ipRetry);
    }

    public void recordFailure(String clientIp, String normalizedEmail) {
        if (!enabled) {
            return;
        }
        long now = clockMillis.getAsLong();
        recordBucketFailure(accountKey(clientIp, normalizedEmail), maxAccountAttempts, now);
        recordBucketFailure(ipKey(clientIp), maxIpAttempts, now);
    }

    public void recordSuccess(String clientIp, String normalizedEmail) {
        if (!enabled) {
            return;
        }
        buckets.remove(accountKey(clientIp, normalizedEmail));
    }

    private long getRemainingLockoutSeconds(String key, long now) {
        AttemptBucket bucket = buckets.get(key);
        if (bucket == null) {
            return 0L;
        }
        synchronized (bucket) {
            if (bucket.lockedUntilMillis > now) {
                long remainingMs = bucket.lockedUntilMillis - now;
                return Math.max(1L, (remainingMs + 999L) / 1000L);
            }
            if (now - bucket.windowStartMillis >= windowMillis) {
                buckets.remove(key, bucket);
            }
            return 0L;
        }
    }

    private void recordBucketFailure(String key, int limit, long now) {
        AttemptBucket bucket = buckets.computeIfAbsent(key, k -> new AttemptBucket(now));
        synchronized (bucket) {
            if (bucket.lockedUntilMillis > now) {
                return;
            }
            if (bucket.lockedUntilMillis > 0L || now - bucket.windowStartMillis >= windowMillis) {
                bucket.windowStartMillis = now;
                bucket.failureCount = 0;
                bucket.lockedUntilMillis = 0L;
            }
            bucket.failureCount++;
            if (bucket.failureCount >= limit) {
                bucket.lockedUntilMillis = now + lockoutMillis;
            }
        }
    }

    private synchronized void maybePruneExpired(long now) {
        operationCounter++;
        if (operationCounter < CLEANUP_INTERVAL_OPERATIONS) {
            return;
        }
        operationCounter = 0;
        Iterator<Map.Entry<String, AttemptBucket>> it = buckets.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, AttemptBucket> entry = it.next();
            AttemptBucket bucket = entry.getValue();
            synchronized (bucket) {
                boolean windowExpired = (now - bucket.windowStartMillis) >= windowMillis;
                boolean lockExpired = bucket.lockedUntilMillis <= now;
                if (windowExpired && lockExpired) {
                    it.remove();
                }
            }
        }
    }

    private static String accountKey(String clientIp, String normalizedEmail) {
        return "acct:" + safeIp(clientIp) + "|" + (normalizedEmail == null ? "" : normalizedEmail);
    }

    private static String ipKey(String clientIp) {
        return "ip:" + safeIp(clientIp);
    }

    private static String safeIp(String clientIp) {
        return (clientIp == null || clientIp.trim().isEmpty()) ? "unknown" : clientIp.trim();
    }

    private static final class AttemptBucket {
        private long windowStartMillis;
        private int failureCount;
        private long lockedUntilMillis;

        private AttemptBucket(long windowStartMillis) {
            this.windowStartMillis = windowStartMillis;
        }
    }
}
