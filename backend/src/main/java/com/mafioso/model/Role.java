package com.mafioso.model;

import java.util.Locale;

/**
 * Supported platform user roles enforced by backend authentication and authorization filters.
 */
public enum Role {
    STUDENT,
    CONSULTANT,
    ADMIN;

    public static Role fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Role value must not be empty");
        }
        return Role.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}
