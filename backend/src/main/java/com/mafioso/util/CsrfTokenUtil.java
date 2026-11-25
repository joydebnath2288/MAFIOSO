package com.mafioso.util;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates, rotates, and verifies session-bound CSRF tokens using {@link SecureRandom}
 * and constant-time equality checks.
 */
public final class CsrfTokenUtil {

    public static final String HEADER_NAME = "X-CSRF-Token";
    public static final String SESSION_ATTR = "MAFIOSO_CSRF_TOKEN";

    private static final int TOKEN_BYTE_LENGTH = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CsrfTokenUtil() {
    }

    public static String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Returns the current CSRF token stored on the session, or creates one if absent.
     *
     * @param session active HTTP session
     * @return CSRF token string
     */
    public static String getOrCreateToken(HttpSession session) {
        if (session == null) {
            throw new IllegalArgumentException("HttpSession must not be null");
        }
        synchronized (session) {
            Object existing = session.getAttribute(SESSION_ATTR);
            if (existing instanceof String && !((String) existing).isEmpty()) {
                return (String) existing;
            }
            String token = generateToken();
            session.setAttribute(SESSION_ATTR, token);
            return token;
        }
    }

    /**
     * Generates and stores a brand new CSRF token on the supplied session (used after login/rotation).
     *
     * @param session active HTTP session
     * @return new CSRF token string
     */
    public static String rotateToken(HttpSession session) {
        if (session == null) {
            throw new IllegalArgumentException("HttpSession must not be null");
        }
        String fresh = generateToken();
        synchronized (session) {
            session.setAttribute(SESSION_ATTR, fresh);
        }
        return fresh;
    }

    /**
     * Validates a header-supplied CSRF token against the session-stored token in constant time.
     */
    public static boolean isValid(HttpSession session, String providedToken) {
        if (session == null || providedToken == null || providedToken.isEmpty()) {
            return false;
        }
        Object expectedObj = session.getAttribute(SESSION_ATTR);
        if (!(expectedObj instanceof String)) {
            return false;
        }
        String expected = (String) expectedObj;
        if (expected.isEmpty()) {
            return false;
        }
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] providedBytes = providedToken.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, providedBytes);
    }
}
