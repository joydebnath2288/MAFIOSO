package com.mafioso.util;

import com.mafioso.dto.RegisterRequest;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for normalization and input validation in {@link ValidationUtil}.
 */
public class ValidationUtilTest {

    @Test
    public void normalizesEmailNameAndOptionalPhone() {
        assertEquals("student@example.com", ValidationUtil.normalizeEmail("  Student@Example.COM  "));
        assertEquals("Afsana Rahman", ValidationUtil.normalizeFullName("  Afsana   Rahman  "));
        assertNull(ValidationUtil.normalizePhoneNumber("   "));
        assertEquals("+880 1711 000000", ValidationUtil.normalizePhoneNumber("  +880 1711 000000  "));
    }

    @Test
    public void acceptsValidStudentRegistration() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Tanvir Hasan");
        req.setEmail("Tanvir.Hasan@example.edu.bd");
        req.setPassword("StrongPass#2026");
        req.setConfirmPassword("StrongPass#2026");
        req.setPhoneNumber("+8801711223344");

        Map<String, String> errors = ValidationUtil.validateRegistration(req);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void rejectsInvalidEmailWeakPasswordMismatchAndBlankFields() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName(" ");
        req.setEmail("not-an-email");
        req.setPassword("weak");
        req.setConfirmPassword("different");
        req.setPhoneNumber("abc");

        Map<String, String> errors = ValidationUtil.validateRegistration(req);
        assertTrue(errors.containsKey("fullName"));
        assertTrue(errors.containsKey("email"));
        assertTrue(errors.containsKey("password"));
        assertTrue(errors.containsKey("confirmPassword"));
        assertTrue(errors.containsKey("phoneNumber"));
    }
}
