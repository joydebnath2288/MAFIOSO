package com.mafioso.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for Java 8 BCrypt hashing and verification in {@link PasswordUtil}.
 */
public class PasswordUtilTest {

    @Test
    public void hashesAndVerifiesValidPassword() {
        String raw = "DhakaLondon#2026";
        String hash = PasswordUtil.hash(raw, 10);

        assertTrue(hash.startsWith("$2a$10$"));
        assertNotEquals(raw, hash);
        assertTrue(PasswordUtil.verify(raw, hash));
        assertFalse(PasswordUtil.verify("WrongPassword#2026", hash));
    }

    @Test
    public void rejectsNullEmptyAndOverlongPasswordsSafely() {
        String hash = PasswordUtil.hash("ValidPass!123", 10);

        assertFalse(PasswordUtil.verify(null, hash));
        assertFalse(PasswordUtil.verify("", hash));
        assertFalse(PasswordUtil.verify("ValidPass!123", null));

        StringBuilder tooLong = new StringBuilder();
        for (int i = 0; i < 80; i++) {
            tooLong.append('A');
        }
        assertFalse(PasswordUtil.verify(tooLong.toString(), hash));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOverlongPasswordWhenHashing() {
        StringBuilder tooLong = new StringBuilder();
        for (int i = 0; i < 80; i++) {
            tooLong.append('A');
        }
        PasswordUtil.hash(tooLong.toString(), 10);
    }
}
