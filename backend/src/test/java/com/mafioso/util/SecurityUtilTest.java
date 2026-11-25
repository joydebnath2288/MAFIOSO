package com.mafioso.util;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link LoginRateLimiter} and {@link CsrfTokenUtil}.
 */
public class SecurityUtilTest {

    @Test
    public void locksOutAfterConfiguredFailedAttemptsAndResetsAfterWindow() {
        AtomicLong nowMs = new AtomicLong(1_000_000L);
        LoginRateLimiter limiter = new LoginRateLimiter(true, 3, 10, 60, 120, nowMs::get);

        assertEquals(0L, limiter.checkAllowed("203.0.113.10", "student@example.com"));
        limiter.recordFailure("203.0.113.10", "student@example.com");
        limiter.recordFailure("203.0.113.10", "student@example.com");
        assertEquals(0L, limiter.checkAllowed("203.0.113.10", "student@example.com"));

        limiter.recordFailure("203.0.113.10", "student@example.com");
        long retryAfter = limiter.checkAllowed("203.0.113.10", "student@example.com");
        assertTrue(retryAfter > 0L);

        // Other email from same IP still permitted under IP ceiling
        assertEquals(0L, limiter.checkAllowed("203.0.113.10", "other@example.com"));

        // Advance past lockout window
        nowMs.addAndGet(121_000L);
        assertEquals(0L, limiter.checkAllowed("203.0.113.10", "student@example.com"));
    }

    @Test
    public void generatesHighEntropyDistinctCsrfTokens() {
        String first = CsrfTokenUtil.generateToken();
        String second = CsrfTokenUtil.generateToken();

        assertTrue(first.length() >= 40);
        assertFalseSame(first, second);
    }

    private static void assertFalseSame(String a, String b) {
        assertTrue(!a.equals(b));
    }
}
