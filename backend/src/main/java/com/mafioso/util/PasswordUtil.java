package com.mafioso.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;
import com.mafioso.config.AppConfig;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * BCrypt password hashing and constant-time verification helper compatible with Java 8.
 */
public final class PasswordUtil {

    private static final int MAX_BCRYPT_BYTES = 72;
    private static final BCrypt.Hasher HASHER =
            BCrypt.with(BCrypt.Version.VERSION_2A, LongPasswordStrategies.strict(BCrypt.Version.VERSION_2A));
    private static final BCrypt.Verifyer VERIFYER = BCrypt.verifyer();
    private static final String DUMMY_HASH =
            HASHER.hashToString(AppConfig.getBcryptCost(), "MafiosoDummyTimingGuard!2026".toCharArray());

    private PasswordUtil() {
    }

    /**
     * Hashes a raw password using BCrypt with the configured cost factor.
     *
     * @param rawPassword plaintext password (must be non-empty and at most 72 UTF-8 bytes)
     * @return BCrypt hash string
     */
    public static String hash(String rawPassword) {
        return hash(rawPassword, AppConfig.getBcryptCost());
    }

    public static String hash(String rawPassword, int cost) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_BCRYPT_BYTES) {
            throw new IllegalArgumentException("Password exceeds maximum BCrypt length");
        }
        char[] chars = rawPassword.toCharArray();
        try {
            return HASHER.hashToString(cost, chars);
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    /**
     * Verifies a plaintext password against a stored BCrypt hash.
     *
     * @param rawPassword candidate plaintext password
     * @param bcryptHash stored BCrypt hash
     * @return {@code true} if the password matches the hash
     */
    public static boolean verify(String rawPassword, String bcryptHash) {
        if (rawPassword == null || rawPassword.isEmpty() || bcryptHash == null || bcryptHash.isEmpty()) {
            return false;
        }
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_BCRYPT_BYTES) {
            return false;
        }
        char[] chars = rawPassword.toCharArray();
        try {
            BCrypt.Result result = VERIFYER.verify(chars, bcryptHash);
            return result.verified;
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    /**
     * Executes a constant-cost BCrypt check when an email does not match any account,
     * reducing timing differences between unknown-email and wrong-password login attempts.
     */
    public static void dummyVerify() {
        VERIFYER.verify("MafiosoNonMatchingCandidate!99".toCharArray(), DUMMY_HASH);
    }

    public static int getMaxBcryptBytes() {
        return MAX_BCRYPT_BYTES;
    }
}
