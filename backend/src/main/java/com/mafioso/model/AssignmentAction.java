package com.mafioso.model;

import java.util.Locale;

/**
 * Audit action recorded for consultant assignment changes.
 */
public enum AssignmentAction {
    ASSIGNED,
    REASSIGNED,
    REMOVED;

    public static AssignmentAction fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Assignment action must not be empty");
        }
        return AssignmentAction.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}
