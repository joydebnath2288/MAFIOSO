package com.mafioso.model;

import java.util.Locale;

/**
 * Account lifecycle statuses stored on {@code users.status}.
 */
public enum AccountStatus {
    ACTIVE,
    DISABLED,
    SUSPENDED;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public static AccountStatus fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Account status must not be empty");
        }
        return AccountStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}
