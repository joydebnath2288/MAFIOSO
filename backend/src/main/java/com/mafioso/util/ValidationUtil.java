package com.mafioso.util;

import com.mafioso.dto.CreateConsultantRequest;
import com.mafioso.dto.LoginRequest;
import com.mafioso.dto.RegisterRequest;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Input normalization and server-side validation rules for authentication workflows.
 */
public final class ValidationUtil {

    private static final int MAX_NAME_LENGTH = 120;
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_BYTES = PasswordUtil.getMaxBcryptBytes();

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\s\\-()]{7,20}$");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}]");

    private ValidationUtil() {
    }

    public static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeFullName(String fullName) {
        if (fullName == null) {
            return "";
        }
        return fullName.trim().replaceAll("\\s+", " ");
    }

    public static String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        String trimmed = phoneNumber.trim().replaceAll("\\s+", " ");
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static boolean isValidEmail(String normalizedEmail) {
        if (normalizedEmail == null || normalizedEmail.isEmpty() || normalizedEmail.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        if (normalizedEmail.contains("..")) {
            return false;
        }
        return EMAIL_PATTERN.matcher(normalizedEmail).matches();
    }

    public static boolean isValidFullName(String normalizedName) {
        if (normalizedName == null || normalizedName.length() < MIN_NAME_LENGTH || normalizedName.length() > MAX_NAME_LENGTH) {
            return false;
        }
        if (CONTROL_CHARS.matcher(normalizedName).find()) {
            return false;
        }
        return !normalizedName.contains("<") && !normalizedName.contains(">");
    }

    public static boolean isValidPhoneNumber(String normalizedPhone) {
        if (normalizedPhone == null) {
            return true;
        }
        if (!PHONE_PATTERN.matcher(normalizedPhone).matches()) {
            return false;
        }
        int digitCount = 0;
        for (int i = 0; i < normalizedPhone.length(); i++) {
            if (Character.isDigit(normalizedPhone.charAt(i))) {
                digitCount++;
            }
        }
        return digitCount >= 7 && digitCount <= 15;
    }

    /**
     * Validates password strength: 8+ characters, max 72 UTF-8 bytes, uppercase, lowercase,
     * numeric digit, and special character.
     */
    public static String validatePasswordPolicy(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required.";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least 8 characters long.";
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            return "Password must not exceed 72 bytes.";
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
            if (Character.isUpperCase(ch)) {
                hasUpper = true;
            } else if (Character.isLowerCase(ch)) {
                hasLower = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else if (!Character.isWhitespace(ch)) {
                hasSpecial = true;
            }
        }

        if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
            return "Password must include uppercase, lowercase, a number, and a special character.";
        }
        return null;
    }

    public static Map<String, String> validateRegistration(RegisterRequest request) {
        if (request == null) {
            Map<String, String> errors = new LinkedHashMap<String, String>();
            errors.put("request", "Registration payload is required.");
            return errors;
        }
        return validateAccountFields(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                request.getConfirmPassword(),
                true,
                request.getPhoneNumber()
        );
    }

    public static Map<String, String> validateConsultantCreation(CreateConsultantRequest request) {
        if (request == null) {
            Map<String, String> errors = new LinkedHashMap<String, String>();
            errors.put("request", "Consultant payload is required.");
            return errors;
        }
        return validateAccountFields(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                request.getConfirmPassword(),
                true,
                request.getPhoneNumber()
        );
    }

    public static Map<String, String> validateLogin(LoginRequest request) {
        Map<String, String> errors = new LinkedHashMap<String, String>();
        if (request == null) {
            errors.put("request", "Login payload is required.");
            return errors;
        }

        String email = normalizeEmail(request.getEmail());
        if (email.isEmpty()) {
            errors.put("email", "Email address is required.");
        } else if (!isValidEmail(email)) {
            errors.put("email", "Enter a valid email address.");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            errors.put("password", "Password is required.");
        }

        return errors;
    }

    public static Map<String, String> validatePrivilegedAccount(String fullName, String email, String password) {
        return validateAccountFields(fullName, email, password, null, false, null);
    }

    private static Map<String, String> validateAccountFields(String rawFullName,
                                                             String rawEmail,
                                                             String password,
                                                             String confirmPassword,
                                                             boolean requireConfirm,
                                                             String rawPhoneNumber) {
        Map<String, String> errors = new LinkedHashMap<String, String>();

        String cleanName = normalizeFullName(rawFullName);
        if (cleanName.isEmpty()) {
            errors.put("fullName", "Full name is required.");
        } else if (!isValidFullName(cleanName)) {
            errors.put("fullName", "Full name must be between 2 and 120 valid characters.");
        }

        String cleanEmail = normalizeEmail(rawEmail);
        if (cleanEmail.isEmpty()) {
            errors.put("email", "Email address is required.");
        } else if (!isValidEmail(cleanEmail)) {
            errors.put("email", "Enter a valid email address.");
        }

        String passwordError = validatePasswordPolicy(password);
        if (passwordError != null) {
            errors.put("password", passwordError);
        }

        if (requireConfirm) {
            if (confirmPassword == null || confirmPassword.isEmpty()) {
                errors.put("confirmPassword", "Confirm password is required.");
            } else if (password != null && !password.equals(confirmPassword)) {
                errors.put("confirmPassword", "Passwords do not match.");
            }
        }

        String phone = normalizePhoneNumber(rawPhoneNumber);
        if (!isValidPhoneNumber(phone)) {
            errors.put("phoneNumber", "Enter a valid phone number (7 to 15 digits).");
        }

        return errors;
    }
}
